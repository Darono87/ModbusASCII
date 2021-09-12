/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.polsl.bdiis.modbus;
import java.util.Formatter;
/**
 *
 * @author Wiktor
 */
public class ASCIIFrame {
    
    private String frame;
    
    public static byte calcLRC(byte address, byte command, String argument){
        byte LRC = 0x00;
        LRC = (byte)((LRC+address)&0xFF);
        LRC = (byte)((LRC+command)&0xFF);
        if(!argument.isEmpty()){
            for(var b: argument.getBytes()){              
                LRC = (byte)((LRC+b)&0xFF);
            }
        }
        return (byte)(((LRC ^ 0xFF) + 1) & 0xFF);
    }
    public static FrameInfo parseFrame(String frame) {
        if(frame.length() < 9 || frame.charAt(0) != ':') return null;
        byte address = (byte)Integer.parseInt(frame.substring(1, 3), 16);
        byte command = (byte)Integer.parseInt(frame.substring(3, 5), 16);
        byte lrc = (byte)Integer.parseInt(frame.substring(frame.length() - 4, frame.length() - 2), 16); 
        StringBuilder msgBuilder = new StringBuilder();
        for(int i = 5; i <= frame.length() - 6; i+=2){
            int msgByte = Integer.parseInt(frame.substring(i, i+2), 16);
            msgBuilder.append((char)msgByte);
        }
        var calculatedLrc = calcLRC(address,command,msgBuilder.toString());
        if(calculatedLrc != lrc) return null;
        return new FrameInfo(address, command, msgBuilder.toString());
    }
    public ASCIIFrame(byte address, byte command, String argument) {
        frame = ":" + String.format("%02x", address) + String.format("%02x", command);

        if (!argument.isEmpty())
        {
            for (var b: argument.getBytes())
            {
                frame += String.format("%02x", b);
            }
        }

        frame += String.format("%02x", calcLRC(address, command, argument));
        frame += "\r\n";
    }

    public String getFrame() {
        return frame;
    }
    
    
    
}

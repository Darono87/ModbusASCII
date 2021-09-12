/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.polsl.bdiis.modbus;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 *
 * @author Wiktor
 */
public class FrameScanner {
    private ByteArrayOutputStream frameData;
    private boolean isAfterStart;
    private long frameReadStart;

    public FrameScanner() {
        frameData = new ByteArrayOutputStream();
    }
    
    public ArrayList<ByteArrayOutputStream> scanNextData(byte[] data, long maxCharTime){
        ArrayList<ByteArrayOutputStream> returnArray = new ArrayList<>();
        for(int i = 0; i < data.length; i++){
            if(data[i] == 58 && !isAfterStart) //start
            {
                isAfterStart = true;
                frameReadStart = System.currentTimeMillis();
            }
            if(data[i] == 10 && isAfterStart) //start
            {
                isAfterStart = false;
                frameData.write(data[i]);
                
                long now = System.currentTimeMillis();
                var diff = now - frameReadStart;
                if(diff < maxCharTime * frameData.size())
                    returnArray.add(frameData);
                else
                    System.out.println("Maksymalny odstep miedzy znakami zostal przekroczony!");
                frameData = new ByteArrayOutputStream();
            }
            if(isAfterStart){
                frameData.write(data[i]);
            }
        }
        return returnArray;
    }
    
    
}

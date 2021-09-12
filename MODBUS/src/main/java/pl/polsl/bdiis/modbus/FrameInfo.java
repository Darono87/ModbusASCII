/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.polsl.bdiis.modbus;

/**
 *
 * @author Wiktor
 */
public class FrameInfo {
   private final byte address;
   private final byte command;
   private final String argument;

    public FrameInfo(byte address, byte command, String argument) {
        this.address = address;
        this.command = command;
        this.argument = argument;
    }

    public byte getAddress() {
        return address;
    }

    public byte getCommand() {
        return command;
    }

    public String getArgument() {
        return argument;
    }
   
}

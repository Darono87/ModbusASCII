/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.polsl.bdiis.modbus;

import java.util.TimerTask;

/**
 *
 * @author Wiktor
 */
public class TimeoutTask extends TimerTask {

    MasterStationController msc; 

    public TimeoutTask(MasterStationController msc){
        this.msc = msc;
    }

    // Add your task here
    @Override
    public void run() {
        this.msc.setRetransmissions(this.msc.getRetransmissions()+1);
        //STATUS BAR - RETRANSMISSION IN PROGRESS
        //..
        //..
        
        this.msc.onSendCommand();
        
        //System.out.println("Retransmission no " + this.msc.getRetransmissions());
        
    }
}

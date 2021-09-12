package pl.polsl.bdiis.modbus;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.IOException;
import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;

public class SlaveStationController {

    @FXML
    Button goBackButton;
    
    @FXML
    TextArea slaveDataTextArea;
    
    @FXML
    TextArea hexSentTextArea;
    
    @FXML
    TextArea hexReceivedTextArea;
    
    @FXML
    TextArea dataTextArea;
    
    @FXML
    Spinner<Integer> slaveAddressSpinner;
    
    @FXML
    Spinner<Integer> charSpinner;
    
    FrameScanner fs;
    
    private void sendBufferData(byte address, byte command){
        var argument = slaveDataTextArea.getText();
        var frame = new ASCIIFrame(address, argument.isEmpty() ? (byte)((int)command+128) : command, argument);
        var frameBytes = frame.getFrame().getBytes();
        hexSentTextArea.setText(Hex.bytesToHex(frameBytes));
        App.selectedPort.writeBytes(frameBytes, frameBytes.length);
    }
    
    @FXML
    protected void initialize(){
        slaveAddressSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,247));
        charSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0, 10));
        fs = new FrameScanner();
        
        App.selectedPort.removeDataListener();
        App.selectedPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            
            @Override
            public void serialEvent(SerialPortEvent event)
            {
               if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                  return;
               byte[] newData = new byte[App.selectedPort.bytesAvailable()];
               App.selectedPort.readBytes(newData, newData.length);
               var frames = fs.scanNextData(newData, charSpinner.getValue());
               for(var frame: frames){
                   var rawFrame = frame.toByteArray();
                   hexReceivedTextArea.setText(Hex.bytesToHex(rawFrame));
                   var info = ASCIIFrame.parseFrame(new String(rawFrame));
                   
                    //OPTIONAL: save ERROR in info, and show recent error in the slave status
                    //...
                    //...
                    //...
                    //-------
                    if(info == null || info.getAddress() != slaveAddressSpinner.getValue() && info.getAddress() != 0) return; //Not for this station

                    if(info.getCommand() == 1){ //Show the argument

                        dataTextArea.setText(info.getArgument());

                    } else { // Send back data

                        //add 128 - odpowiedź szczególna.
                        sendBufferData(info.getAddress(), info.getCommand());
                    }
               }
            }
        });
    }
    
    
    @FXML
    private void goBackToSelection() throws IOException {
        App.setRoot("stationSelector");
    }
}
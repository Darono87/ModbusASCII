package pl.polsl.bdiis.modbus;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class MasterStationController {

    @FXML
    Button goBackButton;
    
    @FXML
    RadioButton broadcastRadioButton;
    
    @FXML
    VBox masterCommandVBox;
    
    @FXML
    RadioButton addressedRadioButton;
    
    @FXML
    TextArea hexSentTextArea;
    
    @FXML
    TextArea hexReceivedTextArea;
    
    @FXML
    TextArea dataTextArea;
    
    @FXML
    Button sendCommandButton;
    
    @FXML
    Spinner<Integer> slaveAddressSpinner;
    
    @FXML
    Spinner<Integer> timeoutSpinner;
    
    @FXML
    Spinner<Integer> retransmissionSpinner;
    
    @FXML
    Spinner<Integer> charSpinner;
    
    @FXML
    ComboBox commandNumberComboBox;
    
    private Timer timeoutTimer;
    
    private FrameScanner fs;
    
    private int retransmissions = 0;
    
    TextField textToSendTextField = new TextField();
    
    ToggleGroup modeGroup = new ToggleGroup();
    
    ObservableList<String> observableList = FXCollections.observableArrayList();
    
    @FXML
    private void updateModeDependentFields(){
        var selected = (RadioButton)modeGroup.getSelectedToggle();
        if("addressed".equals(selected.getId())){
            slaveAddressSpinner.setDisable(false);
            observableList.add("2");
        }else{
            slaveAddressSpinner.setDisable(true);
            observableList.remove("2");
        }
        onCommandChanged();
    }

    public int getRetransmissions() {
        return retransmissions;
    }

    public void setRetransmissions(int retransmissions) {
        this.retransmissions = retransmissions;
    }
    
    
    
    @FXML
    protected void initialize(){
        broadcastRadioButton.setToggleGroup(modeGroup);
        addressedRadioButton.setToggleGroup(modeGroup);
        addressedRadioButton.setSelected(true);
        commandNumberComboBox.setItems(observableList);
        observableList.add("1");
        textToSendTextField.setPromptText("Enter message");
        slaveAddressSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,247));
        timeoutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 1000, 100));
        retransmissionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 2));
        charSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 50, 10));
        sendCommandButton.setDisable(true);
        updateModeDependentFields();
        fs = new FrameScanner();
        timeoutTimer = new Timer();
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
                  var txt = info.getArgument();
                  dataTextArea.setText(((int)info.getCommand()+128) == 2 ? "STATUS 130 - Odpowiedź szczególna. Slave nie posiada danych" : txt);
                  
                  timeoutTimer.cancel();
                  timeoutTimer = new Timer();
                  retransmissions = 0;
               }
            }
        });
    }
    
    @FXML
    private void onCommandChanged(){
        if(commandNumberComboBox.getValue() == "1"){
            if(!masterCommandVBox.getChildren().contains(textToSendTextField))
                 masterCommandVBox.getChildren().add(4, textToSendTextField);
                sendCommandButton.setDisable(false);
        } else if(commandNumberComboBox.getValue() == "2") {
            masterCommandVBox.getChildren().remove(textToSendTextField);
            sendCommandButton.setDisable(false);
        }
    }
    
    @FXML
    public void onSendCommand(){
        byte addressByte = 0;
        var selected = (RadioButton)modeGroup.getSelectedToggle();     
        byte commandByte = Byte.parseByte((String)commandNumberComboBox.getValue());
        if("addressed".equals(selected.getId())){
            addressByte = slaveAddressSpinner.getValue().byteValue();
            if(retransmissions == 0 && commandByte == 2)
                timeoutTimer.schedule(new TimeoutTask(this), timeoutSpinner.getValue(), timeoutSpinner.getValue());
            if(retransmissions > retransmissionSpinner.getValue() && commandByte == 2){
                timeoutTimer.cancel();
                timeoutTimer = new Timer();
                retransmissions = 0;
                return;
            }
        }
        var argument = "";
        if(commandByte == 1){
            argument = textToSendTextField.getText();
        }
        var frame = new ASCIIFrame(addressByte, commandByte, argument);
        var frameBytes = frame.getFrame().getBytes();
        hexSentTextArea.setText(Hex.bytesToHex(frameBytes));
        App.selectedPort.writeBytes(frameBytes, frameBytes.length);
    }
    
    @FXML
    private void goBackToSelection() throws IOException {
        App.setRoot("stationSelector");
    }
    
}
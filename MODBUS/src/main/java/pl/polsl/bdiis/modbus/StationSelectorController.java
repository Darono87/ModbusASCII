package pl.polsl.bdiis.modbus;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class StationSelectorController {
    
    @FXML
    Button createMasterStationButton;
    
    @FXML
    Button createSlaveStationButton;
    
    @FXML
    ComboBox portComboBox;
    
    ObservableList<String> observableList = FXCollections.observableArrayList();
    SerialPort[] serialPorts;
    
    private void updateButtons(){
        createMasterStationButton.setDisable(App.selectedPort == null);
        createSlaveStationButton.setDisable(App.selectedPort == null);
    }
    
    @FXML
    protected void initialize(){
        serialPorts = SerialPort.getCommPorts();
        for(var serialPort : serialPorts){
            observableList.add(serialPort.getDescriptivePortName());
        }
        portComboBox.setItems(observableList);
        if(App.selectedPort != null) {
            App.selectedPort.closePort();
            portComboBox.setValue(App.selectedPort.getDescriptivePortName());
        }
        updateButtons();
    }
    
    @FXML
    private void switchToSlaveStation() throws IOException {
        App.selectedPort.openPort();
        App.setRoot("slaveStation");
    }
    
    @FXML
    private void switchToMasterStation() throws IOException {
        App.selectedPort.openPort();
        App.setRoot("masterStation");
    }
    
    @FXML
    private void onSelectPort() {
        for(int i = 0; i < serialPorts.length; i++){
            if(serialPorts[i].getDescriptivePortName() == portComboBox.getValue()){               
                App.selectedPort = serialPorts[i];
                updateButtons();
                break;
            }
        }
    }
    
}

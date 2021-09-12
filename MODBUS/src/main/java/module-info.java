module pl.polsl.bdiis.modbus {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;

    opens pl.polsl.bdiis.modbus to javafx.fxml;
    exports pl.polsl.bdiis.modbus;
}

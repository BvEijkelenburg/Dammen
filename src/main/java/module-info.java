module Dammen {
    requires javafx.controls;
    requires javafx.fxml;


    opens nl.bve.dammen to javafx.fxml;
    exports nl.bve.dammen;
}
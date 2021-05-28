package by.bsuir.controller;

import javafx.scene.control.Alert;

public class AlertController {

    public static void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

}

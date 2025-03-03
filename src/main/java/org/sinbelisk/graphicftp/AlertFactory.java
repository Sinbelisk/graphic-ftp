package org.sinbelisk.graphicftp;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertFactory {
    private static final String ERROR_TITLE = "Error";
    private static final String WARNING_TITLE = "Warning";
    private static final String INFO_TITLE = "Info";
    private static final String CONFIRM_TITLE = "Confirm";

    public static void showInfoAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, INFO_TITLE, message);
    }

    public static void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, ERROR_TITLE, message);
    }

    public static void showWarningAlert(String message) {
        showAlert(Alert.AlertType.WARNING, WARNING_TITLE, message);
    }

    public static boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRM_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package org.sinbelisk.graphicftp.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Clase que proporciona métodos estáticos para mostrar diferentes tipos de alertas en la interfaz gráfica.
 * Permite mostrar alertas de información, error, advertencia, confirmación y de entrada de texto.
 */
public class AlertFactory {
    private static final String ERROR_TITLE = "Error";
    private static final String WARNING_TITLE = "Warning";
    private static final String INFO_TITLE = "Info";
    private static final String CONFIRM_TITLE = "Confirm";

    /**
     * Muestra una alerta de tipo información con el mensaje especificado.
     *
     * @param message El mensaje que se mostrará en la alerta.
     */
    public static void showInfoAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, INFO_TITLE, message);
    }

    /**
     * Muestra una alerta de tipo error con el mensaje especificado.
     *
     * @param message El mensaje que se mostrará en la alerta.
     */
    public static void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, ERROR_TITLE, message);
    }

    /**
     * Muestra una alerta de tipo advertencia con el mensaje especificado.
     *
     * @param message El mensaje que se mostrará en la alerta.
     */
    public static void showWarningAlert(String message) {
        showAlert(Alert.AlertType.WARNING, WARNING_TITLE, message);
    }

    /**
     * Muestra una alerta de confirmación con el mensaje especificado.
     * El resultado de la alerta se puede usar para determinar si el usuario ha confirmado (OK) o cancelado (CANCEL).
     *
     * @param message El mensaje que se mostrará en la alerta de confirmación.
     * @return true si el usuario hace clic en "OK", false si hace clic en "Cancel".
     */
    public static boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRM_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra un cuadro de diálogo de entrada de texto para que el usuario ingrese un valor.
     *
     * @param message El mensaje que se mostrará en el cuadro de texto.
     * @return El texto ingresado por el usuario, o null si el usuario cancela la operación.
     */
    public static String showTextInputDialog(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ingresa los datos");
        dialog.setHeaderText("Ingrese el nombre de la nueva carpeta:");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Muestra una alerta con el tipo, título y mensaje proporcionados.
     *
     * @param type El tipo de alerta (información, error, advertencia, etc.).
     * @param title El título de la alerta.
     * @param message El mensaje que se mostrará en la alerta.
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

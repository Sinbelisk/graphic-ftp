package org.sinbelisk.graphicftp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.services.FTPClientManager;
import org.sinbelisk.graphicftp.util.AlertFactory;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Controlador para la interfaz de usuario del explorador de archivos FTP. Maneja las interacciones
 * del usuario con los campos de entrada y el árbol de directorios, así como las acciones de conexión
 * y desconexión a un servidor FTP.
 */
public class FileExplorerController {
    private static final Logger logger = LogManager.getLogger(FileExplorerController.class);

    // Campos vinculados a la interfaz gráfica (FXML)
    @FXML
    public TextField portField;
    @FXML
    public TextField serverAddressField;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    private TreeView<String> fileTreeView;

    private FTPClientManager ftpClientManager;
    private FTPFileExplorer ftpFileExplorer;
    private FileTreeContextMenu fileTreeContextMenu;

    /**
     * Método de inicialización que se ejecuta al cargar la vista.
     * Configura la visualización del árbol de archivos y el menú contextual,
     * además de establecer el formateador para el campo de puerto.
     */
    public void initialize() {
        // Configura la acción al hacer clic en el árbol de archivos
        fileTreeView.setOnMouseClicked(this::handleTreeViewClick);

        // Inicializa el explorador de archivos y el menú contextual
        ftpFileExplorer = new FTPFileExplorer(fileTreeView);
        fileTreeContextMenu = new FileTreeContextMenu(fileTreeView, ftpFileExplorer);

        // Configura el filtro para el campo del puerto
        setupFormatterForPortField();
    }

    /**
     * Configura el filtro para el campo de puerto, asegurando que solo se ingresen dígitos.
     */
    private void setupFormatterForPortField(){
        // Expresión regular que permite solo dígitos
        Pattern pattern = Pattern.compile("\\d*");

        // Filtro que permite solo el texto que coincide con la expresión regular
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            }
            return null;
        };

        // Asocia el filtro al campo de texto del puerto
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        portField.setTextFormatter(textFormatter);
    }

    /**
     * Método que se ejecuta cuando el usuario hace clic en el botón de conexión.
     * Valida los campos de entrada y establece una conexión con el servidor FTP.
     * Si ya hay una conexión activa, solicita confirmación para desconectar antes de conectar nuevamente.
     */
    public void onConnectClicked(ActionEvent actionEvent){
        // Verifica si ya hay una conexión activa y solicita confirmación para desconectar
        if (isClientConnected() && !AlertFactory.showConfirmationAlert("Ya hay una conexión activa, estás seguro?")) {
            disconnect();
        }

        // Valida que todos los campos de entrada estén completos
        if (!areFieldsValid()){
            AlertFactory.showErrorAlert("Los campos no pueden estar vacios.");
            logger.warn("Some or all fields are invalid");
            return;
        }

        // Obtiene los datos de conexión desde los campos de texto
        String serverAddress = serverAddressField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        int port = Integer.parseInt(portField.getText());

        // Intenta conectar y autenticar al usuario
        ftpClientManager = new FTPClientManager(serverAddress, port);
        boolean loginSuccess = ftpClientManager.connectAndLogin(username, password);

        // Si la conexión es exitosa, sincroniza el explorador de archivos
        if (loginSuccess) {
            ftpFileExplorer.sync(ftpClientManager);
            AlertFactory.showInfoAlert("Se ha establecido la conexión con el servidor!");
        } else {
            AlertFactory.showErrorAlert("Error al conectarse al servidor especificado.");
            logger.error("Could not connect to server");
            // Si la conexión falla, desconecta si estaba previamente conectado
            if (isClientConnected()) disconnect();
        }
    }

    /**
     * Método que se ejecuta cuando el usuario hace clic en el botón de desconexión.
     * Desconecta del servidor FTP si hay una conexión activa.
     */
    public void onDisconnectClicked(ActionEvent actionEvent) {
        // Verifica si no hay ninguna conexión activa
        if (ftpClientManager == null){
            AlertFactory.showErrorAlert("No hay ninguna conexión abierta.");
            logger.warn("User wanted to disconnect, but there's not active connection.");
            return;
        }

        // Solicita confirmación antes de desconectar
        if(isClientConnected() && AlertFactory.showConfirmationAlert("Seguro que quieres terminar la sesión?")){
            disconnect();
        }
    }

    /**
     * Verifica si el cliente FTP está conectado.
     *
     * @return true si el cliente FTP está conectado, false si no lo está.
     */
    private boolean isClientConnected() {
        return ftpClientManager != null && ftpClientManager.getFtpClient().isConnected();
    }

    /**
     * Verifica que todos los campos de entrada (servidor, puerto, usuario y contraseña) estén completos.
     *
     * @return true si todos los campos son válidos, false si alguno está vacío.
     */
    private boolean areFieldsValid() {
        return !portField.getText().isEmpty() && !serverAddressField.getText().isEmpty() && !usernameField.getText().isEmpty() && !passwordField.getText().isEmpty();
    }

    /**
     * Desconecta el cliente FTP y desactiva la sincronización con el explorador de archivos.
     */
    private void disconnect() {
        ftpClientManager.disconnect();
        ftpFileExplorer.desync();
        logger.info("User disconnected and file explored desynchronized");
    }

    /**
     * Maneja los clics en el árbol de directorios, mostrando un menú contextual si se hace clic con el botón derecho.
     *
     * @param event el evento de clic del ratón.
     */
    private void handleTreeViewClick(MouseEvent event) {
        // Solo maneja clics con el botón derecho del ratón
        if (event.getButton() != MouseButton.SECONDARY) {
            return;
        }

        // Muestra el menú contextual para el archivo o carpeta seleccionado
        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fileTreeContextMenu.showContextMenu(event, selectedItem);
        }
    }
}

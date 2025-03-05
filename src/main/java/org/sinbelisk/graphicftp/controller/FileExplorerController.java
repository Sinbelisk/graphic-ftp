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

public class FileExplorerController {
    private static final Logger logger = LogManager.getLogger(FileExplorerController.class);

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

    public void initialize() {
        fileTreeView.setOnMouseClicked(this::handleTreeViewClick);
        ftpFileExplorer = new FTPFileExplorer(fileTreeView);
        fileTreeContextMenu = new FileTreeContextMenu(fileTreeView, ftpFileExplorer);
        setupFormatterForPortField();
    }

    private void setupFormatterForPortField(){
        // Expresión regular que permite solo dígitos
        Pattern pattern = Pattern.compile("\\d*");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        portField.setTextFormatter(textFormatter);
    }

    public void onConnectClicked(ActionEvent actionEvent){
        if (isClientConnected() && !AlertFactory.showConfirmationAlert("Ya hay una conexión activa, estás seguro?")) {
            disconnect();
        }

        if (!areFieldsValid()){
            AlertFactory.showErrorAlert("Los campos no pueden estar vacios.");
            return;
        }

        String serverAddress = serverAddressField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        int port = Integer.parseInt(portField.getText());

        ftpClientManager = new FTPClientManager(serverAddress, port);
        boolean loginSuccess = ftpClientManager.connectAndLogin(username, password);

        if (loginSuccess) {
            ftpFileExplorer.sync(ftpClientManager);
            AlertFactory.showInfoAlert("Se ha establecido la conexión con el servidor!");
        } else {
            AlertFactory.showErrorAlert("Error al conectarse al servidor especificado.");
            if (isClientConnected()) disconnect();
        }
    }

    public void onDisconnectClicked(ActionEvent actionEvent) {
        if (ftpClientManager == null){
            AlertFactory.showErrorAlert("No hay ninguna conexión abierta.");
            return;
        }

        if(isClientConnected() && AlertFactory.showConfirmationAlert("Seguro que quieres terminar la sesión?")){
           disconnect();
        }
    }

    private boolean isClientConnected() {
        return ftpClientManager != null && ftpClientManager.getFtpClient().isConnected();
    }

    private boolean areFieldsValid() {
        return !portField.getText().isEmpty() && !serverAddressField.getText().isEmpty() && !usernameField.getText().isEmpty() && !passwordField.getText().isEmpty();
    }

    private void disconnect() {
        ftpClientManager.disconnect();
        ftpFileExplorer.desync();
    }

    private void handleTreeViewClick(MouseEvent event) {
        if (event.getButton() != MouseButton.SECONDARY) {
            return;
        }

        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fileTreeContextMenu.showContextMenu(event, selectedItem);
        }
    }
}
package org.sinbelisk.graphicftp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.services.FTPClientManager;

import java.io.IOException;

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
    public Button uploadFileBtn;
    @FXML
    public Button uploadDirBtn;
    @FXML
    private TreeView<String> fileTreeView;

    private FTPClientManager ftpClientManager;
    private FTPFileExplorer ftpFileExplorer;
    private FileTreeContextMenu fileTreeContextMenu;

    public void initialize() {
        fileTreeView.setOnMouseClicked(this::handleTreeViewClick);
        ftpFileExplorer = new FTPFileExplorer(fileTreeView);
        fileTreeContextMenu = new FileTreeContextMenu(fileTreeView, ftpFileExplorer);
    }

    public void onConnectClicked(ActionEvent actionEvent) throws IOException {
        if (isClientConnected() && !AlertFactory.showConfirmationAlert("Ya hay una conexión activa, estás seguro?")) {
            disconnect();
        }

        String serverAddress = serverAddressField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        int port = Integer.parseInt(portField.getText());

        ftpClientManager = new FTPClientManager(serverAddress, port);
        boolean loginSuccess = ftpClientManager.connectAndLogin(username, password);

        if (loginSuccess) {
            ftpFileExplorer.sync(ftpClientManager);

            uploadDirBtn.setVisible(true);
            uploadFileBtn.setVisible(true);
        } else {
            AlertFactory.showErrorAlert("Error al conectarse al servidor especificado.");
            if (isClientConnected()) disconnect();
        }
    }

    public void onDisconnectClicked(ActionEvent actionEvent) {
        disconnect();
    }

    private boolean isClientConnected() {
        return ftpClientManager != null && ftpClientManager.getFtpClient().isConnected();
    }

    private void disconnect() {
        ftpClientManager.disconnect();
        ftpFileExplorer.desync();

        uploadDirBtn.setVisible(false);
        uploadFileBtn.setVisible(false);
    }

    public void onUploadClicked(ActionEvent actionEvent) {
    }

    public void onUploadDirectoryClicked(ActionEvent actionEvent) {
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
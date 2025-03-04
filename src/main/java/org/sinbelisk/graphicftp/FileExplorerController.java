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

    public void initialize() {
        fileTreeView.setOnMouseClicked(this::handleTreeViewClick);
        ftpFileExplorer = new FTPFileExplorer(fileTreeView);
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
            ftpFileExplorer.sync(ftpClientManager.getFtpClient());

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
        if (!(event.getButton() == MouseButton.SECONDARY)) {
            return;
        }

        TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem createFolderItem = new MenuItem("Crear Carpeta");
            createFolderItem.setOnAction(e -> {
                try {
                    ftpFileExplorer.createFolder(selectedItem);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            MenuItem renameItem = new MenuItem("Renombrar");
            renameItem.setOnAction(e -> {
                String newName = AlertFactory.showTextInputDialog("Introduce el nombre del archivo o fichero");
                try {
                    ftpFileExplorer.renameFileOrFolder(selectedItem, newName);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            MenuItem deleteItem = new MenuItem("Eliminar");
            deleteItem.setOnAction(e -> {
                try {
                    ftpFileExplorer.deleteFileOrFolder(selectedItem);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            contextMenu.getItems().addAll(createFolderItem, renameItem, deleteItem);
            contextMenu.show(fileTreeView, event.getScreenX(), event.getScreenY());
        }
    }
}

package org.sinbelisk.graphicftp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.services.FTPClientManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void initialize() {
        fileTreeView.setOnMouseClicked(this::handleTreeViewClick);
    }

    private void populateTreeView(TreeItem<String> parent, String path) throws IOException {
        logger.info("Populating tree view for path: " + path); // Log added here
        FTPFile[] filesAndDirs = ftpClientManager.getFtpClient().listFiles(path);

        List<TreeItem<String>> fileItems = new ArrayList<>();

        for (FTPFile file : filesAndDirs) {
            String icon = file.isDirectory() ? "📁" : file.getName().endsWith(".exe") ? "💽" : "📝";
            TreeItem<String> item = new TreeItem<>(icon + file.getName());

            if (file.isDirectory()) {
                String newPath = path.endsWith("/") ? path + file.getName() : path + "/" + file.getName();
                logger.info("Directory found, recursing into: " + newPath); // Log for recursive directory path
                populateTreeView(item, newPath);
            }

            fileItems.add(item);
        }

        parent.getChildren().addAll(fileItems);
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
            TreeItem<String> rootItem = new TreeItem<>("📁 Root");
            fileTreeView.setRoot(rootItem);
            populateTreeView(rootItem, "/");

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
        fileTreeView.setRoot(null);

        uploadDirBtn.setVisible(false);
        uploadFileBtn.setVisible(false);
    }

    public void onUploadClicked(ActionEvent actionEvent) {
    }

    public void onUploadDirectoryClicked(ActionEvent actionEvent) {
    }

    private void handleTreeViewClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem createFolderItem = new MenuItem("Crear Carpeta");
                createFolderItem.setOnAction(e -> createFolder(selectedItem));

                MenuItem renameItem = new MenuItem("Renombrar");
                renameItem.setOnAction(e -> renameFileOrFolder(selectedItem));

                MenuItem deleteItem = new MenuItem("Eliminar");
                deleteItem.setOnAction(e -> deleteFileOrFolder(selectedItem));

                contextMenu.getItems().addAll(createFolderItem, renameItem, deleteItem);
                contextMenu.show(fileTreeView, event.getScreenX(), event.getScreenY());
            }
        }
    }

    private void createFolder(TreeItem<String> selectedItem) {
        String folderName = AlertFactory.showTextInputDialog("Nombre de la carpeta");

        try {
            String path = getPathFromTreeItem(selectedItem);
            logger.info("Creating folder at path: {}/{}", path, folderName);

            if (ftpClientManager.getFtpClient().makeDirectory(path + "/" + folderName)) {
                TreeItem<String> newFolder = new TreeItem<>("📁" + folderName);
                selectedItem.getChildren().add(newFolder);
            } else {
                AlertFactory.showErrorAlert("Error al crear la carpeta.");
            }
        } catch (IOException e) {
            AlertFactory.showErrorAlert("Error al crear la carpeta: " + e.getMessage());
        }
    }

    private void renameFileOrFolder(TreeItem<String> selectedItem) {
        String newName = AlertFactory.showTextInputDialog("Nombre de la carpeta o fichero");
        try {
            String path = getPathFromTreeItem(selectedItem);
            String parentPath = getPathFromTreeItem(selectedItem.getParent());
            logger.info("Renaming file/folder from: {} to: {}/{}", path, parentPath, newName);

            if (ftpClientManager.getFtpClient().rename(path, parentPath + "/" + newName)) {
                selectedItem.setValue(selectedItem.getValue().charAt(0) + newName);
            } else {
                AlertFactory.showErrorAlert("Error al renombrar.");
            }
        } catch (IOException e) {
            AlertFactory.showErrorAlert("Error al renombrar: " + e.getMessage());
        }
    }

    private void deleteFileOrFolder(TreeItem<String> selectedItem) {
        if (AlertFactory.showConfirmationAlert("¿Seguro que quieres eliminar la carpeta?")){
            try {
                String path = getPathFromTreeItem(selectedItem);
                logger.info("Deleting file/folder at path: {}", path); // Log added here
                boolean success;
                if (selectedItem.getValue().startsWith("📁")) {
                    success = ftpClientManager.getFtpClient().removeDirectory(path);
                } else {
                    success = ftpClientManager.getFtpClient().deleteFile(path);
                }
                if (success) {
                    selectedItem.getParent().getChildren().remove(selectedItem);
                } else {
                    AlertFactory.showErrorAlert("Error al eliminar.");
                }
            } catch (IOException e) {
                AlertFactory.showErrorAlert("Error al eliminar: " + e.getMessage());
            }
        }
    }

    private String getPathFromTreeItem(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();
        while (item != null && item.getParent() != null) {
            // Extraemos solo el nombre real, sin el emoticono
            String itemName = item.getValue().substring(2);  // Elimina el emoticono
            path.insert(0, "/" + itemName);
            item = item.getParent();
        }
        return path.toString();
    }
}

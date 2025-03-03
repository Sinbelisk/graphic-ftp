package org.sinbelisk.graphicftp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    private TreeView<String> fileTreeView;

    private FTPClientManager ftpClientManager;

    public void initialize() {
    }

    private void populateTreeView(TreeItem<String> parent, String path) throws IOException {
        FTPFile[] filesAndDirs = ftpClientManager.getFtpClient().listFiles(path);

        List<TreeItem<String>> fileItems = new ArrayList<>();

        for (FTPFile file : filesAndDirs) {
            String icon = file.isDirectory() ? "📁" : file.getName().endsWith(".exe") ? "💽" : "📝" ;
            TreeItem<String> item = new TreeItem<>(icon + file.getName());

            if (file.isDirectory()) {
                populateTreeView(item, path + "/" + file.getName());
            }

            fileItems.add(item);
        }

        parent.getChildren().addAll(fileItems);
    }

    public void onConnectClicked(ActionEvent actionEvent) {
        if (isClientConnected() && !AlertFactory.showConfirmationAlert("Ya hay una conexión activa, estás seguro?")) {
            disconnect();
        }

        try {
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
            } else {
                AlertFactory.showErrorAlert("Error al conectarse al servidor especificado.");
                if (isClientConnected()) disconnect();

            }
        } catch (IOException e) {
            AlertFactory.showErrorAlert("Error al conectarse al servidor especificado.");
            logger.error("Error al conectarse al servidor especificado.", e);
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
    }
}
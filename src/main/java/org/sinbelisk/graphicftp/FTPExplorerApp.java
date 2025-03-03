package org.sinbelisk.graphicftp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPFile;
import org.sinbelisk.graphicftp.services.FTPClientManager;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

public class FTPExplorerApp extends Application {
    private FTPClientManager ftpManager;
    private TreeView<String> directoryTree;
    private TableView<FTPFile> fileTable;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/file_explorer.fxml"));
        primaryStage.setTitle("FTP File Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}



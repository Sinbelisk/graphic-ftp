package org.sinbelisk.graphicftp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.sinbelisk.graphicftp.util.FileChooserUtils;

public class FTPExplorerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FileChooserUtils.setStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("/file_explorer.fxml"));
        primaryStage.setTitle("FTP File Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}



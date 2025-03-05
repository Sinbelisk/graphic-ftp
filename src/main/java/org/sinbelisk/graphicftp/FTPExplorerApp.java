package org.sinbelisk.graphicftp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.sinbelisk.graphicftp.util.FileChooserUtils;

/**
 * Aplicación principal para el explorador de archivos FTP.
 * Esta clase extiende {@link Application} de JavaFX y es responsable de inicializar la interfaz gráfica
 * de la aplicación y cargar la vista principal desde un archivo FXML.
 * <p>
 * En el método {@link #start(Stage)}, se configura el {@link Stage} principal de la aplicación, se carga la vista
 * del explorador de archivos desde el archivo FXML y se muestra en la ventana principal. Además, antes de cargar la
 * vista, se configura el {@link FileChooserUtils} para que se pueda utilizar en el resto de la aplicación.
 * </p>
 */
public class FTPExplorerApp extends Application {

    /**
     * Método principal para ejecutar la aplicación.
     * para iniciar la aplicación JavaFX.
     *
     * @param args los argumentos de línea de comandos (no utilizados en esta aplicación)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Método que configura y muestra la interfaz gráfica de la aplicación.
     * Carga la vista del explorador de archivos desde el archivo FXML y configura el {@link Stage}.
     * <p>
     * Antes de cargar la vista, se configura el {@link FileChooserUtils} para que la aplicación pueda usar cuadros
     * de diálogo de selección de archivos y carpetas. Luego, se carga el archivo FXML que define la interfaz
     * y se muestra en la ventana principal.
     * </p>
     *
     * @param primaryStage el Stage principal de la aplicación
     * @throws Exception si ocurre algún error al cargar el archivo FXML
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FileChooserUtils.setStage(primaryStage);  // Configura el Stage para el uso de diálogos de selección de archivos
        Parent root = FXMLLoader.load(getClass().getResource("/file_explorer.fxml"));  // Carga la vista desde el archivo FXML
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setTitle("FTP File Explorer");  // Establece el título de la ventana
        primaryStage.setScene(new Scene(root));  // Establece la escena de la aplicación con la vista cargada
        primaryStage.show();  // Muestra la ventana principal
    }
}
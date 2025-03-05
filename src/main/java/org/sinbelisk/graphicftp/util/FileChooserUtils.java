package org.sinbelisk.graphicftp.util;

import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Utilidades para manejar cuadros de diálogo de selección de archivos y carpetas en la interfaz gráfica.
 * Esta clase proporciona métodos para seleccionar archivos, carpetas, y elegir ubicaciones para guardar
 * archivos o carpetas, utilizando el {@link FileChooser} y {@link DirectoryChooser} de JavaFX.
 * <p>
 * Es necesario configurar el {@link Stage} principal de la aplicación antes de usar cualquier método de esta clase.
 * Los métodos de selección permiten elegir un solo archivo, múltiples archivos, archivos dentro de una carpeta,
 * o carpetas completas. También se incluye una opción para elegir dónde guardar un archivo o una carpeta.
 * </p>
 */
public class FileChooserUtils {

    // El Stage principal de la aplicación, necesario para abrir los cuadros de diálogo
    private static Stage stage;

    /**
     * Configura el Stage que se usará por defecto.
     *
     * @param stage el Stage principal de la aplicación
     */
    public static void setStage(Stage stage) {
        FileChooserUtils.stage = stage;
    }

    /**
     * Abre un cuadro de diálogo para seleccionar un archivo.
     *
     * @return el archivo seleccionado o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static File selectFile() {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Abre un cuadro de diálogo para seleccionar múltiples archivos.
     *
     * @return la lista de archivos seleccionados o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static List<File> selectMultipleFiles() {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivos");
        return fileChooser.showOpenMultipleDialog(stage);
    }

    /**
     * Abre un cuadro de diálogo para seleccionar una carpeta y un archivo dentro de ella.
     * El usuario primero selecciona la carpeta y luego se le pide que seleccione un archivo dentro de esa carpeta.
     *
     * @return el archivo seleccionado dentro de la carpeta o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static File selectFileInFolder() {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        // Primero, el usuario selecciona una carpeta
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");
        File selectedFolder = directoryChooser.showDialog(stage);

        if (selectedFolder == null) {
            return null; // Si no seleccionó una carpeta, retorna null
        }

        // Si se seleccionó una carpeta, muestra un FileChooser para seleccionar un archivo dentro de esa carpeta
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo dentro de la carpeta");
        fileChooser.setInitialDirectory(selectedFolder);  // Establecer la carpeta seleccionada como directorio inicial

        return fileChooser.showOpenDialog(stage);  // Abre el diálogo para seleccionar el archivo dentro de la carpeta
    }

    /**
     * Abre un cuadro de diálogo para seleccionar una carpeta.
     *
     * @return la carpeta seleccionada o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static File selectFolder() {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");
        return directoryChooser.showDialog(stage);
    }

    /**
     * Abre un cuadro de diálogo para elegir dónde guardar un archivo.
     *
     * @param defaultFileName nombre por defecto del archivo
     * @return el archivo donde se guardará o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static File saveFile(String defaultFileName) {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo");
        fileChooser.setInitialFileName(defaultFileName);
        return fileChooser.showSaveDialog(stage);
    }

    /**
     * Abre un cuadro de diálogo para elegir dónde guardar una carpeta.
     *
     * @return la carpeta de destino seleccionada o null si se cancela
     * @throws IllegalStateException si el Stage no ha sido configurado
     */
    public static File saveFolder() {
        if (stage == null) {
            throw new IllegalStateException("El Stage no ha sido configurado.");
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta de destino");
        return directoryChooser.showDialog(stage);
    }
}

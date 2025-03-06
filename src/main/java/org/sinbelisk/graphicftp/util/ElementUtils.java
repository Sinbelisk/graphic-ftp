package org.sinbelisk.graphicftp.util;

import javafx.scene.control.TreeItem;
import org.apache.commons.net.ftp.FTPFile;
import org.sinbelisk.graphicftp.controller.FTPFileExplorer;

import java.io.File;

/**
 * Utilidades para manejar elementos dentro de un árbol de archivos, ya sea local o remoto.
 * Esta clase proporciona métodos para obtener iconos de archivos, crear rutas a partir de elementos
 * del árbol y otros métodos relacionados con el manejo de archivos y carpetas.
 * <p>
 * Esta clase se utiliza principalmente en la clase {@link FTPFileExplorer} para representar archivos y carpetas
 * con iconos y realizar manipulaciones de nombre y rutas.
 */
public class ElementUtils {
    // Iconos para representar carpetas, archivos y programas
    public static final String FOLDER_ICON = "\uD83D\uDCC1";
    public static final String FILE_ICON = "\uD83D\uDCDD";
    public static final String PROGRAM_ICON = "\uD83D\uDCBD";

    /**
     * Devuelve el nombre de un archivo o carpeta en el árbol con el nuevo nombre proporcionado.
     * Este método también conserva el icono y la extensión original.
     *
     * @param originalName El nombre original del archivo o carpeta.
     * @param newName El nuevo nombre que se asignará.
     * @return El nombre actualizado con el icono original y la extensión.
     */
    public static String getTreeParsedName(String originalName, String newName) {
        // Obtiene el icono (carpeta o archivo) del nombre original
        String icon = originalName.substring(0, 2);
        // Obtiene la extensión del archivo
        String extension;
        try {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        } catch (IndexOutOfBoundsException e) { extension = ""; }
        // Devuelve el nuevo nombre con el icono y la extensión
        return icon + newName + extension;
    }

    /**
     * Obtiene el icono correspondiente a un archivo dado, dependiendo de si es un directorio,
     * un archivo ejecutable o cualquier otro tipo de archivo.
     *
     * @param file El archivo para el cual se desea obtener el icono.
     * @return El icono correspondiente al archivo.
     */
    public static String getIconForFile(File file) {
        if (file.isDirectory()) {
            return FOLDER_ICON; // Es un directorio
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON; // Es un archivo ejecutable
        } else {
            return FILE_ICON; // Es un archivo normal
        }
    }

    /**
     * Obtiene el icono correspondiente a un archivo FTP, dependiendo de si es un directorio,
     * un archivo ejecutable o cualquier otro tipo de archivo.
     *
     * @param file El archivo FTP para el cual se desea obtener el icono.
     * @return El icono correspondiente al archivo FTP.
     */
    public static String getIconForFile(FTPFile file) {
        if (file.isDirectory()) {
            return FOLDER_ICON; // Es un directorio
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON; // Es un archivo ejecutable
        } else {
            return FILE_ICON; // Es un archivo normal
        }
    }

    /**
     * Extrae la ruta completa a partir de un elemento del árbol (TreeItem), comenzando desde
     * el elemento seleccionado y subiendo hasta la raíz del árbol.
     *
     * @param item El TreeItem del cual se extraerá la ruta.
     * @return La ruta completa del elemento en el árbol.
     */
    public static String getPathFromTreeItem(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();

        // Subir por el árbol hasta llegar a la raíz
        while (item != null && item.getParent() != null) {
            String itemName = item.getValue().substring(2); // Elimina el icono del nombre
            path.insert(0, "/" + itemName); // Agrega el nombre al inicio de la ruta
            item = item.getParent(); // Subir al nodo padre
        }
        return path.toString(); // Devuelve la ruta construida
    }

    /**
     * Determina si el elemento seleccionado en el árbol corresponde a una carpeta.
     *
     * @param selectedItem El TreeItem a verificar.
     * @return true si el elemento es una carpeta, false si no lo es.
     */
    public static boolean isElementFolder(TreeItem<String> selectedItem) {
        return (selectedItem.getValue().startsWith(FOLDER_ICON)); // Verifica si comienza con el icono de carpeta
    }
}
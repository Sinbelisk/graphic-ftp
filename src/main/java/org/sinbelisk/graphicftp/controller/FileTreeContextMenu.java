package org.sinbelisk.graphicftp.controller;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.util.AlertFactory;

import java.io.IOException;

/**
 * Clase que maneja el menú contextual para el explorador de archivos FTP.
 * Proporciona opciones para interactuar con los archivos y carpetas a través del árbol de directorios.
 * Las opciones incluyen subir, descargar, crear carpetas, renombrar y eliminar archivos/carpetas.
 */
public class FileTreeContextMenu {
    private final FTPFileExplorer ftpFileExplorer;
    private final TreeView<String> fileTreeView;
    private final ContextMenu contextMenu;

    /**
     * Constructor que inicializa el menú contextual para el árbol de archivos.
     * Configura las opciones disponibles en el menú y asigna las acciones correspondientes.
     *
     * @param fileTreeView El TreeView que contiene los archivos y carpetas.
     * @param ftpFileExplorer El explorador de archivos FTP que maneja las operaciones de archivos.
     */
    public FileTreeContextMenu(TreeView<String> fileTreeView, FTPFileExplorer ftpFileExplorer) {
        this.ftpFileExplorer = ftpFileExplorer;
        this.fileTreeView = fileTreeView;
        this.contextMenu = new ContextMenu();

        // Objetos del menú desplegable
        MenuItem uploadItem = new MenuItem("Subir");
        MenuItem downloadItem = new MenuItem("Descargar");
        MenuItem createFolderItem = new MenuItem("Crear Carpeta");
        MenuItem renameItem = new MenuItem("Renombrar");
        MenuItem deleteItem = new MenuItem("Eliminar");

        contextMenu.getItems().addAll(createFolderItem, renameItem, deleteItem, downloadItem, uploadItem);
    }

    /**
     * Actualiza los controladores de acción del menú según el TreeItem seleccionado
     * y muestra el menú en la posición del evento.
     *
     * @param event El evento de clic que activa la visualización del menú contextual.
     * @param selectedItem El TreeItem seleccionado sobre el cual se mostrará el menú.
     */
    public void showContextMenu(MouseEvent event, TreeItem<String> selectedItem) {
        if (selectedItem == null) {
            return;
        }

        updateMenuItems(selectedItem);
        contextMenu.show(fileTreeView, event.getScreenX(), event.getScreenY());
    }

    /**
     * Actualiza las acciones de los elementos del menú contextual en función del archivo o carpeta seleccionada.
     * Asigna las acciones correspondientes a las opciones de crear carpeta, renombrar, eliminar, subir y descargar.
     *
     * @param selectedItem El TreeItem seleccionado que corresponde a un archivo o carpeta en el árbol de archivos.
     */
    private void updateMenuItems(TreeItem<String> selectedItem) {
        // Acción para crear una nueva carpeta
        setActionForMenuItem(contextMenu.getItems().get(0), () -> {
            if (!ftpFileExplorer.createFolder(selectedItem))
                AlertFactory.showErrorAlert("No se ha podido crear la carpeta.");
        });

        // Acción para renombrar un archivo o carpeta
        setActionForMenuItem(contextMenu.getItems().get(1), () -> {
            String newName = AlertFactory.showTextInputDialog("Introduce el nombre del archivo o fichero");
            if (!ftpFileExplorer.renameFileOrFolder(selectedItem, newName))
                AlertFactory.showErrorAlert("No se ha podido renombrar la carpeta o fichero");
        });

        // Acción para eliminar un archivo o carpeta
        setActionForMenuItem(contextMenu.getItems().get(2), () -> {
            if (!ftpFileExplorer.deleteFileOrFolder(selectedItem))
                AlertFactory.showErrorAlert("No se ha podido eliminar la carpeta o fichero");
        });

        // Acción para descargar un archivo o carpeta
        setActionForMenuItem(contextMenu.getItems().get(3), () -> {
            if (!ftpFileExplorer.downloadFile(selectedItem))
                AlertFactory.showErrorAlert("No se ha podido descargar el elemento especificado");
            else
                AlertFactory.showInfoAlert("Elemento descargado satisfactoriamente.");
        });

        // Acción para subir un archivo o carpeta
        setActionForMenuItem(contextMenu.getItems().get(4), () -> {
            if (!ftpFileExplorer.uploadFile(selectedItem))
                AlertFactory.showErrorAlert("Error al subir el fichero seleccionado");
            else
                AlertFactory.showInfoAlert("Elemento subido satisfactoriamente al servidor.");
        });
    }

    /**
     * Asigna al MenuItem la acción pasada y centraliza el manejo de excepciones de tipo IOException.
     *
     * @param menuItem El MenuItem que va a tener asignada la acción.
     * @param action La acción que se ejecutará cuando se seleccione el MenuItem.
     */
    private void setActionForMenuItem(MenuItem menuItem, ThrowingRunnable action) {
        menuItem.setOnAction(e -> {
            try {
                action.run();
            } catch (IOException ex) {
                handleException(ex);
            }
        });
    }

    /**
     * Interfaz funcional para encapsular acciones que puedan lanzar excepciones de tipo IOException.
     */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException;
    }

    /**
     * Maneja las excepciones de tipo IOException mostrando un mensaje de error adecuado.
     *
     * @param ex La excepción que ocurrió.
     */
    private void handleException(IOException ex) {
        AlertFactory.showErrorAlert(ex.getMessage());
    }
}

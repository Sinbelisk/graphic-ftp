package org.sinbelisk.graphicftp.controller;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.util.AlertFactory;

import java.io.IOException;

public class FileTreeContextMenu {
    private final FTPFileExplorer ftpFileExplorer;
    private final TreeView<String> fileTreeView;
    private final ContextMenu contextMenu;

    public FileTreeContextMenu(TreeView<String> fileTreeView, FTPFileExplorer ftpFileExplorer) {
        this.ftpFileExplorer = ftpFileExplorer;
        this.fileTreeView = fileTreeView;
        this.contextMenu = new ContextMenu();

        // Objetos del menu desplegable.
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
     */
    public void showContextMenu(MouseEvent event, TreeItem<String> selectedItem) {
        if (selectedItem == null) {
            return;
        }

        updateMenuItems(selectedItem);
        contextMenu.show(fileTreeView, event.getScreenX(), event.getScreenY());
    }

    private void updateMenuItems(TreeItem<String> selectedItem) {
        setActionForMenuItem(contextMenu.getItems().get(0), () -> {
            if (!ftpFileExplorer.createFolder(selectedItem))
                AlertFactory.showErrorAlert("No se ha podido crear la carpeta.");
        });

        setActionForMenuItem(contextMenu.getItems().get(1), () -> {
            String newName = AlertFactory.showTextInputDialog("Introduce el nombre del archivo o fichero");
            if(!ftpFileExplorer.renameFileOrFolder(selectedItem, newName))
                AlertFactory.showErrorAlert("No se ha podido renombrar la carpeta o fichero");
        });

        setActionForMenuItem(contextMenu.getItems().get(2), () ->{
                    if(!ftpFileExplorer.deleteFileOrFolder(selectedItem))
                        AlertFactory.showErrorAlert("No se ha podido eliminar la carpeta o fichero");
                });

        setActionForMenuItem(contextMenu.getItems().get(3), () -> {
            if(!ftpFileExplorer.downloadFile(selectedItem)) AlertFactory.showErrorAlert("No se ha podido descargar el elemento especificado");
            else AlertFactory.showInfoAlert("Elemento descargado satisfactoriamente.");
        });

        setActionForMenuItem(contextMenu.getItems().get(4), () -> {
            if(!ftpFileExplorer.uploadFile(selectedItem)) AlertFactory.showErrorAlert("Error al subir el fichero seleccionado");
            else AlertFactory.showInfoAlert("Elemento subido satisfactoriamente al servidor.");
        });
    }

    /**
     * Asigna al MenuItem la acción pasada y centraliza el manejo de IOException.
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
     * Interfaz funcional para encapsular acciones que puedan lanzar IOException.
     */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException;
    }

    private void handleException(IOException ex) {
        AlertFactory.showErrorAlert(ex.getMessage());
    }
}
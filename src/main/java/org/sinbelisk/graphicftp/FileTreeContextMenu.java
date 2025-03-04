package org.sinbelisk.graphicftp;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileTreeContextMenu {
    private static final Logger logger = LogManager.getLogger(FileTreeContextMenu.class);

    private final FTPFileExplorer ftpFileExplorer;
    private final TreeView<String> fileTreeView;
    private final ContextMenu contextMenu;

    public FileTreeContextMenu(TreeView<String> fileTreeView, FTPFileExplorer ftpFileExplorer) {
        this.ftpFileExplorer = ftpFileExplorer;
        this.fileTreeView = fileTreeView;
        this.contextMenu = new ContextMenu();

        MenuItem createFolderItem = new MenuItem("Crear Carpeta");
        MenuItem renameItem = new MenuItem("Renombrar");
        MenuItem deleteItem = new MenuItem("Eliminar");

        contextMenu.getItems().addAll(createFolderItem, renameItem, deleteItem);
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
        setActionForMenuItem(contextMenu.getItems().get(0), () -> ftpFileExplorer.createFolder(selectedItem));
        setActionForMenuItem(contextMenu.getItems().get(1), () -> {
            String newName = AlertFactory.showTextInputDialog("Introduce el nombre del archivo o fichero");
            ftpFileExplorer.renameFileOrFolder(selectedItem, newName);
        });
        setActionForMenuItem(contextMenu.getItems().get(2), () -> ftpFileExplorer.deleteFileOrFolder(selectedItem));
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
        ex.printStackTrace();
    }
}
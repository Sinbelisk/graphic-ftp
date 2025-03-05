package org.sinbelisk.graphicftp.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.services.FTPClientManager;
import org.sinbelisk.graphicftp.util.AlertFactory;
import org.sinbelisk.graphicftp.util.ElementUtils;
import org.sinbelisk.graphicftp.util.FileChooserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que actúa como un puente entre la interfaz gráfica y el {@link FTPClientManager}.
 * Permite sincronizar y gestionar el árbol de archivos y carpetas del servidor FTP en una vista de {@link TreeView}.
 * Ofrece funcionalidades para subir, descargar, crear, renombrar y eliminar archivos y carpetas en el servidor FTP.
 */
public class FTPFileExplorer {
    private static final Logger logger = LogManager.getLogger(FTPFileExplorer.class);

    private final TreeView<String> treeView;
    private FTPClientManager ftpClientManager;

    /**
     * Constructor de la clase {@code FTPFileExplorer}.
     * Inicializa la vista de {@link TreeView} que representa los archivos y carpetas en el servidor FTP.
     *
     * @param treeView La vista de árbol que muestra los archivos y carpetas.
     */
    public FTPFileExplorer(TreeView<String> treeView) {
        this.treeView = treeView;
    }

    /**
     * Sincroniza el árbol de archivos con el servidor FTP.
     * Establece la conexión con el servidor FTP a través de {@link FTPClientManager} y llena el {@link TreeView}
     * con la estructura de archivos y carpetas.
     *
     * @param ftpClientManager El gestor de conexión FTP que maneja la interacción con el servidor FTP.
     */
    public void sync(FTPClientManager ftpClientManager) {
        this.ftpClientManager = ftpClientManager;
        TreeItem<String> rootItem = new TreeItem<>(ElementUtils.FOLDER_ICON + " Root");
        treeView.setRoot(rootItem);

        Task<Void> syncTask = createSyncTask(rootItem, "/");
        new Thread(syncTask).start();
    }

    /**
     * Desincroniza el árbol de archivos y elimina la conexión con el servidor FTP.
     * Elimina la raíz del {@link TreeView} y limpia la referencia al {@link FTPClientManager}.
     */
    public void desync() {
        treeView.setRoot(null);
        this.ftpClientManager = null;
    }

    /**
     * Crea una tarea para sincronizar los archivos y carpetas del servidor FTP con el {@link TreeView}.
     *
     * @param rootItem El nodo raíz del árbol de archivos en la interfaz gráfica.
     * @param rootPath La ruta inicial del servidor FTP.
     * @return La tarea que realiza la sincronización.
     */
    private Task<Void> createSyncTask(TreeItem<String> rootItem, String rootPath) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                populateTreeView(rootItem, rootPath);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                logger.info("TreeView synchronization completed successfully.");
            }

            @Override
            protected void failed() {
                super.failed();
                logger.error("TreeView synchronization failed.", getException());
            }
        };
    }

    /**
     * Rellena el árbol de archivos con los archivos y carpetas de la ruta proporcionada en el servidor FTP.
     *
     * @param parent El nodo padre del árbol en la interfaz gráfica.
     * @param path La ruta actual en el servidor FTP.
     * @throws IOException Si ocurre un error al obtener los archivos del servidor FTP.
     */
    private void populateTreeView(TreeItem<String> parent, String path) throws IOException {
        FTPFile[] filesAndDirs = ftpClientManager.getFtpClient().listFiles(path);
        List<TreeItem<String>> items = new ArrayList<>();

        for (FTPFile file : filesAndDirs) {
            TreeItem<String> item = createTreeItem(file);
            if (file.isDirectory()) {
                setupDirectoryTreeItem(item, path, file);
            }
            items.add(item);
        }

        updateTreeView(parent, items);
    }

    /**
     * Crea un {@link TreeItem} para representar un archivo o carpeta en el {@link TreeView}.
     *
     * @param file El archivo o carpeta a representar.
     * @return El {@link TreeItem} creado para ese archivo o carpeta.
     */
    private TreeItem<String> createTreeItem(FTPFile file) {
        String icon = ElementUtils.getIconForFile(file);
        TreeItem<String> item = new TreeItem<>(icon + file.getName());
        logger.info("Reading: {}", file.getName());
        return item;
    }

    /**
     * Configura un {@link TreeItem} para un directorio, añadiendo un manejador de eventos para cargar su contenido
     * cuando el usuario expanda el nodo.
     *
     * @param item El {@link TreeItem} que representa el directorio.
     * @param path La ruta del directorio en el servidor FTP.
     * @param file El objeto {@link FTPFile} que representa el directorio.
     */
    private void setupDirectoryTreeItem(TreeItem<String> item, String path, FTPFile file) {
        String newPath = path.endsWith("/") ? path + file.getName() : path + "/" + file.getName();
        item.setExpanded(false);

        item.addEventHandler(TreeItem.branchExpandedEvent(), e -> {
            if (item.getChildren().size() == 1 && "Loading...".equals(item.getChildren().get(0).getValue())) {
                item.getChildren().clear();
                Task<Void> loadTask = createSyncTask(item, newPath);
                new Thread(loadTask).start();
            }
        });

        item.getChildren().add(new TreeItem<>("Loading..."));
    }

    /**
     * Actualiza el {@link TreeView} con los elementos proporcionados.
     *
     * @param parent El nodo raíz del árbol a actualizar.
     * @param items Los nuevos elementos a agregar al árbol.
     */
    private void updateTreeView(TreeItem<String> parent, List<TreeItem<String>> items) {
        Platform.runLater(() -> {
            logger.info("Updating TreeView with {} items.", items.size());
            parent.getChildren().setAll(items);
        });
    }

    /**
     * Crea una nueva carpeta en el servidor FTP bajo el directorio seleccionado.
     *
     * @param selectedItem El elemento seleccionado en el {@link TreeView} donde se creará la carpeta.
     * @return {@code true} si la carpeta fue creada con éxito, {@code false} si ocurrió un error.
     */
    public boolean createFolder(TreeItem<String> selectedItem) {
        String folderName = AlertFactory.showTextInputDialog("Folder Name");
        if (folderName == null || folderName.isBlank()) {
            return false;
        }

        String path = ElementUtils.getPathFromTreeItem(selectedItem);
        logger.info("Creating folder at path: {}/{}", path, folderName);

        if (ftpClientManager.createFolder(path + "/" + folderName)) {
            TreeItem<String> newFolder = new TreeItem<>(ElementUtils.FOLDER_ICON + folderName);
            selectedItem.getChildren().add(newFolder);
            return true;
        }

        return false;
    }

    /**
     * Renombra un archivo o carpeta en el servidor FTP.
     *
     * @param selectedItem El elemento seleccionado en el {@link TreeView} que se desea renombrar.
     * @param newName El nuevo nombre para el archivo o carpeta.
     * @return {@code true} si el renombrado fue exitoso, {@code false} si ocurrió un error.
     */
    public boolean renameFileOrFolder(TreeItem<String> selectedItem, String newName) {
        if (newName == null || newName.isBlank()) {
            return false;
        }

        String path = ElementUtils.getPathFromTreeItem(selectedItem);
        String parentPath = ElementUtils.getPathFromTreeItem(selectedItem.getParent());
        logger.info("Renaming file/folder from: {} to: {}/{}", path, parentPath, newName);

        if (ftpClientManager.renameFileOrFolder(path, parentPath + "/" + newName)) {
            selectedItem.setValue(ElementUtils.getTreeParsedName(selectedItem.getValue(), newName));
            return true;
        }

        return false;
    }

    /**
     * Elimina un archivo o carpeta del servidor FTP.
     *
     * @param selectedItem El elemento seleccionado en el {@link TreeView} que se desea eliminar.
     * @return {@code true} si la eliminación fue exitosa, {@code false} si ocurrió un error.
     */
    public boolean deleteFileOrFolder(TreeItem<String> selectedItem) {
        String path = ElementUtils.getPathFromTreeItem(selectedItem);
        logger.info("Deleting file/folder at path: {}", path);

        if (ftpClientManager.deleteFileOrFolder(path)) {
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedItem);
            }
            return true;
        }

        return false;
    }

    /**
     * Sube un archivo al servidor FTP en la carpeta seleccionada.
     *
     * @param selectedFolder El elemento de carpeta seleccionado en el {@link TreeView} donde se subirá el archivo.
     * @return {@code true} si el archivo se subió con éxito, {@code false} si ocurrió un error.
     */
    public boolean uploadFile(TreeItem<String> selectedFolder) {
        String selectedFolderPath = ElementUtils.getPathFromTreeItem(selectedFolder);

        // Verifica si el elemento seleccionado es una carpeta y ajusta la ruta en consecuencia.
        if(!ElementUtils.isElementFolder(selectedFolder)) {
            int lastIndex = selectedFolderPath.lastIndexOf('/');
            selectedFolderPath = selectedFolderPath.substring(0, lastIndex);
        }

        // Permite al usuario seleccionar un archivo a subir.
        File fileToUpload = FileChooserUtils.selectFile();
        if (fileToUpload == null) {
            logger.warn("No file selected for upload.");
            return false;
        }

        // Define la ruta final del archivo en el servidor FTP.
        String finalUploadPath = selectedFolderPath + "/" + fileToUpload.getName();
        logger.info("Uploading file: {}", fileToUpload.getName());

        // Sube el archivo al servidor FTP.
        if (ftpClientManager.uploadFile(fileToUpload.getPath(), finalUploadPath)) {
            // Si la carga es exitosa, agrega el nuevo archivo al árbol.
            TreeItem<String> newFileItem = new TreeItem<>(ElementUtils.getIconForFile(fileToUpload) + fileToUpload.getName());

            // Añade el archivo al nodo adecuado en el árbol (como archivo o dentro de una carpeta).
            if (!ElementUtils.isElementFolder(selectedFolder)) {
                selectedFolder.getParent().getChildren().add(newFileItem);
            } else {
                selectedFolder.getChildren().add(newFileItem);
            }

            // Expande el nodo para mostrar el nuevo archivo.
            selectedFolder.setExpanded(true);
            return true;
        }

        return false;
    }

    /**
     * Descarga un archivo del servidor FTP a la carpeta seleccionada en el sistema local.
     *
     * @param selectedItem El archivo seleccionado en el {@link TreeView} que se desea descargar.
     * @return {@code true} si el archivo fue descargado con éxito, {@code false} si ocurrió un error.
     */
    public boolean downloadFile(TreeItem<String> selectedItem) {
        // Obtiene la ruta remota del archivo seleccionado.
        String remotePath = ElementUtils.getPathFromTreeItem(selectedItem);
        // Extrae el nombre del archivo de la ruta remota.
        String fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);

        // Permite al usuario seleccionar la ubicación para guardar el archivo.
        File selectedFile = FileChooserUtils.saveFile(fileName);
        if (selectedFile == null) {
            logger.warn("No file selected for download.");
            return false;
        }

        // Define la ruta local donde se guardará el archivo descargado.
        String localPath = selectedFile.getParent() + "/" + selectedFile.getName();

        // Descarga el archivo del servidor FTP a la ubicación local seleccionada.
        return ftpClientManager.downloadFile(remotePath, localPath);
    }
}
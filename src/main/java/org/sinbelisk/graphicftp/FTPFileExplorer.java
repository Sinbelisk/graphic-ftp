package org.sinbelisk.graphicftp;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTPFileExplorer {
    // Nota: esto es "temporal", me gustaria utilizar diréctamente iconos.
    private static final String FOLDER_ICON = "\uD83D\uDCC1";
    private static final String FILE_ICON = "\uD83D\uDCDD";
    private static final String PROGRAM_ICON = "\uD83D\uDCBD";

    private static final Logger logger = LogManager.getLogger(FTPFileExplorer.class);

    private final TreeView<String> treeView;
    private FTPClient client;

    public FTPFileExplorer(TreeView<String> treeView) {
        this.treeView = treeView;
    }

    public void sync(FTPClient client) throws IOException {
        this.client = client;

        TreeItem<String> rootItem = new TreeItem<>(FOLDER_ICON + " Root");
        treeView.setRoot(rootItem);

        // Crear una tarea asíncrona para poblar el TreeView en batches
        Task<Void> syncTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                populateTreeView(rootItem, "/");
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

        new Thread(syncTask).start();
    }

    public void desync() {
        this.client = null;
        treeView.setRoot(null);
    }


    private void populateTreeView(TreeItem<String> parent, String path) throws IOException {
        FTPFile[] filesAndDirs = client.listFiles(path);

        List<TreeItem<String>> items = new ArrayList<>();
        for (FTPFile file : filesAndDirs) {
            String icon = file.isDirectory() ? FOLDER_ICON : file.getName().endsWith(".exe") ? PROGRAM_ICON : FILE_ICON;
            TreeItem<String> item = new TreeItem<>(icon + file.getName());

            logger.info("Reading: {}", file.getName());

            if (file.isDirectory()) {
                String newPath = path.endsWith("/") ? path + file.getName() : path + "/" + file.getName();
                item.setExpanded(false);

                item.addEventHandler(TreeItem.branchExpandedEvent(), e -> {
                    if (!item.getChildren().get(0).getValue().equals("Loading...")) return;
                    item.getChildren().clear();
                    Task<Void> loadTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            populateTreeView(item, newPath);
                            return null;
                        }
                    };
                    new Thread(loadTask).start();
                });

                item.getChildren().add(new TreeItem<>("Loading..."));
            }

            items.add(item);
        }

        updateTreeView(parent, items);
    }

    // Método para actualizar el TreeView desde el hilo de la interfaz de usuario
    private void updateTreeView(TreeItem<String> parent, List<TreeItem<String>> fileItems) {
        Platform.runLater(() -> {
            logger.info("Updating TreeView with {} items.", fileItems.size());
            parent.getChildren().addAll(fileItems);
        });
    }


    public boolean createFolder(TreeItem<String> selectedItem) throws IOException {
        String folderName = AlertFactory.showTextInputDialog("Nombre de la carpeta");

        String path = getPathFromTreeItem(selectedItem);
        logger.info("Creating folder at path: {}/{}", path, folderName);

        if (client.makeDirectory(path + "/" + folderName)) {
            TreeItem<String> newFolder = new TreeItem<>(FOLDER_ICON + folderName);
            selectedItem.getChildren().add(newFolder);
            return true;
        }

        return false;
    }

    public boolean renameFileOrFolder(TreeItem<String> selectedItem, String newName) throws IOException {
        String path = getPathFromTreeItem(selectedItem);
        String parentPath = getPathFromTreeItem(selectedItem.getParent());
        logger.info("Renaming file/folder from: {} to: {}/{}", path, parentPath, newName);

        if (client.rename(path, parentPath + "/" + newName)) {
            String parsedName = getTreeParsedName(selectedItem.getValue(), newName);
            selectedItem.setValue(parsedName);
            return true;
        }

        return false;
    }

    public boolean deleteFileOrFolder(TreeItem<String> selectedItem) throws IOException {
        String path = getPathFromTreeItem(selectedItem);
        logger.info("Deleting file/folder at path: {}", path);
        boolean success;

        if (selectedItem.getValue().startsWith(FOLDER_ICON)) {
            success = client.removeDirectory(path);
        } else {
            success = client.deleteFile(path);
        }

        if (success) {
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedItem);
            }
        }

        return success;
    }

    private String getPathFromTreeItem(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();

        while (item != null && item.getParent() != null) {
            String itemName = item.getValue().substring(2);
            path.insert(0, "/" + itemName);
            item = item.getParent();
        }
        return path.toString();
    }

    private String getTreeParsedName(String originalName, String newName) {
        String icon = originalName.substring(0, 1);
        logger.info(icon);

        if (originalName.startsWith(FOLDER_ICON)) {
            return FOLDER_ICON + newName;

        } else if (originalName.startsWith(FILE_ICON)) {
            return FILE_ICON + newName;

        } else if (originalName.startsWith(PROGRAM_ICON)) {
            return PROGRAM_ICON + newName;

        } else {
            return "[?]" + newName;
        }
    }
}
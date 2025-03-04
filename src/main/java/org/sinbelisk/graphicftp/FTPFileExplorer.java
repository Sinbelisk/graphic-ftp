package org.sinbelisk.graphicftp;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sinbelisk.graphicftp.services.FTPClientManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTPFileExplorer {
    private static final String FOLDER_ICON = "\uD83D\uDCC1";
    private static final String FILE_ICON = "\uD83D\uDCDD";
    private static final String PROGRAM_ICON = "\uD83D\uDCBD";

    private static final Logger logger = LogManager.getLogger(FTPFileExplorer.class);

    private final TreeView<String> treeView;
    private FTPClientManager ftpClientManager;

    public FTPFileExplorer(TreeView<String> treeView) {
        this.treeView = treeView;
    }

    public void sync(FTPClientManager ftpClientManager) {
        this.ftpClientManager = ftpClientManager;
        TreeItem<String> rootItem = new TreeItem<>(FOLDER_ICON + " Root");
        treeView.setRoot(rootItem);

        Task<Void> syncTask = createSyncTask(rootItem, "/");
        new Thread(syncTask).start();
    }

    public void desync() {
        treeView.setRoot(null);
        this.ftpClientManager = null;
    }

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

    private TreeItem<String> createTreeItem(FTPFile file) {
        String icon = getIconForFile(file);
        TreeItem<String> item = new TreeItem<>(icon + file.getName());
        logger.info("Reading: {}", file.getName());
        return item;
    }


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

    private void updateTreeView(TreeItem<String> parent, List<TreeItem<String>> items) {
        Platform.runLater(() -> {
            logger.info("Updating TreeView with {} items.", items.size());
            parent.getChildren().setAll(items);
        });
    }

    public boolean createFolder(TreeItem<String> selectedItem) {
        String folderName = AlertFactory.showTextInputDialog("Folder Name");
        if (folderName == null || folderName.isBlank()) {
            return false;
        }

        String path = getPathFromTreeItem(selectedItem);
        logger.info("Creating folder at path: {}/{}", path, folderName);

        if (ftpClientManager.createFolder(path + "/" + folderName)) {
            TreeItem<String> newFolder = new TreeItem<>(FOLDER_ICON + folderName);
            selectedItem.getChildren().add(newFolder);
            return true;
        }

        return false;
    }

    public boolean renameFileOrFolder(TreeItem<String> selectedItem, String newName) {
        if (newName == null || newName.isBlank()) {
            return false;
        }

        String path = getPathFromTreeItem(selectedItem);
        String parentPath = getPathFromTreeItem(selectedItem.getParent());
        logger.info("Renaming file/folder from: {} to: {}/{}", path, parentPath, newName);

        if (ftpClientManager.renameFileOrFolder(path, parentPath + "/" + newName)) {
            selectedItem.setValue(getTreeParsedName(selectedItem.getValue(), newName));
            return true;
        }

        return false;
    }

    public boolean deleteFileOrFolder(TreeItem<String> selectedItem) {
        String path = getPathFromTreeItem(selectedItem);
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

    public boolean uploadFile(TreeItem<String> selectedFolder) {
        String selectedFolderPath = getPathFromTreeItem(selectedFolder);

        if(!isElementFolder(selectedFolder)) {
            int lastIndex = selectedFolderPath.lastIndexOf('/');
            selectedFolderPath = selectedFolderPath.substring(0, lastIndex);
        }

        File fileToUpload = FileChooserUtils.selectFile();
        if (fileToUpload == null) {
            logger.warn("No file selected for upload.");
            return false;
        }

        String finalUploadPath = selectedFolderPath + "/" + fileToUpload.getName();
        logger.info("Uploading file: {}", fileToUpload.getName());

        if (ftpClientManager.uploadFile(fileToUpload.getPath(), finalUploadPath)) {
            TreeItem<String> newFileItem = new TreeItem<>(getIconForFile(fileToUpload) + fileToUpload.getName());

            if (!isElementFolder(selectedFolder)) {
                selectedFolder.getParent().getChildren().add(newFileItem);
            }
            else{
                selectedFolder.getChildren().add(newFileItem);
            }

            selectedFolder.setExpanded(true);
            return true;
        }

        return false;
    }

    private boolean isElementFolder(TreeItem<String> selectedItem) {
        return (selectedItem.getValue().startsWith(FOLDER_ICON));
    }

    public boolean downloadFile(TreeItem<String> selectedItem) {
        String remotePath = getPathFromTreeItem(selectedItem);
        String fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);

        File selectedFile = FileChooserUtils.saveFile(fileName);
        if (selectedFile == null) {
            logger.warn("No file selected for download.");
            return false;
        }

        String localPath = selectedFile.getParent() + "/" + selectedFile.getName();
        return ftpClientManager.downloadFile(remotePath, localPath);
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
        String icon = originalName.substring(0, 2);
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        return icon + newName + extension;
    }

    private String getIconForFile(File file) {
        if (file.isDirectory()) {
            return FOLDER_ICON;
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON;
        } else {
            return FILE_ICON;
        }
    }

    private String getIconForFile(FTPFile file) {
        if (file.isDirectory()) {
            return FOLDER_ICON;
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON;
        } else {
            return FILE_ICON;
        }
    }
}
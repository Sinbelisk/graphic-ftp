package org.sinbelisk.graphicftp.util;

import javafx.scene.control.TreeItem;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;

public class ElementUtils {
    public static final String FOLDER_ICON = "\uD83D\uDCC1";
    public static final String FILE_ICON = "\uD83D\uDCDD";
    public static final String PROGRAM_ICON = "\uD83D\uDCBD";

    public static String getTreeParsedName(String originalName, String newName) {
        String icon = originalName.substring(0, 2);
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        return icon + newName + extension;
    }

    public static String getIconForFile(File file) {
        if (file.isDirectory()) {
            return FOLDER_ICON;
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON;
        } else {
            return FILE_ICON;
        }
    }

    public static String getIconForFile(FTPFile file) {
        if (file.isDirectory()) {
            return FOLDER_ICON;
        } else if (file.getName().endsWith(".exe")) {
            return PROGRAM_ICON;
        } else {
            return FILE_ICON;
        }
    }

    public static String getPathFromTreeItem(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();

        while (item != null && item.getParent() != null) {
            String itemName = item.getValue().substring(2);
            path.insert(0, "/" + itemName);
            item = item.getParent();
        }
        return path.toString();
    }

    public static boolean isElementFolder(TreeItem<String> selectedItem) {
        return (selectedItem.getValue().startsWith(FOLDER_ICON));
    }
}

package org.sinbelisk.graphicftp.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase que gestiona la conexión y autenticación con un servidor FTP.
 * Proporciona métodos para conectarse, iniciar sesión y desconectarse del servidor,
 * además de permitir la subida, descarga, creación, renombrado y eliminación de archivos y carpetas en el servidor.
 */
public class FTPClientManager {
    private static final Logger logger = LogManager.getLogger(FTPClientManager.class);

    private final FTPClient ftpClient;
    private final String server;
    private final int port;


    /**
     * Constructor de la clase FTPClientManager.
     *
     * @param server Dirección del servidor FTP.
     * @param port   Puerto del servidor FTP.
     */
    public FTPClientManager(String server, int port) {
        this.server = server;
        this.port = port;
        this.ftpClient = new FTPClient();
    }

    /**
     * Conecta con el servidor FTP e intenta iniciar sesión con las credenciales proporcionadas.
     *
     * @param username Nombre de usuario para la autenticación.
     * @param password Contraseña del usuario.
     * @return true si la conexión y autenticación son exitosas, false en caso contrario.
     */
    public boolean connectAndLogin(String username, String password) {
        try {
            logger.info("Connecting to FTP server: {} on port {}", server, port);
            ftpClient.connect(server, port);

            boolean loginSuccess = ftpClient.login(username, password);

            if (loginSuccess) {
                logger.info("User '{}' logged in successfully", username);
                //ftpClient.enterLocalPassiveMode();
            } else {
                logger.warn("Login failed for user '{}'.", username);
            }

            return loginSuccess;
        } catch (IOException e) {
            logger.error("Error connecting to FTP server: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el cliente FTP subyacente.
     *
     * @return Instancia de {@link FTPClient} utilizada en la conexión.
     */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    /**
     * Cierra la sesión y desconecta el cliente FTP si está conectado.
     */
    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                logger.info("FTP client disconnected successfully.");
            }
        } catch (IOException e) {
            logger.error("Error disconnecting from server: {}", e.getMessage(), e);
        }
    }

    /**
     * Sube un archivo al servidor FTP.
     *
     * @param localFilePath Ruta del archivo local.
     * @param remotePath Ruta en el servidor FTP donde se guardará el archivo.
     * @return true si la subida es exitosa, false en caso contrario.
     */
    public boolean uploadFile(String localFilePath, String remotePath) {
        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            logger.info("Uploading file: {} -> {}", localFilePath, remotePath);

            boolean success = ftpClient.storeFile(remotePath, fis);

            if (success) {
                logger.info("File '{}' uploaded successfully", remotePath);
            } else {
                logger.warn("Error uploading file '{}'", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Descarga un archivo del servidor FTP.
     *
     * @param remotePath Ruta del archivo en el servidor FTP.
     * @param localFilePath Ruta local donde se guardará el archivo descargado.
     * @return true si la descarga es exitosa, false en caso contrario.
     */
    public boolean downloadFile(String remotePath, String localFilePath) {

        try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
            logger.info("Downloading file: {} -> {}", remotePath, localFilePath);
            boolean success = ftpClient.retrieveFile(remotePath, fos);

            if (success) {
                logger.info("File downloaded successfully: {}", localFilePath);
            } else {
                logger.warn("Error downloading file: {}", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error downloading file: ", e);
            return false;
        }
    }

    /**
     * Crea una carpeta en el servidor FTP.
     *
     * @param remotePath Ruta en el servidor donde se creará la carpeta.
     * @return true si la creación de la carpeta es exitosa, false en caso contrario.
     */
    public boolean createFolder(String remotePath) {
        try {
            logger.info("Creating folder at path: {}", remotePath);
            boolean success = ftpClient.makeDirectory(remotePath);

            if (success) {
                logger.info("Folder created successfully at {}", remotePath);
            } else {
                logger.warn("Error creating folder at {}", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error creating folder: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Renombra un archivo o carpeta en el servidor FTP.
     *
     * @param remoteOldPath Ruta del archivo o carpeta que se desea renombrar.
     * @param remoteNewPath Nueva ruta para el archivo o carpeta.
     * @return true si el renombrado es exitoso, false en caso contrario.
     */
    public boolean renameFileOrFolder(String remoteOldPath, String remoteNewPath) {
        try {
            logger.info("Renaming file/folder from: {} to: {}", remoteOldPath, remoteNewPath);
            boolean success = ftpClient.rename(remoteOldPath, remoteNewPath);

            if (success) {
                logger.info("File/folder renamed successfully from: {} to: {}", remoteOldPath, remoteNewPath);
            } else {
                logger.warn("Error renaming file/folder from: {} to: {}", remoteOldPath, remoteNewPath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error renaming file/folder: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Elimina un archivo o carpeta del servidor FTP.
     *
     * @param remotePath Ruta del archivo o carpeta a eliminar.
     * @return true si la eliminación es exitosa, false en caso contrario.
     */
    public boolean deleteFileOrFolder(String remotePath) {
        try {
            logger.info("Deleting file/folder at path: {}", remotePath);
            boolean success;

            FTPFile file = ftpClient.mlistFile(remotePath);
            if (file != null && file.isDirectory()) {
                success = ftpClient.removeDirectory(remotePath);
            } else {
                success = ftpClient.deleteFile(remotePath);
            }

            if (success) {
                logger.info("File/folder deleted successfully at path: {}", remotePath);
            } else {
                logger.warn("Error deleting file/folder at path: {}", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error deleting file/folder: {}", e.getMessage(), e);
            return false;
        }
    }
}
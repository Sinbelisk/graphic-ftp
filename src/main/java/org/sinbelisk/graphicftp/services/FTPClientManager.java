package org.sinbelisk.graphicftp.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase que gestiona la conexión y autenticación con un servidor FTP.
 * Proporciona métodos para conectarse, iniciar sesión y desconectarse del servidor.
 */
public class FTPClientManager {
    private static final Logger logger = LogManager.getLogger(FTPClientManager.class);

    private final FTPClient ftpClient;
    private final String server;
    private final int port;

    private String username;

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
            logger.info("Conectándose al servidor FTP: {} en puerto {}", server, port);
            ftpClient.connect(server, port);

            boolean loginSuccess = ftpClient.login(username, password);

            if (loginSuccess) {
                logger.info("Usuario '{}' ha iniciado sesión correctamente.", username);
                this.username = username;
                //ftpClient.enterLocalPassiveMode();
            } else {
                logger.warn("Error al iniciar sesión para el usuario '{}'.", username);
            }

            return loginSuccess;
        } catch (IOException e) {
            logger.error("Error al conectarse al servidor FTP: {}", e.getMessage(), e);
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
                username = null;
                logger.info("Cliente FTP desconectado correctamente.");
            }
        } catch (IOException e) {
            logger.error("Error al desconectarse del servidor FTP: {}", e.getMessage(), e);
        }
    }

    /**
     * Sube un archivo al servidor FTP.
     *
     * @param localFilePath Ruta del archivo local.
     * @param remoteFilePath Ruta en el servidor FTP donde se guardará el archivo.
     * @return true si la subida es exitosa, false en caso contrario.
     */
    public boolean uploadFile(String localFilePath, String remoteFilePath) {
        String remotePath = getUserFolder(remoteFilePath);
        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            logger.info("Subiendo archivo: {} -> {}", localFilePath, remotePath);

            boolean success = ftpClient.storeFile(remotePath, fis);

            if (success) {
                logger.info("Archivo subido correctamente a {}", remotePath);
            } else {
                logger.warn("No se pudo subir el archivo a {}", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error al subir el archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Descarga un archivo del servidor FTP.
     *
     * @param remoteFilePath Ruta del archivo en el servidor FTP.
     * @param localFilePath Ruta local donde se guardará el archivo descargado.
     * @return true si la descarga es exitosa, false en caso contrario.
     */
    public boolean downloadFile(String remoteFilePath, String localFilePath) {
        String remotePath = getUserFolder(remoteFilePath);

        try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
            logger.info("Descargando archivo: {} -> {}", remotePath, localFilePath);
            boolean success = ftpClient.retrieveFile(remotePath, fos);

            if (success) {
                logger.info("Archivo descargado correctamente en {}", localFilePath);
            } else {
                logger.warn("No se pudo descargar el archivo desde {}", remotePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error al descargar el archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getUserFolder(String remoteFilePath) {
        return username + "/" + remoteFilePath;
    }

    public String getUsername() {
        return username;
    }
}
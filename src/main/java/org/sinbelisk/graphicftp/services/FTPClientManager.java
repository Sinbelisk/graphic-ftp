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
                ftpClient.enterLocalPassiveMode();
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
        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            logger.info("Subiendo archivo: {} -> {}", localFilePath, remoteFilePath);
            boolean success = ftpClient.storeFile(remoteFilePath, fis);

            if (success) {
                logger.info("Archivo subido correctamente a {}", remoteFilePath);
            } else {
                logger.warn("No se pudo subir el archivo a {}", remoteFilePath);
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
        try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
            logger.info("Descargando archivo: {} -> {}", remoteFilePath, localFilePath);
            boolean success = ftpClient.retrieveFile(remoteFilePath, fos);

            if (success) {
                logger.info("Archivo descargado correctamente en {}", localFilePath);
            } else {
                logger.warn("No se pudo descargar el archivo desde {}", remoteFilePath);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error al descargar el archivo: {}", e.getMessage(), e);
            return false;
        }
    }
}
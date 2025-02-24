package org.sinbelisk.graphicftp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

/**
 * Clase de prueba para FTPClientManager.
 * Verifica la conexión, subida y descarga de archivos en un servidor FTP real.
 */
public class FTPClientManagerTest {
    private static final String SERVER = "localhost";
    private static final int PORT = 21;
    private static final String USERNAME = "usuario1";
    private static final String PASSWORD = "usu1";

    private static final String TEST_RESOURCE_PATH = "src/test/resources/";
    private static final String LOCAL_FILE = "test_upload.txt";
    private static final String REMOTE_FILE = "server_test.txt";

    private FTPClientManager ftpClientManager;

    @BeforeEach
    void setUp() {
        ftpClientManager = new FTPClientManager(SERVER, PORT);
    }

    /**
     * Prueba la conexión y autenticación con el servidor FTP.
     */
    @Test
    void testConnectAndLogin() {
        assertTrue(ftpClientManager.connectAndLogin(USERNAME, PASSWORD), "La conexión y autenticación deben ser exitosas");
        ftpClientManager.disconnect();
    }

    /**
     * Prueba la subida de un archivo al servidor FTP.
     */
    @Test
    void testUploadFile() {
        ftpClientManager.connectAndLogin(USERNAME, PASSWORD);
        File file = new File(LOCAL_FILE);
        assertTrue(file.exists(), "El archivo local debe existir antes de subirlo");
        assertTrue(ftpClientManager.uploadFile(LOCAL_FILE, USERNAME + REMOTE_FILE), "La subida del archivo debe ser exitosa");
        ftpClientManager.disconnect();
    }

    /**
     * Prueba la descarga de un archivo desde el servidor FTP.
     */
    @Test
    void testDownloadFile() {
        ftpClientManager.connectAndLogin(USERNAME, PASSWORD);
        assertTrue(ftpClientManager.downloadFile(USERNAME + REMOTE_FILE, TEST_RESOURCE_PATH + "downloaded_test.txt"), "La descarga del archivo debe ser exitosa");
        File downloadedFile = new File(TEST_RESOURCE_PATH + "downloaded_test.txt");
        assertTrue(downloadedFile.exists(), "El archivo descargado debe existir");
        ftpClientManager.disconnect();
        downloadedFile.delete(); // Limpieza
    }

    /**
     * Prueba la desconexión del servidor FTP.
     */
    @Test
    void testDisconnect() {
        ftpClientManager.connectAndLogin(USERNAME, PASSWORD);
        ftpClientManager.disconnect();
        assertFalse(ftpClientManager.getFtpClient().isConnected(), "El cliente debe estar desconectado");
    }
}
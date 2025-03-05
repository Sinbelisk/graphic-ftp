package org.sinbelisk.graphicftp.controller;


import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;
import org.sinbelisk.graphicftp.services.FTPClientManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FTPFileExplorerTest {
    private static final String FTP_SERVER = "localhost";
    private static final int FTP_PORT = 21;
    private static final String TEST_USER = "usuario1";
    private static final String TEST_PASSWORD = "usu1";

    private static final String LOCAL_FILE_PATH = "test_file.txt";
    private static final String REMOTE_FILE_PATH = "/test_file.txt";
    private static final String REMOTE_FOLDER_PATH = "/test_folder";
    private static final String RENAMED_FILE_PATH = "/renamed_file.txt";

    private static FTPClientManager ftpManager;
    @BeforeAll
    static void setup() throws IOException {
        // Crear archivo de prueba local
        File localFile = new File(LOCAL_FILE_PATH);
        try (FileWriter writer = new FileWriter(localFile)) {
            writer.write("Este es un archivo de prueba para FTP.");
        }

        // Instanciar el cliente FTP
        ftpManager = new FTPClientManager(FTP_SERVER, FTP_PORT);
    }

    @Test
    @Order(1)
    void testConnectAndLogin() {
        assertTrue(ftpManager.connectAndLogin(TEST_USER, TEST_PASSWORD), "No se pudo conectar al servidor FTP.");
    }

    @Test
    @Order(2)
    void testUploadFile() {
        assertTrue(ftpManager.uploadFile(LOCAL_FILE_PATH, REMOTE_FILE_PATH), "Falló la subida del archivo.");
    }

    @Test
    @Order(3)
    void testDownloadFile() {
        String downloadedFilePath = "downloaded_test_file.txt";
        assertTrue(ftpManager.downloadFile(REMOTE_FILE_PATH, downloadedFilePath), "Falló la descarga del archivo.");
        assertTrue(new File(downloadedFilePath).exists(), "El archivo descargado no existe.");
    }

    @Test
    @Order(4)
    void testCreateFolder() {
        assertTrue(ftpManager.createFolder(REMOTE_FOLDER_PATH), "Falló la creación de la carpeta.");
    }

    @Test
    @Order(5)
    void testRenameFileOrFolder() {
        assertTrue(ftpManager.renameFileOrFolder(REMOTE_FILE_PATH, RENAMED_FILE_PATH), "Falló el renombrado.");
    }

    @Test
    @Order(6)
    void testDeleteFileOrFolder() {
        assertTrue(ftpManager.deleteFileOrFolder(RENAMED_FILE_PATH), "Falló la eliminación del archivo.");
        assertTrue(ftpManager.deleteFileOrFolder(REMOTE_FOLDER_PATH), "Falló la eliminación de la carpeta.");
    }

    @Test
    @Order(7)
    void testDisconnect() {
        ftpManager.disconnect();
        FTPClient client = ftpManager.getFtpClient();
        assertFalse(client.isConnected(), "El cliente FTP sigue conectado después de la desconexión.");
    }

    @AfterAll
    static void cleanup() {
        new File(LOCAL_FILE_PATH).delete();
        new File("downloaded_test_file.txt").delete();
    }
}

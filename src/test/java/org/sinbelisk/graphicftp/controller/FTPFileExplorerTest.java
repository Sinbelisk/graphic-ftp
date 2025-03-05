package org.sinbelisk.graphicftp.controller;


import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;
import org.sinbelisk.graphicftp.services.FTPClientManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas unitarias para el manejo de un cliente FTP utilizando {@link FTPClientManager}.
 * Realiza una serie de operaciones de conexión, carga, descarga, creación de carpetas,
 * renombrado y eliminación de archivos y carpetas en un servidor FTP.
 * Las pruebas están ordenadas para garantizar que cada paso se ejecute de manera secuencial.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FTPFileExplorerTest {

    // Constantes para la configuración del servidor FTP y archivos de prueba
    private static final String FTP_SERVER = "localhost";
    private static final int FTP_PORT = 21;
    private static final String TEST_USER = "usuario1";
    private static final String TEST_PASSWORD = "usu1";

    private static final String LOCAL_FILE_PATH = "test_file.txt";
    private static final String REMOTE_FILE_PATH = "/test_file.txt";
    private static final String REMOTE_FOLDER_PATH = "/test_folder";
    private static final String RENAMED_FILE_PATH = "/renamed_file.txt";

    private static FTPClientManager ftpManager;

    /**
     * Configuración inicial que se ejecuta antes de todas las pruebas.
     * Crea un archivo de prueba en el sistema local y prepara la instancia de {@link FTPClientManager}.
     *
     * @throws IOException Si ocurre un error al escribir el archivo de prueba o al conectar con el servidor FTP.
     */
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

    /**
     * Prueba que valida la conexión y autenticación con el servidor FTP.
     * Intenta conectarse y hacer login con las credenciales especificadas.
     */
    @Test
    @Order(1)
    void testConnectAndLogin() {
        assertTrue(ftpManager.connectAndLogin(TEST_USER, TEST_PASSWORD), "No se pudo conectar al servidor FTP.");
    }

    /**
     * Prueba que valida la carga de un archivo al servidor FTP.
     * Intenta subir un archivo desde el sistema local al servidor FTP.
     */
    @Test
    @Order(2)
    void testUploadFile() {
        assertTrue(ftpManager.uploadFile(LOCAL_FILE_PATH, REMOTE_FILE_PATH), "Falló la subida del archivo.");
    }

    /**
     * Prueba que valida la descarga de un archivo desde el servidor FTP.
     * Intenta descargar un archivo desde el servidor FTP al sistema local.
     */
    @Test
    @Order(3)
    void testDownloadFile() {
        String downloadedFilePath = "downloaded_test_file.txt";
        assertTrue(ftpManager.downloadFile(REMOTE_FILE_PATH, downloadedFilePath), "Falló la descarga del archivo.");
        assertTrue(new File(downloadedFilePath).exists(), "El archivo descargado no existe.");
    }

    /**
     * Prueba que valida la creación de una carpeta en el servidor FTP.
     * Intenta crear una nueva carpeta en el servidor FTP.
     */
    @Test
    @Order(4)
    void testCreateFolder() {
        assertTrue(ftpManager.createFolder(REMOTE_FOLDER_PATH), "Falló la creación de la carpeta.");
    }

    /**
     * Prueba que valida el renombrado de un archivo o carpeta en el servidor FTP.
     * Intenta renombrar un archivo previamente cargado en el servidor.
     */
    @Test
    @Order(5)
    void testRenameFileOrFolder() {
        assertTrue(ftpManager.renameFileOrFolder(REMOTE_FILE_PATH, RENAMED_FILE_PATH), "Falló el renombrado.");
    }

    /**
     * Prueba que valida la eliminación de un archivo o carpeta en el servidor FTP.
     * Intenta eliminar un archivo y una carpeta en el servidor FTP.
     */
    @Test
    @Order(6)
    void testDeleteFileOrFolder() {
        assertTrue(ftpManager.deleteFileOrFolder(RENAMED_FILE_PATH), "Falló la eliminación del archivo.");
        assertTrue(ftpManager.deleteFileOrFolder(REMOTE_FOLDER_PATH), "Falló la eliminación de la carpeta.");
    }

    /**
     * Prueba que valida la desconexión del cliente FTP.
     * Intenta desconectar el cliente FTP y verifica que no está conectado.
     */
    @Test
    @Order(7)
    void testDisconnect() {
        ftpManager.disconnect();
        FTPClient client = ftpManager.getFtpClient();
        assertFalse(client.isConnected(), "El cliente FTP sigue conectado después de la desconexión.");
    }

    /**
     * Limpieza de archivos creados durante las pruebas.
     * Elimina los archivos de prueba locales tras la ejecución de las pruebas.
     */
    @AfterAll
    static void cleanup() {
        new File(LOCAL_FILE_PATH).delete();
        new File("downloaded_test_file.txt").delete();
    }
}

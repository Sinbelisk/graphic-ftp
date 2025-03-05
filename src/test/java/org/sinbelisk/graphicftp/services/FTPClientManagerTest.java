package org.sinbelisk.graphicftp.services;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas unitarias para la clase {@link FTPClientManager}.
 * Esta clase prueba las funcionalidades de conexión, autenticación, carga y descarga de archivos,
 * creación, renombrado y eliminación de archivos y carpetas en el servidor FTP.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FTPClientManagerTest {

    private static final String FTP_SERVER = "localhost";
    private static final int FTP_PORT = 21;
    private static final String FTP_USER = "usuario1";
    private static final String FTP_PASSWORD = "usu1";

    private static final String LOCAL_FILE_PATH = "test_file.txt";
    private static final String REMOTE_FILE_PATH = "/test_file.txt";
    private static final String REMOTE_FOLDER_PATH = "/test_folder";
    private static final String RENAMED_FILE_PATH = "/renamed_file.txt";

    private static FTPClientManager ftpManager;

    /**
     * Configuración inicial antes de ejecutar las pruebas.
     * Crea un archivo de prueba en el sistema local y prepara el gestor de conexión FTP.
     *
     * @throws IOException si ocurre algún error al escribir el archivo de prueba.
     */
    @BeforeAll
    static void setup() throws IOException {
        File localFile = new File(LOCAL_FILE_PATH);
        try (FileWriter writer = new FileWriter(localFile)) {
            writer.write("Este es un archivo de prueba para FTP.");
        }

        ftpManager = new FTPClientManager(FTP_SERVER, FTP_PORT);
    }

    /**
     * Prueba de conexión y autenticación con el servidor FTP.
     * Verifica que la conexión y el inicio de sesión sean exitosos con las credenciales proporcionadas.
     */
    @Test
    @Order(1)
    void testConnectAndLogin() {
        assertTrue(ftpManager.connectAndLogin(FTP_USER, FTP_PASSWORD), "No se pudo conectar al servidor FTP.");
    }

    /**
     * Prueba de subida de un archivo al servidor FTP.
     * Verifica que el archivo local se suba correctamente al servidor FTP.
     */
    @Test
    @Order(2)
    void testUploadFile() {
        assertTrue(ftpManager.uploadFile(LOCAL_FILE_PATH, REMOTE_FILE_PATH), "Falló la subida del archivo.");
    }

    /**
     * Prueba de descarga de un archivo desde el servidor FTP.
     * Verifica que el archivo se descargue correctamente desde el servidor FTP al sistema local.
     */
    @Test
    @Order(3)
    void testDownloadFile() {
        String downloadedFilePath = "downloaded_test_file.txt";
        assertTrue(ftpManager.downloadFile(REMOTE_FILE_PATH, downloadedFilePath), "Falló la descarga del archivo.");
        assertTrue(new File(downloadedFilePath).exists(), "El archivo descargado no existe.");
    }

    /**
     * Prueba de creación de una carpeta en el servidor FTP.
     * Verifica que la carpeta se cree correctamente en el servidor FTP.
     */
    @Test
    @Order(4)
    void testCreateFolder() {
        assertTrue(ftpManager.createFolder(REMOTE_FOLDER_PATH), "Falló la creación de la carpeta.");
    }

    /**
     * Prueba de renombrado de un archivo o carpeta en el servidor FTP.
     * Verifica que el archivo o carpeta sea renombrado correctamente.
     */
    @Test
    @Order(5)
    void testRenameFileOrFolder() {
        assertTrue(ftpManager.renameFileOrFolder(REMOTE_FILE_PATH, RENAMED_FILE_PATH), "Falló el renombrado.");
    }

    /**
     * Prueba de eliminación de un archivo o carpeta en el servidor FTP.
     * Verifica que el archivo o carpeta sea eliminado correctamente del servidor FTP.
     */
    @Test
    @Order(6)
    void testDeleteFileOrFolder() {
        assertTrue(ftpManager.deleteFileOrFolder(RENAMED_FILE_PATH), "Falló la eliminación del archivo.");
        assertTrue(ftpManager.deleteFileOrFolder(REMOTE_FOLDER_PATH), "Falló la eliminación de la carpeta.");
    }

    /**
     * Prueba de desconexión del servidor FTP.
     * Verifica que el cliente FTP se desconecte correctamente después de finalizar la sesión.
     */
    @Test
    @Order(7)
    void testDisconnect() {
        ftpManager.disconnect();
        FTPClient client = ftpManager.getFtpClient();
        assertFalse(client.isConnected(), "El cliente FTP sigue conectado después de la desconexión.");
    }

    /**
     * Limpieza después de ejecutar las pruebas.
     * Elimina los archivos locales generados durante las pruebas.
     */
    @AfterAll
    static void cleanup() {
        new File(LOCAL_FILE_PATH).delete();
        new File("downloaded_test_file.txt").delete();
    }
}

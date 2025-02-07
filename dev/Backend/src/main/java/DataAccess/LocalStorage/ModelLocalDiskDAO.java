package DataAccess.LocalStorage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import DataAccess.ModelDAO;

public class ModelLocalDiskDAO implements ModelDAO {
    private Path storeDir;

    public ModelLocalDiskDAO() {
        storeDir = Path.of("User/Models" + File.separator);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File directory = getStoreDir().toFile();
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory and any necessary parent directories
        }
    }

    @Override
    public void uploadDocument(String documentId, InputStream documentStream) {
        Path filePath = getStoreDir().resolve(documentId);
        try {
            // Copy the input stream to the file
            Files.copy(documentStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document: " + documentId, e);
        }
    }

    @Override
    public InputStream downloadDocument(String documentId) {
        Path filePath = getStoreDir().resolve(documentId);
        try {
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Document not found: " + documentId, e);
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        Path filePath = getStoreDir().resolve(documentId);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete document: " + documentId, e);
        }
    }

    @Override
    public boolean documentExists(String documentId) {
        Path filePath = getStoreDir().resolve(documentId);
        return Files.exists(filePath);
    }

    private Path getStoreDir() {
        return this.storeDir;
    }
}
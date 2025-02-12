package DataAccess.LocalStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;

import DataAccess.ModelRepository;
import Exceptions.InternalErrors.BadRequestException;

public class ModelLocalDiskDAO implements ModelRepository {
    private Path storagePath;

    @Value("${app.file.storage-dir}")
    private String loadedRelativeStoragePath;

    public ModelLocalDiskDAO() {
        String appDir;
        try {
            URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            appDir = new File(uri).getParent();
        } catch (Exception e) {
            appDir = System.getProperty("user.home"); // Fallback
        }

        if (appDir == null) {
            throw new BadRequestException("Could not determine application directory.");
        }
        // Resolve the path relative to the JAR location
        this.storagePath = Paths.get(appDir, loadedRelativeStoragePath);

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
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
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
        return this.storagePath;
    }
}
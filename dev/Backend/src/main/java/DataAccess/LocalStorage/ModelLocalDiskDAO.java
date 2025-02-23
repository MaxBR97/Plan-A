package DataAccess.LocalStorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Value;

import DataAccess.ModelRepository;
import Exceptions.InternalErrors.BadRequestException;


public class ModelLocalDiskDAO extends ModelRepository {
    private Path storagePath;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private List<String> cached;

    
    public ModelLocalDiskDAO(@Value("${app.file.storage-dir}") String loadedRelativeStoragePath) {
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
    
        // Resolve the storage path
        this.storagePath = Paths.get(appDir, loadedRelativeStoragePath);
    
        // Create the storage directory if it doesn't exist
        try {
            Files.createDirectories(storagePath);
        } catch (Exception e) {
            throw new BadRequestException("Failed to create local directory for zpl files: "+e.getMessage());
        }

        ensureDirectoryExists();
        cached = new ArrayList<>();
    }

    private void ensureDirectoryExists() {
        File directory = getStoreDir().toFile();
        if (!directory.exists()) {
            directory.mkdirs(); 
        }
    }

    @Override
    public void uploadDocument(String documentId, InputStream documentStream) throws Exception {

    Path filePath = getStoreDir().resolve(documentId + ".zpl");
    lock.readLock().lock();
    try (OutputStream outStream = Files.newOutputStream(filePath);
         InputStream inStream = documentStream) {  // Ensures InputStream is closed
        byte[] buffer = new byte[8192];  // 8KB buffer for efficient copying
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        cached.add(documentId);
    } catch (Exception e) {
        lock.readLock().unlock();
        throw new BadRequestException("Failed to upload document: " + e.getMessage());
    }
    lock.readLock().unlock();
    
    }


    @Override
    public void uploadDocument(String documentId, String documentString) throws Exception {
        Path filePath = getStoreDir().resolve(documentId + ".zpl");
        lock.readLock().lock();
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(documentString);
        } catch (Exception e) {
            lock.readLock().unlock();
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
        lock.readLock().unlock();
        
    }

    @Override
    public InputStream downloadDocument(String documentId) throws Exception {
        Path filePath = getStoreDir().resolve(documentId + ".zpl");
        lock.readLock().lock();
        try {
            InputStream ans = new FileInputStream(filePath.toFile());
            cached.add(documentId);
            lock.readLock().unlock();
            return ans;
        } catch (Exception e) {
            lock.readLock().unlock();
            throw new BadRequestException("Failed to download document: "+e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) throws Exception {
        Path filePath = getStoreDir().resolve(documentId+ ".zpl");
        lock.writeLock().lock();
        try {
            Files.deleteIfExists(filePath);
            cached.remove(documentId);
        } catch (Exception e) {
            lock.writeLock().unlock();
            throw new BadRequestException("Failed to delete document: "+e.getMessage());
        }
        lock.writeLock().unlock();
    }

    @Override
    public boolean documentExists(String documentId) throws Exception {
        Path filePath = getStoreDir().resolve(documentId+ ".zpl");
        return Files.exists(filePath);
    }

    private Path getStoreDir() {
        return this.storagePath;
    }

    public Path getLocalStoreDir() {
        return getStoreDir();
    }

    // actually deletes current session's files
    public void clearCache() throws Exception {
        List<String> tmp = List.copyOf(cached);
        for(String id : tmp) {
            deleteDocument(id);
        }
    }

    // actually deletes all files in the storage file. 
    public void deleteAll() throws Exception {
        List<String> tmp = List.copyOf(cached);
        for(String id : tmp) {
            deleteDocument(id);
        }
    }

}
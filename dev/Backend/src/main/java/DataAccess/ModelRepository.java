package DataAccess;

import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.stereotype.Repository;

//TODO: Eviction strategy of local files.
public abstract class ModelRepository {
    
    public abstract void uploadDocument(String documentId, InputStream documentStream) throws Exception;
    public abstract void uploadDocument(String documentId, String documentStream) throws Exception;
    public  abstract InputStream downloadDocument(String documentId) throws Exception;
    public  abstract void deleteDocument(String documentId) throws Exception;
    public  abstract boolean documentExists(String documentId) throws Exception;
    public abstract Path getLocalStoreDir();

    public Path getLocalyCachedFile(String documentId) {
        return getLocalStoreDir().resolve(documentId+ ".zpl"); 
    }
    public abstract void clearCache() throws Exception;
    public abstract void deleteAll() throws Exception;
}
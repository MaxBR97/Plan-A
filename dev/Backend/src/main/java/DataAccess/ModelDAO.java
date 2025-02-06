package DataAccess;

import java.io.InputStream;
import java.nio.file.Path;

public interface ModelDAO {
    public abstract void uploadDocument(String documentId, InputStream documentStream);
    public abstract InputStream downloadDocument(String documentId);
    public abstract void deleteDocument(String documentId);
    public abstract boolean documentExists(String documentId);
}
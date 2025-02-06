package DataAccess;

import java.io.InputStream;

public interface ModelDAO {
    void uploadDocument(String documentId, InputStream documentStream);
    InputStream downloadDocument(String documentId);
    void deleteDocument(String documentId);
    boolean documentExists(String documentId);
}
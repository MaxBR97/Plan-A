package DataAccess;

import java.io.InputStream;

public interface ModelRepository {
    public  void uploadDocument(String documentId, InputStream documentStream);
    public  InputStream downloadDocument(String documentId);
    public  void deleteDocument(String documentId);
    public  boolean documentExists(String documentId);
}
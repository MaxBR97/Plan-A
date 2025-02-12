package DataAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import Exceptions.InternalErrors.BadRequestException;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Repository
public class S3ModelStorageService extends  ModelRepository{

    private final S3Client s3;
    private final String bucketName = "zpl-store";

    private Path storagePath;

    public S3ModelStorageService(@Value("${app.file.storage-dir}") String loadedRelativeStoragePath) {
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
            throw new BadRequestException("Failed to create directory for local zpl files: "+e.getMessage());
        }

        this.s3 = S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @Override
    public void uploadDocument(String documentId, InputStream documentStream)throws Exception {
        try {
            byte[] bytes = documentStream.readAllBytes(); // Read full content safely
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId+".zpl")
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload document: "+e.getMessage());
        }
    }

    @Override
    public void uploadDocument(String documentId, String documentStream)throws Exception {
        try {
            byte[] bytes = documentStream.getBytes(); // Read full content safely
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId+".zpl")
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload document: "+e.getMessage());
        }
    }

    @Override
    public InputStream downloadDocument(String documentId) throws Exception {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId+".zpl")
                    .build();
            
            InputStream ans = s3.getObject(getObjectRequest);
            Files.write(this.getLocalStoreDir().resolve(documentId + ".zpl"), ans.readAllBytes());
            return ans;
        } catch (Exception e) {
            throw new BadRequestException("Failed to download document: "+e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) throws Exception {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(documentId+".zpl")
                .build();
        s3.deleteObject(req);
    }

    @Override
    public boolean documentExists(String documentId) throws Exception {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId+".zpl")
                    .build();
            s3.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) { 
            return false;
        }
    }

    public Path getLocalStoreDir(){
        return this.storagePath;
    }
    
}

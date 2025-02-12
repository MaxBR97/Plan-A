package DataAccess;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;

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

@Service
public class S3ModelStorageService implements ModelRepository{

    private final S3Client s3;
    private final String bucketName = "zpl-store";

    public S3ModelStorageService() {
        this.s3 = S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @Override
    public void uploadDocument(String documentId, InputStream documentStream) {
        try {
            byte[] bytes = documentStream.readAllBytes(); // Read full content safely
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId)
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Override
    public InputStream downloadDocument(String documentId) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId)
                    .build();
            
            return s3.getObject(getObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to download document", e);
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(documentId)
                .build();
        s3.deleteObject(req);
    }

    @Override
    public boolean documentExists(String documentId) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId)
                    .build();
            s3.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) { 
            return false;
        }
    }
    
}

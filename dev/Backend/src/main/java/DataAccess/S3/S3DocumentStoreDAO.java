package DataAccess.S3;


import java.io.InputStream;

import DataAccess.ModelRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3DocumentStoreDAO implements ModelRepository {
    private final S3Client s3Client;
    private final String bucketName;

    public S3DocumentStoreDAO(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void uploadDocument(String documentId, InputStream documentStream) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(documentId)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(documentStream, -1));
    }

    @Override
    public InputStream downloadDocument(String documentId) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(documentId)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public void deleteDocument(String documentId) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(documentId)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    @Override
    public boolean documentExists(String documentId) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(documentId)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
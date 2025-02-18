package DataAccess;

import java.io.InputStream;
import java.nio.file.Path;

import DataAccess.LocalStorage.ModelLocalDiskDAO;
import Exceptions.InternalErrors.BadRequestException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


public class S3ModelStorageService extends  ModelRepository{

    private final S3Client s3;
    private final String bucketName;
    private ModelLocalDiskDAO localCache;

    public S3ModelStorageService(String loadedRelativeStoragePath, String region, String bucketName) {
        localCache = new ModelLocalDiskDAO(loadedRelativeStoragePath);
        this.bucketName = bucketName;
        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        createBucketIfNotExists(bucketName);
    }

    public void createBucketIfNotExists(String bucketName) {
        try {   
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket exists: " + bucketName);
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Created bucket: " + bucketName);
        }
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
            // the reason fo that, is because we want to "upload" the file to local cache,
            // and then retrieve the local cache copy (saved file).
            localCache.uploadDocument(documentId, ans);
            ans = localCache.downloadDocument(documentId);
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
        try{
            localCache.deleteDocument(documentId);
        } catch (Exception e) {
            //all good, document might not be cached.
        }
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
        return localCache.getLocalStoreDir();
    }
    
}

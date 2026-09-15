package org.example.tnal_youth_backend.file.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stores uploaded files in S3. Credentials come from the EC2 instance
 * profile on Elastic Beanstalk (aws-elasticbeanstalk-ec2-role) via the
 * SDK's default credentials chain -- no access keys are configured here.
 *
 * <p>Active when app.storage.type=s3 (the default, so an existing AWS
 * deployment needs no configuration change to keep working). See {@link
 * LocalDiskFileStorage} for the alternative used when the app isn't
 * running on AWS -- Elastic Beanstalk's EC2 instances don't persist local
 * disk across redeploys/restarts, which is the reason this class exists
 * at all rather than just writing to disk; that constraint doesn't apply
 * to a plain, self-managed server.
 */
@Component
@ConditionalOnProperty(
        prefix = "app.storage",
        name = "type",
        havingValue = "s3",
        matchIfMissing = true
)
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorage(
            @Value("${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.region:ap-southeast-1}") String region
    ) {
        this.bucket = bucket;

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Override
    public void store(
            String objectKey,
            InputStream data,
            long sizeBytes,
            String contentType
    ) throws IOException {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(data, sizeBytes)
            );
        } catch (S3Exception exception) {
            throw new IOException(
                    "Could not store object in S3: " + objectKey,
                    exception
            );
        }
    }

    @Override
    public Resource load(String objectKey) {
        try {
            return new InputStreamResource(
                    s3Client.getObject(
                            GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(objectKey)
                                    .build()
                    )
            );
        } catch (NoSuchKeyException exception) {
            throw new FileStorageNotFoundException(objectKey);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );
        } catch (S3Exception ignored) {
            /*
             * Cleanup failure must not hide the original exception.
             */
        }
    }
}

package org.example.tnal_youth_backend.file.storage;

/**
 * Thrown by a {@link FileStorage#load(String)} implementation when the
 * requested objectKey has no corresponding stored object -- storage-
 * backend-agnostic, so FileServiceImpl can map it to a 404 without knowing
 * whether the object lives in S3 or on local disk.
 */
public class FileStorageNotFoundException extends RuntimeException {

    public FileStorageNotFoundException(String objectKey) {
        super("Stored object not found: " + objectKey);
    }
}

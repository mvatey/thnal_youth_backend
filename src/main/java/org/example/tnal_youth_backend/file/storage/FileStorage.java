package org.example.tnal_youth_backend.file.storage;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Physically stores/retrieves/deletes an uploaded file's bytes, addressed
 * by the same objectKey (e.g. "images/&lt;uuid&gt;.png") FileServiceImpl
 * already generates. Only the storage backend differs between
 * implementations (S3FileStorage, LocalDiskFileStorage) -- everything
 * else (validation, FileEntity bookkeeping) stays in FileServiceImpl
 * regardless of which one is active. Selected via the app.storage.type
 * property.
 */
public interface FileStorage {

    void store(
            String objectKey,
            InputStream data,
            long sizeBytes,
            String contentType
    ) throws IOException;

    /**
     * @throws FileStorageNotFoundException if objectKey doesn't exist
     */
    Resource load(String objectKey);

    /**
     * Best-effort delete -- a failure here must never hide whatever the
     * caller was already doing (see FileServiceImpl's own delete/rollback
     * paths), so implementations swallow their own failures rather than
     * throw.
     */
    void delete(String objectKey);
}

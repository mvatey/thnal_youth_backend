package org.example.tnal_youth_backend.file.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Stores uploaded files directly on this server's own disk, under a
 * configured root directory -- the option for a self-managed server that
 * isn't AWS Elastic Beanstalk. Active when app.storage.type=local.
 *
 * <p>The root directory must be a path that actually survives redeploys/
 * restarts on whatever server this runs on (a dedicated data directory or
 * volume -- not, say, the same directory the deploy process wipes and
 * re-extracts the application into each release).
 */
@Component
@ConditionalOnProperty(
        prefix = "app.storage",
        name = "type",
        havingValue = "local"
)
public class LocalDiskFileStorage implements FileStorage {

    private final Path rootDir;

    public LocalDiskFileStorage(
            @Value("${app.storage.local.root-dir:./uploads}") String rootDir
    ) throws IOException {
        this.rootDir = Path.of(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(this.rootDir);
    }

    @Override
    public void store(
            String objectKey,
            InputStream data,
            long sizeBytes,
            String contentType
    ) throws IOException {
        Path target = resolve(objectKey);
        Files.createDirectories(target.getParent());
        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource load(String objectKey) {
        Path target = resolve(objectKey);

        if (!Files.exists(target)) {
            throw new FileStorageNotFoundException(objectKey);
        }

        return new FileSystemResource(target);
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException ignored) {
            /*
             * Cleanup failure must not hide the original exception.
             */
        }
    }

    /**
     * Resolves objectKey (e.g. "images/&lt;uuid&gt;.png") against rootDir
     * and rejects anything that would escape it. objectKey always comes
     * from FileServiceImpl's own UUID-based generation rather than user
     * input directly, but this stays defensive regardless.
     */
    private Path resolve(String objectKey) {
        Path target = rootDir.resolve(objectKey).normalize();

        if (!target.startsWith(rootDir)) {
            throw new IllegalArgumentException(
                    "Invalid object key: " + objectKey
            );
        }

        return target;
    }
}

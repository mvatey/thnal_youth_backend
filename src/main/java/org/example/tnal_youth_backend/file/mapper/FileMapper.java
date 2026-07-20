package org.example.tnal_youth_backend.file.mapper;

import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    private static final double BYTES_PER_KB = 1024.0;
    private static final double BYTES_PER_MB =
            1024.0 * 1024.0;

    public FileResponse toResponse(FileEntity file) {
        if (file == null) {
            return null;
        }

        return new FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                file.getSizeBytes(),
                calculateKb(file.getSizeBytes()),
                calculateMb(file.getSizeBytes()),
                file.getUploadedById(),
                file.getCreatedAt()
        );
    }

    private Double calculateKb(Long sizeBytes) {
        if (sizeBytes == null) {
            return null;
        }

        return round(sizeBytes / BYTES_PER_KB);
    }

    private Double calculateMb(Long sizeBytes) {
        if (sizeBytes == null) {
            return null;
        }

        return round(sizeBytes / BYTES_PER_MB);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
package org.example.tnal_youth_backend.file.service;

import org.example.tnal_youth_backend.file.dto.request.CreateFileRequest;
import org.example.tnal_youth_backend.file.dto.request.UpdateFileRequest;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FileService {

    List<FileResponse> getAllFiles();

    FileResponse getFileById(
            Long id
    );

//    FileResponse getFileByPath(
//            String filePath
//    );

    FileResponse createFile(
            CreateFileRequest request
    );

    FileResponse updateFile(
            Long id,
            UpdateFileRequest request
    );

    void deleteFile(
            Long id
    );
}
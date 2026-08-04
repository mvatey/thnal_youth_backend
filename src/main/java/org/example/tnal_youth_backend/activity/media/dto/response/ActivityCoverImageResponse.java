package org.example.tnal_youth_backend.activity.media.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCoverImageResponse {

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("file_id")
    private Long fileId;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    @JsonProperty("download_url")
    private String downloadUrl;
}
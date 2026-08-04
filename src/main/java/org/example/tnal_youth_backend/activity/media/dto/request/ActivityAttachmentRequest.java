package org.example.tnal_youth_backend.activity.media.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAttachmentRequest {

    @Size(max = 255)
    private String title;

    private String description;

    @Builder.Default
    @Min(0)
    private Integer sortOrder = 0;

}
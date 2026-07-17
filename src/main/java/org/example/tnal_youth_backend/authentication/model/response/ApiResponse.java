package org.example.tnal_youth_backend.authentication.model.response;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {

    private boolean success;

    private String message;

}
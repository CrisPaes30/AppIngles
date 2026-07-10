package com.englishmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientLogRequest {

    @NotBlank
    @Size(max = 100)
    private String context;

    @NotBlank
    @Size(max = 2000)
    private String message;

    @Size(max = 500)
    private String url;

    @Size(max = 300)
    private String userAgent;
}

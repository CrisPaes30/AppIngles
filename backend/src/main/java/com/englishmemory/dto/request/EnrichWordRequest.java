package com.englishmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnrichWordRequest {

    @NotBlank
    @Size(max = 200)
    private String word;
}

package com.englishmemory.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hexadecimal (#RRGGBB)")
    private String color;

    @Size(max = 50)
    private String icon;
}

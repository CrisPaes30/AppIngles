package com.englishmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String name;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hexadecimal (#RRGGBB)")
    private String color = "#6366F1";

    @Size(max = 50)
    private String icon = "book";
}

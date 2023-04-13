package com.example.app.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProductDTO {
    @NotBlank
    private String name;
    @Min(1)
    private int quantity;
}

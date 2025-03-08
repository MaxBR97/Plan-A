package DTO.Records.Requests.Commands;

import jakarta.validation.constraints.NotBlank;

public record CreateImageFromFileDTO(@NotBlank String imageName, @NotBlank String imageDescription, @NotBlank String code) {}

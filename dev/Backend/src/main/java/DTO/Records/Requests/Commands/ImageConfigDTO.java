package DTO.Records.Requests.Commands;

import DTO.Records.Image.ImageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageConfigDTO(@NotBlank String imageId,@NotNull ImageDTO image) { }

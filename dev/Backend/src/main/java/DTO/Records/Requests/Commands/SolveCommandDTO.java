package DTO.Records.Requests.Commands;
import DTO.Records.Model.ModelData.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolveCommandDTO(@NotBlank String imageId, @NotNull InputDTO input, @Min(0) int timeout) {}

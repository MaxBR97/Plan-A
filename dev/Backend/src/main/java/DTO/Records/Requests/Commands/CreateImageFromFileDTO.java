package DTO.Records.Requests.Commands;

import jakarta.validation.constraints.NotBlank;

public record CreateImageFromFileDTO(String imageName,
                                    String imageDescription,
                                    @NotBlank String owner ,
                                    Boolean isPrivate ,
                                    @NotBlank String code /*consider using some String buffer*/) {}

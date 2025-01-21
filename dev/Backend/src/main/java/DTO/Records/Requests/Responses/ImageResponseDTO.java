package DTO.Records.Requests.Responses;

import DTO.Records.Image.ImageDTO;

public record ImageResponseDTO(String id, boolean compiled, String message, ImageDTO image) {
}

package DTO.Records.Requests.Commands;

public record KafkaCompileRequestDTO(
    String id,
    float timeout
) {}

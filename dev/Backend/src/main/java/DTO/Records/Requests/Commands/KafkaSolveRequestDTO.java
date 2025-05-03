package DTO.Records.Requests.Commands;


public record KafkaSolveRequestDTO(
    String id,
    float timeout,
    String solverScript
) {}

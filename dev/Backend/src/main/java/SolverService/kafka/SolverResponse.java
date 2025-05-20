package SolverService.kafka;

import Model.Solution;

public record SolverResponse(
    String requestId,
    Solution solution,
    String errorMessage,
    boolean success
) {} 
package SolverService.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

// @JsonInclude(JsonInclude.Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true)
public record SolverRequest(
    String requestId,
    String fileId,
    int timeout,
    String solverScript,
    RequestType type
) {
    public enum RequestType {
        SOLVE,
        SOLVE_ASYNC,
        COMPILE_CHECK,
        COMPILE_CHECK_ASYNC
    }
} 
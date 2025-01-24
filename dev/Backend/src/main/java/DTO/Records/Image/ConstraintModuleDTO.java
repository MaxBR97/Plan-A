package DTO.Records.Image;


import java.util.List;
import java.util.Set;


/**
 *
 *
 */
public record ConstraintModuleDTO(String moduleName, String description, Set<String> constraints,
                                  Set<String> inputSets,
                                  Set<String> inputParams) {}

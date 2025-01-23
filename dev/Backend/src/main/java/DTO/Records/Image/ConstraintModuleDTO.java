package DTO.Records.Image;


import java.util.List;


/**
 *
 *
 */
public record ConstraintModuleDTO(String moduleName, String description, List<String> constraints,
                                  List<String> inputSets,
                                  List<String> inputParams) {}

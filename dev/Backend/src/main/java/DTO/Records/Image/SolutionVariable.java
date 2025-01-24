package DTO.Records.Image;

import java.util.List;
import java.util.Set;

public record SolutionVariable(List<String> setStructure, List<String> typeStructure,
                               Set<SolutionValueDTO> solutions) {
}

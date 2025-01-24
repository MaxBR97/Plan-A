package DTO.Records.Image;

import java.util.List;

public record SolutionVariable(List<String> setStructure, List<String> typeStructure,
                               List<SolutionValueDTO> solutions) {
}

package DTO.Records;

import java.util.Map;

public record PreferenceModuleDTO(boolean isActive, String name, String description, Map<String, PreferenceDTO> module) {
}

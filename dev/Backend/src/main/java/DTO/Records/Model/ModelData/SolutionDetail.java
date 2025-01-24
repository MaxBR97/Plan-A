package DTO.Records.Model.ModelData;

import java.util.Map;

/**
 *
 * @param setValues
 * @param result
 */
public record SolutionDetail(
        Map<String, String> setValues,
        String result
    ) {}
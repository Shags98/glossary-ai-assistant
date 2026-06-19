package com.shagithiya.glossaryai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslateTermRequest {

    @NotBlank(message = "English term is required")
    private String englishTerm;

    private String definition; // optional context to improve translation quality
}

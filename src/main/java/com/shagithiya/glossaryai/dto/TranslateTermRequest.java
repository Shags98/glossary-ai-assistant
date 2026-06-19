package com.shagithiya.glossaryai.dto;

import jakarta.validation.constraints.NotBlank;

public class TranslateTermRequest {

    @NotBlank(message = "English term is required")
    private String englishTerm;

    private String definition;

    public TranslateTermRequest() {}

    public String getEnglishTerm() { return englishTerm; }
    public String getDefinition() { return definition; }

    public void setEnglishTerm(String englishTerm) { this.englishTerm = englishTerm; }
    public void setDefinition(String definition) { this.definition = definition; }
}

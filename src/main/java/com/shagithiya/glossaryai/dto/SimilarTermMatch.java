package com.shagithiya.glossaryai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarTermMatch {
    private String existingTerm;
    private double similarityScore; // 0.0 to 1.0
    private String reason;
}

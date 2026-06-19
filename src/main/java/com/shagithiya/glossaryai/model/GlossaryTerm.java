package com.shagithiya.glossaryai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a glossary term with its English source text and
 * AI-generated or human-approved translations across 14 supported languages.
 */
@Entity
@Table(name = "glossary_terms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlossaryTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String englishTerm;

    @Column(length = 1000)
    private String definition;

    // Stores translations as JSON: { "es": "...", "zh": "...", "vi": "..." }
    @ElementCollection
    @CollectionTable(name = "glossary_translations", joinColumns = @JoinColumn(name = "term_id"))
    @MapKeyColumn(name = "language_code")
    @Column(name = "translation", length = 1000)
    @Builder.Default
    private Map<String, String> translations = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    private String status = "AI_SUGGESTED"; // AI_SUGGESTED, HUMAN_APPROVED

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

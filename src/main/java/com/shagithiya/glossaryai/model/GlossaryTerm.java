package com.shagithiya.glossaryai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "glossary_terms")
public class GlossaryTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String englishTerm;

    @Column(length = 1000)
    private String definition;

    @ElementCollection
    @CollectionTable(name = "glossary_translations", joinColumns = @JoinColumn(name = "term_id"))
    @MapKeyColumn(name = "language_code")
    @Column(name = "translation", length = 1000)
    private Map<String, String> translations = new HashMap<>();

    @Column(nullable = false)
    private String status = "AI_SUGGESTED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public GlossaryTerm() {}

    // Getters
    public Long getId() { return id; }
    public String getEnglishTerm() { return englishTerm; }
    public String getDefinition() { return definition; }
    public Map<String, String> getTranslations() { return translations; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setEnglishTerm(String englishTerm) { this.englishTerm = englishTerm; }
    public void setDefinition(String definition) { this.definition = definition; }
    public void setTranslations(Map<String, String> translations) { this.translations = translations; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GlossaryTerm term = new GlossaryTerm();
        public Builder englishTerm(String v) { term.englishTerm = v; return this; }
        public Builder definition(String v) { term.definition = v; return this; }
        public Builder translations(Map<String, String> v) { term.translations = v; return this; }
        public Builder status(String v) { term.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { term.createdAt = v; return this; }
        public GlossaryTerm build() { return term; }
    }
}

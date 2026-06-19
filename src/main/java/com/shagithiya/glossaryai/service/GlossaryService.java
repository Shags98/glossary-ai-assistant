package com.shagithiya.glossaryai.service;

import com.shagithiya.glossaryai.dto.SimilarTermMatch;
import com.shagithiya.glossaryai.model.GlossaryTerm;
import com.shagithiya.glossaryai.repository.GlossaryTermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GlossaryService {

    private final GlossaryTermRepository repository;
    private final LlmTranslationClient llmClient;
    private final SimilarityService similarityService;

    public GlossaryService(GlossaryTermRepository repository,
                            LlmTranslationClient llmClient,
                            SimilarityService similarityService) {
        this.repository = repository;
        this.llmClient = llmClient;
        this.similarityService = similarityService;
    }

    public List<GlossaryTerm> getAllTerms() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<SimilarTermMatch> checkForSimilarTerms(String englishTerm) {
        return similarityService.findSimilarTerms(englishTerm);
    }

    /**
     * Generates AI-suggested translations for a new term across all 14
     * supported languages and persists it with status AI_SUGGESTED.
     * A human reviewer later promotes it to HUMAN_APPROVED via approveTerm().
     */
    public GlossaryTerm generateAndSaveTranslations(String englishTerm, String definition) {
        log.info("Generating translations for term: {}", englishTerm);

        Map<String, String> translations = llmClient.generateTranslations(englishTerm, definition);

        GlossaryTerm term = GlossaryTerm.builder()
                .englishTerm(englishTerm)
                .definition(definition)
                .translations(translations)
                .status("AI_SUGGESTED")
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(term);
    }

    public GlossaryTerm approveTerm(Long id) {
        GlossaryTerm term = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Term not found: " + id));
        term.setStatus("HUMAN_APPROVED");
        term.setUpdatedAt(LocalDateTime.now());
        return repository.save(term);
    }

    public void deleteTerm(Long id) {
        repository.deleteById(id);
    }
}

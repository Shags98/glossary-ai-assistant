package com.shagithiya.glossaryai.service;

import com.shagithiya.glossaryai.dto.SimilarTermMatch;
import com.shagithiya.glossaryai.model.GlossaryTerm;
import com.shagithiya.glossaryai.repository.GlossaryTermRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GlossaryService {

    private static final Logger log = LoggerFactory.getLogger(GlossaryService.class);

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

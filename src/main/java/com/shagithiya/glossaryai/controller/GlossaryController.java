package com.shagithiya.glossaryai.controller;

import com.shagithiya.glossaryai.dto.SimilarTermMatch;
import com.shagithiya.glossaryai.dto.TranslateTermRequest;
import com.shagithiya.glossaryai.model.GlossaryTerm;
import com.shagithiya.glossaryai.service.GlossaryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {

    private final GlossaryService glossaryService;

    public GlossaryController(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @GetMapping("/terms")
    public ResponseEntity<List<GlossaryTerm>> getAllTerms() {
        return ResponseEntity.ok(glossaryService.getAllTerms());
    }

    /**
     * Core AI endpoint: given an English term, checks for near-duplicates
     * and generates draft translations across all 14 supported languages
     * in a single structured LLM call.
     */
    @PostMapping("/terms/translate")
    public ResponseEntity<Map<String, Object>> translateNewTerm(@Valid @RequestBody TranslateTermRequest request) {
        List<SimilarTermMatch> similarTerms = glossaryService.checkForSimilarTerms(request.getEnglishTerm());
        GlossaryTerm savedTerm = glossaryService.generateAndSaveTranslations(
                request.getEnglishTerm(), request.getDefinition());

        return ResponseEntity.ok(Map.of(
                "term", savedTerm,
                "similarTermWarnings", similarTerms
        ));
    }

    @GetMapping("/terms/{englishTerm}/similar")
    public ResponseEntity<List<SimilarTermMatch>> checkSimilarTerms(@PathVariable String englishTerm) {
        return ResponseEntity.ok(glossaryService.checkForSimilarTerms(englishTerm));
    }

    @PutMapping("/terms/{id}/approve")
    public ResponseEntity<GlossaryTerm> approveTerm(@PathVariable Long id) {
        return ResponseEntity.ok(glossaryService.approveTerm(id));
    }

    @DeleteMapping("/terms/{id}")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        glossaryService.deleteTerm(id);
        return ResponseEntity.noContent().build();
    }
}

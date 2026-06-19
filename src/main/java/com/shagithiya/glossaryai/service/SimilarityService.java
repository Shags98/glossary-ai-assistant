package com.shagithiya.glossaryai.service;

import com.shagithiya.glossaryai.dto.SimilarTermMatch;
import com.shagithiya.glossaryai.model.GlossaryTerm;
import com.shagithiya.glossaryai.repository.GlossaryTermRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Detects near-duplicate glossary terms using normalized Levenshtein distance.
 *
 * This is intentionally a lightweight, dependency-free similarity check rather
 * than full vector embeddings + a vector database - that's a natural "next step"
 * extension of this prototype, and is called out as such in the README. The point
 * here is demonstrating the *category* of capability (semantic-ish duplicate
 * detection) that Power Apps has no native support for, not shipping a
 * production-grade embeddings pipeline.
 */
@Service
public class SimilarityService {

    private final GlossaryTermRepository repository;

    public SimilarityService(GlossaryTermRepository repository) {
        this.repository = repository;
    }

    private static final double SIMILARITY_THRESHOLD = 0.65;

    public List<SimilarTermMatch> findSimilarTerms(String newTerm) {
        List<GlossaryTerm> existing = repository.findAll();
        List<SimilarTermMatch> matches = new ArrayList<>();

        String normalizedNew = normalize(newTerm);

        for (GlossaryTerm term : existing) {
            String normalizedExisting = normalize(term.getEnglishTerm());
            double score = similarityScore(normalizedNew, normalizedExisting);

            if (score >= SIMILARITY_THRESHOLD) {
                String reason = score > 0.95
                        ? "Near-exact match"
                        : "High textual similarity - possible duplicate or variant term";
                matches.add(new SimilarTermMatch(term.getEnglishTerm(), round(score), reason));
            }
        }

        matches.sort(Comparator.comparingDouble(SimilarTermMatch::getSimilarityScore).reversed());
        return matches;
    }

    private String normalize(String input) {
        return input.toLowerCase().trim().replaceAll("[^a-z0-9 ]", "");
    }

    /**
     * Similarity score derived from normalized Levenshtein edit distance.
     * 1.0 = identical, 0.0 = completely different.
     */
    private double similarityScore(String a, String b) {
        int distance = levenshteinDistance(a, b);
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

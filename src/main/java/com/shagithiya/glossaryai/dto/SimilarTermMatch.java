package com.shagithiya.glossaryai.dto;

public class SimilarTermMatch {
    private String existingTerm;
    private double similarityScore;
    private String reason;

    public SimilarTermMatch() {}

    public SimilarTermMatch(String existingTerm, double similarityScore, String reason) {
        this.existingTerm = existingTerm;
        this.similarityScore = similarityScore;
        this.reason = reason;
    }

    public String getExistingTerm() { return existingTerm; }
    public double getSimilarityScore() { return similarityScore; }
    public String getReason() { return reason; }

    public void setExistingTerm(String existingTerm) { this.existingTerm = existingTerm; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    public void setReason(String reason) { this.reason = reason; }
}

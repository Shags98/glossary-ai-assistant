package com.shagithiya.glossaryai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for calling Groq's OpenAI-compatible chat completion API.
 *
 * This is the core piece Power Apps could not do cleanly:
 *   - a single structured prompt requesting translations for 14 languages at once
 *   - strict JSON-only response parsing
 *   - retry-friendly, testable, version-controlled prompt logic
 *
 * Power Apps' AI Builder / Power Automate HTTP connectors only support simple
 * request-response calls with no native JSON schema validation or multi-step
 * orchestration, which made this kind of batch structured generation impractical
 * inside the EDD Glossary Manager.
 */
@Slf4j
@Service
public class LlmTranslationClient {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    // The 14 languages supported by the EDD Glossary Manager
    private static final Map<String, String> SUPPORTED_LANGUAGES = new LinkedHashMap<>() {{
        put("es", "Spanish");
        put("zh", "Chinese (Simplified)");
        put("vi", "Vietnamese");
        put("tl", "Tagalog");
        put("ko", "Korean");
        put("ar", "Arabic");
        put("hy", "Armenian");
        put("fa", "Farsi/Persian");
        put("hmn", "Hmong");
        put("ja", "Japanese");
        put("pa", "Punjabi");
        put("ru", "Russian");
        put("th", "Thai");
        put("uk", "Ukrainian");
    }};

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> generateTranslations(String englishTerm, String definition) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No Groq API key configured - returning mock translations for demo purposes");
            return mockTranslations(englishTerm);
        }

        String prompt = buildTranslationPrompt(englishTerm, definition);

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.2);
            requestBody.put("response_format", Map.of("type", "json_object"));
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content",
                            "You are a professional government-document translator. " +
                            "Respond ONLY with valid JSON mapping language codes to translations. No prose, no markdown."),
                    Map.of("role", "user", "content", prompt)
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            JsonNode response = restTemplate.postForObject(GROQ_API_URL, entity, JsonNode.class);

            String content = response
                    .path("choices").get(0)
                    .path("message").path("content").asText();

            JsonNode translationsJson = objectMapper.readTree(content);
            Map<String, String> result = new LinkedHashMap<>();
            translationsJson.fields().forEachRemaining(e -> result.put(e.getKey(), e.getValue().asText()));
            return result;

        } catch (Exception ex) {
            log.error("LLM translation call failed, falling back to mock response: {}", ex.getMessage());
            return mockTranslations(englishTerm);
        }
    }

    private String buildTranslationPrompt(String englishTerm, String definition) {
        StringBuilder sb = new StringBuilder();
        sb.append("Translate the following government glossary term into all 14 languages listed below.\n\n");
        sb.append("English term: ").append(englishTerm).append("\n");
        if (definition != null && !definition.isBlank()) {
            sb.append("Definition/context: ").append(definition).append("\n");
        }
        sb.append("\nLanguages (respond with JSON keys exactly as these codes):\n");
        SUPPORTED_LANGUAGES.forEach((code, name) -> sb.append(code).append(" = ").append(name).append("\n"));
        sb.append("\nReturn ONLY a JSON object like: {\"es\": \"...\", \"zh\": \"...\", ...} with all 14 keys.");
        return sb.toString();
    }

    /**
     * Mock fallback so the demo runs without a live API key.
     * Clearly labeled so it's never mistaken for a real translation.
     */
    private Map<String, String> mockTranslations(String englishTerm) {
        Map<String, String> mock = new LinkedHashMap<>();
        SUPPORTED_LANGUAGES.forEach((code, name) ->
                mock.put(code, "[MOCK-" + code.toUpperCase() + "] " + englishTerm));
        return mock;
    }

    public Map<String, String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
}

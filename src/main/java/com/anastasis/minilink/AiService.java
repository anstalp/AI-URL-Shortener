package com.anastasis.minilink;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public AiService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateAlias(String originalUrl){
        String prompt = "Analyze the following URL and generate a short, " +
                "meaningful alias for a URL shortener. Constraints: " +
                "1. Use ONLY lowercase letters and hyphens (kebab-case). " +
                "2. Maximum length is 15 characters. " +
                "3. Return ONLY the final string value. " +
                "No quotes, no punctuation, no conversational text. " +
                "URL: " + originalUrl;

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );


        // Στέλνουμε το αίτημα στο ίντερνετ με την "αλυσίδα" του RestClient, ακολουθώντας το documentation!
        String response = restClient.post()
                .uri("/v1beta/models/gemini-3.5-flash:generateContent") // Το σωστό μοντέλο, χωρίς το key στο link!
                .header("x-goog-api-key", apiKey) // Το κλειδί μπαίνει κρυφά στα Headers
                .header("Content-Type", "application/json") // Λέμε στη Google ότι της στέλνουμε JSON
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            // Reads JSON
            ObjectMapper mapper = new ObjectMapper();

            //Translate String from Google to a tree
            JsonNode rootNode = mapper.readTree(response);

            // Digging to JSON for the customAlias
            String finalAlias = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

            return finalAlias;

        } catch (Exception e) {
            System.out.println("Failed reading JSON from AI: " + e.getMessage());
            throw new RuntimeException("Failed to parse AI response");
        }

    }



}

package com.email.email_writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service

public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String getGeminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequestDto emailRequestDto) {

        String prompt = buildPrompt(emailRequestDto);
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            String response = webClient.post()
                    .uri(geminiApiUrl + getGeminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();


            return extractResponseContent(response);
        } catch (WebClientResponseException e) {
            // Log the actual error from the API for easier debugging
            System.err.println("API Error Status: " + e.getStatusCode());
            System.err.println("API Error Body: " + e.getResponseBodyAsString());
            // You can return a user-friendly message or re-throw a custom exception
            return "Error from API: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return "Error processing request: " + e.getMessage();
        }

    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            System.err.println("Failed to parse API response: " + response);
            return "Error parsing API response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequestDto emailRequestDto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Do not generate a subject line.");
        if (emailRequestDto.getTone() != null && !emailRequestDto.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequestDto.getTone()).append(" tone.");
        }
        prompt.append("\n\nOriginal email:\n\"").append(emailRequestDto.getEmailContent()).append("\"");
        return prompt.toString();
    }
}
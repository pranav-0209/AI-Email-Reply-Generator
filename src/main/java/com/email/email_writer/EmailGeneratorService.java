package com.email.email_writer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String getGeminiApiKey;

    public String generateEmailReply(EmailRequestDto emailRequestDto) {

        String prompt = buildPrompt(emailRequestDto);
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + getGeminiApiKey)
                .header("Content-Type","application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return null;
    }

    private String buildPrompt(EmailRequestDto emailRequestDto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line");
        if (emailRequestDto.getTone() != null && !emailRequestDto.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequestDto.getTone()).append(" tone.");
        }
        prompt.append("\nOriginal email: ").append(emailRequestDto.getEmailContent());
        return prompt.toString();
    }
}

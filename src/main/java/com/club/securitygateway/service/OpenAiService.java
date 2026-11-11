package com.club.securitygateway.service;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OpenAiService {

    private final RestClient restClient;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public OpenAiService() {
        this.restClient = RestClient.create("https://api.openai.com/v1");
    }

    public String ask(String question) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", question,
                "max_output_tokens", 300
        );

        JsonNode response = restClient.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new RuntimeException("OpenAI 응답이 없습니다.");
        }

        JsonNode output = response.path("output");

        for (JsonNode item : output) {

            if ("message".equals(item.path("type").asText())) {

                JsonNode content = item.path("content");

                for (JsonNode contentItem : content) {

                    if ("output_text".equals(contentItem.path("type").asText())) {
                        return contentItem.path("text").asText();
                    }
                }
            }
        }

        throw new RuntimeException("OpenAI 응답에서 텍스트를 찾을 수 없습니다.");
    }
}
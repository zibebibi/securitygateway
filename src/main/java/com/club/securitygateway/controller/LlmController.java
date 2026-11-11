package com.club.securitygateway.controller;

import com.club.securitygateway.service.OpenAiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LlmController {

    private final OpenAiService openAiService;

    public LlmController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/llm")
    public Map<String, String> askLlm(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        String answer = openAiService.ask(question);

        return Map.of(
                "question", question,
                "answer", answer
        );
    }
}
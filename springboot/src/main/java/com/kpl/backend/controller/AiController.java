package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.service.AiPromptService;
import com.kpl.backend.service.HunyuanClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiPromptService aiPromptService;
    private final HunyuanClient hunyuanClient;

    @PostMapping("/tactical-analysis")
    public ResponseEntity<ApiResponse<String>> tacticalAnalysis(
            @Valid @RequestBody AiPromptService.TacticalAnalysisRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(aiPromptService.tacticalAnalysis(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/coach-advice")
    public ResponseEntity<ApiResponse<String>> coachAdvice(
            @Valid @RequestBody AiPromptService.CoachAdviceRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(aiPromptService.coachAdvice(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/commentary")
    public ResponseEntity<ApiResponse<String>> commentary(
            @Valid @RequestBody AiPromptService.CommentaryRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(aiPromptService.commentary(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/hunyuan/chat")
    public ResponseEntity<ApiResponse<String>> hunyuanChat(@Valid @RequestBody HunyuanChatRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(hunyuanClient.chatMessages(
                    java.util.List.of(java.util.Map.of("role", "user", "content", request.getPrompt())),
                    request.getTemperature(),
                    request.getTopP()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    public static class HunyuanChatRequest {
        @NotBlank(message = "prompt不能为空")
        private String prompt;
        private Double temperature;
        private Double topP;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }
    }
}

package com.example.naicsdemo.controller;

import com.example.naicsdemo.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {
    
    @Autowired
    private GeminiService geminiService;

    @PostMapping("/recommendations")
    public ResponseEntity<String> getRecommendations(@RequestBody Object payload) {
        try {
            String result = geminiService.getRecommendations(payload.toString());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/models")
    public ResponseEntity<String> listModels() {
        try {
            String result = geminiService.listModels();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Failed to list models: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/companies-needing-recommendations")
    public ResponseEntity<String> getCompaniesNeedingRecommendations() {
        try {
            String result = geminiService.getCompaniesWithLowConfidence();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/test-short-recommendation")
    public ResponseEntity<String> testShortRecommendation() {
        try {
            String result = geminiService.getShortRecommendation("Restaurant", "722511", "3");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

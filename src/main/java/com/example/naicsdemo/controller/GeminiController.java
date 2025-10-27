package com.example.naicsdemo.controller;

import com.example.naicsdemo.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {
    
    @Autowired
    private GeminiService geminiService;

    // @PostMapping("/recommendation")
    // public ResponseEntity<String> getRecommendation(@RequestBody Object payload) {
    //     try {
    //         String result = geminiService.getRecommendations(payload);
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
    //     }
    // }



    // @GetMapping("/models")
    // public ResponseEntity<String> listModels() {
    //     try {
    //         String result = geminiService.listModels();
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Failed to list models: " + e.getMessage() + "\"}");
    //     }
    // }

    // @GetMapping("/companies-needing-recommendations")
    // public ResponseEntity<String> getCompaniesNeedingRecommendations() {
    //     try {
    //         String result = geminiService.getCompaniesWithLowConfidence();
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
    //     }
    // }

    @GetMapping("/test-short-recommendation")
    public ResponseEntity<String> testShortRecommendation() {
        try {
            String result = geminiService.getBusinessSpecificRecommendations("Industrial manufacturing, automation, energy", "333612", "2");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/business-recommendations")
    public ResponseEntity<String> getBusinessRecommendations(@RequestBody Map<String, String> request) {
        try {
            String businessType = request.get("businessType");
            String naicsCode = request.get("naicsCode");
            String confidenceScore = request.get("confidenceScore");
            
            String result = geminiService.getBusinessSpecificRecommendations(businessType, naicsCode, confidenceScore);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/recommendations")
    public ResponseEntity<String> getScoreImprovementTips(@RequestBody Map<String, String> request) {
        try {
            String businessType = request.get("businessType");
            String naicsCode = request.get("naicsCode");
            String currentScore = request.get("currentScore");
            
            String result = geminiService.getBusinessSpecificRecommendations(businessType, naicsCode, currentScore);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // @PostMapping("/clear-cache")
    // public ResponseEntity<String> clearCache() {
    //     geminiService.clearRecommendationCache();
    //     return ResponseEntity.ok("{\"message\":\"Cache cleared successfully\"}");
    // }

    // @GetMapping("/cache-info")
    // public ResponseEntity<String> getCacheInfo() {
    //     int size = geminiService.getCacheSize();
    //     return ResponseEntity.ok("{\"cacheSize\":" + size + "}");
    // }

    // @GetMapping("/test-api-direct")
    // public ResponseEntity<String> testApiDirect() {
    //     try {
    //         String result = geminiService.testDirectApiCall();
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
    //     }
    // }

    // @GetMapping("/api-status")
    // public ResponseEntity<String> getApiStatus() {
    //     String status = geminiService.getApiStatus();
    //     return ResponseEntity.ok("{\"status\":\"" + status + "\"}");
    // }
}
package com.example.citymind.services

import com.example.citymind.models.AIAnalysis
import kotlinx.coroutines.delay

class MockAIService {
    suspend fun analyzeImage(imageUri: String, category: String): AIAnalysis {
        delay(2000) // Simulate processing
        return AIAnalysis(
            detectedIssue = category,
            confidence = 0.94f,
            severity = "HIGH",
            safetyRisk = "HIGH",
            recommendedPriority = "URGENT"
        )
    }
}

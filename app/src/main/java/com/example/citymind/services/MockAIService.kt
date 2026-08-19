package com.example.citymind.services

import com.example.citymind.data.remote.RetrofitClient
import com.example.citymind.data.remote.dtos.AIAnalysisRequest
import com.example.citymind.models.AIAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MockAIService {
    suspend fun analyzeImage(imageUri: String, category: String): AIAnalysis = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.analyzeComplaint(
                AIAnalysisRequest(category = category)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.aiAnalysis
                AIAnalysis(
                    detectedIssue = dto?.detectedCategory ?: category,
                    confidence = dto?.confidence ?: 0.94f,
                    severity = dto?.severity ?: "HIGH",
                    safetyRisk = dto?.safetyRisk ?: "HIGH",
                    recommendedPriority = dto?.recommendedPriority ?: "URGENT"
                )
            } else {
                fallbackAnalysis(category)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            fallbackAnalysis(category)
        }
    }

    private fun fallbackAnalysis(category: String): AIAnalysis {
        return AIAnalysis(
            detectedIssue = category,
            confidence = 0.94f,
            severity = "HIGH",
            safetyRisk = "HIGH",
            recommendedPriority = "URGENT"
        )
    }
}

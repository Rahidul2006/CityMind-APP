package com.example.citymind.models

import kotlinx.serialization.Serializable

@Serializable
data class AIAnalysis(
    val detectedIssue: String,
    val confidence: Float,
    val severity: String, // HIGH, MEDIUM, LOW
    val safetyRisk: String, // HIGH, MEDIUM, LOW
    val recommendedPriority: String // URGENT, NORMAL, LOW
)

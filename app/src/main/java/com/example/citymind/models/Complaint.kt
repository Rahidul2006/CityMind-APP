package com.example.citymind.models

import kotlinx.serialization.Serializable

@Serializable
data class Complaint(
    val complaintId: String,
    val category: String,
    val description: String,
    val imageUri: String?,
    val capturedLocation: LocationData,
    val reportedLocation: LocationData,
    val locationSource: String, // "gps", "manual_adjustment"
    val address: String,
    val aiAnalysis: AIAnalysis,
    val status: ComplaintStatus,
    val statusHistory: List<StatusHistory>,
    val createdAt: Long,
    val resolutionVerified: Boolean = false
)

enum class ComplaintStatus {
    SUBMITTED, VERIFIED, ASSIGNED, IN_PROGRESS, RESOLVED, REOPENED, REJECTED
}

@Serializable
data class StatusHistory(
    val status: ComplaintStatus,
    val timestamp: Long,
    val note: String? = null
)

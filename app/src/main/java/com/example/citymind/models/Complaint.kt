package com.example.citymind.models

import com.example.citymind.data.remote.dtos.ComplaintDto
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
    val resolutionVerified: Boolean = false,
    val resolutionImageUri: String? = null,
    val verificationMessage: String? = null,
    val departmentName: String = "Public Works Department"
)

enum class ComplaintStatus {
    SUBMITTED, VERIFIED, ASSIGNED, IN_PROGRESS, RESOLVED, REOPENED, REJECTED;

    companion object {
        fun fromString(value: String?): ComplaintStatus {
            return when (value?.uppercase()) {
                "SUBMITTED" -> SUBMITTED
                "VERIFIED" -> VERIFIED
                "ASSIGNED" -> ASSIGNED
                "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS
                "RESOLVED" -> RESOLVED
                "REOPENED" -> REOPENED
                "REJECTED" -> REJECTED
                else -> SUBMITTED
            }
        }
    }
}

@Serializable
data class StatusHistory(
    val status: ComplaintStatus,
    val timestamp: Long,
    val note: String? = null
)

fun ComplaintDto.toDomainModel(): Complaint {
    val capLat = capturedLocation?.latitude ?: image?.latitude ?: 0.0
    val capLng = capturedLocation?.longitude ?: image?.longitude ?: 0.0
    val capAcc = capturedLocation?.accuracy ?: image?.gpsAccuracy ?: 0f

    val repLat = reportedLocation?.latitude ?: capLat
    val repLng = reportedLocation?.longitude ?: capLng

    val history = statusHistory?.map { sh ->
        StatusHistory(
            status = ComplaintStatus.fromString(sh.status),
            timestamp = parseDateToLong(sh.timestamp),
            note = sh.message
        )
    } ?: listOf(
        StatusHistory(
            status = ComplaintStatus.fromString(status),
            timestamp = parseDateToLong(createdAt),
            note = "Complaint registered"
        )
    )

    return Complaint(
        complaintId = complaintId ?: mongoId ?: "CM-UNKNOWN",
        category = category ?: "General Civic Issue",
        description = description ?: "",
        imageUri = image?.url,
        capturedLocation = LocationData(latitude = capLat, longitude = capLng, accuracy = capAcc),
        reportedLocation = LocationData(latitude = repLat, longitude = repLng),
        locationSource = locationSource ?: "gps",
        address = address ?: "Captured GPS Location",
        aiAnalysis = AIAnalysis(
            detectedIssue = aiAnalysis?.detectedCategory ?: category ?: "Civic Issue",
            confidence = aiAnalysis?.confidence ?: 0.95f,
            severity = aiAnalysis?.severity ?: "HIGH",
            safetyRisk = aiAnalysis?.safetyRisk ?: "MEDIUM",
            recommendedPriority = aiAnalysis?.recommendedPriority ?: "NORMAL"
        ),
        status = ComplaintStatus.fromString(status),
        statusHistory = history,
        createdAt = parseDateToLong(createdAt),
        resolutionVerified = resolution?.verifiedByCitizen ?: false,
        resolutionImageUri = resolution?.imageUrl,
        verificationMessage = resolution?.verificationMessage,
        departmentName = department?.name ?: "Public Works Department"
    )
}

private fun parseDateToLong(dateStr: String?): Long {
    if (dateStr == null) return System.currentTimeMillis()
    return try {
        java.time.Instant.parse(dateStr).toEpochMilli()
    } catch (e: Exception) {
        try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (ex: Exception) {
            System.currentTimeMillis()
        }
    }
}

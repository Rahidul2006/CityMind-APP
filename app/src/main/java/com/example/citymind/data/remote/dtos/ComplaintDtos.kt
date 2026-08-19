package com.example.citymind.data.remote.dtos

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("complaint") val complaint: T? = null,
    @SerializedName("complaints") val complaints: T? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("errorCode") val errorCode: String? = null
)

data class ComplaintDto(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("complaintId") val complaintId: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image") val image: ImageDto? = null,
    @SerializedName("capturedLocation") val capturedLocation: LatLngAccuracyDto? = null,
    @SerializedName("reportedLocation") val reportedLocation: LatLngDto? = null,
    @SerializedName("locationSource") val locationSource: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("aiAnalysis") val aiAnalysis: AIAnalysisDto? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("department") val department: DepartmentDto? = null,
    @SerializedName("statusHistory") val statusHistory: List<StatusHistoryDto>? = null,
    @SerializedName("resolution") val resolution: ResolutionDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ImageDto(
    @SerializedName("url") val url: String? = null,
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("capturedAt") val capturedAt: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("gpsAccuracy") val gpsAccuracy: Float? = null
)

data class LatLngAccuracyDto(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("accuracy") val accuracy: Float? = null
)

data class LatLngDto(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class AIAnalysisDto(
    @SerializedName("detectedCategory") val detectedCategory: String? = null,
    @SerializedName("confidence") val confidence: Float? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("safetyRisk") val safetyRisk: String? = null,
    @SerializedName("recommendedPriority") val recommendedPriority: String? = null
)

data class DepartmentDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class StatusHistoryDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("message") val message: String? = null
)

data class ResolutionDto(
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("verifiedByCitizen") val verifiedByCitizen: Boolean? = null,
    @SerializedName("verificationMessage") val verificationMessage: String? = null,
    @SerializedName("verifiedAt") val verifiedAt: String? = null
)

data class ResolutionVerificationRequest(
    @SerializedName("resolved") val resolved: Boolean,
    @SerializedName("message") val message: String
)

data class StatusUpdateRequest(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class ConfigDto(
    @SerializedName("googleMapsApiKey") val googleMapsApiKey: String? = null
)

data class AIAnalysisRequest(
    @SerializedName("category") val category: String
)

data class AIAnalysisResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("aiAnalysis") val aiAnalysis: AIAnalysisDto? = null
)

package com.example.citymind.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citymind.data.repository.ComplaintRepository
import com.example.citymind.models.*
import com.example.citymind.services.LocationService
import com.example.citymind.services.MockAIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repository: ComplaintRepository,
    private val locationService: LocationService,
    private val aiService: MockAIService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(category = category, currentStep = ReportStep.PHOTO) }
    }

    fun onPhotoCaptured(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, isCapturingLocation = true) }
        captureLocation()
    }

    private fun captureLocation() {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            _uiState.update { 
                it.copy(
                    capturedLocation = location,
                    reportedLocation = location,
                    isCapturingLocation = false,
                    currentStep = ReportStep.LOCATION
                ) 
            }
        }
    }

    fun onLocationAdjusted(location: LocationData) {
        _uiState.update { it.copy(reportedLocation = location, locationSource = "manual_adjustment") }
    }

    fun proceedFromLocation() {
        _uiState.update { it.copy(isAnalyzing = true, currentStep = ReportStep.AI_ANALYSIS) }
        analyzeImage()
    }

    private fun analyzeImage() {
        viewModelScope.launch {
            val category = _uiState.value.category ?: "Other"
            val analysis = aiService.analyzeImage(_uiState.value.imageUri.toString(), category)
            _uiState.update { 
                it.copy(
                    aiAnalysis = analysis,
                    isAnalyzing = false,
                    currentStep = ReportStep.DETAILS
                ) 
            }
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun proceedToReview() {
        _uiState.update { it.copy(currentStep = ReportStep.REVIEW) }
    }

    fun submitComplaint() {
        viewModelScope.launch {
            val state = _uiState.value
            val complaint = Complaint(
                complaintId = "CM-2026-${(100000..999999).random()}",
                category = state.category ?: "",
                description = state.description,
                imageUri = state.imageUri?.toString(),
                capturedLocation = state.capturedLocation ?: LocationData(0.0, 0.0),
                reportedLocation = state.reportedLocation ?: LocationData(0.0, 0.0),
                locationSource = state.locationSource,
                address = "Mock Address, City", // Should be reverse geocoded
                aiAnalysis = state.aiAnalysis ?: AIAnalysis("", 0f, "", "", ""),
                status = ComplaintStatus.SUBMITTED,
                statusHistory = listOf(StatusHistory(ComplaintStatus.SUBMITTED, System.currentTimeMillis())),
                createdAt = System.currentTimeMillis()
            )
            repository.createComplaint(complaint)
            _uiState.update { it.copy(submittedComplaintId = complaint.complaintId, currentStep = ReportStep.SUBMITTED) }
        }
    }

    fun reset() {
        _uiState.value = ReportUiState()
    }
}

data class ReportUiState(
    val currentStep: ReportStep = ReportStep.CATEGORY,
    val category: String? = null,
    val imageUri: Uri? = null,
    val capturedLocation: LocationData? = null,
    val reportedLocation: LocationData? = null,
    val locationSource: String = "gps",
    val isCapturingLocation: Boolean = false,
    val aiAnalysis: AIAnalysis? = null,
    val isAnalyzing: Boolean = false,
    val description: String = "",
    val submittedComplaintId: String? = null
)

enum class ReportStep {
    CATEGORY, PHOTO, LOCATION, AI_ANALYSIS, DETAILS, REVIEW, SUBMITTED
}

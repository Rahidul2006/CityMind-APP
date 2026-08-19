package com.example.citymind.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
        _uiState.update { it.copy(imageUri = uri, isCapturingLocation = true, isUploadingProof = true) }
        captureLocation()
    }

    private fun captureLocation() {
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Starting GPS capture...")
                val location = locationService.getCurrentLocation()
                _uiState.update {
                    it.copy(
                        capturedLocation = location,
                        reportedLocation = location,
                        isCapturingLocation = false,
                        isUploadingProof = false,
                        currentStep = ReportStep.LOCATION
                    )
                }
                Log.d("ReportViewModel", "GPS capture finished. Moving to LOCATION step.")
            } catch (t: Throwable) {
                Log.e("ReportViewModel", "Critical error in captureLocation", t)
                _uiState.update { 
                    it.copy(
                        isCapturingLocation = false, 
                        isUploadingProof = false,
                        errorMessage = "GPS Error: ${t.localizedMessage}. Please try again." 
                    )
                }
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

    fun submitComplaint(context: Context) {
        val state = _uiState.value
        val uri = state.imageUri ?: return

        _uiState.update {
            it.copy(
                isSubmitting = true,
                submitStatusMessage = "Uploading evidence to CityMind...",
                errorMessage = null
            )
        }

        val appContext = context.applicationContext
        viewModelScope.launch {
            val capLoc = state.capturedLocation ?: LocationData(0.0, 0.0)
            val repLoc = state.reportedLocation ?: capLoc

            val result = repository.createComplaint(
                context = appContext,
                imageUri = uri,
                category = state.category ?: "Civic Issue",
                description = state.description,
                latitude = capLoc.latitude,
                longitude = capLoc.longitude,
                gpsAccuracy = capLoc.accuracy ?: 5f,
                capturedAt = capLoc.timestamp ?: System.currentTimeMillis(),
                reportedLatitude = repLoc.latitude,
                reportedLongitude = repLoc.longitude,
                locationSource = state.locationSource,
                address = "GPS Coordinates: ${repLoc.latitude}, ${repLoc.longitude}"
            )

            result.onSuccess { createdComplaint ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submittedComplaintId = createdComplaint.complaintId,
                        currentStep = ReportStep.SUBMITTED
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.localizedMessage ?: "Unable to submit complaint. Please check your internet connection and try again."
                    )
                }
            }
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
    val isUploadingProof: Boolean = false,
    val aiAnalysis: AIAnalysis? = null,
    val isAnalyzing: Boolean = false,
    val description: String = "",
    val isSubmitting: Boolean = false,
    val submitStatusMessage: String = "",
    val errorMessage: String? = null,
    val submittedComplaintId: String? = null
)

enum class ReportStep {
    CATEGORY, PHOTO, LOCATION, AI_ANALYSIS, DETAILS, REVIEW, SUBMITTED
}

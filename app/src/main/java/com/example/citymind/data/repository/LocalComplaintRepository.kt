package com.example.citymind.data.repository

import android.content.Context
import android.net.Uri
import com.example.citymind.models.AIAnalysis
import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import com.example.citymind.models.LocationData
import com.example.citymind.models.StatusHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class LocalComplaintRepository : ComplaintRepository {
    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())

    override fun getComplaints(): Flow<List<Complaint>> = _complaints

    override fun getComplaintById(id: String): Flow<Complaint?> =
        _complaints.map { list -> list.find { it.complaintId == id } }

    override suspend fun createComplaint(
        context: Context,
        imageUri: Uri,
        category: String,
        description: String,
        latitude: Double,
        longitude: Double,
        gpsAccuracy: Float,
        capturedAt: Long,
        reportedLatitude: Double,
        reportedLongitude: Double,
        locationSource: String,
        address: String
    ): Result<Complaint> {
        val complaint = Complaint(
            complaintId = "CM-LOCAL-${(100000..999999).random()}",
            category = category,
            description = description,
            imageUri = imageUri.toString(),
            capturedLocation = LocationData(latitude, longitude, gpsAccuracy, capturedAt),
            reportedLocation = LocationData(reportedLatitude, reportedLongitude),
            locationSource = locationSource,
            address = address,
            aiAnalysis = AIAnalysis(category, 0.95f, "HIGH", "MEDIUM", "NORMAL"),
            status = ComplaintStatus.SUBMITTED,
            statusHistory = listOf(StatusHistory(ComplaintStatus.SUBMITTED, System.currentTimeMillis(), "Local complaint created")),
            createdAt = System.currentTimeMillis()
        )
        _complaints.update { it + complaint }
        return Result.success(complaint)
    }

    override suspend fun updateComplaintStatus(id: String, status: ComplaintStatus, note: String?) {
        _complaints.update { list ->
            list.map {
                if (it.complaintId == id) {
                    val newHistory = it.statusHistory + StatusHistory(status, System.currentTimeMillis(), note)
                    it.copy(status = status, statusHistory = newHistory)
                } else it
            }
        }
    }

    override suspend fun verifyResolution(id: String, verified: Boolean, message: String): Result<Complaint> {
        var updatedItem: Complaint? = null
        _complaints.update { list ->
            list.map {
                if (it.complaintId == id) {
                    val item = if (verified) {
                        it.copy(resolutionVerified = true)
                    } else {
                        val newHistory = it.statusHistory + StatusHistory(ComplaintStatus.REOPENED, System.currentTimeMillis(), message.ifEmpty { "Citizen reported issue still exists" })
                        it.copy(status = ComplaintStatus.REOPENED, statusHistory = newHistory)
                    }
                    updatedItem = item
                    item
                } else it
            }
        }
        return updatedItem?.let { Result.success(it) } ?: Result.failure(Exception("Complaint not found"))
    }

    override suspend fun getNearbyComplaints(
        latitude: Double,
        longitude: Double,
        radius: Int
    ): Result<List<Complaint>> {
        return Result.success(_complaints.value)
    }
}

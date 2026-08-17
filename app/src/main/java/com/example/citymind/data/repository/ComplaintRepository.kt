package com.example.citymind.data.repository

import android.content.Context
import android.net.Uri
import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    fun getComplaints(): Flow<List<Complaint>>
    fun getComplaintById(id: String): Flow<Complaint?>
    suspend fun createComplaint(
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
    ): Result<Complaint>
    suspend fun updateComplaintStatus(id: String, status: ComplaintStatus, note: String? = null)
    suspend fun verifyResolution(id: String, verified: Boolean, message: String = ""): Result<Complaint>
    suspend fun getNearbyComplaints(latitude: Double, longitude: Double, radius: Int = 5000): Result<List<Complaint>>
}

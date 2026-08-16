package com.example.citymind.data.repository

import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    fun getComplaints(): Flow<List<Complaint>>
    fun getComplaintById(id: String): Flow<Complaint?>
    suspend fun createComplaint(complaint: Complaint)
    suspend fun updateComplaintStatus(id: String, status: ComplaintStatus, note: String? = null)
    suspend fun verifyResolution(id: String, verified: Boolean)
}

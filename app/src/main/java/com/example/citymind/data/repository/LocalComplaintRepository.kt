package com.example.citymind.data.repository

import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
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

    override suspend fun createComplaint(complaint: Complaint) {
        _complaints.update { it + complaint }
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

    override suspend fun verifyResolution(id: String, verified: Boolean) {
        _complaints.update { list ->
            list.map {
                if (it.complaintId == id) {
                    if (verified) {
                        it.copy(resolutionVerified = true)
                    } else {
                        val newHistory = it.statusHistory + StatusHistory(ComplaintStatus.REOPENED, System.currentTimeMillis(), "Citizen reported issue still exists")
                        it.copy(status = ComplaintStatus.REOPENED, statusHistory = newHistory)
                    }
                } else it
            }
        }
    }
}

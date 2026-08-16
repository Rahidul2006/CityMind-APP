package com.example.citymind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citymind.data.repository.ComplaintRepository
import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComplaintViewModel(
    private val repository: ComplaintRepository
) : ViewModel() {

    val complaints: StateFlow<List<Complaint>> = repository.getComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getComplaintById(id: String) = repository.getComplaintById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateStatus(id: String, status: ComplaintStatus, note: String? = null) {
        viewModelScope.launch {
            repository.updateComplaintStatus(id, status, note)
        }
    }

    fun verifyResolution(id: String, verified: Boolean) {
        viewModelScope.launch {
            repository.verifyResolution(id, verified)
        }
    }
}

package com.example.citymind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citymind.data.repository.ComplaintRepository
import com.example.citymind.models.Complaint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ComplaintViewModel(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    val complaints: StateFlow<List<Complaint>> = repository.getComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getComplaintById(id: String): StateFlow<Complaint?> {
        return repository.getComplaintById(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun verifyResolution(id: String, verified: Boolean, note: String = "") {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.verifyResolution(id, verified, note)
            result.onSuccess {
                _verificationMessage.value = if (verified) "Resolution confirmed!" else "Complaint reopened!"
            }.onFailure { err ->
                _verificationMessage.value = err.localizedMessage ?: "Failed to verify resolution"
            }
            _isRefreshing.value = false
        }
    }

    fun clearVerificationMessage() {
        _verificationMessage.value = null
    }
}

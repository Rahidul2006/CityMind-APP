package com.example.citymind.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citymind.data.remote.SocketManager
import com.example.citymind.data.repository.ComplaintRepository
import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import com.example.citymind.models.StatusHistory
import com.example.citymind.models.toDomainModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ComplaintViewModel(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    private val _currentDetailComplaint = MutableStateFlow<Complaint?>(null)
    val currentDetailComplaint: StateFlow<Complaint?> = _currentDetailComplaint.asStateFlow()

    private var activeObserveJob: Job? = null
    private var fallbackPollingJob: Job? = null
    private var activeComplaintId: String? = null

    val complaints: StateFlow<List<Complaint>> = repository.getComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getComplaintById(id: String): StateFlow<Complaint?> {
        return repository.getComplaintById(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun startListeningToComplaint(id: String) {
        if (activeComplaintId == id && activeObserveJob?.isActive == true) {
            return
        }

        stopListeningToComplaint()

        activeComplaintId = id
        Log.d("ComplaintViewModel", "[SOCKET] Starting real-time tracking for complaint: $id")

        // 1. Fetch initial REST data
        viewModelScope.launch {
            repository.getComplaintById(id).collect { fetched ->
                if (fetched != null) {
                    _currentDetailComplaint.value = fetched
                }
            }
        }

        // 2. Connect Socket and join room
        SocketManager.joinComplaintRoom(id)

        // 3. Listen for Socket.IO updates
        activeObserveJob = viewModelScope.launch {
            SocketManager.observeStatusUpdates().collect { payload ->
                Log.d("ComplaintViewModel", "[SOCKET] Status update event received for ${payload.complaintId}: ${payload.status}")
                if (payload.complaintId == id || payload.complaintId == _currentDetailComplaint.value?.complaintId) {
                    if (payload.complaint != null) {
                        _currentDetailComplaint.value = payload.complaint.toDomainModel()
                    } else {
                        // Carefully update current local state
                        _currentDetailComplaint.update { current ->
                            if (current == null) null
                            else {
                                val newStatus = ComplaintStatus.fromString(payload.status)
                                val newHistoryEntry = StatusHistory(
                                    status = newStatus,
                                    timestamp = System.currentTimeMillis(),
                                    note = payload.message ?: "Status changed to ${payload.status}"
                                )
                                val updatedHistory = current.statusHistory + newHistoryEntry
                                current.copy(
                                    status = newStatus,
                                    statusHistory = updatedHistory
                                )
                            }
                        }

                        // Re-fetch via REST to guarantee synchronization with MongoDB
                        refreshCurrentComplaintFromRest(id)
                    }
                }
            }
        }

        // 4. Fallback Polling mechanism if Socket.IO is disconnected
        startFallbackPolling(id)
    }

    private fun startFallbackPolling(id: String) {
        fallbackPollingJob?.cancel()
        fallbackPollingJob = viewModelScope.launch {
            while (activeComplaintId == id) {
                delay(20000) // 20-second interval fallback
                if (!SocketManager.isConnected()) {
                    Log.d("ComplaintViewModel", "[REST FALLBACK] Socket disconnected, polling REST for $id...")
                    refreshCurrentComplaintFromRest(id)
                }
            }
        }
    }

    private fun refreshCurrentComplaintFromRest(id: String) {
        viewModelScope.launch {
            repository.getComplaintById(id).collect { fresh ->
                if (fresh != null) {
                    _currentDetailComplaint.value = fresh
                }
            }
        }
    }

    fun stopListeningToComplaint() {
        activeComplaintId?.let { id ->
            Log.d("ComplaintViewModel", "[SOCKET] Leaving room for complaint: $id")
            SocketManager.leaveComplaintRoom(id)
        }
        activeComplaintId = null
        activeObserveJob?.cancel()
        activeObserveJob = null
        fallbackPollingJob?.cancel()
        fallbackPollingJob = null
    }

    fun verifyResolution(id: String, verified: Boolean, note: String = "") {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.verifyResolution(id, verified, note)
            result.onSuccess { updated ->
                _currentDetailComplaint.value = updated
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

    override fun onCleared() {
        super.onCleared()
        stopListeningToComplaint()
        SocketManager.disconnect()
    }
}

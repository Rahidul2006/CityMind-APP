package com.example.citymind

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.citymind.data.repository.LocalComplaintRepository
import com.example.citymind.services.LocationService
import com.example.citymind.services.MockAIService
import com.example.citymind.ui.MainScreen
import com.example.citymind.ui.theme.CitymindTheme
import com.example.citymind.viewmodel.ComplaintViewModel
import com.example.citymind.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permissions early for prototype convenience
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }
        
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        // Manual DI for prototype
        // In a real app, these would be singletons or provided by Hilt
        val repository = LocalComplaintRepository()
        val locationService = LocationService(this)
        val aiService = MockAIService()
        
        val reportViewModel = ReportViewModel(repository, locationService, aiService)
        val complaintViewModel = ComplaintViewModel(repository)

        setContent {
            CitymindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(reportViewModel, complaintViewModel)
                }
            }
        }
    }
}

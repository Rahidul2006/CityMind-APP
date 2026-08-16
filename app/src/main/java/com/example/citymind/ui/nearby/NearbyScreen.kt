package com.example.citymind.ui.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.citymind.viewmodel.ComplaintViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    viewModel: ComplaintViewModel
) {
    val complaints by viewModel.complaints.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nearby Issues") })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.LightGray)) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nearby Issues Map View")
                Text("Showing ${complaints.size} reports nearby")
            }
        }
    }
}

package com.example.citymind.ui.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.citymind.ui.home.ComplaintCard
import com.example.citymind.ui.navigation.Screen
import com.example.citymind.viewmodel.ComplaintViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(
    navController: NavController,
    viewModel: ComplaintViewModel
) {
    val complaints by viewModel.complaints.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Complaints") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (complaints.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You haven't reported any issues yet.")
                    }
                }
            } else {
                items(complaints.size) { index ->
                    val complaint = complaints[index]
                    ComplaintCard(complaint) {
                        navController.navigate(Screen.ComplaintDetail.createRoute(complaint.complaintId))
                    }
                }
            }
        }
    }
}

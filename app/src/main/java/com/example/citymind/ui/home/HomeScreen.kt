package com.example.citymind.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.citymind.ui.navigation.Screen
import com.example.citymind.viewmodel.ComplaintViewModel
import com.example.citymind.models.ComplaintStatus

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ComplaintViewModel
) {
    val complaints by viewModel.complaints.collectAsState()
    
    val resolvedCount = complaints.count { it.status == ComplaintStatus.RESOLVED }
    val inProgressCount = complaints.count { it.status == ComplaintStatus.IN_PROGRESS || it.status == ComplaintStatus.ASSIGNED }
    val submittedCount = complaints.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Header()
        }
        
        item {
            ReportCTA {
                navController.navigate(Screen.Report.route)
            }
        }
        
        item {
            Statistics(submittedCount, resolvedCount, inProgressCount)
        }
        
        item {
            Text(
                text = "Recent Complaints",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (complaints.isEmpty()) {
            item {
                Text("No complaints yet. Report your first issue!")
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

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CITYMIND AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Report. Track. Improve Your City.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        }
    }
}

@Composable
fun ReportCTA(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.Warning, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "🚨 Report a Civic Issue",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Statistics(submitted: Int, resolved: Int, inProgress: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Reports", submitted.toString(), Modifier.weight(1f))
        StatCard("Resolved", resolved.toString(), Modifier.weight(1f))
        StatCard("In Progress", inProgress.toString(), Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ComplaintCard(complaint: com.example.citymind.models.Complaint, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                // Icon based on category or placeholder
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = complaint.category, fontWeight = FontWeight.Bold)
                Text(text = complaint.address, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Status: ${complaint.status}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

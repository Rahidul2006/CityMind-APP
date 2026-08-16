package com.example.citymind.ui.complaints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.citymind.models.ComplaintStatus
import com.example.citymind.viewmodel.ComplaintViewModel
import com.example.citymind.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    id: String,
    navController: NavController,
    viewModel: ComplaintViewModel
) {
    val complaint by viewModel.getComplaintById(id).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        complaint?.let { c ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                StatusBadge(c.status)
                Spacer(Modifier.height(8.dp))
                Text(text = c.complaintId, style = MaterialTheme.typography.labelLarge, color = Blue)
                Text(text = c.category, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Issue Photo")
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Description", fontWeight = FontWeight.Bold)
                Text(c.description)
                
                Spacer(Modifier.height(24.dp))
                
                Text("Location", fontWeight = FontWeight.Bold)
                Text(c.address)
                Surface(
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.LightGray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Map: ${c.reportedLocation.latitude}, ${c.reportedLocation.longitude}")
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Status Timeline", fontWeight = FontWeight.Bold)
                StatusTimeline(c.statusHistory)
                
                Spacer(Modifier.height(32.dp))
                
                if (c.status == ComplaintStatus.RESOLVED && !c.resolutionVerified) {
                    ResolutionVerificationCard(
                        onVerified = { viewModel.verifyResolution(c.complaintId, true) },
                        onNotResolved = { viewModel.verifyResolution(c.complaintId, false) }
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                DemoModePanel(
                    onStatusChange = { status -> viewModel.updateStatus(c.complaintId, status) }
                )
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun StatusBadge(status: ComplaintStatus) {
    val color = when (status) {
        ComplaintStatus.RESOLVED -> ResolutionGreen
        ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED -> Blue
        ComplaintStatus.SUBMITTED, ComplaintStatus.VERIFIED -> Navy
        ComplaintStatus.REOPENED -> WarningOrange
        ComplaintStatus.REJECTED -> DangerRed
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun StatusTimeline(history: List<com.example.citymind.models.StatusHistory>) {
    Column {
        history.forEachIndexed { index, item ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Blue
                    ) {}
                    if (index < history.size - 1) {
                        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Blue))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(item.status.name, fontWeight = FontWeight.Bold)
                    Text(item.note ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ResolutionVerificationCard(onVerified: () -> Unit, onNotResolved: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("The municipality has marked this issue as resolved.", fontWeight = FontWeight.Bold)
            Text("Is the issue actually fixed?", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onVerified, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ResolutionGreen)) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("YES")
                }
                Button(onClick = onNotResolved, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("NO")
                }
            }
        }
    }
}

@Composable
fun DemoModePanel(onStatusChange: (ComplaintStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DEMO MODE — simulated municipal updates", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DemoButton("SUBMITTED", { onStatusChange(ComplaintStatus.SUBMITTED) }, Modifier.weight(1f))
                DemoButton("VERIFIED", { onStatusChange(ComplaintStatus.VERIFIED) }, Modifier.weight(1f))
                DemoButton("ASSIGNED", { onStatusChange(ComplaintStatus.ASSIGNED) }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DemoButton("IN PROGRESS", { onStatusChange(ComplaintStatus.IN_PROGRESS) }, Modifier.weight(1f))
                DemoButton("RESOLVED", { onStatusChange(ComplaintStatus.RESOLVED) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DemoButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(label, fontSize = 10.sp)
    }
}

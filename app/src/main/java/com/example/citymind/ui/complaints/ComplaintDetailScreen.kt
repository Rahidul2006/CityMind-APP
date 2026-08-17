package com.example.citymind.ui.complaints

import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.citymind.models.ComplaintStatus
import com.example.citymind.models.StatusHistory
import com.example.citymind.viewmodel.ComplaintViewModel
import com.example.citymind.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    id: String,
    navController: NavController,
    viewModel: ComplaintViewModel
) {
    val complaint by viewModel.getComplaintById(id).collectAsState()
    val verificationMsg by viewModel.verificationMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(verificationMsg) {
        verificationMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearVerificationMessage()
        }
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(c.status)
                    Text(
                        text = "Department: ${c.departmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(text = c.complaintId, style = MaterialTheme.typography.labelLarge, color = Blue, fontWeight = FontWeight.Bold)
                Text(text = c.category, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Cloudinary Photo Card
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (!c.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = c.imageUri,
                            contentDescription = "Cloudinary Complaint Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Available", color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Description", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(c.description, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(20.dp))

                Text("Location", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(c.address, style = MaterialTheme.typography.bodyMedium)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Captured GPS: ${c.capturedLocation.latitude}, ${c.capturedLocation.longitude}", fontWeight = FontWeight.SemiBold)
                        Text("Reported Location: ${c.reportedLocation.latitude}, ${c.reportedLocation.longitude}")
                        Text("Source: ${c.locationSource}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Status History & Timeline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                StatusTimeline(c.statusHistory)

                Spacer(Modifier.height(28.dp))

                // Resolution Verification Card when RESOLVED
                if (c.status == ComplaintStatus.RESOLVED) {
                    ResolutionVerificationCard(
                        isAlreadyVerified = c.resolutionVerified,
                        onVerified = {
                            viewModel.verifyResolution(c.complaintId, true, "Issue has been fixed by citizen verification.")
                        },
                        onNotResolved = {
                            viewModel.verifyResolution(c.complaintId, false, "Issue still exists. Reopening complaint.")
                        }
                    )
                }
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
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusTimeline(history: List<StatusHistory>) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    Column {
        history.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(14.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (item.status == ComplaintStatus.RESOLVED) ResolutionGreen else Blue
                    ) {}
                    if (index < history.size - 1) {
                        Box(modifier = Modifier.width(2.dp).height(48.dp).background(Color.LightGray))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.status.name.replace("_", " "), fontWeight = FontWeight.Bold)
                        Text(sdf.format(Date(item.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    if (!item.note.isNullOrEmpty()) {
                        Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ResolutionVerificationCard(
    isAlreadyVerified: Boolean,
    onVerified: () -> Unit,
    onNotResolved: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlreadyVerified) ResolutionGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isAlreadyVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = ResolutionGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Verified Resolved by Citizen", fontWeight = FontWeight.Bold, color = ResolutionGreen)
                }
            } else {
                Text("The municipality has marked this issue as resolved.", fontWeight = FontWeight.Bold)
                Text("Is the issue actually fixed?", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onVerified,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ResolutionGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("YES (FIXED)")
                    }
                    Button(
                        onClick = onNotResolved,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("NO (REOPEN)")
                    }
                }
            }
        }
    }
}

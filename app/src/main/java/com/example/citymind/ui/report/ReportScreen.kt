package com.example.citymind.ui.report

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.citymind.ui.navigation.Screen
import com.example.citymind.viewmodel.ReportViewModel
import com.example.citymind.viewmodel.ReportStep
import com.example.citymind.models.LocationData
import com.example.citymind.ui.components.MapLibreView
import com.example.citymind.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Reset the report state when entering this screen to ensure a fresh start
    LaunchedEffect(Unit) {
        if (uiState.currentStep == ReportStep.SUBMITTED) {
            viewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Issue") },
                navigationIcon = {
                    if (uiState.currentStep != ReportStep.CATEGORY && uiState.currentStep != ReportStep.SUBMITTED) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Crossfade(targetState = uiState.currentStep, label = "ReportStep") { step ->
                when (step) {
                    ReportStep.CATEGORY -> CategoryStep(onCategorySelected = viewModel::onCategorySelected)
                    ReportStep.PHOTO -> PhotoStep(onPhotoCaptured = viewModel::onPhotoCaptured)
                    ReportStep.LOCATION -> LocationStep(
                        capturedLocation = uiState.capturedLocation,
                        reportedLocation = uiState.reportedLocation,
                        address = uiState.address,
                        isCapturing = uiState.isCapturingLocation,
                        onLocationAdjusted = viewModel::onLocationAdjusted,
                        onProceed = viewModel::proceedFromLocation
                    )
                    ReportStep.AI_ANALYSIS -> AIStep(isAnalyzing = uiState.isAnalyzing)
                    ReportStep.DETAILS -> DetailsStep(
                        description = uiState.description,
                        onDescriptionChanged = viewModel::onDescriptionChanged,
                        onProceed = viewModel::proceedToReview
                    )
                    ReportStep.REVIEW -> ReviewStep(
                        uiState = uiState,
                        onSubmit = { viewModel.submitComplaint(context) }
                    )
                    ReportStep.SUBMITTED -> SuccessStep(
                        complaintId = uiState.submittedComplaintId ?: "",
                        onBackToHome = {
                            viewModel.reset()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Cloudinary Upload Animation (Post-Capture)
            if (uiState.isUploadingProof) {
                CloudinaryUploadOverlay()
            }

            // Loading overlay during Cloudinary upload & MongoDB creation
            if (uiState.isSubmitting) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(24.dp))
                        Text(
                            uiState.submitStatusMessage.ifEmpty { "Submitting complaint to CityMind Backend..." },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error dialog / banner
            uiState.errorMessage?.let { error ->
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Submission Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.proceedToReview() }) {
                            Text("Retry")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryStep(onCategorySelected: (String) -> Unit) {
    val categories = listOf(
        "Pothole" to Icons.Default.Warning,
        "Garbage Overflow" to Icons.Default.Delete,
        "Broken Streetlight" to Icons.Default.Info,
        "Water Leakage" to Icons.Default.Build,
        "Blocked Drain" to Icons.Default.Menu,
        "Road Damage" to Icons.Default.Place,
        "Fallen Tree" to Icons.Default.Close,
        "Traffic Signal Issue" to Icons.Default.Refresh,
        "Other" to Icons.Default.MoreVert
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Issue Category", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories.size) { index ->
                val (title, icon) = categories[index]
                CategoryCard(title, icon) { onCategorySelected(title) }
            }
        }
    }
}

@Composable
fun CategoryCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PhotoStep(onPhotoCaptured: (Uri) -> Unit) {
    var isUsingCamera by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPhotoCaptured(it) }
    }

    if (isUsingCamera) {
        CameraCapture(
            onImageCaptured = { file ->
                onPhotoCaptured(Uri.fromFile(file))
                isUsingCamera = false
            },
            onError = { isUsingCamera = false }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Capture Proof", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Take a clear photo of the issue.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { isUsingCamera = true },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("📷 Capture Issue")
            }
            TextButton(onClick = { launcher.launch("image/*") }) {
                Text("Choose From Gallery")
            }
        }
    }
}

@Composable
fun LocationStep(
    capturedLocation: LocationData?,
    reportedLocation: LocationData?,
    address: String,
    isCapturing: Boolean,
    onLocationAdjusted: (LocationData) -> Unit,
    onProceed: () -> Unit
) {
    val initialLocation = reportedLocation ?: LocationData(0.0, 0.0)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Where did you find the problem?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Location helps the maintenance team find the reported problem accurately.", style = MaterialTheme.typography.bodySmall, color = Blue)
        Spacer(Modifier.height(12.dp))

        if (isCapturing) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                    Spacer(Modifier.height(24.dp))
                    Text("Fetching GPS Location...", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Ensuring high accuracy for reporting...", color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                MapLibreView(
                    modifier = Modifier.fillMaxSize(),
                    latitude = initialLocation.latitude,
                    longitude = initialLocation.longitude,
                    onLocationChanged = { lat, lng ->
                        onLocationAdjusted(initialLocation.copy(latitude = lat, longitude = lng))
                    }
                )
            }

            if (address.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            LocationInfoCard(reportedLocation ?: capturedLocation)

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { capturedLocation?.let { onLocationAdjusted(it) } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Current GPS")
                }
                Button(
                    onClick = onProceed,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LocationInfoCard(location: LocationData?) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Blue)
                Spacer(Modifier.width(8.dp))
                Text("📍 REAL GPS Coordinates Captured", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Latitude: ${location?.latitude ?: "N/A"}")
            Text("Longitude: ${location?.longitude ?: "N/A"}")
            Text("GPS Accuracy: ${location?.accuracy?.toInt() ?: "N/A"} meters")
        }
    }
}

@Composable
fun AIStep(isAnalyzing: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isAnalyzing) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp), color = Blue, strokeWidth = 6.dp)
            Spacer(Modifier.height(32.dp))
            Text("🤖 AI Analysis in Progress...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Analyzing severity & recommended priority", style = MaterialTheme.typography.bodyMedium)
            Text("Verifying evidence on Cloudinary...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CloudinaryUploadOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                    color = Blue
                )
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Blue
                )
            }
            Spacer(Modifier.height(32.dp))
            Text("Processing Evidence", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Compressing and preparing proof...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DetailsStep(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Describe the Issue",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                placeholder = { Text("Example: Deep pothole causing traffic congestion near main road.") },
                label = { Text("Description Details") }
            )
        }

        // Action button at the bottom
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = description.isNotBlank()
        ) {
            Text("Review Report")
        }
    }
}

@Composable
fun ReviewStep(uiState: com.example.citymind.viewmodel.ReportUiState, onSubmit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Review Your Report", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                ReviewItem("Category", uiState.category ?: "N/A")
                ReviewItem("Coordinates", "${uiState.reportedLocation?.latitude}, ${uiState.reportedLocation?.longitude}")
                ReviewItem("GPS Accuracy", "${uiState.reportedLocation?.accuracy?.toInt()}m")
                ReviewItem("AI Category", uiState.aiAnalysis?.detectedIssue ?: "N/A")
                ReviewItem("AI Severity", uiState.aiAnalysis?.severity ?: "HIGH")
                ReviewItem("Recommended Priority", uiState.aiAnalysis?.recommendedPriority ?: "NORMAL")
                Spacer(Modifier.height(16.dp))
                Text("Description:", fontWeight = FontWeight.Bold)
                Text(uiState.description)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SUBMIT TO CITYMIND BACKEND")
        }
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SuccessStep(complaintId: String, onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(100.dp), tint = ResolutionGreen)
        Spacer(Modifier.height(24.dp))
        Text("Complaint Submitted Successfully 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Backend Complaint ID", style = MaterialTheme.typography.labelMedium)
                Text(complaintId, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Blue)
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to My Complaints")
        }
    }
}

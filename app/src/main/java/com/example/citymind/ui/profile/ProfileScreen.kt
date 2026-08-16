package com.example.citymind.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Citizen Profile") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Text("John Citizen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Bengaluru, KA", color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileStat("Reports", "12")
                ProfileStat("Resolved", "8")
                ProfileStat("Points", "450")
            }
            
            Spacer(Modifier.height(32.dp))
            
            HorizontalDivider()
            
            ProfileMenuItem("My Account")
            ProfileMenuItem("Notification Settings")
            ProfileMenuItem("Help & Support")
            ProfileMenuItem("About CityMind AI")
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ProfileMenuItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO */ },
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

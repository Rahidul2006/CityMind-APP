# CityMind AI - Citizen App Walkthrough

CityMind AI is a professional native Android application designed for citizens to report and track civic issues transparently. This prototype implements the complete reporting-to-resolution loop using local state and mock services.

## Key Features Implemented

### 1. Professional Dashboard
- **Header**: Branding with "Report. Track. Improve Your City." tagline.
- **Main CTA**: Prominent "🚨 Report a Civic Issue" button.
- **Statistics**: Summary of Submitted, Resolved, and In Progress reports.
- **Recent Complaints**: Quick access to the latest reports.

### 2. Multi-Step Reporting Flow
- **Step 1: Select Issue**: High-quality category selection cards (Pothole, Garbage, etc.).
- **Step 2: Capture Photo**: Integrated **CameraX** for real-time evidence capture and gallery selection.
- **Step 3: Real GPS Capture**: Automated capture of Latitude, Longitude, and Accuracy using `FusedLocationProviderClient`.
- **Step 4: Location Verification**: Interactive map marker with support for manual adjustment (storing both captured and reported locations).
- **Step 5: AI Analysis**: Simulated AI service that detects issue type, severity, and priority from the captured image.
- **Step 6: Description**: Detailed input for citizen observations.
- **Step 7: Review & Submit**: Comprehensive summary of the report before generating a unique Complaint ID (e.g., CM-2026-123456).

### 3. Complaint Tracking & Management
- **My Complaints**: List of all submitted issues with status badges.
- **Complaint Details**: Full view including original image, location details, AI analysis, and a professional status timeline.
- **Resolution Loop**: Citizens can verify if an issue is "Actually Fixed" when marked as Resolved by the municipality. Reopening is supported if the issue persists.
- **Demo Mode**: Built-in status simulation tools for hackathon presentations to demonstrate the full lifecycle (Submitted -> Verified -> Assigned -> In Progress -> Resolved).

## Technical Architecture
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository Pattern
- **Persistence**: Reactive local state management via `StateFlow` (ready for Room/DataStore integration).
- **Navigation**: Type-safe Navigation Compose.
- **Services**:
    - `LocationService`: Handles permissions and high-accuracy GPS capture.
    - `CameraCapture`: CameraX implementation for proof capture.
    - `MockAIService`: Simulates on-device or cloud-based image analysis.

## Verification
- **Build**: Successfully compiled using Gradle (`app:assembleDebug`).
- **Dependencies**: Modern AndroidX, CameraX, Google Play Services, Coil, and Kotlinx Serialization.
- **Permissions**: Properly declared in `AndroidManifest.xml` and requested at runtime.

CityMind AI is ready for demonstration as a complete frontend-only prototype that solves the "Reporting to Resolution" gap in civic technology.

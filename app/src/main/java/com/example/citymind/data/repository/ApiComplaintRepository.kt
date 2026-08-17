package com.example.citymind.data.repository

import android.content.Context
import android.net.Uri
import com.example.citymind.data.remote.RetrofitClient
import com.example.citymind.data.remote.dtos.ResolutionVerificationRequest
import com.example.citymind.models.Complaint
import com.example.citymind.models.ComplaintStatus
import com.example.citymind.models.toDomainModel
import com.example.citymind.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ApiComplaintRepository : ComplaintRepository {

    private val apiService = RetrofitClient.apiService

    override fun getComplaints(): Flow<List<Complaint>> = flow {
        try {
            val response = apiService.getComplaints()
            if (response.isSuccessful) {
                val dtoList = response.body()?.complaints ?: response.body()?.data ?: emptyList()
                val domainList = dtoList.map { it.toDomainModel() }
                emit(domainList)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun getComplaintById(id: String): Flow<Complaint?> = flow {
        try {
            val response = apiService.getComplaintById(id)
            if (response.isSuccessful) {
                val dto = response.body()?.complaint ?: response.body()?.data
                emit(dto?.toDomainModel())
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createComplaint(
        context: Context,
        imageUri: Uri,
        category: String,
        description: String,
        latitude: Double,
        longitude: Double,
        gpsAccuracy: Float,
        capturedAt: Long,
        reportedLatitude: Double,
        reportedLongitude: Double,
        locationSource: String,
        address: String
    ): Result<Complaint> {
        return try {
            val compressedFile = ImageCompressor.compressImage(context, imageUri)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", compressedFile.name, requestFile)

            val textMediaType = "text/plain".toMediaTypeOrNull()

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val isoCapturedAt = sdf.format(Date(capturedAt))

            val categoryBody = category.toRequestBody(textMediaType)
            val descBody = description.toRequestBody(textMediaType)
            val latBody = latitude.toString().toRequestBody(textMediaType)
            val lngBody = longitude.toString().toRequestBody(textMediaType)
            val accBody = gpsAccuracy.toString().toRequestBody(textMediaType)
            val capAtBody = isoCapturedAt.toRequestBody(textMediaType)
            val repLatBody = reportedLatitude.toString().toRequestBody(textMediaType)
            val repLngBody = reportedLongitude.toString().toRequestBody(textMediaType)
            val locSourceBody = locationSource.toRequestBody(textMediaType)
            val addressBody = address.toRequestBody(textMediaType)

            val response = apiService.createComplaint(
                image = imagePart,
                category = categoryBody,
                description = descBody,
                latitude = latBody,
                longitude = lngBody,
                gpsAccuracy = accBody,
                capturedAt = capAtBody,
                reportedLatitude = repLatBody,
                reportedLongitude = repLngBody,
                locationSource = locSourceBody,
                address = addressBody
            )

            if (response.isSuccessful) {
                val dto = response.body()?.complaint ?: response.body()?.data
                if (dto != null) {
                    Result.success(dto.toDomainModel())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Complaint creation failed."))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Server returned error ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateComplaintStatus(id: String, status: ComplaintStatus, note: String?) {
        try {
            val req = com.example.citymind.data.remote.dtos.StatusUpdateRequest(
                status = status.name,
                message = note ?: "Status updated to ${status.name}"
            )
            apiService.updateComplaintStatus(id, req)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun verifyResolution(id: String, verified: Boolean, message: String): Result<Complaint> {
        return try {
            val req = ResolutionVerificationRequest(resolved = verified, message = message)
            val response = apiService.verifyResolution(id, req)
            if (response.isSuccessful) {
                val dto = response.body()?.complaint ?: response.body()?.data
                if (dto != null) {
                    Result.success(dto.toDomainModel())
                } else {
                    Result.failure(Exception("Verification failed"))
                }
            } else {
                Result.failure(Exception("Server returned error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNearbyComplaints(
        latitude: Double,
        longitude: Double,
        radius: Int
    ): Result<List<Complaint>> {
        return try {
            val response = apiService.getNearbyComplaints(latitude, longitude, radius)
            if (response.isSuccessful) {
                val dtoList = response.body()?.complaints ?: response.body()?.data ?: emptyList()
                Result.success(dtoList.map { it.toDomainModel() })
            } else {
                Result.failure(Exception("Failed to fetch nearby complaints (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

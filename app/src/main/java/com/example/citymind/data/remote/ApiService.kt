package com.example.citymind.data.remote

import com.example.citymind.data.remote.dtos.ApiResponse
import com.example.citymind.data.remote.dtos.ComplaintDto
import com.example.citymind.data.remote.dtos.ResolutionVerificationRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("complaints")
    suspend fun createComplaint(
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("gpsAccuracy") gpsAccuracy: RequestBody,
        @Part("capturedAt") capturedAt: RequestBody,
        @Part("reportedLatitude") reportedLatitude: RequestBody,
        @Part("reportedLongitude") reportedLongitude: RequestBody,
        @Part("locationSource") locationSource: RequestBody,
        @Part("address") address: RequestBody
    ): Response<ApiResponse<ComplaintDto>>

    @GET("complaints")
    suspend fun getComplaints(): Response<ApiResponse<List<ComplaintDto>>>

    @GET("complaints/nearby")
    suspend fun getNearbyComplaints(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5000
    ): Response<ApiResponse<List<ComplaintDto>>>

    @GET("complaints/{id}")
    suspend fun getComplaintById(
        @Path("id") id: String
    ): Response<ApiResponse<ComplaintDto>>

    @POST("complaints/{id}/verify")
    suspend fun verifyResolution(
        @Path("id") id: String,
        @Body request: ResolutionVerificationRequest
    ): Response<ApiResponse<ComplaintDto>>

    @PATCH("complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Path("id") id: String,
        @Body request: com.example.citymind.data.remote.dtos.StatusUpdateRequest
    ): Response<ApiResponse<ComplaintDto>>
}

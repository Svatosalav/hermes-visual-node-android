package com.hermes.visualnode.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val message: String,
    val image: String? = null
)

data class ChatResponse(
    val response: String,
    val timestamp: Long
)

data class NodeRegistration(
    val node_id: String,
    val url: String
)

interface HermesApi {
    
    @POST("nodes/register")
    suspend fun registerNode(@Body registration: NodeRegistration): Response<Unit>
    
    @Multipart
    @POST("nodes/{nodeId}/screenshot")
    suspend fun uploadScreenshot(
        @Path("nodeId") nodeId: String,
        @Part image: MultipartBody.Part
    ): Response<Unit>
    
    @POST("chat")
    suspend fun sendChatMessage(@Body request: ChatRequest): Response<ChatResponse>
    
    @GET("chat/history")
    suspend fun getChatHistory(): Response<List<ChatMessage>>
}

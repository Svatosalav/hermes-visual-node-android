package com.hermes.visualnode.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.visualnode.api.ChatMessage
import com.hermes.visualnode.api.ChatRequest
import com.hermes.visualnode.api.HermesApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.Base64

class ChatViewModel : ViewModel() {
    
    val messages = mutableStateListOf<ChatMessage>()
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val serverUrl = mutableStateOf("http://192.168.1.100:8766")
    val nodeId = mutableStateOf("android-phone")
    
    private var api: HermesApi? = null
    
    fun initializeApi(url: String) {
        serverUrl.value = url
        api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HermesApi::class.java)
    }
    
    fun sendMessage(text: String, screenshot: Bitmap? = null) {
        if (text.isBlank() && screenshot == null) return
        
        viewModelScope.launch {
            try {
                isLoading.value = true
                error.value = null
                
                // Добавить сообщение пользователя
                messages.add(ChatMessage(role = "user", content = text))
                
                // Конвертировать скриншот в base64 если есть
                val imageBase64 = screenshot?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    Base64.getEncoder().encodeToString(stream.toByteArray())
                }
                
                // Отправить запрос
                val request = ChatRequest(message = text, image = imageBase64)
                val response = api?.sendChatMessage(request)
                
                if (response?.isSuccessful == true) {
                    val chatResponse = response.body()
                    chatResponse?.let {
                        messages.add(
                            ChatMessage(
                                role = "assistant",
                                content = it.response,
                                timestamp = it.timestamp
                            )
                        )
                    }
                } else {
                    error.value = "Ошибка: ${response?.code()}"
                }
                
            } catch (e: Exception) {
                error.value = "Ошибка: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun uploadScreenshot(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val imageBytes = stream.toByteArray()
                
                val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("image", "screenshot.jpg", requestBody)
                
                api?.uploadScreenshot(nodeId.value, part)
                
            } catch (e: Exception) {
                error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun registerNode() {
        viewModelScope.launch {
            try {
                val registration = com.hermes.visualnode.api.NodeRegistration(
                    node_id = nodeId.value,
                    url = "android://${nodeId.value}"
                )
                api?.registerNode(registration)
            } catch (e: Exception) {
                // Игнорируем ошибки регистрации
            }
        }
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            try {
                val response = api?.getChatHistory()
                if (response?.isSuccessful == true) {
                    response.body()?.let { history ->
                        messages.clear()
                        messages.addAll(history)
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибки загрузки истории
            }
        }
    }
    
    fun clearMessages() {
        messages.clear()
    }
}

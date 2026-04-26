package com.hermes.visualnode

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PhoneController(private val service: OverlayService) {
    
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    
    fun executeCommand(command: String, params: JSONObject, serverUrl: String) {
        Thread {
            try {
                val result = when (command) {
                    "click" -> performClick(params)
                    "swipe" -> performSwipe(params)
                    "type" -> performType(params)
                    "back" -> performBack()
                    "home" -> performHome()
                    "recents" -> performRecents()
                    "notifications" -> performNotifications()
                    "screenshot" -> performScreenshot()
                    "open_app" -> performOpenApp(params)
                    "find_text" -> performFindText(params)
                    "click_text" -> performClickText(params)
                    "get_screen_text" -> performGetScreenText()
                    else -> JSONObject().apply {
                        put("success", false)
                        put("error", "Unknown command: $command")
                    }
                }
                
                // Отправить результат на сервер
                sendResult(serverUrl, command, result)
                
            } catch (e: Exception) {
                val errorResult = JSONObject().apply {
                    put("success", false)
                    put("error", e.message)
                }
                sendResult(serverUrl, command, errorResult)
            }
        }.start()
    }
    
    private fun performClick(params: JSONObject): JSONObject {
        val x = params.getDouble("x").toFloat()
        val y = params.getDouble("y").toFloat()
        
        val accessibilityService = HermesAccessibilityService.instance
        val success = if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            accessibilityService.performClick(x, y)
        } else {
            false
        }
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Clicked at ($x, $y)" else "Accessibility service not available")
        }
    }
    
    private fun performSwipe(params: JSONObject): JSONObject {
        val startX = params.getDouble("start_x").toFloat()
        val startY = params.getDouble("start_y").toFloat()
        val endX = params.getDouble("end_x").toFloat()
        val endY = params.getDouble("end_y").toFloat()
        val duration = params.optLong("duration", 300)
        
        val accessibilityService = HermesAccessibilityService.instance
        val success = if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            accessibilityService.performSwipe(startX, startY, endX, endY, duration)
        } else {
            false
        }
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Swiped from ($startX, $startY) to ($endX, $endY)" else "Accessibility service not available")
        }
    }
    
    private fun performType(params: JSONObject): JSONObject {
        val text = params.getString("text")
        
        val accessibilityService = HermesAccessibilityService.instance
        val success = if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            accessibilityService.inputText(text)
        } else {
            false
        }
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Typed: $text" else "Accessibility service not available")
        }
    }
    
    private fun performBack(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.performBack() ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Back pressed" else "Accessibility service not available")
        }
    }
    
    private fun performHome(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.performHome() ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Home pressed" else "Accessibility service not available")
        }
    }
    
    private fun performRecents(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.performRecents() ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Recents opened" else "Accessibility service not available")
        }
    }
    
    private fun performNotifications(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.performNotifications() ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Notifications opened" else "Accessibility service not available")
        }
    }
    
    private fun performScreenshot(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val success = if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            accessibilityService.performScreenshot()
        } else {
            false
        }
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Screenshot taken" else "Screenshot requires Android 9+")
        }
    }
    
    private fun performOpenApp(params: JSONObject): JSONObject {
        val packageName = params.getString("package")
        
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.openApp(packageName) ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Opened app: $packageName" else "Failed to open app")
        }
    }
    
    private fun performFindText(params: JSONObject): JSONObject {
        val text = params.getString("text")
        
        val accessibilityService = HermesAccessibilityService.instance
        val node = accessibilityService?.findNodeByText(text)
        
        return JSONObject().apply {
            put("success", node != null)
            put("found", node != null)
            if (node != null) {
                put("text", node.text?.toString() ?: "")
            }
        }
    }
    
    private fun performClickText(params: JSONObject): JSONObject {
        val text = params.getString("text")
        
        val accessibilityService = HermesAccessibilityService.instance
        val success = accessibilityService?.clickByText(text) ?: false
        
        return JSONObject().apply {
            put("success", success)
            put("message", if (success) "Clicked on: $text" else "Element not found")
        }
    }
    
    private fun performGetScreenText(): JSONObject {
        val accessibilityService = HermesAccessibilityService.instance
        val texts = accessibilityService?.getAllText() ?: emptyList()
        
        return JSONObject().apply {
            put("success", true)
            put("texts", org.json.JSONArray(texts))
            put("count", texts.size)
        }
    }
    
    private fun sendResult(serverUrl: String, command: String, result: JSONObject) {
        try {
            val payload = JSONObject().apply {
                put("command", command)
                put("result", result)
                put("timestamp", System.currentTimeMillis())
            }
            
            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$serverUrl/command/result")
                .post(requestBody)
                .build()
            
            client.newCall(request).execute().use { response ->
                // Результат отправлен
            }
        } catch (e: IOException) {
            // Игнорируем ошибки отправки
        }
    }
    
    fun showToast(message: String) {
        handler.post {
            Toast.makeText(service, message, Toast.LENGTH_SHORT).show()
        }
    }
}

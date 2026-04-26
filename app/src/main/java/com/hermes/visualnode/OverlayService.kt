package com.hermes.visualnode

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class OverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingButton: View? = null
    private var chatOverlay: View? = null
    private lateinit var serverUrl: String
    private lateinit var nodeId: String
    private val client = OkHttpClient()
    private var pendingScreenshot: Bitmap? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "hermes_visual_node_channel"
        var instance: OverlayService? = null
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverUrl = intent?.getStringExtra("server_url") ?: ""
        nodeId = intent?.getStringExtra("node_id") ?: ""
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        createFloatingButton()
        registerNode()
        
        return START_STICKY
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hermes Visual Node")
            .setContentText("Алиса готова помочь")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hermes Visual Node Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createFloatingButton() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        floatingButton = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 200
        
        windowManager.addView(floatingButton, params)
        
        val button = floatingButton?.findViewById<ImageView>(R.id.floatingButton)
        button?.setOnClickListener {
            takeScreenshotAndShowChat()
        }
        
        // Перетаскивание
        button?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var moved = false
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            moved = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager.updateViewLayout(floatingButton, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }
    
    private fun takeScreenshotAndShowChat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(this, ScreenshotActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Скриншоты требуют Android 5.0+", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun showChatOverlay(screenshot: Bitmap?) {
        pendingScreenshot = screenshot
        
        if (chatOverlay != null) {
            windowManager.removeView(chatOverlay)
        }
        
        chatOverlay = LayoutInflater.from(this).inflate(R.layout.chat_overlay, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(chatOverlay, params)
        
        // Настроить UI
        val messageInput = chatOverlay?.findViewById<EditText>(R.id.messageInput)
        val sendButton = chatOverlay?.findViewById<ImageButton>(R.id.sendButton)
        val closeButton = chatOverlay?.findViewById<ImageButton>(R.id.closeButton)
        val screenshotPreview = chatOverlay?.findViewById<ImageView>(R.id.screenshotPreview)
        val removeScreenshotButton = chatOverlay?.findViewById<ImageButton>(R.id.removeScreenshot)
        
        // Показать превью скриншота
        if (screenshot != null) {
            screenshotPreview?.setImageBitmap(screenshot)
            screenshotPreview?.visibility = View.VISIBLE
            removeScreenshotButton?.visibility = View.VISIBLE
        }
        
        removeScreenshotButton?.setOnClickListener {
            pendingScreenshot = null
            screenshotPreview?.visibility = View.GONE
            removeScreenshotButton.visibility = View.GONE
        }
        
        sendButton?.setOnClickListener {
            val message = messageInput?.text.toString()
            if (message.isNotBlank() || pendingScreenshot != null) {
                sendMessageToHermes(message, pendingScreenshot)
                messageInput?.setText("")
                pendingScreenshot = null
                screenshotPreview?.visibility = View.GONE
                removeScreenshotButton?.visibility = View.GONE
            }
        }
        
        closeButton?.setOnClickListener {
            closeChatOverlay()
        }
        
        // Фокус на поле ввода
        messageInput?.requestFocus()
    }
    
    private fun closeChatOverlay() {
        chatOverlay?.let {
            windowManager.removeView(it)
            chatOverlay = null
        }
        pendingScreenshot = null
    }
    
    private fun sendMessageToHermes(message: String, screenshot: Bitmap?) {
        Thread {
            try {
                val json = JSONObject()
                json.put("message", message)
                
                // Добавить скриншот если есть
                if (screenshot != null) {
                    val stream = ByteArrayOutputStream()
                    screenshot.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val imageBytes = stream.toByteArray()
                    val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
                    json.put("image", base64)
                }
                
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$serverUrl/chat")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val responseJson = JSONObject(responseBody ?: "{}")
                            val reply = responseJson.optString("response", "Ответ получен")
                            
                            Toast.makeText(this, "Алиса: $reply", Toast.LENGTH_LONG).show()
                            closeChatOverlay()
                        } else {
                            Toast.makeText(this, "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    
    fun sendScreenshot(bitmap: Bitmap) {
        showChatOverlay(bitmap)
    }
    
    private fun registerNode() {
        Thread {
            try {
                val json = """{"node_id": "$nodeId", "url": "android://$nodeId"}"""
                val requestBody = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$serverUrl/nodes/register")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Подключено к Hermes", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                // Игнорируем
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        floatingButton?.let { windowManager.removeView(it) }
        chatOverlay?.let { windowManager.removeView(it) }
        instance = null
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}

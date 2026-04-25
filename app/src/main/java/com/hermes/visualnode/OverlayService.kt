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
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class OverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var serverUrl: String
    private lateinit var nodeId: String
    private val client = OkHttpClient()
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "hermes_visual_node_channel"
        const val SCREENSHOT_REQUEST = 1002
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverUrl = intent?.getStringExtra("server_url") ?: ""
        nodeId = intent?.getStringExtra("node_id") ?: ""
        
        // Запустить foreground service
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Создать плавающую кнопку
        createFloatingButton()
        
        // Зарегистрировать ноду на сервере
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
            .setContentText("Плавающая кнопка активна")
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
        
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        
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
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        
        windowManager.addView(floatingView, params)
        
        // Обработка кликов
        val button = floatingView?.findViewById<ImageView>(R.id.floatingButton)
        button?.setOnClickListener {
            takeScreenshot()
        }
        
        // Перетаскивание кнопки
        button?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }
    
    private fun takeScreenshot() {
        // Для Android 5.0+ используем Media Projection API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            
            // Запустить активность для получения разрешения
            val intent = Intent(this, ScreenshotActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Скриншоты требуют Android 5.0+", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun sendScreenshot(bitmap: Bitmap) {
        Thread {
            try {
                // Конвертировать в JPEG
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val imageBytes = stream.toByteArray()
                
                // Отправить на сервер
                val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
                val request = Request.Builder()
                    .url("$serverUrl/nodes/$nodeId/screenshot")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Скриншот отправлен", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Ошибка отправки: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
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
                            Toast.makeText(this, "Нода зарегистрирована", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                // Игнорируем ошибки регистрации
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}

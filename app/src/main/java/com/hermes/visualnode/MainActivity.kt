package com.hermes.visualnode

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var serverUrlInput: EditText
    private lateinit var nodeIdInput: EditText
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    
    companion object {
        const val OVERLAY_PERMISSION_REQUEST = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        serverUrlInput = findViewById(R.id.serverUrlInput)
        nodeIdInput = findViewById(R.id.nodeIdInput)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        
        // Загрузить сохранённые настройки
        val prefs = getSharedPreferences("hermes_visual_node", MODE_PRIVATE)
        serverUrlInput.setText(prefs.getString("server_url", "http://192.168.1.100:8766"))
        nodeIdInput.setText(prefs.getString("node_id", Build.MODEL))
        
        startButton.setOnClickListener {
            if (checkOverlayPermission()) {
                startOverlayService()
            } else {
                requestOverlayPermission()
            }
        }
        
        stopButton.setOnClickListener {
            stopOverlayService()
        }
    }
    
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (checkOverlayPermission()) {
                startOverlayService()
            } else {
                Toast.makeText(this, "Нужно разрешение для отображения поверх других приложений", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun startOverlayService() {
        val serverUrl = serverUrlInput.text.toString()
        val nodeId = nodeIdInput.text.toString()
        
        if (serverUrl.isEmpty() || nodeId.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Сохранить настройки
        val prefs = getSharedPreferences("hermes_visual_node", MODE_PRIVATE)
        prefs.edit().apply {
            putString("server_url", serverUrl)
            putString("node_id", nodeId)
            apply()
        }
        
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("server_url", serverUrl)
            putExtra("node_id", nodeId)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        Toast.makeText(this, "Плавающая кнопка активирована", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Плавающая кнопка отключена", Toast.LENGTH_SHORT).show()
    }
}

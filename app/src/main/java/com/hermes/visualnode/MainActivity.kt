package com.hermes.visualnode

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermes.visualnode.ui.ChatScreen
import com.hermes.visualnode.ui.SettingsScreen
import com.hermes.visualnode.ui.theme.HermesVisualNodeTheme
import com.hermes.visualnode.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    
    private var isFloatingButtonActive by mutableStateOf(false)
    private var currentScreen by mutableStateOf("chat")
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkOverlayPermission()) {
            startFloatingButton()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            HermesVisualNodeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ChatViewModel = viewModel()
                    
                    // Инициализация
                    LaunchedEffect(Unit) {
                        val prefs = getSharedPreferences("hermes_visual_node", MODE_PRIVATE)
                        val serverUrl = prefs.getString("server_url", "http://192.168.1.100:8766") ?: ""
                        val nodeId = prefs.getString("node_id", Build.MODEL) ?: ""
                        
                        viewModel.serverUrl.value = serverUrl
                        viewModel.nodeId.value = nodeId
                        viewModel.initializeApi(serverUrl)
                    }
                    
                    when (currentScreen) {
                        "chat" -> ChatScreen(
                            viewModel = viewModel,
                            onScreenshotClick = {
                                // Запустить активность скриншота
                                val intent = Intent(this@MainActivity, ScreenshotActivity::class.java)
                                startActivity(intent)
                            },
                            onSettingsClick = {
                                currentScreen = "settings"
                            }
                        )
                        "settings" -> SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                currentScreen = "chat"
                            },
                            onStartFloatingButton = {
                                if (checkOverlayPermission()) {
                                    startFloatingButton()
                                } else {
                                    requestOverlayPermission()
                                }
                            },
                            onStopFloatingButton = {
                                stopFloatingButton()
                            },
                            isFloatingButtonActive = isFloatingButtonActive
                        )
                    }
                }
            }
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
            overlayPermissionLauncher.launch(intent)
        }
    }
    
    private fun startFloatingButton() {
        // Сохранить настройки
        val prefs = getSharedPreferences("hermes_visual_node", MODE_PRIVATE)
        
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("server_url", prefs.getString("server_url", ""))
            putExtra("node_id", prefs.getString("node_id", ""))
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        isFloatingButtonActive = true
    }
    
    private fun stopFloatingButton() {
        stopService(Intent(this, OverlayService::class.java))
        isFloatingButtonActive = false
    }
    
    override fun onResume() {
        super.onResume()
        // Проверить статус сервиса
        // TODO: добавить проверку через broadcast receiver
    }
}

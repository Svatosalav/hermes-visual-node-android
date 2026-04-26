package com.hermes.visualnode.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.visualnode.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onStartFloatingButton: () -> Unit,
    onStopFloatingButton: () -> Unit,
    isFloatingButtonActive: Boolean
) {
    var serverUrl by remember { mutableStateOf(viewModel.serverUrl.value) }
    var nodeId by remember { mutableStateOf(viewModel.nodeId.value) }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Секция подключения
            Text(
                "Подключение",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL сервера") },
                placeholder = { Text("http://192.168.1.100:8766") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Cloud, null)
                }
            )
            
            OutlinedTextField(
                value = nodeId,
                onValueChange = { nodeId = it },
                label = { Text("ID устройства") },
                placeholder = { Text("android-phone") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.PhoneAndroid, null)
                }
            )
            
            Button(
                onClick = {
                    viewModel.serverUrl.value = serverUrl
                    viewModel.nodeId.value = nodeId
                    viewModel.initializeApi(serverUrl)
                    viewModel.registerNode()
                    showSaveDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Сохранить и подключиться")
            }
            
            Divider()
            
            // Секция плавающей кнопки
            Text(
                "Плавающая кнопка",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isFloatingButtonActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            tint = if (isFloatingButtonActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isFloatingButtonActive) "Активна" else "Неактивна",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        "Плавающая кнопка позволяет быстро делать скриншоты и общаться с Алисой поверх любых приложений",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFloatingButtonActive) {
                            Button(
                                onClick = onStopFloatingButton,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Остановить")
                            }
                        } else {
                            Button(
                                onClick = onStartFloatingButton,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Запустить")
                            }
                        }
                    }
                }
            }
            
            Divider()
            
            // Секция управления
            Text(
                "Управление",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedButton(
                onClick = { viewModel.clearMessages() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(8.dp))
                Text("Очистить историю чата")
            }
            
            OutlinedButton(
                onClick = { viewModel.loadHistory() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Загрузить историю с сервера")
            }
            
            Divider()
            
            // Информация
            Text(
                "О приложении",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow("Версия", "2.0")
                    InfoRow("Статус", if (isFloatingButtonActive) "Подключено" else "Отключено")
                    InfoRow("Сервер", serverUrl)
                    InfoRow("ID устройства", nodeId)
                }
            }
        }
    }
    
    // Диалог сохранения
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null) },
            title = { Text("Настройки сохранены") },
            text = { Text("Подключение к серверу установлено") },
            confirmButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            value,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

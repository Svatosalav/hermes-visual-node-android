package com.hermes.visualnode.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.visualnode.api.ChatMessage
import com.hermes.visualnode.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onScreenshotClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var attachedScreenshot by remember { mutableStateOf<Bitmap?>(null) }
    val listState = rememberLazyListState()
    
    // Автоскролл к последнему сообщению
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Алиса", fontWeight = FontWeight.Bold)
                        Text(
                            "Hermes Visual Node",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Настройки")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank() || attachedScreenshot != null) {
                        viewModel.sendMessage(messageText, attachedScreenshot)
                        messageText = ""
                        attachedScreenshot = null
                    }
                },
                onScreenshotClick = onScreenshotClick,
                attachedScreenshot = attachedScreenshot,
                onRemoveScreenshot = { attachedScreenshot = null },
                isLoading = viewModel.isLoading.value
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Ошибка
            viewModel.error.value?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Список сообщений
            if (viewModel.messages.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.messages) { message ->
                        MessageBubble(message)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    attachedScreenshot: Bitmap?,
    onRemoveScreenshot: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Превью скриншота
            attachedScreenshot?.let { bitmap ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Screenshot",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Скриншот прикреплён", fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onRemoveScreenshot) {
                        Icon(Icons.Default.Close, "Удалить")
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = onScreenshotClick,
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.CameraAlt, "Скриншот")
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Сообщение Алисе...") },
                    maxLines = 4,
                    enabled = !isLoading
                )
                
                IconButton(
                    onClick = onSendClick,
                    enabled = !isLoading && (messageText.isNotBlank() || attachedScreenshot != null)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, "Отправить")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Привет! Я Алиса",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Напиши мне что-нибудь или отправь скриншот",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

package com.hermes.visualnode

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class HermesAccessibilityService : AccessibilityService() {
    
    companion object {
        var instance: HermesAccessibilityService? = null
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Можно отслеживать события UI для контекста
    }
    
    override fun onInterrupt() {
        // Обработка прерывания
    }
    
    // Клик по координатам
    @RequiresApi(Build.VERSION_CODES.N)
    fun performClick(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        
        return dispatchGesture(gestureBuilder.build(), null, null)
    }
    
    // Свайп
    @RequiresApi(Build.VERSION_CODES.N)
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        
        return dispatchGesture(gestureBuilder.build(), null, null)
    }
    
    // Нажатие кнопки "Назад"
    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    // Нажатие кнопки "Домой"
    fun performHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    // Открыть недавние приложения
    fun performRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    // Открыть уведомления
    fun performNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
    
    // Открыть быстрые настройки
    fun performQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }
    
    // Заблокировать экран
    @RequiresApi(Build.VERSION_CODES.P)
    fun performLockScreen(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }
    
    // Сделать скриншот
    @RequiresApi(Build.VERSION_CODES.P)
    fun performScreenshot(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
    
    // Найти элемент по тексту
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeByTextRecursive(rootNode, text)
    }
    
    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByTextRecursive(child, text)
            if (result != null) return result
        }
        
        return null
    }
    
    // Кликнуть по элементу с текстом
    fun clickByText(text: String): Boolean {
        val node = findNodeByText(text)
        return node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }
    
    // Ввести текст в фокусированное поле
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun inputText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        
        val arguments = android.os.Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        
        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
    
    // Получить текст всех элементов на экране
    fun getAllText(): List<String> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val texts = mutableListOf<String>()
        collectTextsRecursive(rootNode, texts)
        return texts
    }
    
    private fun collectTextsRecursive(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        node.text?.toString()?.let { if (it.isNotBlank()) texts.add(it) }
        node.contentDescription?.toString()?.let { if (it.isNotBlank()) texts.add(it) }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextsRecursive(child, texts)
        }
    }
    
    // Открыть приложение
    fun openApp(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return true
        }
        return false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}

package com.ff.aimassist

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AimAccessibilityService : AccessibilityService() {

    private var sensitivity = 50

    companion object {
        var instance: AimAccessibilityService? = null
        var isServiceRunning = false

        fun updateSensitivity(s: Int) {
            instance?.sensitivity = s
        }

        fun startService() {
            isServiceRunning = true
        }

        fun stopService() {
            isServiceRunning = false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true
        Log.d("AimAccessibility", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isServiceRunning || event == null) return
        
        if (event.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START) {
            event.source?.let { node ->
                val rect = android.graphics.Rect()
                node.getBoundsInScreen(rect)
                val x = rect.centerX().toFloat()
                val y = rect.centerY().toFloat()
                if (x > 0 && y > 0) {
                    performAimAdjust(x, y)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AimAccessibility", "Interrupted")
    }

    override fun onDestroy() {
        isServiceRunning = false
        instance = null
        super.onDestroy()
    }

    private fun performAimAdjust(x: Float, y: Float) {
        if (!isServiceRunning) return
        
        val offset = (sensitivity / 100f) * 20f
        val targetY = y - offset
        
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, targetY)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        dispatchGesture(gesture, null, null)
        Log.d("AimAccessibility", "Adjust aim: offset=$offset")
    }
}

package com.ff.aimassist

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.MotionEvent
import android.util.Log
import rikka.shizuku.Shizuku
import java.lang.reflect.Method

class AimService : Service() {

    private var sensitivity = 50
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private var injectMethod: Method? = null

    private fun injectTouch(x: Float, y: Float, action: Int) {
        try {
            // Sửa đúng tham số
           val inputManager = Shizuku.getSystemService("input", "android")
                Log.e("AimService", "InputManager is null")
                return
            }
            if (injectMethod == null) {
                injectMethod = inputManager.javaClass.getMethod(
                    "injectInputEvent",
                    MotionEvent::class.java,
                    Int::class.javaPrimitiveType
                )
                injectMethod?.isAccessible = true
            }
            val event = MotionEvent.obtain(
                System.currentTimeMillis(), System.currentTimeMillis(),
                action, x, y, 0f, 0f, 0, 0f, 0f,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            injectMethod?.invoke(inputManager, event, 0)
            event.recycle()
        } catch (e: Exception) {
            Log.e("AimService", "Inject error: ${e.message}")
        }
    }

    private val injectRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            val x = 540f
            val y = 960f
            val offset = (sensitivity / 100f) * 15f
            injectTouch(x, y - offset, MotionEvent.ACTION_DOWN)
            handler.postDelayed({
                injectTouch(x, y - offset, MotionEvent.ACTION_UP)
            }, 50)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        running = true
        handler.post(injectRunnable)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(injectRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            sensitivity = it.getIntExtra("sensitivity", 50)
        }
        return START_STICKY
    }

    companion object {
        fun start(context: Context, sensitivity: Int) {
            val intent = Intent(context, AimService::class.java)
            intent.putExtra("sensitivity", sensitivity)
            context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AimService::class.java))
        }

        fun updateSensitivity(newSens: Int) {}
    }
}

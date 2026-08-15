package com.ff.aimassist

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var btnConnect: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var seekSensitivity: SeekBar
    private lateinit var tvSensitivity: TextView
    private lateinit var tvStatus: TextView

    private var isShizukuReady = false
    private var isServiceRunning = false
    private var sensitivity = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect = findViewById(R.id.btnConnect)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        seekSensitivity = findViewById(R.id.seekSensitivity)
        tvSensitivity = findViewById(R.id.tvSensitivity)
        tvStatus = findViewById(R.id.tvStatus)

        checkShizuku()

        btnConnect.setOnClickListener {
            if (!isShizukuReady) {
                requestShizukuPermission()
            } else {
                Toast.makeText(this, "Shizuku đã kết nối", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            if (!isShizukuReady) {
                Toast.makeText(this, "Chưa kết nối Shizuku", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startAimService()
        }

        btnStop.setOnClickListener {
            stopAimService()
        }

        seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sensitivity = progress
                tvSensitivity.text = "Độ nhạy: $progress%"
                if (isServiceRunning) {
                    AimService.updateSensitivity(progress)
                    AimAccessibilityService.updateSensitivity(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkShizuku() {
        if (Shizuku.pingBinder()) {
            isShizukuReady = true
            tvStatus.text = "Trạng thái: Shizuku đã kết nối"
            btnConnect.isEnabled = false
        } else {
            isShizukuReady = false
            tvStatus.text = "Trạng thái: Chưa kết nối Shizuku"
            btnConnect.isEnabled = true
        }
    }

    private fun requestShizukuPermission() {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            Toast.makeText(this, "Shizuku version không hỗ trợ", Toast.LENGTH_LONG).show()
            return
        }
if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) { ... }
            Shizuku.requestPermission(0)
        } else {
            Toast.makeText(this, "Đã có quyền Shizuku", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAimService() {
        if (isServiceRunning) return
        AimService.start(this, sensitivity)
        AimAccessibilityService.startService()
        isServiceRunning = true
        btnStart.isEnabled = false
        btnStop.isEnabled = true
        tvStatus.text = "Trạng thái: Đang chạy (độ nhạy $sensitivity%)"
        Toast.makeText(this, "Đã bắt đầu hỗ trợ aim", Toast.LENGTH_SHORT).show()
    }

    private fun stopAimService() {
        if (!isServiceRunning) return
        AimService.stop(this)
        AimAccessibilityService.stopService()
        isServiceRunning = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        tvStatus.text = "Trạng thái: Đã dừng"
        Toast.makeText(this, "Đã dừng hỗ trợ aim", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        checkShizuku()
    }
}

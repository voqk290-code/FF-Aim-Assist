package com.ff.aimassist

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import moe.shizuku.Shizuku

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
                Toast.makeText(this, "Shizuku da ket noi", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            if (!isShizukuReady) {
                Toast.makeText(this, "Chua ket noi Shizuku", Toast.LENGTH_SHORT).show()
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
                tvSensitivity.text = "Do nhay: $progress%"
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
            tvStatus.text = "Trang thai: Shizuku da ket noi"
            btnConnect.isEnabled = false
        } else {
            isShizukuReady = false
            tvStatus.text = "Trang thai: Chua ket noi Shizuku"
            btnConnect.isEnabled = true
        }
    }

    private fun requestShizukuPermission() {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            Toast.makeText(this, "Shizuku version khong ho tro", Toast.LENGTH_LONG).show()
            return
        }
        if (!Shizuku.hasPermission()) {
            Shizuku.requestPermission(0)
        }
    }

    private fun startAimService() {
        if (isServiceRunning) return
        AimService.start(this, sensitivity)
        AimAccessibilityService.startService()
        isServiceRunning = true
        btnStart.isEnabled = false
        btnStop.isEnabled = true
        tvStatus.text = "Trang thai: Dang chay (do nhay $sensitivity%)"
        Toast.makeText(this, "Da bat dau ho tro aim", Toast.LENGTH_SHORT).show()
    }

    private fun stopAimService() {
        if (!isServiceRunning) return
        AimService.stop(this)
        AimAccessibilityService.stopService()
        isServiceRunning = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        tvStatus.text = "Trang thai: Da dung"
        Toast.makeText(this, "Da dung ho tro aim", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        checkShizuku()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (Shizuku.hasPermission()) {
                isShizukuReady = true
                tvStatus.text = "Trang thai: Shizuku da ket noi"
                btnConnect.isEnabled = false
                Toast.makeText(this, "Da nhan quyen Shizuku", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Tu choi quyen Shizuku", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

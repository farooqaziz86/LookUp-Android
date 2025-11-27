package com.lookup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var slider: Slider
    private lateinit var tvInterval: TextView

    private var intervalSeconds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupClickListeners()
        updateIntervalText()
    }

    private fun setupViews() {
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        slider = findViewById(R.id.intervalSlider)
        tvInterval = findViewById(R.id.tvInterval)

        slider.addOnChangeListener { _, value, _ ->
            intervalSeconds = value.toInt()
            updateIntervalText()
        }
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            if (hasOverlayPermission()) {
                startOverlayService()
            } else {
                requestOverlayPermission()
            }
        }

        btnStop.setOnClickListener {
            stopOverlayService()
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("interval", intervalSeconds)
        startService(intent)
        
        btnStart.isEnabled = false
        btnStop.isEnabled = true
        
        // Optional: Minimize app to test overlay
        moveTaskToBack(true)
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
        
        btnStart.isEnabled = true
        btnStop.isEnabled = false
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (hasOverlayPermission()) {
                startOverlayService()
            }
        }
    }

    private fun updateIntervalText() {
        tvInterval.text = getString(R.string.nudge_interval, intervalSeconds)
    }

    override fun onResume() {
        super.onResume()
        // Update button states when returning to app
        btnStop.isEnabled = isServiceRunning()
        btnStart.isEnabled = !btnStop.isEnabled
    }

    private fun isServiceRunning(): Boolean {
        // Simple implementation - in production use more robust checking
        return false
    }
}

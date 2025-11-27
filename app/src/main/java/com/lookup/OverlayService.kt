package com.lookup

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var timer: Timer? = null
    private var intervalSeconds = 10

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intervalSeconds = intent?.getIntExtra("interval", 10) ?: 10
        startTimer()
        return START_STICKY
    }

    private fun createOverlayView() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 300

        windowManager.addView(overlayView, params)
        overlayView.visibility = View.GONE
    }

    private fun startTimer() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    showNudge()
                }
            }
        }, 0, intervalSeconds * 1000L)
    }

    private fun showNudge() {
        if (!::overlayView.isInitialized) return
        
        overlayView.visibility = View.VISIBLE
        
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(100)
            }
        }
        
        Handler(Looper.getMainLooper()).postDelayed({
            overlayView.visibility = View.GONE
        }, 2000)
    }

    override fun onDestroy() {
        timer?.cancel()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        super.onDestroy()
    }
}

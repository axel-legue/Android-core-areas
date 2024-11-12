package com.skittles.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

// BackgroundService.kt
class BackgroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            // Perform a background task
            Thread.sleep(5000) // Simulate work
            stopSelf()
        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

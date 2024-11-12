package com.skittles.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BoundService : Service() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // LocalBinder class to provide binding
    inner class LocalBinder : Binder() {
        fun getService(): BoundService = this@BoundService
    }

    fun getRandomNumber(): Int = (1..100).random() // Example method for clients to call
}
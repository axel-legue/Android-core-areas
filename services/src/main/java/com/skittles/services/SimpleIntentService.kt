package com.skittles.services

import android.app.IntentService
import android.content.Intent

class SimpleIntentService : IntentService("SimpleIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        // Perform task in the background
        Thread.sleep(3000) // Simulate work
    }
}
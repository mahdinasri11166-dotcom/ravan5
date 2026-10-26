package com.therapist.assistant

import com.therapist.assistant.data.BackupWorker

import android.app.Application
import com.therapist.assistant.notification.NotificationHelper

class TherapistApp : Application() {
    override fun onCreate() {
        BackupWorker.schedule(this)
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}

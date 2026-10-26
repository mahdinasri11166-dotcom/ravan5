package com.therapist.assistant.data

import android.content.Context
import androidx.work.*
import com.therapist.assistant.util.BackupHelper
import java.util.concurrent.TimeUnit

class BackupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val clients = db.clientDao().getAllClientsOnce()
            val appts = db.appointmentDao().getAllOnce()
            val questionnaires = db.questionnaireDao().getAllOnce()
            val workshops = db.workshopDao().getAllOnce()
            val participants = db.workshopDao().getAllParticipantsOnce()
            val settings = SettingsRepository(applicationContext).getAllAsMap()
            val json = BackupHelper.buildJson(
                clients, appts, questionnaires, workshops, participants, settings
            )
            BackupHelper.saveToAppStorage(applicationContext, json)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE = "weekly_backup"
        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<BackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE, ExistingPeriodicWorkPolicy.KEEP, req
            )
        }
    }
}

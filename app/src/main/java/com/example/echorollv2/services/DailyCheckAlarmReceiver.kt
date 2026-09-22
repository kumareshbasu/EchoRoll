package com.example.echorollv2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DailyCheckAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        android.util.Log.d(
            "DailyCheckAlarmReceiver",
            "Daily alarm fired. Restoring today's class reminders."
        )

        val data = androidx.work.Data.Builder()
            .putBoolean("SILENT_CHECK", true)
            .build()

        val request = OneTimeWorkRequestBuilder<DailyCheckWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "DailyCheckImmediate",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )

        // AlarmManager alarms are one-shot, so chain the next day explicitly.
        NotificationScheduler.scheduleNextDailyCheck(context)
    }
}

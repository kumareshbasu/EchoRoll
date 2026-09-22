package com.example.echorollv2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {

                android.util.Log.d(
                    "BootReceiver",
                    "System/package time event. Restoring attendance notification scheduling."
                )

                // Register the OS-owned daily trigger so scheduling does not depend on
                // opening the app every day.
                NotificationScheduler.scheduleNextDailyCheck(context)

                // Keep the existing immediate worker refresh as well.
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
            }
        }
    }
}

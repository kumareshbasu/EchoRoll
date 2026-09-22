package com.example.echorollv2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class ClassAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getIntExtra("ROUTINE_ID", -1)
        val subjectCode = intent.getStringExtra("SUBJECT_CODE") ?: return
        
        val db = com.example.echorollv2.data.local.EchoDatabase.getDatabase(context)
        val repo = com.example.echorollv2.data.repository.EchoRepository(db.echoDao())

        // Only remind when this exact class slot is still unmarked today.
        // Present, Absent, and Cancelled all count as "marked".
        kotlinx.coroutines.MainScope().launch {
            val dateStr = java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val records = repo.getAttendanceRecordsForDate(dateStr).first()
            val isAlreadyMarked = records.any {
                it.routineId == routineId && it.subjectCode == subjectCode
            }

            if (isAlreadyMarked) {
                android.util.Log.d(
                    "ClassAlarmReceiver",
                    "Skipping notification: $subjectCode / routine $routineId is already marked."
                )
                return@launch
            }

            val subject = repo.getSubjectByCode(subjectCode)
            val subjectName = subject?.name ?: subjectCode

            NotificationHelper.sendNotification(
                context,
                "Class Wrap-up! \uD83D\uDCDD",
                com.example.echorollv2.utils.HumorUtils.getAttendanceReminder(subjectName),
                routineId,
                subjectCode = subjectCode,
                routineId = routineId
            )
        }
    }
}

package com.count.iautista.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<RoutineNotificationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RoutineNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(RoutineNotificationWorker.WORK_NAME)
    }
}

package com.count.iautista.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.count.iautista.R
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class RoutineNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val routineRepository: RoutineRepository,
    private val profileRepository: ProfileRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val profileId = profileRepository.getProfileOnce()?.id ?: return Result.success()
        val items = routineRepository.getAllItemsOnce(profileId)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val upcoming = items.filter { item ->
            item.status != RoutineStatus.DONE &&
                item.suggestedHour != null &&
                item.suggestedHour == currentHour
        }

        if (upcoming.isEmpty()) return Result.success()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        upcoming.forEachIndexed { index, item ->
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Hora da atividade!")
                .setContentText("${item.emoji} ${item.text}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID_BASE + index, notification)
        }

        return Result.success()
    }

    private fun createChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rotina diária",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Lembretes das atividades da rotina"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "routine_reminders"
        const val WORK_NAME = "routine_hourly_check"
        private const val NOTIFICATION_ID_BASE = 1000
    }
}

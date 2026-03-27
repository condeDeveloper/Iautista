package com.count.iautista

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.worker.RoutineNotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class IautistaApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationScheduler: RoutineNotificationScheduler
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val prefs = userPreferencesDataStore.preferences.first()
            if (prefs.notificationsEnabled) {
                notificationScheduler.schedule()
            }
        }
    }
}

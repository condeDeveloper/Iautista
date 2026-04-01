package com.count.iautista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.data.seed.DatabaseSeeder
import com.count.iautista.ui.navigation.VozinhaNavGraph
import com.count.iautista.ui.navigation.Screen
import com.count.iautista.ui.theme.VozinhaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var seeder: DatabaseSeeder
    @Inject lateinit var preferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed inicial em background
        lifecycleScope.launch {
            seeder.seedIfEmpty()
        }

        setContent {
            VozinhaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val prefs = preferencesDataStore.preferences.first()
                        startDestination = if (prefs.onboardingCompleted) {
                            Screen.Inicio.route
                        } else {
                            Screen.Onboarding.route
                        }
                    }

                    if (startDestination == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        VozinhaNavGraph(startDestination = startDestination!!)
                    }
                }
            }
        }
    }
}

package com.example.jeeptours2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.jeeptours2.di.AppContainer
import com.example.jeeptours2.ui.MainScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ініціалізація Firebase та App Check Debug Provider
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // Ініціалізація Manual Dependency Injection (Composition Root)
        val appContainer = AppContainer(applicationContext)

        setContent {
            MaterialTheme {
                Surface {
                    MainScreen(repository = appContainer.tourRepository)
                }
            }
        }
    }
}
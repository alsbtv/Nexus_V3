package com.hiyako.app

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hiyako.app.ui.ChatScreen
import com.hiyako.app.ui.theme.NexusAITheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogger()
        super.onCreate(savedInstanceState)

        // Nexus AI is dark-only — keep system bar icons light on every device.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            NexusAITheme {
                ChatScreen()
            }
        }
    }

    /**
     * The app has no Hilt/DI graph: the only dependency is the ViewModel, which
     * `viewModel()` creates with the Application from the default factory.
     *
     * This handler is purely diagnostic — it records the fatal stack trace to
     * `files/last_crash.txt` and Logcat before the system takes over, so a
     * native/device-specific crash can be identified instead of guessed at.
     */
    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                File(filesDir, "last_crash.txt").writeText(
                    buildString {
                        appendLine("time=${System.currentTimeMillis()}")
                        appendLine("thread=${thread.name}")
                        appendLine(Log.getStackTraceString(error))
                    }
                )
            }
            Log.e(TAG, "Fatal crash on ${thread.name}", error)
            previous?.uncaughtException(thread, error)
        }
    }

    private companion object {
        const val TAG = "NexusAI"
    }
}

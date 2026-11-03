package com.hiyako.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.DeepSpaceElevated
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NexusAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nexus AI is dark-only — keep system bar icons light on every device.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            NexusAITheme {
                NexusReadyScreen()
            }
        }
    }
}

@Composable
private fun NexusReadyScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepSpaceBlack,
                        DeepSpaceElevated,
                        DeepSpaceBlack
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nexus AI is Ready",
            color = NeonCyan,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(
                    color = NeonCyan.copy(alpha = 0.55f),
                    blurRadius = 24f
                )
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05060F)
@Composable
private fun NexusReadyScreenPreview() {
    NexusAITheme {
        NexusReadyScreen()
    }
}

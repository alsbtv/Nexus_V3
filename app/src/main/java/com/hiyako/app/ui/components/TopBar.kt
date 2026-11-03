package com.hiyako.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.SignalGreen
import com.hiyako.app.ui.theme.SolarAmber
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextTertiary

/**
 * The floating command bar: menu orb on the left, the live conversation
 * title in the middle and a pulsing status chip on the right.
 */
@Composable
fun NexusTopBar(
    title: String,
    isGenerating: Boolean,
    isSignedIn: Boolean,
    onMenu: () -> Unit,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            icon = Icons.Rounded.Menu,
            contentDescription = "Open menu",
            size = 42.dp,
            onClick = onMenu
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(4.dp).background(NeonCyan, CircleShape))
                Spacer(Modifier.width(5.dp))
                MicroLabel("Nexus AI")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        val accent = when {
            isGenerating -> NeonMagenta
            isSignedIn -> SignalGreen
            else -> SolarAmber
        }
        val label = when {
            isGenerating -> "Thinking"
            isSignedIn -> "Online"
            else -> "Guest"
        }

        StatusChip(label = label, accent = accent, onClick = onStatusClick)
    }
}

@Composable
private fun StatusChip(label: String, accent: Color, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "statusPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statusDot"
    )

    Row(
        Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.07f))
            .border(1.dp, accent.copy(alpha = 0.22f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(5.dp)
                .graphicsLayer { alpha = pulse }
                .background(accent, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = TextTertiary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

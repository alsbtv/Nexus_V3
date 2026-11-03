package com.hiyako.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.SolarAmber
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary
import com.hiyako.app.ui.theme.TextTertiary

data class QuickAction(
    val title: String,
    val subtitle: String,
    val prompt: String,
    val icon: ImageVector,
    val accent: Color,
    val hero: Boolean = false
)

@Composable
fun EmptyStateGrid(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = remember {
        listOf(
            QuickAction(
                title = "Build App",
                subtitle = "One prompt in. A real, shippable Android app out.",
                prompt = "Build me an Android app that ",
                icon = Icons.Rounded.AutoAwesome,
                accent = NeonCyan,
                hero = true
            ),
            QuickAction(
                title = "Image",
                subtitle = "Artwork, logos and concepts.",
                prompt = "Create an image of ",
                icon = Icons.Rounded.Image,
                accent = NeonMagenta
            ),
            QuickAction(
                title = "Video",
                subtitle = "Storyboard a scene in motion.",
                prompt = "Create a short video concept for ",
                icon = Icons.Rounded.Movie,
                accent = ElectricViolet
            ),
            QuickAction(
                title = "Deep Research",
                subtitle = "Structured briefs with sources.",
                prompt = "Research deeply and give me a structured brief on ",
                icon = Icons.Rounded.Science,
                accent = ElectricBlue
            ),
            QuickAction(
                title = "Ghost Fix",
                subtitle = "Drop a bug. Get the exact fix.",
                prompt = "Find and fix this bug in my code: ",
                icon = Icons.Rounded.BugReport,
                accent = SolarAmber
            )
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        CoreOnlinePill()
        Spacer(Modifier.height(18.dp))
        Text(
            text = "What will you\ncreate today?",
            style = MaterialTheme.typography.displaySmall.copy(
                brush = Brush.linearGradient(listOf(TextPrimary, NeonCyan, NeonMagenta)),
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp
            )
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "One prompt. Any model. Nexus turns it into something real.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(24.dp))

        QuickActionCard(
            action = actions[0],
            onClick = { onAction(actions[0].prompt) },
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                action = actions[1],
                onClick = { onAction(actions[1].prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
            )
            QuickActionCard(
                action = actions[2],
                onClick = { onAction(actions[2].prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                action = actions[3],
                onClick = { onAction(actions[3].prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
            )
            QuickActionCard(
                action = actions[4],
                onClick = { onAction(actions[4].prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Chats stay on this device · Models via Puter",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun CoreOnlinePill() {
    val transition = rememberInfiniteTransition(label = "corePulse")
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreDot"
    )
    Row(
        Modifier
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.07f))
            .border(1.dp, NeonCyan.copy(alpha = 0.22f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(5.dp)
                .graphicsLayer { alpha = pulse }
                .background(NeonCyan, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "NEXUS CORE ONLINE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = NeonCyan,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "quickActionScale"
    )
    val shape = RoundedCornerShape(if (action.hero) 28.dp else 24.dp)

    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(shape, alpha = if (action.hero) 0.07f else 0.05f, strokeAlpha = 0.13f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Box(
            Modifier
                .size(if (action.hero) 170.dp else 110.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(action.accent.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(if (action.hero) 18.dp else 15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconOrb(
                    icon = action.icon,
                    accent = action.accent,
                    size = if (action.hero) 46.dp else 38.dp
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(if (action.hero) 30.dp else 26.dp)
                        .clip(CircleShape)
                        .background(action.accent.copy(alpha = 0.12f))
                        .border(1.dp, action.accent.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = action.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Column {
                Text(
                    text = action.title,
                    style = if (action.hero) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun IconOrb(icon: ImageVector, accent: Color, size: Dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.9f),
                        accent.copy(alpha = 0.34f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(size * 0.34f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepSpaceBlack,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

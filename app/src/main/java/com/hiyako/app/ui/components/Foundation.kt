package com.hiyako.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextTertiary
import kotlin.math.sin
import kotlin.random.Random

const val NEXUS_VERSION = "1.0.0"

/** A frosted glass surface with a soft top-light and a hairline edge. */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color.White,
    alpha: Float = 0.055f,
    strokeAlpha: Float = 0.12f
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            colors = listOf(
                tint.copy(alpha = alpha + 0.035f),
                tint.copy(alpha = alpha * 0.55f),
                tint.copy(alpha = alpha * 0.22f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = strokeAlpha),
                Color.White.copy(alpha = strokeAlpha * 0.22f),
                Color.White.copy(alpha = strokeAlpha * 0.7f)
            )
        ),
        shape = shape
    )

/** The Deep Core backdrop: aurora blobs, a slow starfield and a bottom fade. */
@Composable
fun NexusBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(modifier.fillMaxSize().background(DeepSpaceBlack)) {
        Box(
            Modifier
                .size(560.dp)
                .offset(x = (-190).dp, y = (-210).dp)
                .graphicsLayer {
                    translationX = drift * 42f
                    translationY = drift * 28f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.17f),
                            NeonCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            Modifier
                .size(520.dp)
                .align(Alignment.TopEnd)
                .offset(x = 170.dp, y = (-90).dp)
                .graphicsLayer { translationY = drift * 64f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonMagenta.copy(alpha = 0.15f),
                            NeonMagenta.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            Modifier
                .size(640.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-240).dp, y = 190.dp)
                .graphicsLayer { translationX = drift * -34f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Starfield(shimmer)

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DeepSpaceBlack.copy(alpha = 0.72f)),
                        startY = 0f,
                        endY = 2600f
                    )
                )
        )
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val warm: Boolean
)

@Composable
private fun Starfield(shimmer: Float) {
    val stars = remember {
        val random = Random(11)
        List(68) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.5f + random.nextFloat() * 1.3f,
                phase = random.nextFloat() * 6.28318f,
                warm = random.nextFloat() > 0.82f
            )
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val twinkle = 0.22f + 0.62f * ((sin(shimmer + star.phase) + 1f) / 2f)
            drawCircle(
                color = if (star.warm) {
                    NeonMagenta.copy(alpha = 0.34f * twinkle)
                } else {
                    Color.White.copy(alpha = 0.44f * twinkle)
                },
                radius = star.radius * density,
                center = Offset(size.width * star.x, size.height * star.y)
            )
        }
    }
}

/** Nexus monogram — the app's living signature. */
@Composable
fun NexusAvatar(size: Dp = 32.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.32f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(size * 0.82f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.96f),
                            ElectricViolet.copy(alpha = 0.92f),
                            NeonMagenta.copy(alpha = 0.88f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = DeepSpaceBlack,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.42f).sp,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
fun MicroLabel(text: String, modifier: Modifier = Modifier, color: Color = TextTertiary) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "glassIconScale"
    )
    Box(
        modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(CircleShape, alpha = 0.06f, strokeAlpha = 0.1f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "gradientButtonScale"
    )
    Box(
        modifier
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(NeonCyan, ElectricBlue, NeonMagenta)))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepSpaceBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = DeepSpaceBlack,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = NeonCyan,
    icon: ImageVector? = null
) {
    Box(
        modifier
            .height(48.dp)
            .glass(CircleShape, alpha = 0.05f, strokeAlpha = 0.16f)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Small glass stat tile used by the workspace. */
@Composable
fun StatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier
            .glass(RoundedCornerShape(18.dp), alpha = 0.05f)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = TextTertiary
        )
    }
}

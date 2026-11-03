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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hiyako.app.ModelOption
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.SignalGreen
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary
import com.hiyako.app.ui.theme.TextTertiary

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    model: ModelOption,
    isGenerating: Boolean,
    isSignedIn: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onModelClick: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onModelClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).background(model.accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Change model",
                    tint = TextTertiary,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            if (isSignedIn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).background(SignalGreen, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Puter connected",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            } else {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onSignIn)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Sign in to Puter",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(30.dp), alpha = 0.075f, strokeAlpha = 0.16f)
                .padding(start = 8.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(model.accent.copy(alpha = 0.1f))
                        .border(1.dp, model.accent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Current model",
                        tint = model.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = { next -> if (next.length <= 4000) onValueChange(next) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (focusRequester != null) {
                                    Modifier.focusRequester(focusRequester)
                                } else {
                                    Modifier
                                }
                            ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                        cursorBrush = SolidColor(NeonCyan),
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (value.isNotBlank() && !isGenerating) onSend()
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = "Message Nexus…",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextTertiary
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.width(4.dp))
                SendStopButton(
                    isGenerating = isGenerating,
                    enabled = value.isNotBlank(),
                    onSend = onSend,
                    onStop = onStop
                )
            }
        }
    }
}

@Composable
private fun SendStopButton(
    isGenerating: Boolean,
    enabled: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sendPulse")
    val ring by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring"
    )
    val accent = if (isGenerating) NeonMagenta else NeonCyan
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 900f),
        label = "sendScale"
    )

    Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer { alpha = if (enabled || isGenerating) 0.9f else 0.25f }
                .background(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        if (isGenerating) {
            Box(
                Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        scaleX = ring
                        scaleY = ring
                        alpha = 1.4f - ring
                    }
                    .border(1.dp, NeonMagenta.copy(alpha = 0.4f), CircleShape)
            )
        }
        Box(
            Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = if (enabled || isGenerating) 1f else 0.45f
                }
                .clip(CircleShape)
                .background(
                    if (isGenerating) {
                        Brush.linearGradient(listOf(NeonMagenta, ElectricViolet))
                    } else {
                        Brush.linearGradient(listOf(NeonCyan, ElectricBlue))
                    }
                )
                .clickable(
                    enabled = enabled || isGenerating,
                    interactionSource = interaction,
                    indication = null
                ) {
                    if (isGenerating) onStop() else onSend()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                Box(
                    Modifier
                        .size(13.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DeepSpaceBlack.copy(alpha = 0.9f))
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send message",
                    tint = DeepSpaceBlack,
                    modifier = Modifier.size(23.dp)
                )
            }
        }
    }
}

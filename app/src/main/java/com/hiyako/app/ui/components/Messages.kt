package com.hiyako.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.hiyako.app.ChatMessageUi
import com.hiyako.app.data.MessageType
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonError
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary
import com.hiyako.app.ui.theme.TextTertiary

@Composable
fun MessageList(
    messages: List<ChatMessageUi>,
    isGenerating: Boolean,
    streamingText: String,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message)
        }
        if (isGenerating) {
            item(key = "pending-response") {
                PendingBubble(streamingText = streamingText)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessageUi, modifier: Modifier = Modifier) {
    val isUser = message.isUser
    val shape = RoundedCornerShape(
        topStart = 22.dp,
        topEnd = 22.dp,
        bottomStart = if (isUser) 22.dp else 5.dp,
        bottomEnd = if (isUser) 5.dp else 22.dp
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            NexusAvatar(28.dp)
            Spacer(Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 332.dp)
        ) {
            if (!isUser && !message.model.isNullOrBlank()) {
                Text(
                    text = message.model.orEmpty().uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = TextTertiary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            Box(
                Modifier
                    .clip(shape)
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.18f),
                                    ElectricBlue.copy(alpha = 0.12f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.025f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isUser) {
                            Brush.linearGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.36f),
                                    ElectricBlue.copy(alpha = 0.18f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.11f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            )
                        },
                        shape = shape
                    )
                    .padding(horizontal = 15.dp, vertical = 12.dp)
            ) {
                when (message.type) {
                    MessageType.IMAGE -> GeneratedImage(message.content)
                    MessageType.ERROR -> ErrorMessage(message.content)
                    else -> {
                        SelectionContainer {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                lineHeight = 23.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = relativeTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun GeneratedImage(url: String) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = "Generated image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .widthIn(min = 190.dp, max = 286.dp)
            .heightIn(min = 140.dp, max = 320.dp)
            .clip(RoundedCornerShape(16.dp)),
        loading = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                ThinkingDots(color = NeonCyan)
            }
        },
        error = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White.copy(alpha = 0.03f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Image unavailable",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    )
}

@Composable
private fun ErrorMessage(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = NeonError,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.92f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun PendingBubble(streamingText: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(
        topStart = 22.dp,
        topEnd = 22.dp,
        bottomStart = 5.dp,
        bottomEnd = 22.dp
    )
    val transition = rememberInfiniteTransition(label = "caret")
    val caretAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "caretAlpha"
    )

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        NexusAvatar(28.dp)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.025f))
                    )
                )
                .border(1.dp, NeonCyan.copy(alpha = 0.18f), shape)
                .padding(horizontal = 15.dp, vertical = 13.dp)
        ) {
            if (streamingText.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThinkingDots()
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Nexus is thinking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        append(streamingText)
                        withStyle(SpanStyle(color = NeonCyan.copy(alpha = caretAlpha))) {
                            append("▍")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    lineHeight = 23.sp
                )
            }
        }
    }
}

@Composable
fun ThinkingDots(color: Color = NeonCyan) {
    val transition = rememberInfiniteTransition(label = "thinkingDots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 520,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                Modifier
                    .size(6.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.4f + 0.6f * scale
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

package com.hiyako.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiyako.app.ModelOption
import com.hiyako.app.ui.theme.DeepSpaceElevated
import com.hiyako.app.ui.theme.DeepSpaceSurface
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonError
import com.hiyako.app.ui.theme.SignalGreen
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary
import com.hiyako.app.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSheet(
    models: List<ModelOption>,
    selected: ModelOption,
    onSelect: (ModelOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepSpaceSurface,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NexusAvatar(36.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Choose intelligence",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Every model runs on your own Puter account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 470.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(models, key = { it.id }) { option ->
                    ModelRow(
                        model = option,
                        selected = option.id == selected.id,
                        onClick = { onSelect(option) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Powered by Puter.js · ${models.size} models available",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun ModelRow(model: ModelOption, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) model.accent.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.035f)
            )
            .border(
                1.dp,
                if (selected) model.accent.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.07f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(model.accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = model.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${model.provider} · ${model.tagline}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(model.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSheet(
    chatCount: Int,
    messageCount: Int,
    modelCount: Int,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepSpaceSurface,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricViolet.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Workspace",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Everything you create with Nexus, in one place.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Chats",
                    value = chatCount.toString(),
                    accent = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Messages",
                    value = messageCount.toString(),
                    accent = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Models",
                    value = modelCount.toString(),
                    accent = SignalGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            MicroLabel("Artifacts")
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.1f),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f))
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Your artifacts will live here",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Apps, images and research packs you create.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton(
                    text = "New conversation",
                    onClick = onNewChat,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Add
                )
                OutlineButton(
                    text = "Clear chat",
                    onClick = onClearChat,
                    modifier = Modifier.weight(1f),
                    accent = NeonError,
                    icon = Icons.Rounded.DeleteOutline
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "History is stored locally with Room. Puter handles the intelligence.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepSpaceSurface,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NexusAvatar(42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Nexus AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Deep Core · v$NEXUS_VERSION",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(SignalGreen.copy(alpha = 0.1f))
                        .border(1.dp, SignalGreen.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(5.dp).background(SignalGreen, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "LATEST",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = SignalGreen
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            MicroLabel("What's new")
            Spacer(Modifier.height(10.dp))
            ChangelogRow("Puter.js chat bridge with streaming responses")
            ChangelogRow("Popup sign-in that returns straight to the chat")
            ChangelogRow("Auto-named conversations in a living history drawer")
            ChangelogRow("Deep Core glass UI with aurora backdrop")
            ChangelogRow("Model picker synced to your Puter account")

            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = "View project on GitHub",
                onClick = { uriHandler.openUri("https://github.com/alsbtv/Nexus_V3") },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.SystemUpdate
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Updates arrive automatically with every successful cloud build.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChangelogRow(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(11.dp)
            )
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ClearChatDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(
                    text = "Clear chat",
                    color = NeonError,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Keep it",
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        title = {
            Text(
                text = "Clear this conversation?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Every message in this chat will be deleted from this device. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        containerColor = DeepSpaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .glass(RoundedCornerShape(16.dp), tint = NeonError, alpha = 0.06f, strokeAlpha = 0.24f)
            .padding(start = 12.dp, end = 2.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = NeonError,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            lineHeight = 17.sp
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss",
                tint = TextTertiary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

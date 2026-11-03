package com.hiyako.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiyako.app.data.ChatSessionEntity
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.DeepSpaceSurface
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonError
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.SignalGreen
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary
import com.hiyako.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NexusDrawer(
    sessions: List<ChatSessionEntity>,
    currentSessionId: Long?,
    userName: String?,
    isSignedIn: Boolean,
    totalMessages: Int,
    onClose: () -> Unit,
    onNewChat: () -> Unit,
    onOpenSession: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onWorkspace: () -> Unit,
    onUpdate: () -> Unit,
    onClearChat: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp),
        drawerContainerColor = DeepSpaceSurface,
        drawerContentColor = TextPrimary,
        drawerTonalElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 90.dp, y = (-110).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonCyan.copy(alpha = 0.13f), Color.Transparent)
                        )
                    )
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 18.dp, end = 18.dp, bottom = 14.dp)
            ) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NexusAvatar(44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "NEXUS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                brush = Brush.linearGradient(
                                    listOf(NeonCyan, ElectricBlue, NeonMagenta)
                                ),
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = "DEEP CORE · V$NEXUS_VERSION",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                            color = TextTertiary
                        )
                    }
                    GlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close menu",
                        size = 34.dp,
                        onClick = onClose
                    )
                }

                Spacer(Modifier.height(18.dp))
                if (isSignedIn) {
                    SignedInCard(userName = userName, onSignOut = onSignOut)
                } else {
                    ConnectCard(onSignIn = onSignIn)
                }

                Spacer(Modifier.height(20.dp))
                MicroLabel("Command deck")
                Spacer(Modifier.height(6.dp))

                DrawerAction(
                    icon = Icons.Rounded.Add,
                    title = "New chat",
                    subtitle = "Start from a blank canvas",
                    accent = NeonCyan,
                    onClick = onNewChat
                )
                DrawerAction(
                    icon = Icons.Rounded.Dashboard,
                    title = "Workspace",
                    subtitle = "Your projects and artifacts",
                    accent = ElectricViolet,
                    onClick = onWorkspace
                )
                DrawerAction(
                    icon = Icons.Rounded.SystemUpdate,
                    title = "Update app",
                    subtitle = "Deep Core $NEXUS_VERSION is installed",
                    accent = SignalGreen,
                    onClick = onUpdate
                )
                DrawerAction(
                    icon = Icons.Rounded.DeleteOutline,
                    title = "Clear chat",
                    subtitle = "Delete this conversation",
                    accent = NeonError,
                    onClick = onClearChat
                )

                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MicroLabel("Recent chats")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = sessions.size.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                Spacer(Modifier.height(6.dp))

                if (sessions.isEmpty()) {
                    EmptyHistory()
                    Spacer(Modifier.weight(1f))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            SessionRow(
                                session = session,
                                selected = session.id == currentSessionId,
                                onClick = { onOpenSession(session.id) },
                                onDelete = { onDeleteSession(session.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).background(SignalGreen, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$totalMessages messages stored on this device",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedInCard(userName: String?, onSignOut: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp), alpha = 0.05f)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SignalGreen, NeonCyan))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName?.take(1)?.uppercase() ?: "N",
                color = DeepSpaceBlack,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = userName ?: "Creator",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).background(SignalGreen, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Connected to Puter",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
        IconButton(onClick = onSignOut, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Rounded.Logout,
                contentDescription = "Sign out",
                tint = TextTertiary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun ConnectCard(onSignIn: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(22.dp), alpha = 0.06f, strokeAlpha = 0.16f)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Guest mode",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Connect Puter to unlock frontier models and sync your chats to the cloud.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = "Connect Puter",
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.AutoAwesome
        )
    }
}

@Composable
private fun DrawerAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.1f))
                .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = TextTertiary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SessionRow(
    session: ChatSessionEntity,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) NeonCyan.copy(alpha = 0.08f) else Color.Transparent)
            .then(
                if (selected) Modifier.border(1.dp, NeonCyan.copy(alpha = 0.2f), shape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 2.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) NeonCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = if (selected) NeonCyan else TextTertiary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) TextPrimary else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = relativeTime(session.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Delete chat",
                tint = TextTertiary.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(92.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No conversations yet",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Your first prompt names the chat.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

fun relativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000L
    return when {
        minutes < 1L -> "Just now"
        minutes < 60L -> "${minutes}m ago"
        minutes < 1440L -> "${minutes / 60L}h ago"
        minutes < 10_080L -> "${minutes / 1440L}d ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

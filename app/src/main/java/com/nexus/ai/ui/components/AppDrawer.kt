package com.nexus.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.viewmodel.DrawerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    viewModel: DrawerViewModel,
    onNavigateToSettings: () -> Unit,
    onNewChatClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(320.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search chat content...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                    LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        if (viewModel.chatList.isNotEmpty()) {
                            item {
                                Text("Recent", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
                            }
                            items(viewModel.chatList) { chat ->
                                var showMenu by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                        .background(if (viewModel.selectedChats.contains(chat.id)) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                        .clickable { viewModel.toggleMultiSelect(chat.id) }
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (viewModel.isMultiSelectMode) {
                                        Checkbox(checked = viewModel.selectedChats.contains(chat.id), onCheckedChange = { viewModel.toggleMultiSelect(chat.id) }, modifier = Modifier.padding(end = 8.dp))
                                    }
                                    Text(chat.title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (!viewModel.isMultiSelectMode) {
                                        Box {
                                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                                                DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; viewModel.deleteSingleChat(chat.id) })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (viewModel.isMultiSelectMode) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.deleteSelected() }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", color = Color.Red)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth().clickable { scope.launch { drawerState.close() }; onNavigateToSettings() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("User Profile", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        content = content
    )
}

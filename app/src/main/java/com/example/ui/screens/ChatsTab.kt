package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.components.AvatarView
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppCheckBlue
import com.example.ui.theme.WhatsAppGreen
import com.example.viewmodel.ChatFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsTab(
    chats: List<Chat>,
    selectedFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit,
    onChatClick: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalWhatsAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    text = "Todos",
                    isSelected = selectedFilter == ChatFilter.ALL,
                    onClick = { onFilterSelected(ChatFilter.ALL) }
                )
            }
            item {
                FilterChipItem(
                    text = "No leídos",
                    isSelected = selectedFilter == ChatFilter.UNREAD,
                    onClick = { onFilterSelected(ChatFilter.UNREAD) }
                )
            }
            item {
                FilterChipItem(
                    text = "Favoritos",
                    isSelected = selectedFilter == ChatFilter.FAVORITES,
                    onClick = { onFilterSelected(ChatFilter.FAVORITES) }
                )
            }
            item {
                FilterChipItem(
                    text = "Grupos",
                    isSelected = selectedFilter == ChatFilter.GROUPS,
                    onClick = { onFilterSelected(ChatFilter.GROUPS) }
                )
            }
        }

        // Archived row header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "Archivados",
                tint = extraColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "Archivados",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = extraColors.textPrimary
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 64.dp)
        )

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay chats que coincidan con el filtro",
                    color = extraColors.textSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("chats_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(chats, key = { it.id }) { chat ->
                    ChatItemRow(
                        chat = chat,
                        onClick = { onChatClick(chat.id) },
                        onTogglePin = { onTogglePin(chat.id) },
                        onToggleMute = { onToggleMute(chat.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = WhatsAppGreen.copy(alpha = 0.15f),
            selectedLabelColor = WhatsAppGreen,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = LocalWhatsAppColors.current.textSecondary
        ),
        border = null
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatItemRow(
    chat: Chat,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("chat_item_${chat.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(
                contact = chat.contact,
                isGroup = chat.isGroup,
                size = 52.dp,
                showOnlineBadge = !chat.isGroup
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.contact.name,
                        fontSize = 16.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = extraColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val timeFormatted = chat.lastMessage?.let { formatMessageTime(it.timestamp) } ?: ""
                    Text(
                        text = timeFormatted,
                        fontSize = 12.sp,
                        color = if (chat.unreadCount > 0) WhatsAppGreen else extraColors.textSecondary,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.isTyping) {
                        Text(
                            text = "escribiendo...",
                            fontSize = 14.sp,
                            color = WhatsAppGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Message snippet preview
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (chat.lastMessage?.isFromMe == true) {
                                MessageStatusIcon(status = chat.lastMessage.status)
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            when (chat.lastMessage?.type) {
                                MessageType.AUDIO -> {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Audio",
                                        tint = extraColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                MessageType.IMAGE -> {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Foto",
                                        tint = extraColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                MessageType.LOCATION -> {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Ubicación",
                                        tint = extraColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                else -> Unit
                            }

                            Text(
                                text = chat.lastMessage?.text ?: "Sin mensajes",
                                fontSize = 14.sp,
                                color = extraColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Trailing icons: Pin, Mute, Unread Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (chat.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Silenciado",
                                tint = extraColors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (chat.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Fijado",
                                tint = extraColors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Long-press options menu
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) "Desfijar chat" else "Fijar chat") },
                onClick = {
                    showContextMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isMuted) "Desactivar silencio" else "Silenciar notificaciones") },
                onClick = {
                    showContextMenu = false
                    onToggleMute()
                }
            )
            DropdownMenuItem(
                text = { Text("Marcar como no leído") },
                onClick = { showContextMenu = false }
            )
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> {
            Text("🕒", fontSize = 12.sp)
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Enviado",
                tint = LocalWhatsAppColors.current.checkGray,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Entregado",
                tint = LocalWhatsAppColors.current.checkGray,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Leído",
                tint = WhatsAppCheckBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val sameDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)

    return if (sameDay) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}

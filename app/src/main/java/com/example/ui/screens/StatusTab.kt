package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserStatus
import com.example.ui.components.AvatarView
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppGreen

@Composable
fun StatusTab(
    statuses: List<UserStatus>,
    myStatus: UserStatus?,
    onStatusClick: (String) -> Unit,
    onCreateStatusClick: () -> Unit,
    onCameraStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalWhatsAppColors.current
    val recentStatuses = statuses.filter { !it.isViewed }
    val viewedStatuses = statuses.filter { it.isViewed }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("status_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header section: Estado
            item {
                Text(
                    text = "Estado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Mi Estado row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myStatus != null) onStatusClick(myStatus.id) else onCreateStatusClick()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A884).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yo",
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen,
                                fontSize = 16.sp
                            )
                        }

                        // Plus Green Badge
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir estado",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Mi estado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extraColors.textPrimary
                        )
                        Text(
                            text = if (myStatus != null) myStatus.timeAgo else "Añade una actualización",
                            fontSize = 13.sp,
                            color = extraColors.textSecondary
                        )
                    }
                }
            }

            // Recientes Section
            if (recentStatuses.isNotEmpty()) {
                item {
                    Text(
                        text = "Recientes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textSecondary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(recentStatuses, key = { it.id }) { userStatus ->
                    StatusRowItem(
                        userStatus = userStatus,
                        isViewed = false,
                        onClick = { onStatusClick(userStatus.id) }
                    )
                }
            }

            // Vistos Section
            if (viewedStatuses.isNotEmpty()) {
                item {
                    Text(
                        text = "Vistos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textSecondary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(viewedStatuses, key = { it.id }) { userStatus ->
                    StatusRowItem(
                        userStatus = userStatus,
                        isViewed = true,
                        onClick = { onStatusClick(userStatus.id) }
                    )
                }
            }

            // Channels Section (Canales)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canales",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Explorar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WhatsAppGreen
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ChannelCardItem(name = "WhatsApp", followers = "185 M seguidores", desc = "Novedades oficiales y lanzamientos de funciones.")
                ChannelCardItem(name = "National Geographic", followers = "24 M seguidores", desc = "Historias fascinantes de naturaleza y ciencia.")
                ChannelCardItem(name = "Tech Crunch", followers = "12 M seguidores", desc = "Las noticias más importantes de tecnología e IA.")
            }
        }

        // Floating Action Buttons for Status (Pencil and Camera)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = onCreateStatusClick,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = extraColors.textPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("status_text_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Estado de texto",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = onCameraStatusClick,
                containerColor = WhatsAppGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("status_camera_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Estado de cámara"
                )
            }
        }
    }
}

@Composable
private fun StatusRowItem(
    userStatus: UserStatus,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("status_item_${userStatus.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with colored ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 2.5.dp,
                    color = if (isViewed) extraColors.textSecondary.copy(alpha = 0.4f) else WhatsAppGreen,
                    shape = CircleShape
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            AvatarView(
                contact = userStatus.contact,
                size = 48.dp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = userStatus.contact.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = extraColors.textPrimary
            )
            Text(
                text = userStatus.timeAgo,
                fontSize = 13.sp,
                color = extraColors.textSecondary
            )
        }
    }
}

@Composable
private fun ChannelCardItem(
    name: String,
    followers: String,
    desc: String
) {
    var followed by remember { mutableStateOf(false) }
    val extraColors = LocalWhatsAppColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1),
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppGreen,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = extraColors.textPrimary
                )
                Text(
                    text = followers,
                    fontSize = 12.sp,
                    color = extraColors.textSecondary
                )
                Text(
                    text = desc,
                    fontSize = 12.5.sp,
                    color = extraColors.textPrimary.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }

            Button(
                onClick = { followed = !followed },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (followed) MaterialTheme.colorScheme.surfaceVariant else WhatsAppGreen,
                    contentColor = if (followed) extraColors.textPrimary else Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (followed) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Siguiendo", fontSize = 12.sp)
                } else {
                    Text("Seguir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

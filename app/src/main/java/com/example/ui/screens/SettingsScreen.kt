package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.AvatarView
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppGreen

@Composable
fun SettingsScreen(
    myProfile: Contact,
    isDarkMode: Boolean?,
    onToggleDarkMode: (Boolean?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val extraColors = LocalWhatsAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = extraColors.textPrimary)
            }
            Text(
                text = "Ajustes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = extraColors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // User Profile Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* edit profile */ }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(contact = myProfile, size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = myProfile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = extraColors.textPrimary
                        )
                        Text(
                            text = myProfile.statusText,
                            fontSize = 13.sp,
                            color = extraColors.textSecondary
                        )
                        Text(
                            text = myProfile.phoneNumber,
                            fontSize = 12.5.sp,
                            color = extraColors.textSecondary
                        )
                    }
                    IconButton(onClick = { /* show my QR code */ }) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Código QR",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            // E2EE Card Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = WhatsAppGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cifrado de extremo a extremo activo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = extraColors.textPrimary
                            )
                            Text(
                                text = "Tus mensajes y llamadas personales se protegen con AES-256-GCM. Solo tú y tus contactos tienen las claves.",
                                fontSize = 12.sp,
                                color = extraColors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Dark Mode Toggle Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDarkMode(!(isDarkMode ?: false)) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Modo oscuro",
                        tint = extraColors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tema oscuro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extraColors.textPrimary
                        )
                        Text(
                            text = if (isDarkMode == true) "Activado" else "Desactivado",
                            fontSize = 13.sp,
                            color = extraColors.textSecondary
                        )
                    }
                    Switch(
                        checked = isDarkMode == true,
                        onCheckedChange = { onToggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = WhatsAppGreen, checkedTrackColor = WhatsAppGreen.copy(alpha = 0.4f))
                    )
                }
            }

            // Settings Items
            item {
                SettingsRowItem(
                    icon = Icons.Default.Key,
                    title = "Cuenta",
                    subtitle = "Notificaciones de seguridad, claves de acceso"
                )
                SettingsRowItem(
                    icon = Icons.Default.Lock,
                    title = "Privacidad",
                    subtitle = "Bloqueo con huella, mensajes temporales"
                )
                SettingsRowItem(
                    icon = Icons.Default.Chat,
                    title = "Chats",
                    subtitle = "Fondo de pantalla, copia de seguridad cifrada"
                )
                SettingsRowItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Tonos de mensajes, grupos y llamadas"
                )
                SettingsRowItem(
                    icon = Icons.Default.Storage,
                    title = "Almacenamiento y datos",
                    subtitle = "Uso de red, descarga automática"
                )
                SettingsRowItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Ayuda",
                    subtitle = "Centro de ayuda, política de privacidad"
                )
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    val extraColors = LocalWhatsAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* action */ }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = extraColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = extraColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = extraColors.textSecondary
            )
        }
    }
}

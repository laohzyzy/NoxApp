package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.CallDirection
import com.example.data.model.CallLog
import com.example.data.model.Contact
import com.example.ui.components.AvatarView
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppDangerRed
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsTab(
    callLogs: List<CallLog>,
    onStartCall: (Contact, Boolean) -> Unit,
    onNewCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalWhatsAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("calls_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Create Call Link Item
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* create call link */ }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Crear enlace de llamada",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Crear enlace de llamada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extraColors.textPrimary
                        )
                        Text(
                            text = "Comparte un enlace para tu llamada cifrada",
                            fontSize = 13.sp,
                            color = extraColors.textSecondary
                        )
                    }
                }
            }

            // Recientes Header
            item {
                Text(
                    text = "Recientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textPrimary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
                )
            }

            if (callLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros de llamadas recientes",
                            color = extraColors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(callLogs, key = { it.id }) { call ->
                    CallLogRowItem(
                        callLog = call,
                        onCallClick = { onStartCall(call.contact, call.isVideo) }
                    )
                }
            }

            // E2EE Notice at bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = extraColors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tus llamadas personales están cifradas de extremo a extremo",
                        fontSize = 12.sp,
                        color = extraColors.textSecondary
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewCallClick,
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp)
                .testTag("new_call_fab")
        ) {
            Icon(
                imageVector = Icons.Default.AddIcCall,
                contentDescription = "Iniciar nueva llamada"
            )
        }
    }
}

@Composable
private fun CallLogRowItem(
    callLog: CallLog,
    onCallClick: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("call_item_${callLog.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(contact = callLog.contact, size = 52.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callLog.contact.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (callLog.direction == CallDirection.MISSED) WhatsAppDangerRed else extraColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                when (callLog.direction) {
                    CallDirection.INCOMING -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = "Entrante",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    CallDirection.OUTGOING -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = "Saliente",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    CallDirection.MISSED -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMissed,
                            contentDescription = "Perdida",
                            tint = WhatsAppDangerRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val formattedDate = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale("es")).format(Date(callLog.timestamp))
                Text(
                    text = formattedDate,
                    fontSize = 13.sp,
                    color = extraColors.textSecondary
                )
            }
        }

        IconButton(
            onClick = onCallClick,
            modifier = Modifier.testTag("call_item_button_${callLog.id}")
        ) {
            Icon(
                imageVector = if (callLog.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = if (callLog.isVideo) "Videollamada" else "Llamar",
                tint = WhatsAppGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

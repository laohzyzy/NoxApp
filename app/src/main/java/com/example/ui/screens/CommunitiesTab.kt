package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppGreen

@Composable
fun CommunitiesTab(
    onNewCommunityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalWhatsAppColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("communities_tab"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Comunidades",
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Mantén a tus comunidades conectadas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = extraColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Las comunidades reúnen a los miembros en grupos por temas, permitiendo enviar avisos generales y organizar conversaciones.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = extraColors.textSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNewCommunityClick,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Iniciar una comunidad",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            CommunityCard(
                title = "Comunidad Vecinal Los Olivos",
                groupCount = "4 subgrupos • 128 miembros",
                announcement = "Avisos importantes: Mañana limpieza general de áreas comunes 🌳"
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            CommunityCard(
                title = "Equipo de Desarrollo & Tech",
                groupCount = "6 subgrupos • 42 ingenieros",
                announcement = "Despliegue exitoso del nuevo protocolo de cifrado E2EE 🚀"
            )
        }
    }
}

@Composable
private fun CommunityCard(
    title: String,
    groupCount: String,
    announcement: String
) {
    val extraColors = LocalWhatsAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WhatsAppGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = extraColors.textPrimary
                    )
                    Text(
                        text = groupCount,
                        fontSize = 12.sp,
                        color = extraColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = announcement,
                    fontSize = 13.sp,
                    color = extraColors.textPrimary,
                    maxLines = 2
                )
            }
        }
    }
}

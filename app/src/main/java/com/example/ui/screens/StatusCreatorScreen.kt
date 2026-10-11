package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen

@Composable
fun StatusCreatorScreen(
    onDismiss: () -> Unit,
    onPublishStatus: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onDismiss() }

    val statusColors = listOf(
        0xFF8E24AA, // Purple
        0xFFD81B60, // Pink
        0xFF00897B, // Teal
        0xFF1E88E5, // Blue
        0xFFFB8C00, // Orange
        0xFFE53935  // Red
    )

    var colorIndex by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    val currentColor = statusColors[colorIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(currentColor))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("status_creator_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Color switch button
                IconButton(
                    onClick = {
                        colorIndex = (colorIndex + 1) % statusColors.size
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Cambiar color",
                        tint = Color.White
                    )
                }
            }

            // Text Input in the middle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_text_input"),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Escribe un estado...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Bottom bar with Send FAB
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (text.isNotBlank()) {
                    FloatingActionButton(
                        onClick = {
                            onPublishStatus(text, currentColor)
                        },
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("publish_status_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Publicar")
                    }
                }
            }
        }
    }
}

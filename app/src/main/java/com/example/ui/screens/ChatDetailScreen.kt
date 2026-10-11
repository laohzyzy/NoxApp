package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Chat
import com.example.data.model.Contact
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.ui.components.AvatarView
import com.example.ui.components.MessageCryptoDetailSheet
import com.example.ui.components.SecurityVerifySheet
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppAudioPlayedBlue
import com.example.ui.theme.WhatsAppDangerRed
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat?,
    messages: List<Message>,
    composerText: String,
    replyingTo: Message?,
    isRecordingAudio: Boolean,
    recordingDurationSec: Int,
    playingAudioId: String?,
    audioProgress: Float,
    selectedMessage: Message?,
    onBack: () -> Unit,
    onComposerTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendLocation: () -> Unit,
    onSendImage: (String) -> Unit,
    onStartRecording: () -> Unit,
    onFinishRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onToggleAudioPlayback: (String, Int) -> Unit,
    onSelectMessage: (Message?) -> Unit,
    onReactToMessage: (String) -> Unit,
    onDeleteSelectedMessage: () -> Unit,
    onToggleStarSelectedMessage: () -> Unit,
    onSetReplyingTo: (Message?) -> Unit,
    onStartCall: (Contact, Boolean) -> Unit,
    onVerifySecurity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (selectedMessage != null) {
            onSelectMessage(null)
        } else {
            onBack()
        }
    }

    if (chat == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chat no encontrado")
        }
        return
    }

    val extraColors = LocalWhatsAppColors.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showSecuritySheet by remember { mutableStateOf(false) }
    var viewingCryptoMessage by remember { mutableStateOf<Message?>(null) }

    var showMicPermissionDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartRecording()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartCall(chat.contact, true)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(extraColors.chatBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (extraColors.isDark) 0.08f else 0.45f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top App Bar
            ChatTopBar(
                contact = chat.contact,
                isGroup = chat.isGroup,
                isTyping = chat.isTyping,
                selectedMessage = selectedMessage,
                onBack = onBack,
                onAudioCall = {
                    val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (micGranted) {
                        onStartCall(chat.contact, false)
                    } else {
                        showMicPermissionDialog = true
                    }
                },
                onVideoCall = {
                    val camGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (camGranted) {
                        onStartCall(chat.contact, true)
                    } else {
                        showCameraPermissionDialog = true
                    }
                },
                onDeleteMessage = onDeleteSelectedMessage,
                onStarMessage = onToggleStarSelectedMessage,
                onViewCryptoDetails = {
                    viewingCryptoMessage = selectedMessage
                    onSelectMessage(null)
                },
                onReplyMessage = {
                    onSetReplyingTo(selectedMessage)
                    onSelectMessage(null)
                },
                onClearSelection = { onSelectMessage(null) },
                onOpenSecurityVerify = { showSecuritySheet = true }
            )

            // Reactions Quick Bar
            AnimatedVisibility(
                visible = selectedMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ReactionsBar(onReactionSelected = onReactToMessage)
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    EncryptionDisclaimerPill(
                        onPillClick = { showSecuritySheet = true }
                    )
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isGroup = chat.isGroup,
                        contact = chat.contact,
                        isPlaying = playingAudioId == msg.id,
                        audioProgress = if (playingAudioId == msg.id) audioProgress else 0f,
                        isSelected = selectedMessage?.id == msg.id,
                        onClick = {
                            if (selectedMessage != null) onSelectMessage(null)
                        },
                        onLongClick = { onSelectMessage(msg) },
                        onToggleAudio = { onToggleAudioPlayback(msg.id, msg.audioDurationSec) }
                    )
                }
            }

            // Bottom Input Section
            ChatBottomInput(
                composerText = composerText,
                replyingTo = replyingTo,
                isRecording = isRecordingAudio,
                recordingDurationSec = recordingDurationSec,
                showEmojiPicker = showEmojiPicker,
                onComposerTextChange = onComposerTextChange,
                onSendMessage = onSendMessage,
                onToggleEmoji = { showEmojiPicker = !showEmojiPicker },
                onOpenAttachments = { showAttachmentSheet = true },
                onQuickCamera = { onSendImage("Foto instantánea 📸") },
                onStartRecording = {
                    val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (micGranted) {
                        onStartRecording()
                    } else {
                        showMicPermissionDialog = true
                    }
                },
                onFinishRecording = onFinishRecording,
                onCancelRecording = onCancelRecording,
                onCancelReply = { onSetReplyingTo(null) }
            )

            if (showEmojiPicker) {
                EmojiPickerGrid(
                    onEmojiClicked = { emoji ->
                        onComposerTextChange(composerText + emoji)
                    }
                )
            }
        }

        // Microphone Permission Dialog
        if (showMicPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showMicPermissionDialog = false },
                title = { Text("Permitir que WhatsApp grabe audio", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Para grabar y enviar notas de voz con cifrado de extremo a extremo, WhatsApp necesita permiso para acceder a tu micrófono.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showMicPermissionDialog = false
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMicPermissionDialog = false }) {
                        Text("Ahora no")
                    }
                }
            )
        }

        // Camera Permission Dialog
        if (showCameraPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showCameraPermissionDialog = false },
                title = { Text("Permitir acceso a la cámara", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Para realizar videollamadas con cifrado de extremo a extremo, WhatsApp necesita permiso para acceder a tu cámara.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCameraPermissionDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCameraPermissionDialog = false }) {
                        Text("Ahora no")
                    }
                }
            )
        }

        // Security 60-digit Verification Sheet
        if (showSecuritySheet) {
            SecurityVerifySheet(
                contact = chat.contact,
                onDismiss = { showSecuritySheet = false },
                onMarkVerified = {
                    onVerifySecurity(chat.contact.id)
                }
            )
        }

        // Cryptographic Inspection Sheet
        if (viewingCryptoMessage != null) {
            MessageCryptoDetailSheet(
                message = viewingCryptoMessage!!,
                onDismiss = { viewingCryptoMessage = null }
            )
        }

        // WhatsApp Attachments Bottom Sheet
        if (showAttachmentSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachmentSheet = false },
                onSendLocation = {
                    showAttachmentSheet = false
                    onSendLocation()
                },
                onSendGallery = {
                    showAttachmentSheet = false
                    onSendImage("Foto cifrada de la galería 🖼️")
                },
                onSendDocument = {
                    showAttachmentSheet = false
                    onSendImage("Documento_confidencial.pdf 📄")
                }
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    contact: Contact,
    isGroup: Boolean,
    isTyping: Boolean,
    selectedMessage: Message?,
    onBack: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onDeleteMessage: () -> Unit,
    onStarMessage: () -> Unit,
    onViewCryptoDetails: () -> Unit,
    onReplyMessage: () -> Unit,
    onClearSelection: () -> Unit,
    onOpenSecurityVerify: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (selectedMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }
                Text(
                    text = "1 seleccionado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onViewCryptoDetails, modifier = Modifier.testTag("action_view_crypto")) {
                    Icon(Icons.Default.Shield, contentDescription = "Ver cifrado E2EE", tint = WhatsAppGreen)
                }
                IconButton(onClick = onReplyMessage) {
                    Icon(Icons.Default.Reply, contentDescription = "Responder")
                }
                IconButton(onClick = onStarMessage) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Destacar",
                        tint = if (selectedMessage.isStarred) Color(0xFFFFB300) else extraColors.textPrimary
                    )
                }
                IconButton(onClick = onDeleteMessage) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = WhatsAppDangerRed)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = extraColors.textPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenSecurityVerify),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(contact = contact, isGroup = isGroup, size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extraColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (contact.isSecurityVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Cifrado verificado",
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isTyping) "escribiendo..." else if (contact.isOnline) "en línea 🔒" else contact.lastSeen,
                            fontSize = 12.sp,
                            color = if (isTyping) WhatsAppGreen else extraColors.textSecondary,
                            fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                IconButton(onClick = onVideoCall, modifier = Modifier.testTag("chat_video_call_button")) {
                    Icon(Icons.Default.Videocam, contentDescription = "Videollamada", tint = extraColors.textPrimary)
                }

                IconButton(onClick = onAudioCall, modifier = Modifier.testTag("chat_audio_call_button")) {
                    Icon(Icons.Default.Call, contentDescription = "Llamada", tint = extraColors.textPrimary)
                }

                IconButton(onClick = onOpenSecurityVerify, modifier = Modifier.testTag("chat_security_icon")) {
                    Icon(Icons.Default.Lock, contentDescription = "Cifrado E2EE", tint = WhatsAppGreen)
                }
            }
        }
    }
}

@Composable
private fun ReactionsBar(onReactionSelected: (String) -> Unit) {
    val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable { onReactionSelected(emoji) }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun EncryptionDisclaimerPill(onPillClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFE082).copy(alpha = 0.28f)
            ),
            modifier = Modifier
                .clickable(onClick = onPillClick)
                .testTag("e2ee_disclaimer_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF795548),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Los mensajes y las llamadas están cifrados de extremo a extremo. Nadie fuera de este chat, ni siquiera WhatsApp, puede leerlos ni escucharlos. Toca para verificar el código.",
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    color = LocalWhatsAppColors.current.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    isGroup: Boolean,
    contact: Contact,
    isPlaying: Boolean,
    audioProgress: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleAudio: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current
    val isMe = message.isFromMe

    val bubbleColor = if (isMe) extraColors.outgoingBubble else extraColors.incomingBubble
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) bubbleColor.copy(alpha = 0.8f) else bubbleColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .widthIn(min = 80.dp, max = 310.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .testTag("bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (isGroup && !isMe) {
                    Text(
                        text = contact.name,
                        color = Color(contact.avatarColorSeed),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (message.replyToText != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(WhatsAppGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Mensaje",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppGreen
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 11.5.sp,
                                    color = extraColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            fontSize = 15.sp,
                            color = extraColors.textPrimary,
                            lineHeight = 20.sp
                        )
                    }

                    MessageType.AUDIO -> {
                        AudioNotePlayer(
                            isPlaying = isPlaying,
                            progress = audioProgress,
                            durationSec = message.audioDurationSec,
                            onTogglePlay = onToggleAudio
                        )
                    }

                    MessageType.IMAGE -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Foto",
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            if (message.text.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    fontSize = 14.5.sp,
                                    color = extraColors.textPrimary
                                )
                            }
                        }
                    }

                    MessageType.LOCATION -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF81C784).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Ubicación",
                                        tint = WhatsAppDangerRed,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "Ubicación encriptada",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = extraColors.textPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = extraColors.textPrimary
                            )
                        }
                    }
                }

                // Time, Lock & status ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Destacado",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier
                                .size(12.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cifrado E2EE",
                        tint = extraColors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(10.dp)
                            .padding(end = 3.dp)
                    )

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 11.sp,
                        color = extraColors.textSecondary
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.status)
                    }
                }

                if (message.reactions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 2.dp)
                    ) {
                        Text(
                            text = message.reactions.joinToString(" "),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioNotePlayer(
    isPlaying: Boolean,
    progress: Float,
    durationSec: Int,
    onTogglePlay: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(40.dp)
                .background(WhatsAppGreen, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = WhatsAppAudioPlayedBlue,
                trackColor = extraColors.textSecondary.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(4.dp))

            val remainingSec = if (isPlaying) {
                ((1f - progress) * durationSec).toInt()
            } else durationSec

            Text(
                text = String.format("%02d:%02d", remainingSec / 60, remainingSec % 60),
                fontSize = 11.sp,
                color = extraColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(WhatsAppGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = WhatsAppGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChatBottomInput(
    composerText: String,
    replyingTo: Message?,
    isRecording: Boolean,
    recordingDurationSec: Int,
    showEmojiPicker: Boolean,
    onComposerTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleEmoji: () -> Unit,
    onOpenAttachments: () -> Unit,
    onQuickCamera: () -> Unit,
    onStartRecording: () -> Unit,
    onFinishRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onCancelReply: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        if (replyingTo != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(32.dp)
                            .background(WhatsAppGreen, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (replyingTo.isFromMe) "Tú" else "Contacto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WhatsAppGreen
                        )
                        Text(
                            text = replyingTo.text,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = extraColors.textSecondary
                        )
                    }
                    IconButton(onClick = onCancelReply) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDangerRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%02d:%02d", recordingDurationSec / 60, recordingDurationSec % 60),
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppDangerRed,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Grabando audio cifrado...",
                        fontSize = 13.sp,
                        color = extraColors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancelar",
                        color = WhatsAppDangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onCancelRecording() }
                            .padding(4.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleEmoji, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emojis",
                            tint = if (showEmojiPicker) WhatsAppGreen else extraColors.textSecondary
                        )
                    }

                    BasicTextField(
                        value = composerText,
                        onValueChange = onComposerTextChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .testTag("chat_message_input"),
                        textStyle = TextStyle(color = extraColors.textPrimary, fontSize = 16.sp),
                        cursorBrush = SolidColor(WhatsAppGreen),
                        decorationBox = { innerTextField ->
                            if (composerText.isEmpty()) {
                                Text(
                                    text = "Mensaje cifrado",
                                    color = extraColors.textSecondary,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(onClick = onOpenAttachments, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar", tint = extraColors.textSecondary)
                    }

                    if (composerText.isEmpty()) {
                        IconButton(onClick = onQuickCamera, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Cámara", tint = extraColors.textSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen)
                    .clickable {
                        if (isRecording) {
                            onFinishRecording()
                        } else if (composerText.isNotBlank()) {
                            onSendMessage()
                        } else {
                            onStartRecording()
                        }
                    }
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                if (composerText.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar cifrado", tint = Color.White, modifier = Modifier.size(20.dp))
                } else if (isRecording) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar audio", tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Mic, contentDescription = "Grabar audio cifrado", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun EmojiPickerGrid(onEmojiClicked: (String) -> Unit) {
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
        "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
        "🔒", "🔑", "🛡️", "❤️", "👍", "🔥", "✨", "🎉",
        "💯", "⚽", "🏖️", "🍕", "🚀", "🙌", "👏", "🙏"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            val chunks = emojis.chunked(8)
            items(chunks) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    row.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { onEmojiClicked(emoji) }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSendLocation: () -> Unit,
    onSendGallery: () -> Unit,
    onSendDocument: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Compartir (Cifrado E2EE 🔒)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionItem(icon = Icons.Default.Description, label = "Documento", bgColor = Color(0xFF7F66FF), onClick = onSendDocument)
                AttachmentOptionItem(icon = Icons.Default.PhotoCamera, label = "Cámara", bgColor = Color(0xFFE91E63), onClick = onSendGallery)
                AttachmentOptionItem(icon = Icons.Default.Image, label = "Galería", bgColor = Color(0xFF9C27B0), onClick = onSendGallery)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionItem(icon = Icons.Default.Mic, label = "Audio", bgColor = Color(0xFFFF9800), onClick = onSendGallery)
                AttachmentOptionItem(icon = Icons.Default.Place, label = "Ubicación", bgColor = Color(0xFF4CAF50), onClick = onSendLocation)
                AttachmentOptionItem(icon = Icons.Default.Reply, label = "Contacto", bgColor = Color(0xFF00BCD4), onClick = onDismiss)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.5.sp, color = LocalWhatsAppColors.current.textPrimary)
    }
}

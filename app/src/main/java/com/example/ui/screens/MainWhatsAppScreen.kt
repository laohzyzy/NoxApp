package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.CreateContactDialog
import com.example.ui.components.CreateGroupDialog
import com.example.ui.components.NewChatSheet
import com.example.ui.components.WhatsAppBottomNav
import com.example.ui.components.WhatsAppTopBar
import com.example.ui.screens.onboarding.OnboardingFlowScreen
import com.example.ui.theme.WhatsAppGreen
import com.example.viewmodel.MainTab
import com.example.viewmodel.WhatsAppNavDestination
import com.example.viewmodel.WhatsAppViewModel

@Composable
fun MainWhatsAppScreen(
    viewModel: WhatsAppViewModel
) {
    val isRegistered by viewModel.isRegistered.collectAsState()
    if (!isRegistered) {
        OnboardingFlowScreen(
            onRegistrationComplete = { phone, name ->
                viewModel.completeRegistration(phone, name)
            }
        )
        return
    }

    val destination by viewModel.currentDestination.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedFilter by viewModel.chatFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val filteredChats by viewModel.filteredChats.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()

    val currentChat by viewModel.currentChat.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val composerText by viewModel.composerText.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val recordingDurationSec by viewModel.recordingDurationSec.collectAsState()
    val playingAudioId by viewModel.playingAudioMessageId.collectAsState()
    val audioProgress by viewModel.audioProgress.collectAsState()
    val selectedMessage by viewModel.selectedMessage.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showNewChatSheet by remember { mutableStateOf(false) }
    var showCreateContactDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    when (val dest = destination) {
        is WhatsAppNavDestination.ChatDetail -> {
            ChatDetailScreen(
                chat = currentChat,
                messages = currentMessages,
                composerText = composerText,
                replyingTo = replyingTo,
                isRecordingAudio = isRecordingAudio,
                recordingDurationSec = recordingDurationSec,
                playingAudioId = playingAudioId,
                audioProgress = audioProgress,
                selectedMessage = selectedMessage,
                onBack = { viewModel.navigateBack() },
                onComposerTextChange = { viewModel.setComposerText(it) },
                onSendMessage = { viewModel.sendMessage() },
                onSendLocation = { viewModel.sendLocationMessage() },
                onSendImage = { viewModel.sendImageMessage(it) },
                onStartRecording = { viewModel.startAudioRecording() },
                onFinishRecording = { viewModel.finishAudioRecording() },
                onCancelRecording = { viewModel.cancelAudioRecording() },
                onToggleAudioPlayback = { id, dur -> viewModel.toggleAudioPlayback(id, dur) },
                onSelectMessage = { viewModel.setSelectedMessage(it) },
                onReactToMessage = { viewModel.reactToMessage(it) },
                onDeleteSelectedMessage = { viewModel.deleteSelectedMessage() },
                onToggleStarSelectedMessage = { viewModel.toggleStarSelectedMessage() },
                onSetReplyingTo = { viewModel.setReplyingTo(it) },
                onStartCall = { contact, isVideo -> viewModel.startCall(contact, isVideo) },
                onVerifySecurity = { viewModel.verifyContactSecurity(it) }
            )
        }

        is WhatsAppNavDestination.ActiveCall -> {
            ActiveCallScreen(
                callState = callState,
                onEndCall = { viewModel.endCall() },
                onToggleMute = { viewModel.toggleCallMute() },
                onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                onToggleVideo = { viewModel.toggleCallVideo() }
            )
        }

        is WhatsAppNavDestination.StatusViewer -> {
            val userStatus = statuses.find { it.id == dest.statusId } ?: myStatus
            StatusViewerScreen(
                userStatus = userStatus,
                onClose = { viewModel.navigateBack() },
                onReplyToStatus = { reply ->
                    if (userStatus != null) {
                        viewModel.navigateTo(WhatsAppNavDestination.ChatDetail(userStatus.contact.id))
                        viewModel.setComposerText(reply)
                    }
                }
            )
        }

        is WhatsAppNavDestination.StatusCreator -> {
            StatusCreatorScreen(
                onDismiss = { viewModel.navigateBack() },
                onPublishStatus = { text, color ->
                    viewModel.addTextStatus(text, color)
                }
            )
        }

        is WhatsAppNavDestination.Settings -> {
            SettingsScreen(
                myProfile = myProfile,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                onBack = { viewModel.navigateBack() }
            )
        }

        is WhatsAppNavDestination.Main -> {
            Scaffold(
                topBar = {
                    WhatsAppTopBar(
                        searchQuery = searchQuery,
                        isSearchActive = isSearchActive,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchActiveChange = { viewModel.setSearchActive(it) },
                        onNewGroupClick = { showCreateGroupDialog = true },
                        onSettingsClick = { viewModel.navigateTo(WhatsAppNavDestination.Settings) },
                        onCameraClick = { viewModel.navigateTo(WhatsAppNavDestination.StatusCreator) }
                    )
                },
                bottomBar = {
                    val totalUnread = filteredChats.sumOf { it.unreadCount }
                    val hasUnseen = statuses.any { !it.isViewed }
                    WhatsAppBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        unreadChatsCount = totalUnread,
                        hasUnseenStatus = hasUnseen
                    )
                },
                floatingActionButton = {
                    if (selectedTab == MainTab.CHATS) {
                        FloatingActionButton(
                            onClick = { showNewChatSheet = true },
                            containerColor = WhatsAppGreen,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("fab_new_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = "Nuevo chat cifrado"
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_switch"
                    ) { tab ->
                        when (tab) {
                            MainTab.CHATS -> {
                                ChatsTab(
                                    chats = filteredChats,
                                    selectedFilter = selectedFilter,
                                    onFilterSelected = { viewModel.setChatFilter(it) },
                                    onChatClick = { chatId ->
                                        viewModel.navigateTo(WhatsAppNavDestination.ChatDetail(chatId))
                                    },
                                    onTogglePin = { viewModel.togglePin(it) },
                                    onToggleMute = { viewModel.toggleMute(it) }
                                )
                            }

                            MainTab.UPDATES -> {
                                StatusTab(
                                    statuses = statuses,
                                    myStatus = myStatus,
                                    onStatusClick = { viewModel.openStatusViewer(it) },
                                    onCreateStatusClick = { viewModel.navigateTo(WhatsAppNavDestination.StatusCreator) },
                                    onCameraStatusClick = { viewModel.navigateTo(WhatsAppNavDestination.StatusCreator) }
                                )
                            }

                            MainTab.COMMUNITIES -> {
                                CommunitiesTab(
                                    onNewCommunityClick = { showCreateGroupDialog = true }
                                )
                            }

                            MainTab.CALLS -> {
                                CallsTab(
                                    callLogs = callLogs,
                                    onStartCall = { contact, isVideo ->
                                        viewModel.startCall(contact, isVideo)
                                    },
                                    onNewCallClick = { showNewChatSheet = true }
                                )
                            }
                        }
                    }
                }
            }

            // Sheets and Dialogs
            if (showNewChatSheet) {
                NewChatSheet(
                    contacts = allContacts,
                    onDismiss = { showNewChatSheet = false },
                    onSelectContact = { contact ->
                        showNewChatSheet = false
                        viewModel.navigateTo(WhatsAppNavDestination.ChatDetail(contact.id))
                    },
                    onNewGroupClick = {
                        showNewChatSheet = false
                        showCreateGroupDialog = true
                    },
                    onCreateContactClick = {
                        showNewChatSheet = false
                        showCreateContactDialog = true
                    }
                )
            }

            if (showCreateContactDialog) {
                CreateContactDialog(
                    onDismiss = { showCreateContactDialog = false },
                    onCreateContact = { name, phone ->
                        showCreateContactDialog = false
                        viewModel.createChat(name, phone)
                    }
                )
            }

            if (showCreateGroupDialog) {
                CreateGroupDialog(
                    contacts = allContacts,
                    onDismiss = { showCreateGroupDialog = false },
                    onCreateGroup = { name, members ->
                        showCreateGroupDialog = false
                        viewModel.createGroup(name, members)
                    }
                )
            }
        }
    }
}

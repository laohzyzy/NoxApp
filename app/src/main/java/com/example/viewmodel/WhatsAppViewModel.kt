package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.data.model.CallDirection
import com.example.data.model.Chat
import com.example.data.model.Contact
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.UserStatus
import com.example.data.repository.WhatsAppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS, UPDATES, COMMUNITIES, CALLS
}

enum class ChatFilter {
    ALL, UNREAD, FAVORITES, GROUPS
}

sealed class WhatsAppNavDestination {
    data object Main : WhatsAppNavDestination()
    data class ChatDetail(val chatId: String) : WhatsAppNavDestination()
    data class StatusViewer(val statusId: String) : WhatsAppNavDestination()
    data object StatusCreator : WhatsAppNavDestination()
    data class ActiveCall(val contact: Contact, val isVideo: Boolean, val isIncoming: Boolean) : WhatsAppNavDestination()
    data object Settings : WhatsAppNavDestination()
}

class WhatsAppViewModel(
    private val repository: WhatsAppRepository = WhatsAppRepository()
) : ViewModel() {

    // Current navigation destination
    private val _currentDestination = MutableStateFlow<WhatsAppNavDestination>(WhatsAppNavDestination.Main)
    val currentDestination: StateFlow<WhatsAppNavDestination> = _currentDestination.asStateFlow()

    // Navigation history for back handling
    private val navStack = mutableListOf<WhatsAppNavDestination>(WhatsAppNavDestination.Main)

    // Active bottom navigation tab
    private val _selectedTab = MutableStateFlow(MainTab.CHATS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    // Filter chip in chats tab
    private val _chatFilter = MutableStateFlow(ChatFilter.ALL)
    val chatFilter: StateFlow<ChatFilter> = _chatFilter.asStateFlow()

    // Global Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Dark Mode Override (User preference in Settings)
    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // Data from repository
    val isRegistered = repository.isRegistered
    val chats = repository.chats
    val statuses = repository.statuses
    val myStatus = repository.myStatus
    val callLogs = repository.callLogs
    val myProfile = repository.myProfile
    val allContacts = repository.allContacts

    fun completeRegistration(phone: String, name: String) {
        repository.completeRegistration(phone, name)
    }

    // Filtered chats based on filter & search
    val filteredChats: StateFlow<List<Chat>> = combine(
        chats,
        chatFilter,
        searchQuery
    ) { chatList, filter, query ->
        chatList.filter { chat ->
            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> chat.unreadCount > 0
                ChatFilter.FAVORITES -> chat.isPinned
                ChatFilter.GROUPS -> chat.isGroup
            }
            val matchesQuery = if (query.isBlank()) true else {
                chat.contact.name.contains(query, ignoreCase = true) ||
                        (chat.lastMessage?.text?.contains(query, ignoreCase = true) ?: false)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    val currentChat: StateFlow<Chat?> = combine(chats, _activeChatId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<Message>> = combine(repository.messages, _activeChatId) { map, id ->
        if (id != null) map[id] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message composer state
    private val _composerText = MutableStateFlow("")
    val composerText: StateFlow<String> = _composerText.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    // Audio recording simulation state
    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private var recordingJob: Job? = null

    // Audio playback state
    private val _playingAudioMessageId = MutableStateFlow<String?>(null)
    val playingAudioMessageId: StateFlow<String?> = _playingAudioMessageId.asStateFlow()

    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress.asStateFlow()

    private var audioPlaybackJob: Job? = null

    // Active Call Simulation
    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    private var callTimerJob: Job? = null

    // New Chat / Group Dialog State
    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog.asStateFlow()

    // Selected message for contextual options (Reactions, delete, star, info)
    private val _selectedMessage = MutableStateFlow<Message?>(null)
    val selectedMessage: StateFlow<Message?> = _selectedMessage.asStateFlow()

    fun navigateTo(dest: WhatsAppNavDestination) {
        navStack.add(dest)
        _currentDestination.value = dest
        if (dest is WhatsAppNavDestination.ChatDetail) {
            _activeChatId.value = dest.chatId
            repository.setCurrentChat(dest.chatId)
        }
    }

    fun navigateBack(): Boolean {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
            val previous = navStack.last()
            _currentDestination.value = previous
            if (previous !is WhatsAppNavDestination.ChatDetail) {
                _activeChatId.value = null
                repository.setCurrentChat(null)
            }
            return true
        }
        return false
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun setComposerText(text: String) {
        _composerText.value = text
    }

    fun setReplyingTo(message: Message?) {
        _replyingToMessage.value = message
    }

    fun setSelectedMessage(message: Message?) {
        _selectedMessage.value = message
    }

    fun sendMessage() {
        val chatId = _activeChatId.value ?: return
        val text = _composerText.value.trim()
        if (text.isEmpty()) return

        repository.sendMessage(
            chatId = chatId,
            text = text,
            type = MessageType.TEXT,
            replyTo = _replyingToMessage.value
        )
        _composerText.value = ""
        _replyingToMessage.value = null
    }

    fun sendLocationMessage() {
        val chatId = _activeChatId.value ?: return
        repository.sendMessage(
            chatId = chatId,
            text = "📍 Ubicación en tiempo real compartida (Av. Corrientes 1234)",
            type = MessageType.LOCATION
        )
    }

    fun sendImageMessage(caption: String = "Foto compartida 📷") {
        val chatId = _activeChatId.value ?: return
        repository.sendMessage(
            chatId = chatId,
            text = caption,
            type = MessageType.IMAGE
        )
    }

    fun startAudioRecording() {
        _isRecordingAudio.value = true
        _recordingDurationSec.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_isRecordingAudio.value) {
                delay(1000)
                _recordingDurationSec.update { it + 1 }
            }
        }
    }

    fun cancelAudioRecording() {
        _isRecordingAudio.value = false
        _recordingDurationSec.value = 0
        recordingJob?.cancel()
    }

    fun finishAudioRecording() {
        val chatId = _activeChatId.value ?: return
        val duration = _recordingDurationSec.value.coerceAtLeast(1)
        _isRecordingAudio.value = false
        recordingJob?.cancel()

        repository.sendMessage(
            chatId = chatId,
            text = "Nota de voz ($duration s)",
            type = MessageType.AUDIO,
            audioDuration = duration
        )
        _recordingDurationSec.value = 0
    }

    fun toggleAudioPlayback(messageId: String, durationSec: Int) {
        if (_playingAudioMessageId.value == messageId) {
            // Pause
            _playingAudioMessageId.value = null
            audioPlaybackJob?.cancel()
            _audioProgress.value = 0f
        } else {
            // Play
            audioPlaybackJob?.cancel()
            _playingAudioMessageId.value = messageId
            _audioProgress.value = 0f
            val sec = durationSec.coerceAtLeast(3)

            audioPlaybackJob = viewModelScope.launch {
                try {
                    AudioPlayerManager.playSynthesizedAudioNote(sec) { fraction ->
                        _audioProgress.value = fraction
                    }
                } catch (e: Exception) {
                    val totalMillis = sec * 1000L
                    val step = 100L
                    var elapsed = 0L
                    while (elapsed < totalMillis) {
                        delay(step)
                        elapsed += step
                        _audioProgress.value = (elapsed.toFloat() / totalMillis).coerceIn(0f, 1f)
                    }
                } finally {
                    _playingAudioMessageId.value = null
                    _audioProgress.value = 0f
                }
            }
        }
    }

    fun reactToMessage(reaction: String) {
        val msg = _selectedMessage.value ?: return
        val chatId = _activeChatId.value ?: return
        repository.addReaction(chatId, msg.id, reaction)
        _selectedMessage.value = null
    }

    fun deleteSelectedMessage() {
        val msg = _selectedMessage.value ?: return
        val chatId = _activeChatId.value ?: return
        repository.deleteMessage(chatId, msg.id)
        _selectedMessage.value = null
    }

    fun toggleStarSelectedMessage() {
        val msg = _selectedMessage.value ?: return
        val chatId = _activeChatId.value ?: return
        repository.toggleStarMessage(chatId, msg.id)
        _selectedMessage.value = null
    }

    fun startCall(contact: Contact, isVideo: Boolean) {
        _callState.value = CallState(
            contact = contact,
            isVideo = isVideo,
            status = CallStatus.RINGING,
            durationSec = 0
        )
        navigateTo(WhatsAppNavDestination.ActiveCall(contact, isVideo, false))

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(2500) // Ringing for 2.5s then connects
            _callState.update { it.copy(status = CallStatus.CONNECTED) }
            while (_callState.value.status == CallStatus.CONNECTED) {
                delay(1000)
                _callState.update { it.copy(durationSec = it.durationSec + 1) }
            }
        }
    }

    fun endCall() {
        val state = _callState.value
        callTimerJob?.cancel()
        if (state.contact != null) {
            repository.addCallRecord(
                contact = state.contact,
                isVideo = state.isVideo,
                direction = CallDirection.OUTGOING,
                durationSec = state.durationSec
            )
        }
        _callState.value = CallState(status = CallStatus.ENDED)
        navigateBack()
    }

    fun toggleCallMute() {
        _callState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallSpeaker() {
        _callState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleCallVideo() {
        _callState.update { it.copy(isVideo = !it.isVideo) }
    }

    fun openStatusViewer(statusId: String) {
        repository.markStatusViewed(statusId)
        navigateTo(WhatsAppNavDestination.StatusViewer(statusId))
    }

    fun addTextStatus(text: String, colorHex: Long) {
        if (text.isNotBlank()) {
            repository.addStatus(text.trim(), colorHex)
        }
        navigateBack()
    }

    fun setShowNewChatDialog(show: Boolean) {
        _showNewChatDialog.value = show
    }

    fun setShowNewGroupDialog(show: Boolean) {
        _showNewGroupDialog.value = show
    }

    fun createChat(name: String, phone: String) {
        val newChat = repository.createChat(name, phone)
        _showNewChatDialog.value = false
        navigateTo(WhatsAppNavDestination.ChatDetail(newChat.id))
    }

    fun createGroup(name: String, members: List<String>) {
        val newChat = repository.createGroupChat(name, members)
        _showNewGroupDialog.value = false
        navigateTo(WhatsAppNavDestination.ChatDetail(newChat.id))
    }

    fun togglePin(chatId: String) = repository.togglePinChat(chatId)
    fun toggleMute(chatId: String) = repository.toggleMuteChat(chatId)

    fun verifyContactSecurity(contactId: String) {
        repository.verifyContactSecurity(contactId)
    }

    fun toggleDarkMode(enableDark: Boolean?) {
        _isDarkMode.value = enableDark
    }
}

enum class CallStatus {
    RINGING, CONNECTED, ENDED
}

data class CallState(
    val contact: Contact? = null,
    val isVideo: Boolean = false,
    val status: CallStatus = CallStatus.ENDED,
    val durationSec: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

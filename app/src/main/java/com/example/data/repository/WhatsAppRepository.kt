package com.example.data.repository

import com.example.R
import com.example.audio.AudioPlayerManager
import com.example.crypto.E2EEManager
import com.example.data.model.CallDirection
import com.example.data.model.CallLog
import com.example.data.model.Chat
import com.example.data.model.Contact
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StatusSlide
import com.example.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class WhatsAppRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // Registration State
    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _myProfile = MutableStateFlow(
        Contact(
            id = "me",
            name = "Usuario",
            phoneNumber = "",
            avatarRes = null,
            avatarColorSeed = 0xFF00A884,
            statusText = "¡Hola! Estoy usando WhatsApp cifrado.",
            isOnline = true
        )
    )
    val myProfile: StateFlow<Contact> = _myProfile.asStateFlow()

    // The sole assistant contact initially: Noxsz
    val contactNoxsz = Contact(
        id = "contact_noxsz",
        name = "Noxsz 🤖",
        phoneNumber = "WhatsApp Personal Assistant",
        avatarRes = R.drawable.avatar_noxsz,
        avatarColorSeed = 0xFF00A884,
        statusText = "Tu Asistente Personal con cifrado E2EE 🔒",
        isOnline = true,
        lastSeen = "en línea",
        securityCode60 = E2EEManager.generate60DigitSecurityCode("me", "contact_noxsz"),
        isSecurityVerified = true
    )

    private val _allContacts = MutableStateFlow(listOf(contactNoxsz))
    val allContacts: StateFlow<List<Contact>> = _allContacts.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _statuses = MutableStateFlow<List<UserStatus>>(emptyList())
    val statuses: StateFlow<List<UserStatus>> = _statuses.asStateFlow()

    private val _myStatus = MutableStateFlow<UserStatus?>(null)
    val myStatus: StateFlow<UserStatus?> = _myStatus.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    init {
        seedNoxszInitialData()
    }

    private fun seedNoxszInitialData() {
        val now = System.currentTimeMillis()
        val welcomeText = "¡Hola! 👋 Soy Noxsz, tu asistente personal en WhatsApp. Todos nuestros mensajes, llamadas, fotos y notas de voz están protegidos con cifrado de extremo a extremo (E2EE) con AES-256-GCM. ¿En qué puedo ayudarte hoy?"
        val encWelcome = E2EEManager.encrypt(welcomeText, contactNoxsz.id)

        val welcomeMessage = Message(
            id = "msg_noxsz_welcome",
            chatId = contactNoxsz.id,
            text = welcomeText,
            timestamp = now,
            isFromMe = false,
            status = MessageStatus.READ,
            isEncrypted = true,
            ciphertext = encWelcome.ciphertextBase64,
            iv = encWelcome.ivBase64,
            algorithm = encWelcome.algorithm,
            encryptionFingerprint = encWelcome.fingerprint,
            isEncryptedVerified = true
        )

        _messages.value = mapOf(
            contactNoxsz.id to listOf(welcomeMessage)
        )

        _chats.value = listOf(
            Chat(
                id = contactNoxsz.id,
                contact = contactNoxsz,
                lastMessage = welcomeMessage,
                unreadCount = 1,
                isPinned = true
            )
        )
    }

    fun completeRegistration(phoneNumber: String, name: String) {
        _myProfile.update {
            it.copy(
                name = name.ifBlank { "Usuario" },
                phoneNumber = phoneNumber
            )
        }
        _isRegistered.value = true
    }

    fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        audioDuration: Int = 0,
        replyTo: Message? = null
    ) {
        val messageId = UUID.randomUUID().toString()
        val encryptedPayload = E2EEManager.encrypt(text, chatId)

        val newMessage = Message(
            id = messageId,
            chatId = chatId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            status = MessageStatus.SENT,
            type = type,
            audioDurationSec = audioDuration,
            replyToText = replyTo?.text,
            replyToSender = if (replyTo != null) (if (replyTo.isFromMe) "Tú" else getChatContact(chatId)?.name) else null,
            isEncrypted = true,
            ciphertext = encryptedPayload.ciphertextBase64,
            iv = encryptedPayload.ivBase64,
            algorithm = encryptedPayload.algorithm,
            encryptionFingerprint = encryptedPayload.fingerprint,
            isEncryptedVerified = true
        )

        AudioPlayerManager.playSentBeep()

        val currentList = _messages.value[chatId] ?: emptyList()
        val updatedList = currentList + newMessage
        _messages.update { it + (chatId to updatedList) }

        _chats.update { chatList ->
            chatList.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(lastMessage = newMessage)
                } else chat
            }
        }

        scope.launch {
            delay(600)
            markMessageDelivered(chatId, messageId)
            delay(900)
            markMessageRead(chatId, messageId)
            triggerNoxszReply(chatId, text, type)
        }
    }

    private fun markMessageDelivered(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId]?.map { msg ->
                if (msg.id == messageId && msg.status == MessageStatus.SENT) {
                    msg.copy(status = MessageStatus.DELIVERED)
                } else msg
            } ?: emptyList()
            map + (chatId to list)
        }
    }

    private fun markMessageRead(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId]?.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(status = MessageStatus.READ)
                } else msg
            } ?: emptyList()
            map + (chatId to list)
        }
    }

    private suspend fun triggerNoxszReply(chatId: String, userText: String, type: MessageType) {
        setChatTyping(chatId, true)
        delay(1800)
        setChatTyping(chatId, false)

        val replyText = if (type == MessageType.AUDIO) {
            "🎙️ He recibido tu nota de voz cifrada. La escuché perfectamente a través del canal seguro. ¿Hay algo específico que quieras que organice o anote para ti?"
        } else {
            generateNoxszResponse(userText)
        }

        val encReply = E2EEManager.encrypt(replyText, chatId)
        val replyMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            text = replyText,
            timestamp = System.currentTimeMillis(),
            isFromMe = false,
            status = MessageStatus.READ,
            isEncrypted = true,
            ciphertext = encReply.ciphertextBase64,
            iv = encReply.ivBase64,
            algorithm = encReply.algorithm,
            encryptionFingerprint = encReply.fingerprint,
            isEncryptedVerified = true
        )

        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.update { it + (chatId to (currentList + replyMessage)) }

        _chats.update { chatList ->
            chatList.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(
                        lastMessage = replyMessage,
                        unreadCount = if (currentChatId != chatId) chat.unreadCount + 1 else 0
                    )
                } else chat
            }
        }
    }

    private fun generateNoxszResponse(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("hola") || lower.contains("buen") ->
                "¡Hola! Es un placer saludarte. Estoy listo para ayudarte con lo que necesites aquí en WhatsApp 🔒"
            lower.contains("quien eres") || lower.contains("quién eres") || lower.contains("noxsz") ->
                "Soy Noxsz 🤖, tu asistente personal exclusivo en WhatsApp. Puedo conversar contigo, recordarte tareas, verificar el cifrado E2EE y acompañarte en llamadas de voz o video."
            lower.contains("cifrado") || lower.contains("seguridad") || lower.contains("e2ee") ->
                "🔒 Todos nuestros mensajes se cifran con AES-256-GCM. El vector IV de 96 bits y el código de seguridad de 60 dígitos aseguran que nadie externo pueda interceptar la comunicación."
            lower.contains("llamada") || lower.contains("videollamada") ->
                "Puedes iniciar una llamada de voz o videollamada conmigo en cualquier momento pulsando los iconos en la barra superior. Verás la cámara activa y el cifrado en tiempo real 📹📞"
            lower.contains("audio") || lower.contains("nota de voz") ->
                "Mantén presionado el icono del micrófono en la barra inferior para enviarme una nota de voz cifrada. ¡Responderé en cuanto la reciba!"
            lower.contains("gracias") ->
                "¡Para eso estoy! Si necesitas cualquier otra cosa, solo dime ✨"
            else ->
                "Entendido: \"$text\". Todo ha sido registrado y procesado bajo cifrado de extremo a extremo (E2EE) con éxito ✓✓"
        }
    }

    private var currentChatId: String? = null

    fun setCurrentChat(chatId: String?) {
        currentChatId = chatId
        if (chatId != null) {
            _chats.update { chatList ->
                chatList.map { if (it.id == chatId) it.copy(unreadCount = 0) else it }
            }
        }
    }

    private fun setChatTyping(chatId: String, typing: Boolean) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isTyping = typing) else it }
        }
    }

    fun addReaction(chatId: String, messageId: String, reaction: String) {
        _messages.update { map ->
            val list = map[chatId]?.map { msg ->
                if (msg.id == messageId) {
                    val currentReactions = msg.reactions.toMutableList()
                    if (currentReactions.contains(reaction)) {
                        currentReactions.remove(reaction)
                    } else {
                        currentReactions.add(reaction)
                    }
                    msg.copy(reactions = currentReactions)
                } else msg
            } ?: emptyList()
            map + (chatId to list)
        }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId]?.filterNot { it.id == messageId } ?: emptyList()
            map + (chatId to list)
        }
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        _messages.update { map ->
            val list = map[chatId]?.map { msg ->
                if (msg.id == messageId) msg.copy(isStarred = !msg.isStarred) else msg
            } ?: emptyList()
            map + (chatId to list)
        }
    }

    fun verifyContactSecurity(contactId: String) {
        _chats.update { list ->
            list.map { chat ->
                if (chat.contact.id == contactId) {
                    chat.copy(contact = chat.contact.copy(isSecurityVerified = true))
                } else chat
            }
        }
    }

    fun addStatus(text: String, colorHex: Long) {
        val slide = StatusSlide(
            id = UUID.randomUUID().toString(),
            text = text,
            backgroundColorHex = colorHex,
            timeAgo = "Justo ahora"
        )
        val myCurrent = _myStatus.value
        if (myCurrent == null) {
            _myStatus.value = UserStatus(
                id = "my_status",
                contact = _myProfile.value,
                timeAgo = "Justo ahora",
                isViewed = false,
                slides = listOf(slide)
            )
        } else {
            _myStatus.value = myCurrent.copy(
                timeAgo = "Justo ahora",
                slides = myCurrent.slides + slide
            )
        }
    }

    fun markStatusViewed(statusId: String) {
        _statuses.update { list ->
            list.map { if (it.id == statusId) it.copy(isViewed = true) else it }
        }
    }

    fun createChat(contactName: String, phone: String): Chat {
        val newId = "c_${UUID.randomUUID()}"
        val newContact = Contact(
            id = newId,
            name = contactName,
            phoneNumber = phone,
            avatarColorSeed = 0xFF00796B,
            statusText = "¡Hola! Estoy usando WhatsApp cifrado.",
            isOnline = true,
            securityCode60 = E2EEManager.generate60DigitSecurityCode("me", newId),
            isSecurityVerified = true
        )
        _allContacts.update { it + newContact }
        val newChat = Chat(
            id = newContact.id,
            contact = newContact,
            unreadCount = 0
        )
        _chats.update { listOf(newChat) + it }
        _messages.update { it + (newContact.id to emptyList()) }
        return newChat
    }

    fun createGroupChat(groupName: String, memberNames: List<String>): Chat {
        val newId = "group_${UUID.randomUUID()}"
        val newGroupContact = Contact(
            id = newId,
            name = groupName,
            phoneNumber = "",
            avatarColorSeed = 0xFF43A047,
            statusText = "Grupo cifrado",
            isOnline = true,
            securityCode60 = E2EEManager.generate60DigitSecurityCode("me", newId),
            isSecurityVerified = true
        )
        val welcomeMsg = Message(
            id = UUID.randomUUID().toString(),
            chatId = newGroupContact.id,
            text = "Creaste el grupo \"$groupName\" con cifrado de extremo a extremo 🔒",
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            status = MessageStatus.READ,
            isEncrypted = true,
            algorithm = "AES-256-GCM"
        )
        val newChat = Chat(
            id = newGroupContact.id,
            contact = newGroupContact,
            lastMessage = welcomeMsg,
            unreadCount = 0,
            isGroup = true,
            groupMembers = memberNames + "Tú"
        )
        _chats.update { listOf(newChat) + it }
        _messages.update { it + (newGroupContact.id to listOf(welcomeMsg)) }
        return newChat
    }

    fun addCallRecord(contact: Contact, isVideo: Boolean, direction: CallDirection, durationSec: Int) {
        val newCall = CallLog(
            id = UUID.randomUUID().toString(),
            contact = contact,
            timestamp = System.currentTimeMillis(),
            isVideo = isVideo,
            direction = direction,
            durationSec = durationSec
        )
        _callLogs.update { listOf(newCall) + it }
    }

    fun togglePinChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it }
        }
    }

    fun toggleMuteChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it }
        }
    }

    fun getChatContact(chatId: String): Contact? {
        return _chats.value.find { it.id == chatId }?.contact
    }
}

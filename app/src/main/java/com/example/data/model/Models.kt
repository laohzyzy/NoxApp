package com.example.data.model

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

enum class MessageType {
    TEXT, AUDIO, IMAGE, LOCATION
}

data class Message(
    val id: String,
    val chatId: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val audioDurationSec: Int = 0,
    val audioPlaying: Boolean = false,
    val audioProgress: Float = 0f,
    val imageRes: Int? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: List<String> = emptyList(),
    val isStarred: Boolean = false,
    // End-to-End Encryption fields
    val isEncrypted: Boolean = true,
    val ciphertext: String? = null,
    val iv: String? = null,
    val algorithm: String = "AES-256-GCM",
    val encryptionFingerprint: String? = null,
    val isEncryptedVerified: Boolean = true
)

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarRes: Int? = null,
    val avatarColorSeed: Long = 0xFF128C7E,
    val statusText: String = "¡Hola! Estoy usando WhatsApp.",
    val isOnline: Boolean = true,
    val lastSeen: String = "hoy a las 14:32",
    val securityCode60: String = "",
    val isSecurityVerified: Boolean = false
)

data class Chat(
    val id: String,
    val contact: Contact,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val groupMembers: List<String> = emptyList(),
    val isTyping: Boolean = false
)

data class StatusSlide(
    val id: String,
    val text: String,
    val backgroundColorHex: Long,
    val imageRes: Int? = null,
    val timeAgo: String
)

data class UserStatus(
    val id: String,
    val contact: Contact,
    val timeAgo: String,
    val isViewed: Boolean,
    val slides: List<StatusSlide>
)

enum class CallDirection {
    INCOMING, OUTGOING, MISSED
}

data class CallLog(
    val id: String,
    val contact: Contact,
    val timestamp: Long,
    val isVideo: Boolean,
    val direction: CallDirection,
    val durationSec: Int = 0
)

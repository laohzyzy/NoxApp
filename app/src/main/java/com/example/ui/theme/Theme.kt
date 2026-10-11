package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val WhatsAppDarkColorScheme = darkColorScheme(
    primary = WhatsAppGreen,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreenDark,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppAccentGreen,
    onSecondary = Color.White,
    background = WhatsAppBgDark,
    onBackground = WhatsAppTextPrimaryDark,
    surface = WhatsAppSurfaceDark,
    onSurface = WhatsAppTextPrimaryDark,
    surfaceVariant = WhatsAppCardDark,
    onSurfaceVariant = WhatsAppTextSecondaryDark,
    outline = WhatsAppDividerDark
)

private val WhatsAppLightColorScheme = lightColorScheme(
    primary = WhatsAppGreenDark,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreen,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppAccentGreen,
    onSecondary = Color.White,
    background = WhatsAppBgLight,
    onBackground = WhatsAppTextPrimaryLight,
    surface = WhatsAppSurfaceLight,
    onSurface = WhatsAppTextPrimaryLight,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = WhatsAppTextSecondaryLight,
    outline = WhatsAppDividerLight
)

@Immutable
data class WhatsAppExtraColors(
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val chatBackground: Color,
    val topBarBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val checkBlue: Color,
    val checkGray: Color,
    val unreadBadge: Color,
    val isDark: Boolean
)

val LocalWhatsAppColors = staticCompositionLocalOf {
    WhatsAppExtraColors(
        outgoingBubble = WhatsAppOutgoingBubbleLight,
        incomingBubble = WhatsAppIncomingBubbleLight,
        chatBackground = WhatsAppChatBgLight,
        topBarBackground = WhatsAppGreenDark,
        textPrimary = WhatsAppTextPrimaryLight,
        textSecondary = WhatsAppTextSecondaryLight,
        checkBlue = WhatsAppCheckBlue,
        checkGray = WhatsAppCheckGray,
        unreadBadge = WhatsAppUnreadBadge,
        isDark = false
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WhatsAppDarkColorScheme else WhatsAppLightColorScheme
    val extraColors = if (darkTheme) {
        WhatsAppExtraColors(
            outgoingBubble = WhatsAppOutgoingBubbleDark,
            incomingBubble = WhatsAppIncomingBubbleDark,
            chatBackground = WhatsAppChatBgDark,
            topBarBackground = WhatsAppTopBarDark,
            textPrimary = WhatsAppTextPrimaryDark,
            textSecondary = WhatsAppTextSecondaryDark,
            checkBlue = WhatsAppCheckBlue,
            checkGray = WhatsAppCheckGray,
            unreadBadge = WhatsAppUnreadBadge,
            isDark = true
        )
    } else {
        WhatsAppExtraColors(
            outgoingBubble = WhatsAppOutgoingBubbleLight,
            incomingBubble = WhatsAppIncomingBubbleLight,
            chatBackground = WhatsAppChatBgLight,
            topBarBackground = WhatsAppGreenDark,
            textPrimary = WhatsAppTextPrimaryLight,
            textSecondary = WhatsAppTextSecondaryLight,
            checkBlue = WhatsAppCheckBlue,
            checkGray = WhatsAppCheckGray,
            unreadBadge = WhatsAppUnreadBadge,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalWhatsAppColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

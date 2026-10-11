package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppGreen
import com.example.viewmodel.MainTab

@Composable
fun WhatsAppBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    unreadChatsCount: Int,
    hasUnseenStatus: Boolean
) {
    val extraColors = LocalWhatsAppColors.current

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("whatsapp_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        // Tab 1: Chats
        val isChats = selectedTab == MainTab.CHATS
        NavigationBarItem(
            selected = isChats,
            onClick = { onTabSelected(MainTab.CHATS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadChatsCount > 0) {
                            Badge(
                                containerColor = WhatsAppGreen,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unreadChatsCount > 99) "99+" else unreadChatsCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isChats) Icons.Filled.Chat else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chats"
                    )
                }
            },
            label = {
                Text(
                    text = "Chats",
                    fontWeight = if (isChats) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = extraColors.textPrimary,
                unselectedIconColor = extraColors.textSecondary,
                unselectedTextColor = extraColors.textSecondary,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_chats")
        )

        // Tab 2: Novedades / Estados
        val isUpdates = selectedTab == MainTab.UPDATES
        NavigationBarItem(
            selected = isUpdates,
            onClick = { onTabSelected(MainTab.UPDATES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (hasUnseenStatus) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen)
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isUpdates) Icons.Filled.Update else Icons.Outlined.Update,
                        contentDescription = "Novedades"
                    )
                }
            },
            label = {
                Text(
                    text = "Novedades",
                    fontWeight = if (isUpdates) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = extraColors.textPrimary,
                unselectedIconColor = extraColors.textSecondary,
                unselectedTextColor = extraColors.textSecondary,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_updates")
        )

        // Tab 3: Comunidades
        val isCommunities = selectedTab == MainTab.COMMUNITIES
        NavigationBarItem(
            selected = isCommunities,
            onClick = { onTabSelected(MainTab.COMMUNITIES) },
            icon = {
                Icon(
                    imageVector = if (isCommunities) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Comunidades"
                )
            },
            label = {
                Text(
                    text = "Comunidades",
                    fontWeight = if (isCommunities) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = extraColors.textPrimary,
                unselectedIconColor = extraColors.textSecondary,
                unselectedTextColor = extraColors.textSecondary,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_communities")
        )

        // Tab 4: Llamadas
        val isCalls = selectedTab == MainTab.CALLS
        NavigationBarItem(
            selected = isCalls,
            onClick = { onTabSelected(MainTab.CALLS) },
            icon = {
                Icon(
                    imageVector = if (isCalls) Icons.Filled.Call else Icons.Outlined.Call,
                    contentDescription = "Llamadas"
                )
            },
            label = {
                Text(
                    text = "Llamadas",
                    fontWeight = if (isCalls) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = extraColors.textPrimary,
                unselectedIconColor = extraColors.textSecondary,
                unselectedTextColor = extraColors.textSecondary,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_calls")
        )
    }
}

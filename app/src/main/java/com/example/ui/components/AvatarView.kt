package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact

@Composable
fun AvatarView(
    contact: Contact,
    isGroup: Boolean = false,
    size: Dp = 50.dp,
    modifier: Modifier = Modifier,
    showOnlineBadge: Boolean = false
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (contact.avatarRes != null) {
            Image(
                painter = painterResource(id = contact.avatarRes),
                contentDescription = contact.name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val bgColor = Color(contact.avatarColorSeed)
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isGroup) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Grupo",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                } else if (contact.name.isNotBlank()) {
                    val initials = contact.name.trim().take(1).uppercase()
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Contacto",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }

        if (showOnlineBadge && contact.isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366))
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

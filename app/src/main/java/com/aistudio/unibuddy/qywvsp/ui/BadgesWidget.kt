package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.data.Badge
import com.aistudio.unibuddy.qywvsp.ui.theme.*

// Helper function to map badge category to Material Icons (adhering to NO emoji preference)
fun getBadgeIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Attendance" -> Icons.Default.DateRange
        "Grades" -> Icons.Default.Star
        "Focus" -> Icons.Default.Lock
        else -> Icons.Default.CheckCircle
    }
}

@Composable
fun BadgesMiniWidget(viewModel: UniBuddyViewModel) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val unlockedBadges = badges.filter { it.isUnlocked }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetailsDialog = true },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = NavyBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mis Logros",
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${unlockedBadges.size}/${badges.size}",
                    fontSize = 12.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (unlockedBadges.isEmpty()) {
                Text(
                    text = "Aún no tienes logros. ¡Sigue adelante!",
                    fontSize = 12.sp,
                    color = SlateGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(unlockedBadges.take(5)) { badge ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MintGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getBadgeIcon(badge.category),
                                contentDescription = badge.name,
                                tint = DarkGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (unlockedBadges.size > 5) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+${unlockedBadges.size - 5}", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        BadgesDetailsDialog(
            badges = badges,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun BadgesDetailsDialog(
    badges: List<Badge>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = NavyBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logros y Reconocimientos", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
            ) {
                items(badges) { badge ->
                    val iconColor = if (badge.isUnlocked) DarkGreen else SlateGray
                    val bgTone = if (badge.isUnlocked) MintGreen.copy(alpha = 0.1f) else BackgroundGray.copy(alpha = 0.5f)
                    
                    Surface(
                        color = bgTone,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getBadgeIcon(badge.category),
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NavyBlue
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = SlateGray,
                                    lineHeight = 15.sp
                                )
                                if (badge.isUnlocked && badge.dateUnlocked.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Desbloqueado: ${badge.dateUnlocked}",
                                        fontSize = 10.sp,
                                        color = DarkGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            if (badge.isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Desbloqueado",
                                    tint = DarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Bloqueado",
                                    tint = SlateGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Entendido", color = Bone)
            }
        },
        containerColor = Color.White
    )
}

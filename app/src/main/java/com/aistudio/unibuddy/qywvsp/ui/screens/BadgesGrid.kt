package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
import com.aistudio.unibuddy.qywvsp.ui.theme.Amber

@Composable
fun BadgesGrid(viewModel: UniBuddyViewModel) {
    val badges by viewModel.badges.collectAsState(initial = emptyList())

    if (badges.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay insignias disponibles.", color = SlateGray)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(badges) { badge ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (badge.isUnlocked) Amber.copy(alpha = 0.2f) else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (badge.isUnlocked) {
                            Icon(Icons.Filled.Star, contentDescription = "Desbloqueado", tint = Amber, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Filled.Lock, contentDescription = "Bloqueado", tint = SlateGray, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = badge.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavyBlue,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        fontSize = 10.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

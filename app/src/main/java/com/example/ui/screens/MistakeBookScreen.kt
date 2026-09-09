package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MistakeEntity
import com.example.ui.components.MarkdownText
import com.example.ui.viewmodel.AppDestination

@Composable
fun MistakeBookScreen(
    mistakes: List<MistakeEntity>,
    onMasterMistake: (MistakeEntity) -> Unit,
    onDeleteMistake: (Long) -> Unit,
    onRetestMistake: (MistakeEntity) -> Unit
) {
    var showOnlyUnmastered by remember { mutableStateOf(true) }

    val filteredMistakes = if (showOnlyUnmastered) {
        mistakes.filter { !it.isMastered }
    } else {
        mistakes
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Personal Mistake Book",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active learning treats every error as an opportunity for deep retention.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = showOnlyUnmastered,
                        onClick = { showOnlyUnmastered = true },
                        label = { Text("To Fix (${mistakes.count { !it.isMastered }})") }
                    )
                    FilterChip(
                        selected = !showOnlyUnmastered,
                        onClick = { showOnlyUnmastered = false },
                        label = { Text("All (${mistakes.size})") }
                    )
                }

                Text(
                    text = "${mistakes.count { it.isMastered }} Mastered",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
        }

        if (filteredMistakes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No pending mistakes in this view!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Take a quiz or practice exam to test your recall.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredMistakes) { mistake ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mistake.isMastered) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mistake_card_${mistake.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (mistake.isMastered) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
                            ) {
                                Text(
                                    text = mistake.errorType.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mistake.isMastered) Color(0xFF15803D) else Color(0xFFBE123C),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteMistake(mistake.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = mistake.question,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        // Answer Comparison Row
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Your Answer: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE11D48),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mistake.studentAnswer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE11D48)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Correct: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mistake.correctAnswer,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }

                        // Explanation
                        Text(
                            text = mistake.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Retest & Master buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRetestMistake(mistake) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retest with AI", fontSize = 11.sp)
                            }

                            if (!mistake.isMastered) {
                                Button(
                                    onClick = { onMasterMistake(mistake) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Mastered (+25 XP)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

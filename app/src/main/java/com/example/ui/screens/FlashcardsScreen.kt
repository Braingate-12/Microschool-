package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FlashcardEntity
import com.example.data.local.SubjectEntity
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    flashcards: List<FlashcardEntity>,
    subjects: List<SubjectEntity>,
    currentIndex: Int,
    isFlipped: Boolean,
    selectedSubjectFilter: Long?,
    onFilterSubject: (Long?) -> Unit,
    onFlipCard: () -> Unit,
    onRateCard: (FlashcardEntity, String) -> Unit,
    onAddFlashcard: (String, String, Long, String) -> Unit,
    onDeleteCard: (FlashcardEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCards = if (selectedSubjectFilter == null) {
        flashcards
    } else {
        flashcards.filter { it.subjectId == selectedSubjectFilter }
    }

    val currentCard = filteredCards.getOrNull(currentIndex)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spaced Repetition Decks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredCards.size} cards total • Anki SM-2 Algorithm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_flashcard_button")
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Card", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Subject filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedSubjectFilter == null,
                    onClick = { onFilterSubject(null) },
                    label = { Text("All Decks") }
                )
            }
            items(subjects) { subject ->
                FilterChip(
                    selected = selectedSubjectFilter == subject.id,
                    onClick = { onFilterSubject(subject.id) },
                    label = { Text(subject.name) }
                )
            }
        }

        // Active Card Display
        if (currentCard != null) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { onFlipCard() }
                    .testTag("flashcard_flipper")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // Front of Card
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = currentCard.cardType,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "Card ${currentIndex + 1} / ${filteredCards.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = currentCard.front,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 30.sp
                                )

                                if (!currentCard.hint.isNullOrBlank()) {
                                    Text(
                                        text = "💡 Hint: ${currentCard.hint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            Text(
                                text = "👆 Tap to flip and reveal answer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Back of Card (Mirror-adjusted)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f },
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "ANSWER & RECALL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "Status: ${currentCard.status} (Interval: ${currentCard.intervalDays}d)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                MarkdownText(
                                    text = currentCard.back,
                                    textColor = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Rate your active recall below:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SM-2 Rating Controls (Active when flipped)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Again
                Button(
                    onClick = { onRateCard(currentCard, "Again") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_again_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Again", fontWeight = FontWeight.Bold)
                        Text("< 10m", fontSize = 10.sp)
                    }
                }

                // Hard
                Button(
                    onClick = { onRateCard(currentCard, "Hard") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_hard_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hard", fontWeight = FontWeight.Bold)
                        Text("1d", fontSize = 10.sp)
                    }
                }

                // Good
                Button(
                    onClick = { onRateCard(currentCard, "Good") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_good_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Good", fontWeight = FontWeight.Bold)
                        Text("${currentCard.intervalDays * 2}d", fontSize = 10.sp)
                    }
                }

                // Easy
                Button(
                    onClick = { onRateCard(currentCard, "Easy") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_easy_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Easy", fontWeight = FontWeight.Bold)
                        Text("${currentCard.intervalDays * 4}d", fontSize = 10.sp)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All caught up on flashcards for this deck! 🎉",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Add Card Dialog
    if (showAddDialog) {
        var frontText by remember { mutableStateOf("") }
        var backText by remember { mutableStateOf("") }
        var cardType by remember { mutableStateOf("QA") }
        var subjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Create Active Flashcard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = frontText,
                        onValueChange = { frontText = it },
                        label = { Text("Front (Question or Prompt)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = backText,
                        onValueChange = { backText = it },
                        label = { Text("Back (Answer or Derivation)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (frontText.isNotBlank() && backText.isNotBlank()) {
                                    onAddFlashcard(frontText, backText, subjectId, cardType)
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Add Card")
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.ui.viewmodel.AppDestination

@Composable
fun SubjectsKnowledgeMapScreen(
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity>,
    onNavigate: (AppDestination) -> Unit,
    onTopicClick: (TopicEntity) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    val filteredTopics = if (selectedSubjectId == null) {
        topics
    } else {
        topics.filter { it.subjectId == selectedSubjectId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Interactive Knowledge Map",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track topic mastery, prerequisite trees, and eliminate knowledge gaps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subject filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text("All Subjects") }
                    )
                }
                items(subjects) { subject ->
                    FilterChip(
                        selected = selectedSubjectId == subject.id,
                        onClick = { selectedSubjectId = subject.id },
                        label = { Text(subject.name) }
                    )
                }
            }
        }

        // Visual Knowledge Map Concept Diagram Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("knowledge_map_tree_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Prerequisite Tree",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Prerequisite Dependency Tree",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prerequisite Flow Nodes
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrerequisiteNodeRow(
                            from = "Foundational Algebra",
                            to = "Functions & Limits",
                            isStrong = true
                        )
                        PrerequisiteNodeRow(
                            from = "Functions & Limits",
                            to = "Calculus & Integration",
                            isStrong = true
                        )
                        PrerequisiteNodeRow(
                            from = "Glycolysis (Anaerobic)",
                            to = "Krebs Cycle & ATP Yield",
                            isStrong = false
                        )
                        PrerequisiteNodeRow(
                            from = "Matrix Determinants",
                            to = "Eigenvalues & Diagonalization",
                            isStrong = false
                        )
                    }
                }
            }
        }

        // Topics list
        item {
            Text(
                text = "Tracked Topics & Mastery Scores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredTopics) { topic ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (topic.isCriticalGap) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTopicClick(topic) }
                    .testTag("topic_card_${topic.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (topic.isCriticalGap) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE11D48),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "CRITICAL GAP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = topic.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (topic.isCriticalGap) Color(0xFF9F1239) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (topic.prerequisites.isNotBlank()) {
                                Text(
                                    text = "Prerequisites: ${topic.prerequisites}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Mastery Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                topic.masteryScore >= 80 -> Color(0xFFDCFCE7)
                                topic.masteryScore >= 60 -> Color(0xFFFEF9C3)
                                else -> Color(0xFFFFE4E6)
                            },
                            contentColor = when {
                                topic.masteryScore >= 80 -> Color(0xFF15803D)
                                topic.masteryScore >= 60 -> Color(0xFFA16207)
                                else -> Color(0xFFBE123C)
                            }
                        ) {
                            Text(
                                text = "${topic.masteryScore}%",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { topic.masteryScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            topic.masteryScore >= 80 -> Color(0xFF16A34A)
                            topic.masteryScore >= 60 -> Color(0xFFCA8A04)
                            else -> Color(0xFFE11D48)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Accuracy: ${topic.accuracy}% (${topic.attempts} attempts)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Drill with AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrerequisiteNodeRow(from: String, to: String, isStrong: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isStrong) Color(0xFF16A34A) else Color(0xFFE11D48))
            )
            Text(
                text = from,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = to,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isStrong) MaterialTheme.colorScheme.onSurface else Color(0xFFE11D48),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

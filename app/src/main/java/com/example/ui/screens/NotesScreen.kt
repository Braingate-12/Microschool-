package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.NoteEntity
import com.example.data.local.SubjectEntity
import com.example.ui.components.MarkdownText

@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    subjects: List<SubjectEntity>,
    onSaveNote: (id: Long, subjectId: Long, title: String, content: String, summary: String, cues: String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onConvertToFlashcards: (NoteEntity) -> Unit
) {
    var selectedNote by remember { mutableStateOf<NoteEntity?>(notes.firstOrNull()) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(notes) {
        if (selectedNote == null && notes.isNotEmpty()) {
            selectedNote = notes.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Intelligent Cornell Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active Cues • Main Notes • Synthesis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    selectedNote = null
                    showEditDialog = true
                },
                modifier = Modifier.testTag("add_note_button")
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = "New Note", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Note selector chips
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(notes) { note ->
                val isSelected = selectedNote?.id == note.id

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedNote = note }
                        .testTag("note_item_${note.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Row {
                                IconButton(
                                    onClick = { onConvertToFlashcards(note) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Style,
                                        contentDescription = "Convert to Flashcards",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteNote(note) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Note",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Cornell Format Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Cues Column
                            if (note.cues.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.width(110.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "CUES / RECALL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = note.cues,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // Main Content Column
                            Box(modifier = Modifier.weight(1f)) {
                                MarkdownText(
                                    text = note.content,
                                    textColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Summary Section
                        if (note.summary.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "SYNTHESIS / SUMMARY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = note.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Quick AI Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onConvertToFlashcards(note) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate Cards", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit/Create Note Dialog
    if (showEditDialog) {
        var title by remember { mutableStateOf(selectedNote?.title ?: "") }
        var cues by remember { mutableStateOf(selectedNote?.cues ?: "") }
        var content by remember { mutableStateOf(selectedNote?.content ?: "") }
        var summary by remember { mutableStateOf(selectedNote?.summary ?: "") }

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (selectedNote == null) "New Cornell Note" else "Edit Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cues,
                        onValueChange = { cues = it },
                        label = { Text("Cues / Recall Prompts") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Main Content (Markdown)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Bottom Synthesis / Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveNote(
                                    selectedNote?.id ?: 0L,
                                    1L,
                                    title,
                                    content,
                                    summary,
                                    cues
                                )
                                showEditDialog = false
                            }
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
        }
    }
}

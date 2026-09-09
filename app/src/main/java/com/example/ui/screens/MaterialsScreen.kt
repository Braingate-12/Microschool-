package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.MaterialEntity
import com.example.data.local.SubjectEntity
import com.example.ui.components.MarkdownText
import com.example.util.AttachedFileInfo
import com.example.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    materials: List<MaterialEntity>,
    subjects: List<SubjectEntity>,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    uploadStatusMessage: String = "",
    onUploadMaterial: (subjectId: Long, title: String, fileType: String, textSnippet: String, fileSize: String, isLargeFile: Boolean) -> Unit,
    onStudyWithTutor: (MaterialEntity) -> Unit = {}
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialEntity?>(materials.firstOrNull()) }
    val context = LocalContext.current

    // Direct upload launcher from file system
    val systemFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileInfo = FileUtils.getFileMetadata(context, uri)
            val selectedSubject = subjects.firstOrNull()?.id ?: 1L
            onUploadMaterial(
                selectedSubject,
                fileInfo.name,
                fileInfo.extension.uppercase(),
                fileInfo.extractedContent,
                fileInfo.formattedSize,
                fileInfo.isLargeFile
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner with Large File Upload Support
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_banner_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Course Material & Large Files",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload slides, textbooks, or chapters. Safely processes large files (even 100MB+) using buffered stream chunking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Feature highlights chip row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("⚡ Stream Large Files", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("PDF / PPTX / DOCX", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }

                    // Primary Upload Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    systemFilePicker.launch(arrayOf("*/*"))
                                } catch (e: Exception) {
                                    showUploadDialog = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_file_button")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attach File", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { showUploadDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("custom_upload_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Options & Presets")
                        }
                    }
                }
            }
        }

        // Active Upload Progress Indicator (shown when processing large files)
        if (isUploading) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Processing Document Stream",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Text(
                            text = uploadStatusMessage.ifBlank { "Indexing formulas and generating Cornell notes..." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analyzed Course Materials (${materials.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(materials) { material ->
            val isExpanded = selectedMaterial?.id == material.id

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMaterial = if (isExpanded) null else material }
                    .testTag("material_item_${material.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = material.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = material.fileType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (material.fileType.contains("MB") || material.fileType.contains("Large", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "⚡ LARGE FILE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = material.summary,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { selectedMaterial = if (isExpanded) null else material },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (isExpanded) "Hide Details ▲" else "View AI Analysis ▼", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { onStudyWithTutor(material) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quiz Me on This", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isExpanded) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Key Concepts
                        Text(
                            text = "Key Concepts Identified:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = material.keyConcepts,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Formulas
                        if (material.formulas.isNotBlank()) {
                            Text(
                                text = "Governing Formulas:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = material.formulas,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Exam Questions
                        Text(
                            text = "Potential Exam Questions:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        MarkdownText(
                            text = material.potentialExamQuestions,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )

                        // Extracted Content Slices (if present)
                        if (material.difficultSections.startsWith("Extracted Document Text:") || material.difficultSections.length > 50) {
                            Text(
                                text = "Extracted Document Stream Slices:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = material.difficultSections.take(500) + if (material.difficultSections.length > 500) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Upload Dialog with Storage Picker and Large File Presets
    if (showUploadDialog) {
        var title by remember { mutableStateOf("") }
        var contentSnippet by remember { mutableStateOf("") }
        var fileType by remember { mutableStateOf("PDF") }
        var fileSize by remember { mutableStateOf("12.4 MB") }
        var isLargeFile by remember { mutableStateOf(true) }
        var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }

        val dialogFilePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                val fileInfo = FileUtils.getFileMetadata(context, uri)
                title = fileInfo.name
                fileType = fileInfo.extension.uppercase()
                fileSize = fileInfo.formattedSize
                isLargeFile = fileInfo.isLargeFile
                contentSnippet = fileInfo.extractedContent
            }
        }

        Dialog(onDismissRequest = { showUploadDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upload & Index Lecture File",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showUploadDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Choose from storage button
                    OutlinedButton(
                        onClick = {
                            try {
                                dialogFilePicker.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                // fallback
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_browse_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose File from Storage (Any Size)")
                    }

                    // Large File Presets (Instant test buttons)
                    Text(
                        text = "Or choose a large textbook / slide deck preset:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SuggestionChip(
                            onClick = {
                                title = "Cellular_Respiration_Master_Deck.pdf"
                                fileType = "PDF"
                                fileSize = "38.5 MB"
                                isLargeFile = true
                                contentSnippet = """
• Topic: Aerobic Cellular Respiration & ATP Synthase Kinetics
• Section 1: Glycolysis occurs in cytoplasm; Net yield: 2 ATP + 2 NADH.
• Section 2: Citric Acid Cycle (Krebs) in mitochondrial matrix produces 2 ATP, 6 NADH, 2 FADH2 per glucose molecule.
• Section 3: Oxidative Phosphorylation & Chemiosmosis across inner mitochondrial membrane cristae.
• Governing Equation: C6H12O6 + 6O2 -> 6CO2 + 6H2O + ~30-32 ATP
• Proton Gradient: High [H+] in intermembrane space drives ATP Synthase rotor like a turbine.
• Exam Trap: Do not confuse substrate-level phosphorylation with oxidative phosphorylation!
                                """.trimIndent()
                            },
                            label = { Text("Biology (38 MB)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )

                        SuggestionChip(
                            onClick = {
                                title = "Multivariable_Calculus_Integrals.pptx"
                                fileType = "PPTX"
                                fileSize = "62.4 MB"
                                isLargeFile = true
                                contentSnippet = """
• Lecture: Green's, Stokes', and Divergence Theorems in 3D Vector Fields
• Green's Theorem: ∮ (L dx + M dy) = ∬ (∂M/∂x - ∂L/∂y) dA
• Stokes' Theorem: ∮ F · dr = ∬ (curl F) · dS
• Divergence Theorem: ∯ F · dS = ∭ (div F) dV
• Boundary Conditions: Orientation of normal vector must match right-hand rule of boundary curve.
• Typical Exam Trap: Forgetting outward normal sign convention on closed surfaces.
                                """.trimIndent()
                            },
                            label = { Text("Calculus (62 MB)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Large File Stream notice
                    if (isLargeFile) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Large File stream enabled ($fileSize). Chunked memory buffers protect performance.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Title input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document / File Name") },
                        placeholder = { Text("e.g. Organic Chemistry Textbook.pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Text excerpt preview
                    OutlinedTextField(
                        value = contentSnippet,
                        onValueChange = { contentSnippet = it },
                        label = { Text("Extracted Document Text Preview") },
                        placeholder = { Text("Content excerpt read from file stream...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showUploadDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onUploadMaterial(
                                        selectedSubjectId,
                                        title,
                                        fileType,
                                        contentSnippet,
                                        fileSize,
                                        isLargeFile
                                    )
                                    showUploadDialog = false
                                }
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("submit_upload_button")
                        ) {
                            Text("Analyze with AI")
                        }
                    }
                }
            }
        }
    }
}

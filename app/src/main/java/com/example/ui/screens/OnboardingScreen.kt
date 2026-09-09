package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MicroschoolLogo

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        educationLevel: String,
        school: String,
        targetGrade: String,
        explanationStyle: String,
        academicLevel: String,
        goals: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("Alex Rivera") }
    var school by remember { mutableStateOf("University of California, Berkeley") }
    var educationLevel by remember { mutableStateOf("Undergraduate") }
    var targetGrade by remember { mutableStateOf("First-Class Honours (A*)") }
    var explanationStyle by remember { mutableStateOf("Conceptual & Socratic") }
    var goals by remember { mutableStateOf("Master STEM core concepts and score 90%+ on finals") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MicroschoolLogo(
                size = 60.dp,
                shapeRadius = 16.dp
            )
        }

        item {
            Text(
                text = "Welcome to Microschool",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Let's calibrate your personalized AI tutor and active learning environment.",

                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("What should your AI Tutor call you?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )
        }

        item {
            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("School / College / University") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Current Education Level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf("High School", "AP / IB", "Undergraduate", "Postgraduate").forEach { level ->
                    FilterChip(
                        selected = educationLevel == level,
                        onClick = { educationLevel = level },
                        label = { Text(level, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = targetGrade,
                onValueChange = { targetGrade = it },
                label = { Text("Target Grade / Exam Standard") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Preferred AI Explanation Style",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf("Conceptual & Socratic", "Everyday Analogies", "Mathematical Rigor").forEach { styleOption ->
                    FilterChip(
                        selected = explanationStyle == styleOption,
                        onClick = { explanationStyle = styleOption },
                        label = { Text(styleOption, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = goals,
                onValueChange = { goals = it },
                label = { Text("Primary Learning Goals") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onComplete(
                        name,
                        educationLevel,
                        school,
                        targetGrade,
                        explanationStyle,
                        "Intermediate",
                        goals
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_onboarding_button")
            ) {
                Text("Launch My Study Environment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

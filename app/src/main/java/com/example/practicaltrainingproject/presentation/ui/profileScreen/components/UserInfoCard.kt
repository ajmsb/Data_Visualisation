package com.example.practicaltrainingproject.presentation.ui.profileScreen.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserInfoCard(
    name: String,
    email: String,
    age: Int,
    gender: String,
    profession: String,
    healthConditions: List<String>,
    emergencyContact: String,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Details in two columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoItem(label = "Full Name", value = name)
                    InfoItem(label = "Email", value = email)
                    InfoItem(
                        label = "Age",
                        value = if (age > 0) "$age years" else ""
                    )
                    InfoItem(label = "Gender", value = gender)
                }

                // Right Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoItem(label = "Profession", value = profession)
                    InfoItem(
                        label = "Health Conditions",
                        value = healthConditions.joinToString(", ").ifBlank { "None" }
                    )
                    InfoItem(label = "Emergency Contact", value = emergencyContact)
                }
            }
        }
    }
}
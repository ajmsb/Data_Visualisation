package com.example.practicaltrainingproject.presentation.ui.profileScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.practicaltrainingproject.data.auth.Gender
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Edit mode state
    var isEditMode by remember { mutableStateOf(false) }

    // Editable fields
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var age by remember { mutableStateOf(currentUser?.age?.toString() ?: "0") }
    var gender by remember { mutableStateOf(currentUser?.gender ?: Gender.PREFER_NOT_TO_SAY) }
    var profession by remember { mutableStateOf(currentUser?.profession ?: "") }
    var emergencyContact by remember { mutableStateOf(currentUser?.emergencyContact ?: "") }
    var emergencyContacts by remember {
        mutableStateOf(currentUser?.emergencyContacts?.joinToString("\n") ?: "")
    }
    var healthConditions by remember {
        mutableStateOf(currentUser?.healthConditions?.joinToString(", ") ?: "")
    }

    // Update fields when user data changes
    currentUser?.let { user ->
        if (!isEditMode) {
            name = user.name
            age = user.age.toString()
            gender = user.gender
            profession = user.profession
            emergencyContact = user.emergencyContact
            emergencyContacts = user.emergencyContacts.joinToString("\n")
            healthConditions = user.healthConditions.joinToString(", ")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Title and Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Profile" else "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!isEditMode) {
                    FilledTonalIconButton(
                        onClick = { isEditMode = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Information Section
                ModernSectionCard(
                    title = "Personal Information",
                    icon = Icons.Default.Person
                ) {
                    ModernTextField(
                        label = "Full Name",
                        value = name,
                        isEditable = isEditMode,
                        onValueChange = { name = it },
                        icon = Icons.Default.Person
                    )

                    ModernTextField(
                        label = "Age",
                        value = age,
                        isEditable = isEditMode,
                        onValueChange = { age = it },
                        icon = Icons.Default.Cake,
                        keyboardType = KeyboardType.Number
                    )

                    ModernGenderField(
                        label = "Gender",
                        selectedGender = gender,
                        isEditable = isEditMode,
                        onGenderSelected = { gender = it }
                    )

                    ModernTextField(
                        label = "Profession",
                        value = profession,
                        isEditable = isEditMode,
                        onValueChange = { profession = it },
                        icon = Icons.Default.Work
                    )
                }

                // Health Information Section
                ModernSectionCard(
                    title = "Health Information",
                    icon = Icons.Default.LocalHospital
                ) {
                    ModernTextField(
                        label = "Health Conditions",
                        value = healthConditions,
                        isEditable = isEditMode,
                        onValueChange = { healthConditions = it },
                        icon = Icons.Default.HealthAndSafety,
                        singleLine = false,
                        placeholder = "Hypertension, Diabetes..."
                    )
                }

                // Emergency Contacts Section
                ModernSectionCard(
                    title = "Emergency Contacts",
                    icon = Icons.Default.Phone
                ) {
                    ModernTextField(
                        label = "Primary Contact",
                        value = emergencyContact,
                        isEditable = isEditMode,
                        onValueChange = { emergencyContact = it },
                        icon = Icons.Default.ContactPhone,
                        keyboardType = KeyboardType.Phone,
                        placeholder = "+1234567890"
                    )

                    ModernTextField(
                        label = "Additional Contacts",
                        value = emergencyContacts,
                        isEditable = isEditMode,
                        onValueChange = { emergencyContacts = it },
                        icon = Icons.Default.Group,
                        keyboardType = KeyboardType.Phone,
                        singleLine = false,
                        placeholder = "One per line..."
                    )
                }

                // Action Buttons
                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditMode = false
                                // Reset fields
                                currentUser?.let { user ->
                                    name = user.name
                                    age = user.age.toString()
                                    gender = user.gender
                                    profession = user.profession
                                    emergencyContact = user.emergencyContact
                                    emergencyContacts = user.emergencyContacts.joinToString("\n")
                                    healthConditions = user.healthConditions.joinToString(", ")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val ageInt = age.toIntOrNull() ?: 0
                                val healthConditionsList = healthConditions
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }

                                val emergencyContactsList = emergencyContacts
                                    .split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }

                                authViewModel.updateUserProfile(
                                    name = name,
                                    age = ageInt,
                                    gender = gender,
                                    profession = profession,
                                    healthConditions = healthConditionsList,
                                    emergencyContact = emergencyContact,
                                    emergencyContacts = emergencyContactsList
                                )

                                Toast.makeText(
                                    context,
                                    "Profile updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                isEditMode = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Modern Section Card Component
@Composable
fun ModernSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

// Modern TextField Component
@Composable
fun ModernTextField(
    label: String,
    value: String,
    isEditable: Boolean,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    placeholder: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                placeholder = {
                    if (placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                minLines = if (singleLine) 1 else 3,
                maxLines = if (singleLine) 1 else 5
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = value.ifBlank { "Not set" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// Modern Gender Field Component
@Composable
fun ModernGenderField(
    label: String,
    selectedGender: Gender,
    isEditable: Boolean,
    onGenderSelected: (Gender) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Wc,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isEditable) {
            Box {
                OutlinedTextField(
                    value = selectedGender.name.replace("_", " "),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = false
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Gender.entries.forEach { gender ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = gender.name.replace("_", " "),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onGenderSelected(gender)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (gender == selectedGender)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                )
                            }
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = selectedGender.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


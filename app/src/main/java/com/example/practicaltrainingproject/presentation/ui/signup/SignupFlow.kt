package com.example.practicaltrainingproject.presentation.ui.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.data.auth.Gender
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel

@Composable
fun SignupFlow(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onSignupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step content
        when (currentStep) {
            1 -> BasicInfoStep(
                authViewModel = authViewModel,
                onNext = { currentStep++ }
            )

            2 -> PersonalInfoStep(
                authViewModel = authViewModel,
                onNext = { currentStep++ },
                onBack = { currentStep-- }
            )

            3 -> HealthInfoStep(
                authViewModel = authViewModel,
                onNext = { currentStep++ },
                onBack = { currentStep-- }
            )

            4 -> ReviewStep(
                authViewModel = authViewModel,
                onBack = { currentStep-- },
                onSignupComplete = onSignupComplete
            )
        }
    }
}

@Composable
fun BasicInfoStep(
    authViewModel: AuthViewModel,
    onNext: () -> Unit
) {
    val tempUserData by authViewModel.tempUserData.collectAsState()
    var email by remember { mutableStateOf(tempUserData.email) }
    var name by remember { mutableStateOf(tempUserData.name) }

    val isFormValid = email.isNotBlank() && name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Let's start with your basic details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                authViewModel.updateTempEmail(it)
            },
            label = { Text("Email Address *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                authViewModel.updateTempName(it)
            },
            label = { Text("Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Continue")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
        }
    }
}

@Composable
fun PersonalInfoStep(
    authViewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val tempUserData by authViewModel.tempUserData.collectAsState()
    var age by remember { mutableStateOf(tempUserData.age.toString()) }
    var profession by remember { mutableStateOf(tempUserData.profession) }
    var selectedGender by remember { mutableStateOf(tempUserData.gender) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tell us more about yourself",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age
        OutlinedTextField(
            value = age,
            onValueChange = {
                age = it.filter { char -> char.isDigit() }
                if (age.isNotBlank()) {
                    authViewModel.updateTempAge(age.toInt())
                }
            },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Gender
        Text(
            text = "Gender",
            style = MaterialTheme.typography.titleMedium
        )

        Column {
            Gender.values().forEach { gender ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGender == gender,
                        onClick = {
                            selectedGender = gender
                            authViewModel.updateTempGender(gender)
                        }
                    )
                    Text(
                        text = when (gender) {
                            Gender.MALE -> "Male"
                            Gender.FEMALE -> "Female"
                            Gender.OTHER -> "Other"
                            Gender.PREFER_NOT_TO_SAY -> "Prefer not to say"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Profession
        OutlinedTextField(
            value = profession,
            onValueChange = {
                profession = it
                authViewModel.updateTempProfession(it)
            },
            label = { Text("Profession") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun HealthInfoStep(
    authViewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val tempUserData by authViewModel.tempUserData.collectAsState()
    var emergencyContact by remember { mutableStateOf(tempUserData.emergencyContact) }
    var selectedConditions by remember { mutableStateOf(tempUserData.healthConditions.toMutableSet()) }

    val commonConditions = listOf(
        "Diabetes",
        "Hypertension",
        "Heart Disease",
        "Arthritis",
        "Asthma",
        "Osteoporosis",
        "Balance Issues",
        "Vision Problems",
        "Hearing Loss",
        "None"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Health Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "This helps us provide better fall detection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Contact
        OutlinedTextField(
            value = emergencyContact,
            onValueChange = {
                emergencyContact = it
                authViewModel.updateTempEmergencyContact(it)
            },
            label = { Text("Emergency Contact Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        // Health Conditions
        Text(
            text = "Health Conditions (Select all that apply)",
            style = MaterialTheme.typography.titleMedium
        )

        Column {
            commonConditions.forEach { condition ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedConditions.contains(condition),
                        onCheckedChange = { checked ->
                            val newConditions = selectedConditions.toMutableSet()
                            if (checked) {
                                if (condition == "None") {
                                    newConditions.clear()
                                } else {
                                    newConditions.remove("None")
                                }
                                newConditions.add(condition)
                            } else {
                                newConditions.remove(condition)
                            }
                            selectedConditions = newConditions
                            authViewModel.updateTempHealthConditions(newConditions.toList())
                        }
                    )
                    Text(
                        text = condition,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Review")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun ReviewStep(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignupComplete: () -> Unit
) {
    val tempUserData by authViewModel.tempUserData.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }

    val isFormValid = password.length >= 6 && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review & Complete",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Review information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Your Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                InfoRow("Name", tempUserData.name)
                InfoRow("Email", tempUserData.email)
                InfoRow(
                    "Age",
                    if (tempUserData.age > 0) tempUserData.age.toString() else "Not specified"
                )
                InfoRow("Gender", tempUserData.gender.toString())
                InfoRow("Profession", tempUserData.profession.ifBlank { "Not specified" })
                InfoRow(
                    "Health Conditions",
                    tempUserData.healthConditions.joinToString(", ").ifBlank { "None" })
                InfoRow(
                    "Emergency Contact",
                    tempUserData.emergencyContact.ifBlank { "Not specified" })
            }
        }

        // Password fields
        Text(
            text = "Create Password",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password *") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPasswords = !showPasswords }) {
                    Icon(
                        if (showPasswords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPasswords) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password *") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )

        if (password.isNotEmpty() && password.length < 6) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }

            Button(
                onClick = {
                    authViewModel.register(password)
                    onSignupComplete()
                },
                modifier = Modifier.weight(1f),
                enabled = isFormValid
            ) {
                Text("Complete Signup")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
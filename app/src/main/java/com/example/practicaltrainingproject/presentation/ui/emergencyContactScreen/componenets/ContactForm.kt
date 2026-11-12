/**
 * Form for adding/editing emergency contact information
 * Includes validation and contact information fields
 */
package com.example.practicaltrainingproject.presentation.ui.emergencyContactScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ContactForm(
    currentContact: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var contactName by remember { mutableStateOf(extractName(currentContact) ?: "") }
    var phoneNumber by remember { mutableStateOf(extractPhone(currentContact) ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentContact != null) "Edit Emergency Contact" else "Add Emergency Contact"
            )
        },
        text = {
            Column {
                // Name Field
                OutlinedTextField(
                    value = contactName,
                    onValueChange = {
                        contactName = it
                        nameError = null
                    },
                    label = { Text("Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name")
                    },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = null
                    },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = {
                        phoneError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This contact will be notified automatically in case of emergency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation
                    var isValid = true

                    if (contactName.isBlank()) {
                        nameError = "Name is required"
                        isValid = false
                    }

                    if (phoneNumber.isBlank()) {
                        phoneError = "Phone number is required"
                        isValid = false
                    } else if (!isValidPhone(phoneNumber)) {
                        phoneError = "Please enter a valid phone number"
                        isValid = false
                    }

                    if (isValid) {
                        val contactInfo = "$contactName - $phoneNumber"
                        onSave(contactInfo)
                    }
                }
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
private fun extractName(contact: String?): String? {
    return contact?.substringBefore(" - ")?.takeIf { it.isNotBlank() }
}

private fun extractPhone(contact: String?): String? {
    return contact?.substringAfter(" - ")?.takeIf { it.isNotBlank() }
}

private fun isValidPhone(phone: String): Boolean {
    // Simple phone validation - can be enhanced
    return phone.length >= 10 && phone.any { it.isDigit() }
}
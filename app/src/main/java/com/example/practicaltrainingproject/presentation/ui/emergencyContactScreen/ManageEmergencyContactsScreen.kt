package com.example.practicaltrainingproject.presentation.ui.emergencyContactScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEmergencyContactsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // State for managing contacts - using snapshots for proper compose state
    var contactsList by remember {
        mutableStateOf<List<String>>(
            currentUser?.emergencyContacts ?: emptyList()
        )
    }

    // Add primary contact if not in list
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val allContacts = user.emergencyContacts.toMutableList()
            if (user.emergencyContact.isNotBlank() && user.emergencyContact !in allContacts) {
                allContacts.add(0, user.emergencyContact)
            }
            contactsList = allContacts
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingContact by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Emergency Contacts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Contact") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Emergency Contacts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "All contacts will be notified simultaneously when a fall is detected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contacts List
            if (contactsList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Emergency Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the button below to add contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "${contactsList.size} Contact${if (contactsList.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                contactsList.forEachIndexed { index, contact ->
                    EmergencyContactCard(
                        contact = contact,
                        isPrimary = index == 0,
                        onEdit = {
                            editingIndex = index
                            editingContact = contact
                        },
                        onDelete = {
                            deletingIndex = index
                            showDeleteDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newContact ->
                if (newContact.isNotBlank()) {
                    contactsList = contactsList + newContact
                    saveContacts(authViewModel, currentUser, contactsList, context)
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Contact Dialog
    if (editingIndex != null) {
        EditContactDialog(
            contact = editingContact,
            onDismiss = {
                editingIndex = null
                editingContact = ""
            },
            onSave = { updatedContact ->
                if (updatedContact.isNotBlank()) {
                    contactsList = contactsList.toMutableList().apply {
                        set(editingIndex!!, updatedContact)
                    }
                    saveContacts(authViewModel, currentUser, contactsList, context)
                }
                editingIndex = null
                editingContact = ""
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && deletingIndex != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletingIndex = null
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Remove Contact?") },
            text = {
                Text("Are you sure you want to remove ${contactsList[deletingIndex!!]} from your emergency contacts?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        contactsList = contactsList.toMutableList().apply {
                            removeAt(deletingIndex!!)
                        }
                        saveContacts(authViewModel, currentUser, contactsList, context)
                        showDeleteDialog = false
                        deletingIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deletingIndex = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmergencyContactCard(
    contact: String,
    isPrimary: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isPrimary) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isPrimary) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Primary Contact",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = contact,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var contact by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact") },
        text = {
            Column {
                Text(
                    text = "Enter phone number with country code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1234567890") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(contact) },
                enabled = contact.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactDialog(
    contact: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedContact by remember { mutableStateOf(contact) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Emergency Contact") },
        text = {
            Column {
                Text(
                    text = "Update phone number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = editedContact,
                    onValueChange = { editedContact = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editedContact) },
                enabled = editedContact.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun saveContacts(
    authViewModel: AuthViewModel,
    currentUser: com.example.practicaltrainingproject.data.auth.User?,
    contacts: List<String>,
    context: android.content.Context
) {
    currentUser?.let { user ->
        val primaryContact = contacts.firstOrNull() ?: ""
        val additionalContacts = if (contacts.size > 1) contacts.drop(1) else emptyList()

        authViewModel.updateUserProfile(
            name = user.name,
            age = user.age,
            gender = user.gender,
            profession = user.profession,
            healthConditions = user.healthConditions,
            emergencyContact = primaryContact,
            emergencyContacts = additionalContacts
        )

        Toast.makeText(
            context,
            "Emergency contacts updated successfully",
            Toast.LENGTH_SHORT
        ).show()
    }
}


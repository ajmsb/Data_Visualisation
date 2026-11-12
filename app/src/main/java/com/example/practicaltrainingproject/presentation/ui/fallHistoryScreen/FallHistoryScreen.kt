package com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen

/**
 * Detailed fall history screen showing all recorded fall events
 * with filtering, statistics, and export options
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.practicaltrainingproject.presentation.ui.components.ConfirmationDialog
import com.example.practicaltrainingproject.presentation.ui.components.ExportUtil
import com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components.FallDetailModal
import com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components.FallHistoryItem
import com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components.FallHistoryStats
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallDetectionViewModel
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallHistoryScreen(
    navController: NavHostController,
    fallDetectionViewModel: FallDetectionViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by fallDetectionViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var selectedFallEvent by remember { mutableStateOf<FallEvent?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fall History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            ExportUtil.exportFallHistoryToCSV(
                                context = context,
                                fallHistory = uiState.fallHistory
                            )
                        },
                        enabled = uiState.fallHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        enabled = uiState.fallHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        // Show loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Show error state
        else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error Loading History",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { /* Retry logic */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        Text("Retry")
                    }
                }
            }
        }
        // Show content
        else if (uiState.fallHistory.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Fall History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All detected falls and cancelled alerts will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Card
                item {
                    FallHistoryStats(
                        totalFalls = uiState.fallCount,
                        cancelledAlerts = uiState.fallHistory.count { it.wasCancelled },
                        confirmedFalls = uiState.fallHistory.count { !it.wasCancelled }
                    )
                }

                // History Items
                items(uiState.fallHistory.reversed()) { event ->
                    FallHistoryItem(
                        event = event,
                        onClick = { selectedFallEvent = event }
                    )
                }
            }
        }
    }

    // Show fall detail modal when event is selected
    selectedFallEvent?.let { event ->
        FallDetailModal(
            fallEvent = event,
            emergencyContact = currentUser?.emergencyContact,
            onDismiss = { selectedFallEvent = null }
        )
    }

    // Confirmation dialog for clearing history
    if (showClearConfirmation) {
        ConfirmationDialog(
            title = "Clear All History?",
            message = "This will permanently delete all ${uiState.fallHistory.size} fall events. This action cannot be undone.",
            confirmText = "Clear All",
            dismissText = "Cancel",
            isDangerous = true,
            onConfirm = { fallDetectionViewModel.clearHistory() },
            onDismiss = { showClearConfirmation = false }
        )
    }
}
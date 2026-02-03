package com.achaquisse.smsgatewayclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: ConfigViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    var newTopic by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Gateway Configuration") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.baseUrl,
                onValueChange = { viewModel.baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            )

            OutlinedTextField(
                value = viewModel.deviceKey,
                onValueChange = { viewModel.deviceKey = it },
                label = { Text("Device Key") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Enable Polling", style = MaterialTheme.typography.titleMedium)
                    Text("Background service will poll for messages", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = viewModel.pollingEnabled,
                    onCheckedChange = { viewModel.togglePolling(it) },
                    enabled = !viewModel.isLoading
                )
            }

            Divider()

            Text("Topics", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTopic,
                    onValueChange = { newTopic = it },
                    label = { Text("Add Topic") },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                )
                IconButton(onClick = {
                    viewModel.addTopic(newTopic)
                    newTopic = ""
                }, enabled = !viewModel.isLoading) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.topics) { topic ->
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = { Text(topic) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.removeTopic(topic) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveAndSync() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            ) {
                Text("Save & Sync")
            }
        }
    }
}

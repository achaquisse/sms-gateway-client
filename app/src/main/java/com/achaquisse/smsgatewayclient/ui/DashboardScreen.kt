package com.achaquisse.smsgatewayclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PollingStatusCard(
                pollingEnabled = viewModel.pollingEnabled,
                lastPollTime = viewModel.lastPollTime
            )

            when {
                viewModel.isLoading && viewModel.reportData == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                viewModel.errorMessage != null && viewModel.reportData == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                viewModel.reportData != null -> {
                    val report = viewModel.reportData!!
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SummaryCard("Sent", report.summary.sent.toString(), Modifier.weight(1f))
                            SummaryCard("Failed", report.summary.failed.toString(), Modifier.weight(1f))
                            SummaryCard(
                                "Total",
                                report.summary.total.toString(),
                                Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider()

                        Text("Breakdown by Topic", style = MaterialTheme.typography.titleMedium)

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(report.byTopic) { topicStat ->
                                ListItem(
                                    headlineContent = { Text(topicStat.topic) },
                                    trailingContent = { Text("${topicStat.total} messages") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PollingStatusCard(pollingEnabled: Boolean, lastPollTime: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (pollingEnabled) "Polling Enabled" else "Polling Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (pollingEnabled) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Last poll: ${lastPollTime ?: "Never"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

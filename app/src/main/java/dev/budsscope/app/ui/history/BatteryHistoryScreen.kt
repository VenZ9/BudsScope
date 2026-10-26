package dev.budsscope.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.budsscope.app.R
import dev.budsscope.app.ui.components.BatteryHistoryChart
import dev.budsscope.app.ui.components.BudsCard
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.BgDark
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.TextMuted
import dev.budsscope.app.ui.theme.TextPrimary
import dev.budsscope.app.ui.theme.TextSecondary

@Composable
fun BatteryHistoryScreen(
    viewModel: BatteryHistoryViewModel
) {
    val activeDevice by viewModel.activeDevice.collectAsState()
    val snapshots by viewModel.historySnapshots.collectAsState()
    val trend by viewModel.trendAnalysis.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            if (snapshots.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Clear", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Connection Banner
        val deviceName = activeDevice?.name ?: "No device connected"
        Text(
            text = "Device: $deviceName",
            style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Observed Drain Rate
            BudsCard(
                modifier = Modifier.weight(1f),
                contentPadding = 14.dp
            ) {
                Text(
                    text = stringResource(R.string.history_drain_rate),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (trend.hasSufficientData) trend.formattedDrainRate else "--",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                )
            }

            // Estimated Remaining Listening Time
            BudsCard(
                modifier = Modifier.weight(1f),
                contentPadding = 14.dp
            ) {
                Text(
                    text = stringResource(R.string.history_estimated_time),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (trend.hasSufficientData) trend.formattedRemainingHours else "--",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Timeline Chart Card
        BudsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discharge Timeline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${snapshots.size} sample(s)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Chart (chronological order)
            BatteryHistoryChart(
                snapshotsAscending = snapshots.reversed(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trend.summaryBasis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Battery Health Notice (Mandated honesty requirement)
        BudsCard {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.history_health_notice_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.history_health_notice_body),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

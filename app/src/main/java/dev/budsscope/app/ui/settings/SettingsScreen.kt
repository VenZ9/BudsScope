package dev.budsscope.app.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.budsscope.app.R
import dev.budsscope.app.ui.components.BudsCard
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.BgDark
import dev.budsscope.app.ui.theme.StatusRed
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.SurfaceElevated
import dev.budsscope.app.ui.theme.TextMuted
import dev.budsscope.app.ui.theme.TextPrimary
import dev.budsscope.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToDiagnostics: () -> Unit
) {
    val lowBatteryAlert by viewModel.lowBatteryAlertEnabled.collectAsState()
    val lowBatteryThreshold by viewModel.lowBatteryThreshold.collectAsState()
    val disconnectAlert by viewModel.disconnectAlertEnabled.collectAsState()
    val persistentService by viewModel.persistentServiceEnabled.collectAsState()
    val fluidAnimations by viewModel.fluidWaveAnimationEnabled.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var dataClearedNotice by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Category: Alerts & Notifications
        Text(
            text = stringResource(R.string.pref_notifications_category),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        BudsCard {
            // Low Battery Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pref_low_battery_alert),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = stringResource(R.string.pref_low_battery_desc),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Switch(
                    checked = lowBatteryAlert,
                    onCheckedChange = { viewModel.setLowBatteryAlert(it) },
                    colors = switchColors()
                )
            }

            if (lowBatteryAlert) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.pref_low_battery_threshold, lowBatteryThreshold),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
                Slider(
                    value = lowBatteryThreshold.toFloat(),
                    onValueChange = { viewModel.setLowBatteryThreshold(it.roundToInt()) },
                    valueRange = 5f..40f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = SurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Disconnect Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pref_disconnect_alert),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = stringResource(R.string.pref_disconnect_desc),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Switch(
                    checked = disconnectAlert,
                    onCheckedChange = { viewModel.setDisconnectAlert(it) },
                    colors = switchColors()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Persistent Foreground Service
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pref_persistent_service),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = stringResource(R.string.pref_persistent_service_desc),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Switch(
                    checked = persistentService,
                    onCheckedChange = { viewModel.setPersistentService(it) },
                    colors = switchColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category: Visuals & Performance
        Text(
            text = stringResource(R.string.pref_visuals_category),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        BudsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pref_animations),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = stringResource(R.string.pref_animations_desc),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Switch(
                    checked = fluidAnimations,
                    onCheckedChange = { viewModel.setFluidWaveAnimation(it) },
                    colors = switchColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category: Diagnostics
        Text(
            text = stringResource(R.string.pref_diagnostics_category),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        BudsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pref_view_diagnostics),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Inspect Bluetooth adapter status, runtime permissions, and hardware profiles.",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToDiagnostics,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category: Privacy & Data Deletion
        Text(
            text = stringResource(R.string.pref_privacy_category),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        BudsCard {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BudsScope is completely offline. No user data, device identifiers, or playback metadata ever leaves this device.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusRed.copy(alpha = 0.2f),
                    contentColor = StatusRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.pref_clear_all_data),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (dataClearedNotice) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Local history and settings cleared.",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Erase Local Data?") },
            text = {
                Text(text = "This will delete all recorded battery history snapshots, connection event logs, and reset settings. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showDeleteConfirmDialog = false
                        dataClearedNotice = true
                    }
                ) {
                    Text(text = "Erase", color = StatusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Cancel")
                }
            },
            containerColor = SurfaceElevated
        )
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = BgDark,
    checkedTrackColor = AccentCyan,
    uncheckedThumbColor = TextMuted,
    uncheckedTrackColor = SurfaceElevated
)

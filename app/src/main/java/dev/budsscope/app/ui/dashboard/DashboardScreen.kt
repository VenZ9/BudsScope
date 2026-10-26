package dev.budsscope.app.ui.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.budsscope.app.R
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import dev.budsscope.app.domain.DeviceCapabilityResolver
import dev.budsscope.app.ui.components.BatteryRingGauge
import dev.budsscope.app.ui.components.BudsCard
import dev.budsscope.app.ui.components.EarbudVectorVisual
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.BgDark
import dev.budsscope.app.ui.theme.StatusGreen
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.SurfaceElevated
import dev.budsscope.app.ui.theme.TextMuted
import dev.budsscope.app.ui.theme.TextPrimary
import dev.budsscope.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRadar: () -> Unit
) {
    val context = LocalContext.current
    val activeDevice by viewModel.activeDevice.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Buds",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Scope",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection Status Pill
                val isConnected = activeDevice?.isConnected == true
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) StatusGreen else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) stringResource(R.string.status_connected) else stringResource(R.string.status_disconnected),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (isConnected) TextPrimary else TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.nav_settings),
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vector Earbud Graphic
        EarbudVectorVisual(
            isConnected = activeDevice?.isConnected == true,
            isCharging = activeDevice?.batteryState?.isAnyCharging == true
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (activeDevice != null && activeDevice!!.isConnected) {
            ConnectedDeviceContent(
                device = activeDevice!!,
                onRefresh = { viewModel.refresh() },
                onOpenBluetoothSettings = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (_: Exception) {}
                }
            )
        } else {
            DisconnectedEmptyState(
                onOpenBluetoothSettings = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (_: Exception) {}
                },
                onScanRadar = onNavigateToRadar
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectedDeviceContent(
    device: BluetoothDeviceInfo,
    onRefresh: () -> Unit,
    onOpenBluetoothSettings: () -> Unit
) {
    val battery = device.batteryState

    BudsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = DeviceCapabilityResolver.getReportingTypeLabel(device.capabilities),
                    style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
                )
            }

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.action_refresh),
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Battery Gauges
        if (battery.hasSeparateReadings) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryRingGauge(
                    reading = battery.left,
                    label = stringResource(R.string.earbud_left),
                    size = 92.dp
                )
                BatteryRingGauge(
                    reading = battery.right,
                    label = stringResource(R.string.earbud_right),
                    size = 92.dp
                )
                if (battery.case.isValid || device.capabilities.reportsCaseBattery) {
                    BatteryRingGauge(
                        reading = battery.case,
                        label = stringResource(R.string.charging_case),
                        size = 92.dp
                    )
                }
            }
        } else if (battery.combined.isValid) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BatteryRingGauge(
                    reading = battery.combined,
                    label = stringResource(R.string.battery_combined),
                    size = 110.dp,
                    strokeWidth = 9.dp
                )
            }
        } else {
            // Hardware reports no battery
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Battery Reading Unsupported by Connected Hardware",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Text(
                    text = "This device does not broadcast standard Android battery intents or GATT battery level.",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Profile Details Card
    BudsCard {
        Text(
            text = "Connection Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Bluetooth Address", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = device.address,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Active Profiles", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (device.profiles.isNotEmpty()) device.profiles.joinToString(", ") else "Standard Audio",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = AccentCyan
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Last Synced", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = timeFormat.format(Date(device.lastUpdatedEpochMs)),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onOpenBluetoothSettings,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.action_open_bluetooth))
        }
    }
}

@Composable
private fun DisconnectedEmptyState(
    onOpenBluetoothSettings: () -> Unit,
    onScanRadar: () -> Unit
) {
    BudsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothConnected,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.no_device_connected),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.no_device_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onOpenBluetoothSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = BgDark
                )
            ) {
                Text(
                    text = stringResource(R.string.action_open_bluetooth),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onScanRadar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Text(text = stringResource(R.string.action_scan_nearby))
            }
        }
    }
}

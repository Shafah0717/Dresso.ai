package com.dresso.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.UnitSystem
import com.dresso.app.data.model.UserPreferences
import com.dresso.app.ui.components.CompletionMeta
import com.dresso.app.ui.components.DressoWordmark
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.SectionLabel
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import com.dresso.app.viewmodel.BodyProfileState

@Composable
fun ProfileScreen(
    bodyProfile: BodyProfileState,
    preferences: UserPreferences,
    onMeasurements: () -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurements = (bodyProfile as? BodyProfileState.Ready)?.measurements
    val isLoading = bodyProfile is BodyProfileState.Loading

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DressoWordmark(size = 19.dp)
                Spacer(Modifier.weight(1f))
                Eyebrow("Private / v1.0", color = DressoColors.MutedDeep)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp),
            ) {
                Eyebrow("Your space", color = DressoColors.MutedDeep)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Profile.",
                    color = DressoColors.White,
                    style = DressoTypography.displayLarge.copy(
                        fontSize = 64.sp,
                        lineHeight = 62.sp,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "A local profile for your body measurements and display " +
                        "preferences. No account is required.",
                    color = DressoColors.Muted,
                    style = DressoTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.9f),
                )
                Spacer(Modifier.height(34.dp))

                SectionLabel("Body profile", color = DressoColors.MutedDeep)
                Spacer(Modifier.height(8.dp))
                MeasurementRow(
                    measurements = measurements,
                    isLoading = isLoading,
                    onClick = onMeasurements,
                )
                Hairline(color = DressoColors.Hairline)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 42.dp),
            ) {
                SectionLabel("Preferences", color = DressoColors.MutedDeep)
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Units",
                            color = DressoColors.Ivory,
                            style = DressoTypography.titleMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Measurements are stored in centimetres.",
                            color = DressoColors.MutedDeep,
                            style = DressoTypography.bodySmall,
                        )
                    }
                    UnitSystemSelector(
                        selected = preferences.unitSystem,
                        onSelect = onUnitSystemChange,
                    )
                }
                Spacer(Modifier.height(22.dp))
                Hairline(color = DressoColors.Hairline)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 42.dp),
            ) {
                SectionLabel("About Dresso", color = DressoColors.MutedDeep)
                Spacer(Modifier.height(18.dp))
                InfoRow(
                    label = "The app",
                    value = "Demo marketplace · v1.0",
                )
                InfoRow(
                    label = "Privacy",
                    value = "Measurements stay on this device",
                )
                InfoRow(
                    label = "3D fitting",
                    value = "In development",
                )
                InfoRow(
                    label = "Typefaces",
                    value = "Cormorant Garamond · Inter",
                    showDivider = false,
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 50.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(DressoColors.Panel.copy(alpha = 0.6f))
                        .padding(22.dp),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(DressoColors.Ivory.copy(alpha = 0.7f)),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No account.\nNo cloud.\nNo noise.",
                            color = DressoColors.Ivory,
                            style = DressoTypography.headlineSmall.copy(
                                fontSize = 23.sp,
                                lineHeight = 29.sp,
                            ),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Your measurements live only in this app's local storage. " +
                                "The avatar preview uses mesh-estimated neutral values; garment " +
                                "fitting still needs verified calibration.",
                            color = DressoColors.MutedDeep,
                            style = DressoTypography.bodySmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 16.sp,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(34.dp))
                Text(
                    text = "DRESSO / PRIVATE PROFILE",
                    color = DressoColors.MutedDeep,
                    style = EyebrowStyle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MeasurementRow(
    measurements: BodyMeasurements?,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 21.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "My Measurements",
                color = DressoColors.Ivory,
                style = DressoTypography.titleLarge.copy(fontSize = 24.sp),
            )
            Spacer(Modifier.height(8.dp))
            val status = when {
                isLoading -> "Reading device profile…"
                measurements == null -> "Not set · Height, chest, waist and more"
                measurements.isComplete -> "Complete · Saved on this device"
                else -> "Saved on this device"
            }
            Text(
                text = status,
                color = DressoColors.MutedDeep,
                style = DressoTypography.bodySmall,
            )
            if (measurements != null) {
                Spacer(Modifier.height(11.dp))
                CompletionMeta(
                    completed = measurements.completedFields,
                    total = BodyMeasurements.MEASUREMENT_FIELD_COUNT,
                    color = DressoColors.Muted,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DressoColors.PanelRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = DressoColors.Ivory,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun UnitSystemSelector(
    selected: UnitSystem,
    onSelect: (UnitSystem) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(DressoColors.Panel),
    ) {
        UnitOption(
            label = "CM",
            selected = selected == UnitSystem.METRIC,
            onClick = { onSelect(UnitSystem.METRIC) },
        )
        UnitOption(
            label = "IN",
            selected = selected == UnitSystem.IMPERIAL,
            onClick = { onSelect(UnitSystem.IMPERIAL) },
        )
    }
}

@Composable
private fun UnitOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 40.dp)
            .background(if (selected) DressoColors.Ivory else Color.Transparent)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) DressoColors.Ink else DressoColors.Muted,
            style = EyebrowStyle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = DressoColors.Muted,
                style = DressoTypography.bodyMedium,
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = value,
                color = DressoColors.MutedDeep,
                style = DressoTypography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.End,
            )
        }
        if (showDivider) {
            Hairline(color = DressoColors.HairlineSoft)
        }
    }
}

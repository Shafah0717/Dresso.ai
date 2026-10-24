package com.dresso.app.ui.screens.tryon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.core.threeD.AvatarRenderRequest
import com.dresso.app.core.threeD.AvatarViewport
import com.dresso.app.core.threeD.BodyMorphControl
import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.GarmentColor
import com.dresso.app.data.model.GarmentSize
import com.dresso.app.domain.FitRecommendation
import com.dresso.app.ui.components.CompletionMeta
import com.dresso.app.ui.components.DressoButton
import com.dresso.app.ui.components.DressoWordmark
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.GarmentArtwork
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.QuietAction
import com.dresso.app.ui.components.SectionLabel
import com.dresso.app.ui.components.priceLabel
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import com.dresso.app.viewmodel.BodyProfileState
import com.dresso.app.viewmodel.SelectedLook

@Composable
fun TryOnScreen(
    look: SelectedLook?,
    bodyProfile: BodyProfileState,
    avatarMeasurements: BodyMeasurements?,
    fitRecommendation: FitRecommendation?,
    onBodyControlChange: (BodyMorphControl, Float) -> Unit,
    onBodyControlFinished: () -> Unit,
    onSelectColor: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onViewProduct: () -> Unit,
    onEditMeasurements: () -> Unit,
    onDiscover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 34.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DressoWordmark(size = 16.dp, color = DressoColors.White.copy(alpha = 0.9f))
                Spacer(Modifier.weight(1f))
                Eyebrow("Fitting room / 02", color = DressoColors.MutedDeep)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 36.dp),
            ) {
                Eyebrow("The fitting room", color = DressoColors.MutedDeep)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Try on.",
                    color = DressoColors.White,
                    style = DressoTypography.displayMedium.copy(
                        fontSize = 58.sp,
                        lineHeight = 58.sp,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (look == null) {
                        "Choose a piece. Your measurements stay private on this device."
                    } else {
                        "Preview your avatar. Garment fitting is not available yet."
                    },
                    color = DressoColors.Muted,
                    style = DressoTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.88f),
                )
            }
        }

        item {
            AvatarViewport(
                request = AvatarRenderRequest(
                    garmentId = look?.garment?.id,
                    garmentAssetPath = look?.garment?.modelAssetPath,
                    colorId = look?.color?.id,
                    sizeId = look?.size?.id,
                    measurements = avatarMeasurements,
                ),
                bodyControlsEnabled = bodyProfile is BodyProfileState.Ready,
                onBodyControlChange = onBodyControlChange,
                onBodyControlFinished = onBodyControlFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp)
                    .aspectRatio(0.79f),
            )
        }

        if (look == null) {
            item {
                EmptySelection(
                    onDiscover = onDiscover,
                    onEditMeasurements = onEditMeasurements,
                    bodyProfile = bodyProfile,
                )
            }
        } else {
            item {
                SelectedGarmentSummary(
                    look = look,
                    onViewProduct = onViewProduct,
                )
            }

            item {
                TryOnControls(
                    look = look,
                    onSelectColor = onSelectColor,
                    onSelectSize = onSelectSize,
                )
            }

            item {
                FitSummary(
                    look = look,
                    bodyProfile = bodyProfile,
                    fitRecommendation = fitRecommendation,
                    onEditMeasurements = onEditMeasurements,
                )
            }
        }
    }
}

@Composable
private fun SelectedGarmentSummary(
    look: SelectedLook,
    onViewProduct: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 26.dp),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(66.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(1.dp)),
            ) {
                GarmentArtwork(
                    garment = look.garment,
                    color = look.color ?: look.garment.colors.first(),
                    imageKey = look.garment.imageKeys.first(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow(
                    text = look.brand?.name ?: "Selected piece",
                    color = DressoColors.MutedDeep,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = look.garment.name,
                    color = DressoColors.Ivory,
                    style = DressoTypography.headlineSmall.copy(
                        fontSize = 21.sp,
                        lineHeight = 25.sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append(look.color?.name ?: "Colour")
                        append("  ·  ")
                        append(look.size?.label?.let { "Size $it" } ?: "No size")
                    },
                    color = DressoColors.Muted,
                    style = DressoTypography.bodySmall,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = look.garment.priceLabel,
                color = DressoColors.Muted,
                style = DressoTypography.bodySmall,
            )
        }

        Spacer(Modifier.height(4.dp))
        QuietAction(
            label = "View piece",
            onClick = onViewProduct,
            color = DressoColors.Ivory,
        )
    }
}

@Composable
private fun TryOnControls(
    look: SelectedLook,
    onSelectColor: (String) -> Unit,
    onSelectSize: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp),
    ) {
        Hairline(color = DressoColors.Hairline)
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Colour",
                color = DressoColors.White,
                style = DressoTypography.labelMedium.copy(letterSpacing = 1.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = look.color?.name ?: "—",
                color = DressoColors.Muted,
                style = DressoTypography.bodySmall,
            )
        }
        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            look.garment.colors.forEach { color ->
                TryOnColorSwatch(
                    color = color,
                    selected = color.id == look.color?.id,
                    onClick = { onSelectColor(color.id) },
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Size",
                color = DressoColors.White,
                style = DressoTypography.labelMedium.copy(letterSpacing = 1.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = look.size?.let { "EU ${it.label}" }
                    ?: if (look.garment.sizes.isEmpty()) "Not specified" else "Select a size",
                color = DressoColors.Muted,
                style = DressoTypography.bodySmall,
            )
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            look.garment.sizes.forEach { size ->
                TryOnSizeChip(
                    size = size,
                    selected = size.id == look.size?.id,
                    onClick = { onSelectSize(size.id) },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Hairline(color = DressoColors.Hairline)
    }
}

@Composable
private fun TryOnColorSwatch(
    color: GarmentColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .semantics {
                contentDescription = color.name
                this.selected = selected
            }
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 30.dp else 26.dp)
                .border(
                    width = 1.dp,
                    color = if (selected) DressoColors.White else DressoColors.Hairline,
                    shape = CircleShape,
                )
                .padding(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(color.argb)),
            )
        }
    }
}

@Composable
private fun TryOnSizeChip(
    size: GarmentSize,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 50.dp, minHeight = 42.dp)
            .background(
                color = if (selected) DressoColors.Ivory else Color.Transparent,
            )
            .border(
                width = 1.dp,
                color = if (selected) DressoColors.Ivory else DressoColors.Hairline,
            )
            .semantics {
                contentDescription = "Size ${size.label}"
                this.selected = selected
            }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = size.label,
            color = if (selected) DressoColors.Ink else DressoColors.Muted,
            style = EyebrowStyle.copy(
                fontSize = 10.sp,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun FitSummary(
    look: SelectedLook,
    bodyProfile: BodyProfileState,
    fitRecommendation: FitRecommendation?,
    onEditMeasurements: () -> Unit,
) {
    val measurements = (bodyProfile as? BodyProfileState.Ready)?.measurements
    val isLoadingProfile = bodyProfile is BodyProfileState.Loading

    val guidanceLabel = when (fitRecommendation) {
        null -> if (isLoadingProfile) "Checking…" else "Unavailable"
        FitRecommendation.MeasurementsRequired -> "Unavailable"
        is FitRecommendation.Unavailable -> "Unavailable"
        is FitRecommendation.Available -> "Available"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 26.dp),
    ) {
        SectionLabel("Fit summary", color = DressoColors.MutedDeep)
        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, DressoColors.Hairline, RoundedCornerShape(2.dp))
                .background(DressoColors.Panel.copy(alpha = 0.55f))
                .padding(18.dp),
        ) {
            SummaryRow(
                label = "Body profile",
                value = when {
                    isLoadingProfile -> "Checking…"
                    measurements == null -> "Not saved"
                    else -> "Measurements saved"
                },
            )
            if (measurements != null) {
                Spacer(Modifier.height(9.dp))
                CompletionMeta(
                    completed = measurements.completedFields,
                    total = com.dresso.app.data.model.BodyMeasurements.MEASUREMENT_FIELD_COUNT,
                    color = DressoColors.MutedDeep,
                )
            }
            Spacer(Modifier.height(15.dp))
            Hairline(color = DressoColors.HairlineSoft)
            Spacer(Modifier.height(15.dp))
            SummaryRow(
                label = "Fit guidance",
                value = guidanceLabel,
            )
            Spacer(Modifier.height(13.dp))
            Text(
                text = if (measurements == null) {
                    "Fit guidance is unavailable. Add measurements in Profile to preview " +
                        "your avatar's shape; garments are not fitted yet."
                } else {
                    "Measurements saved. Fit guidance is unavailable because this demo " +
                        "has no verified size chart for the selected piece."
                },
                color = DressoColors.MutedDeep,
                style = DressoTypography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp),
            )
            Spacer(Modifier.height(6.dp))
            QuietAction(
                label = if (measurements == null) "Add measurements" else "Edit measurements",
                onClick = onEditMeasurements,
                color = DressoColors.Ivory,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "The avatar can be viewed and animated. No garment is fitted or " +
                "simulated on the body in this version.",
            color = DressoColors.MutedDeep,
            style = DressoTypography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp),
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = DressoColors.Muted,
            style = DressoTypography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = DressoColors.Ivory,
            style = DressoTypography.bodySmall.copy(letterSpacing = 0.2.sp),
        )
    }
}

@Composable
private fun EmptySelection(
    onDiscover: () -> Unit,
    onEditMeasurements: () -> Unit,
    bodyProfile: BodyProfileState,
) {
    val measurements = (bodyProfile as? BodyProfileState.Ready)?.measurements

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 34.dp),
    ) {
        SectionLabel("Nothing selected", color = DressoColors.MutedDeep)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Choose a piece from a brand collection to begin.",
            color = DressoColors.Ivory,
            style = DressoTypography.headlineSmall.copy(
                fontSize = 23.sp,
                lineHeight = 29.sp,
            ),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "The fitting room will keep your selected colour and size while you " +
                "move between the catalogue and your profile.",
            color = DressoColors.Muted,
            style = DressoTypography.bodyMedium,
        )
        Spacer(Modifier.height(22.dp))
        DressoButton(
            label = "Explore collections",
            onClick = onDiscover,
            modifier = Modifier.width(238.dp),
        )
        Spacer(Modifier.height(20.dp))
        QuietAction(
            label = if (measurements == null) "Add measurements" else "View measurements",
            onClick = onEditMeasurements,
            color = DressoColors.Muted,
        )
    }
}

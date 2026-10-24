package com.dresso.app.ui.screens.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dresso.app.AppContainer
import com.dresso.app.core.threeD.BodyMorphControl
import com.dresso.app.data.model.MeasurementField
import com.dresso.app.data.model.UnitSystem
import com.dresso.app.ui.components.CompletionMeta
import com.dresso.app.ui.components.DressoBackButton
import com.dresso.app.ui.components.DressoButton
import com.dresso.app.ui.components.DressoWordmark
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.QuietAction
import com.dresso.app.ui.components.SectionLabel
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import com.dresso.app.viewmodel.MeasurementsViewModel
import kotlin.math.roundToInt

@Composable
fun MeasurementsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MeasurementsViewModel = viewModel(
        factory = MeasurementsViewModel.factory(container),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) {
            viewModel.onSavedHandled()
            onSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DressoColors.Ink,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            SaveBar(
                isSaving = state.isSaving,
                isLoading = state.isLoading,
                onSave = viewModel::save,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 34.dp),
        ) {
            item {
                MeasurementsTopBar(onBack = onBack)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 28.dp),
                ) {
                    Eyebrow("Profile / Measurements", color = DressoColors.MutedDeep)
                    Spacer(Modifier.height(15.dp))
                    Text(
                        text = "My\nmeasurements.",
                        color = DressoColors.White,
                        style = DressoTypography.displayMedium.copy(
                            fontSize = 52.sp,
                            lineHeight = 50.sp,
                        ),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "Private to this device. Every field is optional; add what " +
                            "you know and update it at any time.",
                        color = DressoColors.Muted,
                        style = DressoTypography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                    Spacer(Modifier.height(26.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompletionMeta(
                            completed = state.completedFields,
                            total = MeasurementField.entries.size,
                            color = DressoColors.Muted,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    DressoColors.Panel,
                                    RoundedCornerShape(1.dp),
                                )
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = state.unitSystem.suffix.uppercase(),
                                color = DressoColors.Muted,
                                style = EyebrowStyle,
                            )
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    SectionLabel("Body measurements", color = DressoColors.MutedDeep)
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = if (state.unitSystem == UnitSystem.METRIC) {
                            "Shown in centimetres. Approximate neutral hints come from the " +
                                "avatar mesh, not your body."
                        } else {
                            "Shown in inches; stored in centimetres. Neutral hints are " +
                                "mesh estimates, not your measurements."
                        },
                        color = DressoColors.MutedDeep,
                        style = DressoTypography.bodySmall.copy(fontSize = 10.sp),
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    MeasurementField.entries.forEachIndexed { index, field ->
                        MeasurementInputRow(
                            field = field,
                            value = state.fields[field].orEmpty(),
                            unit = state.unitSystem.suffix,
                            error = state.fieldErrors[field],
                            neutralHint = BodyMorphControl.entries.firstOrNull { it.field == field }
                                ?.let { control ->
                                    val value = if (state.unitSystem == UnitSystem.METRIC) {
                                        control.neutralCm
                                    } else {
                                        control.neutralCm / 2.54f
                                    }
                                    "≈${value.roundToInt()}"
                                },
                            enabled = !state.isLoading && !state.isSaving,
                            isLast = index == MeasurementField.entries.lastIndex,
                            onValueChange = { viewModel.onFieldChange(field, it) },
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp),
                ) {
                    if (state.generalMessage != null) {
                        Row(
                            modifier = Modifier.padding(bottom = 14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp, end = 9.dp)
                                    .size(4.dp)
                                    .background(DressoColors.Ivory, RoundedCornerShape(50)),
                            )
                            Text(
                                text = state.generalMessage.orEmpty(),
                                color = DressoColors.Ivory.copy(alpha = 0.9f),
                                style = DressoTypography.bodySmall,
                            )
                        }
                    }
                    Text(
                        text = "Saved only on this device. Measurements preview the avatar " +
                            "within the artist's morph range; they do not fit garments or " +
                            "provide verified sizing advice.",
                        color = DressoColors.MutedDeep,
                        style = DressoTypography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp),
                    )
                    if (state.existingMeasurements != null || state.completedFields > 0) {
                        Spacer(Modifier.height(4.dp))
                        QuietAction(
                            label = "Clear measurements",
                            onClick = viewModel::requestClear,
                            color = DressoColors.Muted,
                            showArrow = false,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (state.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClear,
            containerColor = DressoColors.PanelRaised,
            titleContentColor = DressoColors.Ivory,
            textContentColor = DressoColors.Muted,
            shape = RoundedCornerShape(2.dp),
            title = {
                Text(
                    text = "Clear measurements?",
                    style = DressoTypography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "This removes your body profile from this device. It cannot be " +
                        "undone.",
                    style = DressoTypography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::clearAll) {
                    Text("Clear", color = DressoColors.Ivory, style = EyebrowStyle)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClear) {
                    Text("Keep", color = DressoColors.Muted, style = EyebrowStyle)
                }
            },
        )
    }
}

@Composable
private fun MeasurementsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(58.dp)
            .padding(start = 10.dp, end = 24.dp),
    ) {
        DressoBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        DressoWordmark(
            size = 16.dp,
            color = DressoColors.White.copy(alpha = 0.9f),
            modifier = Modifier.align(Alignment.Center),
        )
        Eyebrow(
            text = "Private",
            color = DressoColors.MutedDeep,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun MeasurementInputRow(
    field: MeasurementField,
    value: String,
    unit: String,
    error: String?,
    neutralHint: String?,
    enabled: Boolean,
    isLast: Boolean,
    onValueChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 66.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { focusRequester.requestFocus() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = field.label,
                color = DressoColors.Ivory,
                style = DressoTypography.titleMedium.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp,
                ),
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.width(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    interactionSource = interactionSource,
                    textStyle = DressoTypography.titleLarge.copy(
                        fontFamily = com.dresso.app.ui.theme.Inter,
                        fontSize = 19.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.End,
                        color = DressoColors.White,
                    ),
                    cursorBrush = SolidColor(DressoColors.Ivory),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.clearFocus() },
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = "${field.label}, ${field.minCm} to " +
                                "${field.maxCm} centimetres"
                            if (error != null) this.error(error)
                        },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = neutralHint ?: "—",
                                    color = DressoColors.MutedDeep,
                                    style = DressoTypography.titleLarge.copy(
                                        fontFamily = com.dresso.app.ui.theme.Inter,
                                        fontSize = 19.sp,
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = unit,
                    color = DressoColors.MutedDeep,
                    style = EyebrowStyle,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFocused) 1.dp else 1.dp)
                .background(
                    if (isFocused) {
                        DressoColors.White.copy(alpha = 0.7f)
                    } else if (error != null) {
                        DressoColors.White.copy(alpha = 0.4f)
                    } else {
                        DressoColors.Hairline
                    },
                ),
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "!  $error",
                color = DressoColors.Ivory.copy(alpha = 0.9f),
                style = DressoTypography.bodySmall.copy(fontSize = 10.sp),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SaveBar(
    isSaving: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
) {
    Surface(
        color = DressoColors.Ink,
        contentColor = DressoColors.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Hairline(color = DressoColors.Hairline)
            DressoButton(
                label = when {
                    isLoading -> "Loading profile"
                    isSaving -> "Saving"
                    else -> "Save measurements"
                },
                enabled = !isLoading && !isSaving,
                onClick = onSave,
                showArrow = false,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

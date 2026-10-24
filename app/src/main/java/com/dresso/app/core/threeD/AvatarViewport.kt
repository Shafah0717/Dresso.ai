package com.dresso.app.core.threeD

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.QuietAction
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.renderableEntities
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderInvalidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private const val AVATAR_ASSET = "models/Human_TryOn_Rigged.glb"
private const val TAG = "DressoAvatar"
private const val ANIMATION_A_POSE = "A-Pose"
private const val ZARA_RIGGED_GARMENT = "garments/zara_garmet.glb"

private sealed interface GarmentLoadState {
    data object None : GarmentLoadState
    data class Loading(val path: String) : GarmentLoadState
    data class Ready(
        val path: String,
        val instance: ModelInstance,
        val bounds: NeutralBounds,
    ) : GarmentLoadState
    data class Failed(val path: String, val message: String) : GarmentLoadState
}

private sealed interface AvatarLoadState {
    data object Loading : AvatarLoadState
    data class Ready(
        val instance: ModelInstance,
        val morphs: FilamentMorphBinding,
        val neutralBounds: NeutralBounds,
    ) : AvatarLoadState
    data class Failed(val message: String) : AvatarLoadState
}

/**
 * One Filament scene and one GLB instance per visible fitting room. Only the calibrated body
 * morphs are driven by measurements; rigged garment exports share the avatar animation.
 */
@Composable
fun AvatarViewport(
    modifier: Modifier = Modifier,
    request: AvatarRenderRequest? = null,
    bodyControlsEnabled: Boolean = false,
    onBodyControlChange: (BodyMorphControl, Float) -> Unit = { _, _ -> },
    onBodyControlFinished: () -> Unit = {},
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val renderInvalidator = rememberRenderInvalidator()
    val context = LocalContext.current
    var retryAttempt by remember { mutableIntStateOf(0) }
    var loadState by remember(modelLoader) { mutableStateOf<AvatarLoadState>(AvatarLoadState.Loading) }
    var garmentRetryAttempt by remember { mutableIntStateOf(0) }
    var garmentLoadState by remember(modelLoader) {
        mutableStateOf<GarmentLoadState>(GarmentLoadState.None)
    }
    val garmentPath = request?.garmentAssetPath
    var selectedAnimation by rememberSaveable {
        mutableStateOf(if (request?.garmentAssetPath != null) ANIMATION_A_POSE else "Idle")
    }
    var garmentVisible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(garmentPath) {
        garmentVisible = true
        if (garmentPath != null) {
            // The Zara export carries the same armature/actions as the avatar, so start it
            // in motion. Static source meshes remain in A-Pose for alignment inspection.
            selectedAnimation = if (garmentPath == ZARA_RIGGED_GARMENT) "Idle" else ANIMATION_A_POSE
        }
    }
    var editingBody by rememberSaveable { mutableStateOf(false) }
    var selectedControl by remember { mutableStateOf(BodyMorphControl.HEIGHT) }

    LaunchedEffect(modelLoader, retryAttempt) {
        loadState = AvatarLoadState.Loading
        loadState = try {
            val metadata = withContext(Dispatchers.IO) {
                context.assets.open(AVATAR_ASSET).use(::readGlbAvatarMetadata)
            }
            val instance = modelLoader.loadModelInstance(AVATAR_ASSET)
            if (instance == null) {
                AvatarLoadState.Failed("The avatar could not be loaded from this app's assets.")
            } else {
                val morphs = try {
                    FilamentMorphBinding(engine, instance, metadata.morphDefaults)
                } catch (error: Exception) {
                    modelLoader.destroyModel(instance.asset)
                    throw error
                }
                AvatarLoadState.Ready(instance, morphs, metadata.neutralBounds)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Could not load $AVATAR_ASSET", error)
            AvatarLoadState.Failed(error.message ?: "The avatar could not be opened. Please try again.")
        }
    }

    val ready = loadState as? AvatarLoadState.Ready
    val instance = ready?.instance
    var modelPresented by remember(instance) { mutableStateOf(false) }
    DisposableEffect(modelLoader, instance) {
        onDispose { instance?.let { modelLoader.destroyModel(it.asset) } }
    }

    LaunchedEffect(modelLoader, garmentPath, instance, garmentRetryAttempt) {
        if (garmentPath == null) {
            garmentLoadState = GarmentLoadState.None
            return@LaunchedEffect
        }
        garmentLoadState = GarmentLoadState.Loading(garmentPath)
        if (instance == null) return@LaunchedEffect
        if (garmentAlignProfile(garmentPath) == null) {
            garmentLoadState = GarmentLoadState.Failed(
                garmentPath, "This garment has no inspected 3D preview orientation.",
            )
            return@LaunchedEffect
        }
        garmentLoadState = try {
            val bounds = withContext(Dispatchers.IO) {
                context.assets.open(garmentPath).use(::readGlbPositionBounds)
            }
            val loaded = modelLoader.loadModelInstance(garmentPath)
            if (loaded == null) {
                GarmentLoadState.Failed(garmentPath, "Could not load the 3D garment sample.")
            } else {
                GarmentLoadState.Ready(garmentPath, loaded, bounds)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Could not load $garmentPath", error)
            GarmentLoadState.Failed(garmentPath, "Could not open the 3D garment sample.")
        }
    }
    val garmentReady = (garmentLoadState as? GarmentLoadState.Ready)
        ?.takeIf { it.path == garmentPath }
    val garment = garmentReady?.instance
    val alignment = remember(ready?.neutralBounds, garmentReady?.bounds, garmentPath, request?.measurements) {
        val profile = garmentPath?.let(::garmentAlignProfile)
        if (ready != null && garmentReady != null && profile != null) {
            if (garmentPath == ZARA_RIGGED_GARMENT) {
                // Zara is exported in the avatar rig's coordinate space. Reuse the exact
                // avatar root translation; applying bounds-based fitting would move the shirt
                // away from the skeleton (and place it near the legs).
                GarmentAlignment(
                    position = Position(
                        x = -ready.neutralBounds.centerX,
                        y = -ready.neutralBounds.centerY,
                        z = -ready.neutralBounds.centerZ,
                    ),
                    rotation = Rotation(),
                    scale = Scale(x = 1f, y = 1f, z = 1f),
                )
            } else {
                val base = alignGarmentToAvatar(ready.neutralBounds, garmentReady.bounds, profile)
                val measurements = request?.measurements
                // Keep unrigged Nike meshes proportional to the same body changes that drive
                // the avatar morphs. The asset remains one node, so width/depth/height change
                // together instead of drifting away from the body.
                val height = (measurements?.heightCm ?: 170) / 170f
                val widthValues = listOf(
                    measurements?.shoulderCm?.toFloat()?.div(44f),
                    measurements?.chestCm?.toFloat()?.div(96f),
                    measurements?.hipsCm?.toFloat()?.div(100f),
                ).filterNotNull()
                val width = widthValues.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 1f
                val depth = ((measurements?.chestCm ?: 96) / 96f)
                    .coerceIn(0.75f, 1.35f)
                val uniform = ((height + width + depth) / 3f).coerceIn(0.75f, 1.35f)
                GarmentAlignment(
                    position = base.position,
                    rotation = base.rotation,
                    scale = Scale(
                        x = base.scale.x * uniform,
                        y = base.scale.y * uniform,
                        z = base.scale.z * uniform,
                    ),
                )
            }
        } else {
            null
        }
    }
    DisposableEffect(modelLoader, garment) {
        onDispose { garment?.let { modelLoader.destroyModel(it.asset) } }
    }

    LaunchedEffect(ready?.morphs, request?.measurements) {
        val morphs = ready?.morphs ?: return@LaunchedEffect
        val initial = morphs.snapshot()
        val target = morphs.targetWeights(request?.measurements)
        if (initial.contentEquals(target)) {
            // gltfio has no morph-weight getter: explicitly restore all exported identity defaults.
            morphs.apply(initial, target, 1f)
            renderInvalidator.requestRender()
        } else {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            ) { fraction, _ ->
                morphs.apply(initial, target, fraction)
                renderInvalidator.requestRender()
            }
        }
    }

    val skeletalAnimations = remember(instance) {
        instance?.animator?.let { animator ->
            (0 until animator.animationCount).mapNotNull(animator::getAnimationName)
        }.orEmpty()
    }
    val allAnimationOptions = remember(skeletalAnimations) {
        listOf(ANIMATION_A_POSE) + skeletalAnimations
    }
    val activeAnimation = if (selectedAnimation == ANIMATION_A_POSE) {
        null
    } else {
        selectedAnimation.takeIf { it in skeletalAnimations } ?: skeletalAnimations.firstOrNull()
    }
    val rigDetails = remember(instance) {
        instance?.let { model ->
            val joints = if (model.skinCount > 0) model.getJointCountAt(0) else 0
            val renderableManager = engine.renderableManager
            val morphs = model.renderableEntities.maxOfOrNull { entity ->
                renderableManager.getMorphTargetCount(renderableManager.getInstance(entity))
            } ?: 0
            "$joints joints  ·  $morphs morphs"
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFE4E4E1))
            .border(1.dp, DressoColors.Hairline, RoundedCornerShape(2.dp))
            .semantics {
                contentDescription = if (request?.garmentId == null) {
                    "Human avatar viewer. Drag to orbit; pinch to zoom."
                } else if (garmentPath != null) {
                    "Human avatar with a rigged garment preview. The garment follows animation; " +
                        "drag to orbit; pinch to zoom."
                } else {
                    "Human avatar viewer. The selected garment is not simulated yet. " +
                        "Drag to orbit; pinch to zoom."
                }
            },
    ) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraManipulator = rememberCameraManipulator(orbitRadius = 2.78f),
            renderInvalidator = renderInvalidator,
            autoCenterContent = false,
            contentPadding = PaddingValues(top = 42.dp, bottom = if (editingBody) 158.dp else 78.dp),
            onFrame = {
                if (instance != null && !modelLoader.isLoading && !modelPresented) {
                    modelPresented = true
                }
            },
        ) {
            instance?.let { model ->
                ModelNode(
                    modelInstance = model,
                    autoAnimate = false,
                    animationName = activeAnimation,
                    animationLoop = true,
                    position = Position(
                        x = -requireNotNull(ready).neutralBounds.centerX,
                        y = -ready.neutralBounds.centerY,
                        z = -ready.neutralBounds.centerZ,
                    ),
                )
            }
            if (garment != null && alignment != null) {
                ModelNode(
                    modelInstance = garment,
                    autoAnimate = activeAnimation != null,
                    animationName = activeAnimation,
                    position = alignment.position,
                    rotation = alignment.rotation,
                    // Keep the node mounted: SceneView can otherwise retain the removed node
                    // and fail to recreate it when the user taps Show.
                    scale = if (garmentVisible) {
                        alignment.scale
                    } else {
                        Scale(x = 0f, y = 0f, z = 0f)
                    },
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(DressoColors.Ink.copy(alpha = 0.76f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow("Avatar / 3D", color = DressoColors.Ivory)
            Spacer(Modifier.weight(1f))
            Eyebrow(
                rigDetails ?: "Drag to orbit · pinch to zoom",
                color = DressoColors.Muted,
            )
        }

        if (garmentPath != null) {
            when (val state = garmentLoadState) {
                is GarmentLoadState.Ready -> if (state.path == garmentPath) {
                    val label = if (garmentPath == ZARA_RIGGED_GARMENT) {
                        "Rigged garment · ${selectedAnimation.replace('_', ' ')}"
                    } else if (selectedAnimation == ANIMATION_A_POSE) {
                        "Aligned with avatar · A-Pose"
                    } else {
                        "Alignment preview · select A-Pose to align"
                    }
                    Eyebrow(
                        label,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (editingBody) 174.dp else 78.dp)
                            .background(DressoColors.Ink.copy(alpha = 0.88f))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        color = DressoColors.Ivory,
                    )
                }
                is GarmentLoadState.Loading -> if (state.path == garmentPath) {
                    Eyebrow(
                        "Loading garment sample",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (editingBody) 174.dp else 78.dp),
                        color = DressoColors.Muted,
                    )
                }
                is GarmentLoadState.Failed -> if (state.path == garmentPath) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (editingBody) 174.dp else 78.dp)
                            .background(DressoColors.Ink.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Eyebrow(state.message, color = DressoColors.Ivory)
                        QuietAction("Retry sample", onClick = { garmentRetryAttempt++ })
                    }
                }
                GarmentLoadState.None -> Unit
            }
        }

        when (val state = loadState) {
            AvatarLoadState.Loading -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CircularProgressIndicator(color = DressoColors.Ivory)
                Eyebrow("Loading avatar", color = DressoColors.Muted)
            }

            is AvatarLoadState.Failed -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.message,
                    color = DressoColors.Ivory,
                    style = DressoTypography.bodyMedium,
                )
                QuietAction(label = "Try again", onClick = { retryAttempt++ })
            }

            is AvatarLoadState.Ready -> if (!modelPresented) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircularProgressIndicator(color = DressoColors.Ivory)
                    Eyebrow("Preparing avatar", color = DressoColors.Muted)
                }
            }
        }

        if (instance != null) {
            if (editingBody) {
                BodyControlPanel(
                    control = selectedControl,
                    measurements = request?.measurements,
                    enabled = bodyControlsEnabled,
                    onSelect = { selectedControl = it },
                    onChange = onBodyControlChange,
                    onChangeFinished = onBodyControlFinished,
                    onClose = { editingBody = false },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(DressoColors.Ink.copy(alpha = 0.88f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(allAnimationOptions, key = { it }) { name ->
                            AnimationChip(
                                name = name,
                                selected = name == selectedAnimation,
                                onClick = { selectedAnimation = name },
                            )
                        }
                    }
                    Eyebrow(
                        "Body",
                        modifier = Modifier
                            .clickable(role = Role.Button) { editingBody = true }
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        color = DressoColors.Ivory,
                    )
                    if (garmentPath != null) {
                        Eyebrow(
                            if (garmentVisible) "Hide" else "Show",
                            modifier = Modifier
                                .clickable(role = Role.Button) { garmentVisible = !garmentVisible }
                                .padding(horizontal = 12.dp, vertical = 18.dp),
                            color = DressoColors.Ivory,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyControlPanel(
    control: BodyMorphControl,
    measurements: com.dresso.app.data.model.BodyMeasurements?,
    enabled: Boolean,
    onSelect: (BodyMorphControl) -> Unit,
    onChange: (BodyMorphControl, Float) -> Unit,
    onChangeFinished: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stored = control.valueCm(measurements)?.takeIf { it.isFinite() }
    val value = stored ?: control.neutralCm
    val previewIsClamped = stored != null && (stored < control.minCm || stored > control.maxCm)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DressoColors.Ink.copy(alpha = 0.96f))
            .padding(top = 9.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow("Body preview · cm", color = DressoColors.Muted)
            Spacer(Modifier.weight(1f))
            Eyebrow(
                "Motion",
                modifier = Modifier.clickable(role = Role.Button, onClick = onClose),
                color = DressoColors.Ivory,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(BodyMorphControl.entries, key = { it.name }) { option ->
                AnimationChip(
                    name = option.field?.label ?: "Belly projection",
                    selected = option == control,
                    onClick = { onSelect(option) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = control.field?.label ?: "Belly projection (extra depth)",
                color = DressoColors.Ivory,
                style = DressoTypography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (control == BodyMorphControl.BELLY_PROJECTION) {
                    String.format(Locale.US, "%+.1f cm", value.coerceIn(control.minCm, control.maxCm))
                } else {
                    "${value.coerceIn(control.minCm, control.maxCm).roundToInt()} cm"
                },
                color = DressoColors.Ivory,
                style = DressoTypography.bodySmall,
            )
        }
        Slider(
            value = value.coerceIn(control.minCm, control.maxCm),
            onValueChange = { onChange(control, it) },
            onValueChangeFinished = onChangeFinished,
            valueRange = control.minCm..control.maxCm,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = DressoColors.Ivory,
                activeTrackColor = DressoColors.Ivory,
                inactiveTrackColor = DressoColors.Hairline,
            ),
        )
        Eyebrow(
            if (previewIsClamped) {
                "Saved value clipped to safe preview range"
            } else {
                "Mesh-estimated neutral · limited preview range"
            },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            color = DressoColors.MutedDeep,
        )
    }
}

@Composable
private fun AnimationChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = name.replace('_', ' ').uppercase(),
        color = if (selected) DressoColors.Ink else DressoColors.Ivory,
        style = EyebrowStyle.copy(fontSize = 9.sp),
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) DressoColors.Ivory else Color.Transparent)
            .border(1.dp, if (selected) DressoColors.Ivory else DressoColors.Hairline)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
    )
}

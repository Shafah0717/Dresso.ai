package com.dresso.app.core.threeD

import com.google.android.filament.Engine
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.renderableEntities
import java.io.DataInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.json.JSONObject

internal data class NeutralBounds(
    val minX: Float, val minY: Float, val minZ: Float,
    val maxX: Float, val maxY: Float, val maxZ: Float,
) {
    val centerX get() = (minX + maxX) / 2f
    val centerY get() = (minY + maxY) / 2f
    val centerZ get() = (minZ + maxZ) / 2f
    val width get() = maxX - minX
    val height get() = maxY - minY
    val depth get() = maxZ - minZ
}

internal data class GlbAvatarMetadata(
    val morphDefaults: Map<String, Float>,
    val neutralBounds: NeutralBounds,
)

/**
 * Read neutral POSITION accessor bounds from a GLB JSON chunk.
 * Filament's asset boundingBox includes animated/morphed extremes (and puts the floor
 * 17 cm below its actual neutral position), so it cannot anchor a garment to the feet.
 */
internal fun readGlbPositionBounds(source: InputStream): NeutralBounds =
    readGlbJsonRoot(source).let(::neutralBoundsFromGltfRoot)

/**
 * Read the avatar's exported mesh.weights and neutral POSITION accessor bounds once.
 */
internal fun readGlbAvatarMetadata(source: InputStream): GlbAvatarMetadata {
    val root = readGlbJsonRoot(source)
    val defaults = morphDefaultsFromGltfRoot(root)
    require(defaults.isNotEmpty()) { "GLB contains no named morph targets" }
    return GlbAvatarMetadata(
        morphDefaults = defaults,
        neutralBounds = neutralBoundsFromGltfRoot(root),
    )
}

internal fun readGlbJsonRoot(source: InputStream): JSONObject =
    DataInputStream(source.buffered()).use { stream ->
        val header = ByteArray(20)
        stream.readFully(header)
        val bytes = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        require(bytes.getInt(0) == 0x46546C67 && bytes.getInt(4) == 2) { "Not a glTF 2.0 GLB" }
        val jsonSize = bytes.getInt(12)
        require(bytes.getInt(16) == 0x4E4F534A && jsonSize in 1..2_000_000) {
            "GLB has no readable JSON chunk"
        }
        val json = ByteArray(jsonSize)
        stream.readFully(json)
        JSONObject(String(json, Charsets.UTF_8))
    }

internal fun neutralBoundsFromGltfRoot(root: JSONObject): NeutralBounds {
    val meshes = root.getJSONArray("meshes")
    val accessors = root.getJSONArray("accessors")
    val minimum = FloatArray(3) { Float.POSITIVE_INFINITY }
    val maximum = FloatArray(3) { Float.NEGATIVE_INFINITY }
    for (meshIndex in 0 until meshes.length()) {
        val mesh = meshes.getJSONObject(meshIndex)
        val primitives = mesh.getJSONArray("primitives")
        for (primitiveIndex in 0 until primitives.length()) {
            val attributes = primitives.getJSONObject(primitiveIndex).getJSONObject("attributes")
            val positionIndex = attributes.getDouble("POSITION").toInt()
            val position = accessors.getJSONObject(positionIndex)
            val low = position.getJSONArray("min")
            val high = position.getJSONArray("max")
            for (axis in 0..2) {
                minimum[axis] = minOf(minimum[axis], low.getDouble(axis).toFloat())
                maximum[axis] = maxOf(maximum[axis], high.getDouble(axis).toFloat())
            }
        }
    }
    require((0..2).all { minimum[it].isFinite() && maximum[it] > minimum[it] }) {
        "GLB has no valid neutral position bounds"
    }
    return NeutralBounds(
        minimum[0], minimum[1], minimum[2],
        maximum[0], maximum[1], maximum[2],
    )
}

private fun morphDefaultsFromGltfRoot(root: JSONObject): Map<String, Float> {
    val meshes = root.getJSONArray("meshes")
    return buildMap {
        for (meshIndex in 0 until meshes.length()) {
            val mesh = meshes.getJSONObject(meshIndex)
            val names = mesh.optJSONObject("extras")?.optJSONArray("targetNames")
            val weights = mesh.optJSONArray("weights")
            if (names != null) for (index in 0 until names.length()) {
                val name = names.getString(index)
                val weight = if (weights != null && index < weights.length()) {
                    weights.getDouble(index).toFloat()
                } else {
                    0f
                }
                require(name !in this || this[name] == weight) {
                    "Morph $name has inconsistent defaults across meshes"
                }
                put(name, weight)
            }
        }
    }
}

/** One reusable weight array per Filament renderable; names, not asset indices, select controls. */
internal class FilamentMorphBinding(
    private val engine: Engine,
    instance: ModelInstance,
    defaults: Map<String, Float>,
) {
    private data class Target(
        val entity: Int,
        val controlIndices: IntArray,
        val weights: FloatArray,
    )

    private val manager = engine.renderableManager
    private val controls = BodyMorphControl.entries
    private val targets: List<Target> = instance.renderableEntities.mapNotNull { entity ->
        val renderable = manager.getInstance(entity)
        val count = manager.getMorphTargetCount(renderable)
        if (count == 0) return@mapNotNull null
        val names = instance.asset.getMorphTargetNames(entity).toList()
        require(names.size == count && names.distinct().size == count) {
            "Filament morph count or names differ from the GLB on entity $entity"
        }
        require(names.all { it in defaults }) { "GLB morph defaults differ from Filament target names" }
        Target(
            entity = entity,
            controlIndices = IntArray(controls.size) { names.indexOf(controls[it].morphName) },
            weights = FloatArray(count) { defaults.getValue(names[it]) },
        )
    }

    private val current = FloatArray(controls.size) { defaults[controls[it].morphName] ?: 0f }

    init {
        require(targets.isNotEmpty()) { "The avatar has no morphable renderable" }
        require(controls.all { control ->
            targets.any { it.controlIndices[control.ordinal] >= 0 }
        }) { "The avatar is missing one or more named body morphs" }
    }

    fun snapshot(): FloatArray = current.copyOf()

    fun targetWeights(measurements: com.dresso.app.data.model.BodyMeasurements?): FloatArray =
        FloatArray(controls.size) { controls[it].weightFor(measurements) }

    /** Called on the main thread while SceneView owns this same model instance. */
    fun apply(from: FloatArray, to: FloatArray, fraction: Float) {
        for (index in current.indices) {
            current[index] = from[index] + (to[index] - from[index]) * fraction
        }
        targets.forEach { target ->
            for (index in current.indices) {
                val morphIndex = target.controlIndices[index]
                if (morphIndex >= 0) target.weights[morphIndex] = current[index]
            }
            manager.setMorphWeights(manager.getInstance(target.entity), target.weights, 0)
        }
    }
}

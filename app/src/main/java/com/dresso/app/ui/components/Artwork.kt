package com.dresso.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.dresso.app.data.model.Brand
import com.dresso.app.data.model.Garment
import com.dresso.app.data.model.GarmentColor
import com.dresso.app.data.model.GarmentSilhouette
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.R
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class ArtworkView { FRONT, BACK, DETAIL }

fun Garment.viewForImageKey(imageKey: String): ArtworkView = when {
    imageKey.endsWith("-back") -> ArtworkView.BACK
    imageKey.endsWith("-detail") -> ArtworkView.DETAIL
    else -> ArtworkView.FRONT
}

/**
 * Product "photography" for the demo catalogue: a lit studio flat-lay rendered
 * with vectors. There is no remote image dependency and no branded imagery.
 */
@Composable
fun GarmentArtwork(
    garment: Garment,
    color: GarmentColor,
    imageKey: String,
    modifier: Modifier = Modifier,
) {
    val view = garment.viewForImageKey(imageKey)
    Canvas(modifier = modifier) {
        drawStudioBackground()
        when (view) {
            ArtworkView.DETAIL -> drawFabricDetail(color)
            else -> drawGarmentFlatLay(
                garment = garment,
                color = color,
                view = view,
                seed = (garment.id + color.id + view.name).hashCode(),
            )
        }
        drawGrain(seed = (garment.id + imageKey).hashCode(), alpha = 0.035f)
    }
}

@Composable
fun BrandCampaignArtwork(
    brand: Brand,
    modifier: Modifier = Modifier,
) {
    val image = when (brand.id) {
        "nike" -> R.drawable.nike_campaign
        "zara" -> R.drawable.zara_campaign
        else -> null
    }
    if (image != null) {
        Image(
            painter = painterResource(image),
            contentDescription = "${brand.name} editorial campaign",
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        return
    }
    Canvas(modifier = modifier) {
        val seed = brand.id.hashCode()
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF191918), Color(0xFF090909)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawDarkSilk(seed = seed, strength = 0.13f)
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.28f),
                0.45f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.78f),
            ),
        )
        drawGrain(seed = seed, alpha = 0.05f)
    }
}

@Composable
fun HeroArtwork(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.dresso_hero_editorial),
            contentDescription = "Dresso editorial fashion campaign",
            contentScale = ContentScale.Crop,
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        )
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.7f, size.height * 0.18f),
                radius = max(size.width, size.height) * 0.8f,
            ),
        )

        // A draped panel of light falling through the frame.
        val drape = Path().apply {
            moveTo(size.width * 0.18f, -size.height * 0.04f)
            cubicTo(
                size.width * 0.72f, size.height * 0.10f,
                size.width * 0.12f, size.height * 0.38f,
                size.width * 0.56f, size.height * 0.58f,
            )
            cubicTo(
                size.width * 0.82f, size.height * 0.70f,
                size.width * 0.34f, size.height * 0.86f,
                size.width * 0.48f, size.height * 1.04f,
            )
            lineTo(size.width * 1.06f, size.height * 1.04f)
            lineTo(size.width * 1.06f, -size.height * 0.04f)
            close()
        }
        drawPath(
            path = drape,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.06f),
                    Color.White.copy(alpha = 0.16f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Black.copy(alpha = 0.18f),
                ),
                start = Offset(size.width * 0.35f, 0f),
                end = Offset(size.width * 0.75f, size.height),
            ),
        )

        drawDarkSilk(seed = 41, strength = 0.2f, vertical = true)

        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.20f),
                0.38f to Color.Transparent,
                0.72f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.93f),
            ),
        )
        drawGrain(seed = 9, alpha = 0.055f)
        }
    }
}

private fun DrawScope.drawStudioBackground() {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(DressoColors.StudioLight, DressoColors.StudioMid),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.62f), Color.Transparent),
            center = Offset(size.width * 0.67f, size.height * 0.14f),
            radius = size.width * 1.05f,
        ),
    )
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.78f to Color.Transparent,
            1f to DressoColors.StudioShadow.copy(alpha = 0.22f),
        ),
    )
}

private fun DrawScope.drawGarmentFlatLay(
    garment: Garment,
    color: GarmentColor,
    view: ArtworkView,
    seed: Int,
) {
    val base = Color(color.argb)
    val scale = min(size.width * 0.9f / 100f, size.height * 0.9f / 125f)
    val dx = (size.width - 100f * scale) / 2f
    val dy = (size.height - 125f * scale) / 2f + size.height * 0.015f

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.20f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.8f),
            radius = size.width * 0.44f,
        ),
        topLeft = Offset(size.width * 0.16f, size.height * 0.70f),
        size = Size(size.width * 0.68f, size.height * 0.24f),
    )

    val geometry = garmentGeometry(garment.silhouette, view)

    withTransform({
        translate(dx, dy)
        scale(scale, scale, pivot = Offset.Zero)
    }) {
        geometry.behind.forEach { shape ->
            drawPath(shape, color = lerp(base, Color.Black, 0.18f))
        }

        drawPath(
            path = geometry.body,
            brush = Brush.linearGradient(
                colors = listOf(
                    lerp(base, Color.White, 0.16f),
                    base,
                    lerp(base, Color.Black, 0.20f),
                ),
                start = Offset(22f, 18f),
                end = Offset(82f, 122f),
            ),
        )

        clipPath(geometry.body) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(34f, 34f),
                    radius = 92f,
                ),
                topLeft = Offset(-8f, -8f),
                size = Size(118f, 142f),
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.10f)),
                    start = Offset(50f, 40f),
                    end = Offset(100f, 125f),
                ),
                topLeft = Offset(-8f, -8f),
                size = Size(118f, 142f),
            )
        }

        val seam = if (base.luminance() > 0.52f) {
            Color.Black.copy(alpha = 0.30f)
        } else {
            Color.White.copy(alpha = 0.25f)
        }
        val seamSoft = seam.copy(alpha = seam.alpha * 0.6f)
        val stroke = 0.42f

        geometry.details.forEach { detail ->
            drawPath(
                path = detail.path,
                color = if (detail.subtle) seamSoft else seam,
                style = Stroke(
                    width = stroke * detail.weight,
                    cap = StrokeCap.Round,
                    pathEffect = if (detail.dashed) {
                        PathEffect.dashPathEffect(floatArrayOf(2.6f, 2.2f))
                    } else {
                        null
                    },
                ),
            )
        }

        drawPath(
            path = geometry.body,
            color = if (base.luminance() > 0.52f) {
                Color.Black.copy(alpha = 0.22f)
            } else {
                Color.White.copy(alpha = 0.16f)
            },
            style = Stroke(width = 0.42f),
        )

        drawFabricLighting(seed)
    }
}

private fun DrawScope.drawFabricLighting(seed: Int) {
    val random = Random(seed)
    repeat(26) {
        val x = random.nextFloat() * 100f
        val y = random.nextFloat() * 125f
        drawLine(
            color = Color.White.copy(alpha = random.nextFloat() * 0.035f),
            start = Offset(x, y),
            end = Offset(x + random.nextFloat() * 10f, y + 19f),
            strokeWidth = 0.35f,
        )
    }
}

private fun DrawScope.drawFabricDetail(color: GarmentColor) {
    val base = Color(color.argb)
    val dark = lerp(base, Color.Black, 0.35f)
    val light = lerp(base, Color.White, 0.28f)

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(lerp(base, Color.White, 0.10f), base, dark),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        ),
    )

    repeat(9) { index ->
        val startY = size.height * (index / 9f) - size.height * 0.15f
        val path = Path().apply {
            moveTo(-size.width * 0.1f, startY)
            cubicTo(
                size.width * 0.32f, startY + size.height * 0.2f,
                size.width * 0.54f, startY - size.height * 0.12f,
                size.width * 1.1f, startY + size.height * 0.24f,
            )
        }
        drawPath(
            path = path,
            color = if (index % 2 == 0) {
                Color.White.copy(alpha = 0.075f)
            } else {
                Color.Black.copy(alpha = 0.08f)
            },
            style = Stroke(width = size.height * 0.075f, cap = StrokeCap.Round),
        )
        drawPath(
            path = path,
            color = if (index % 2 == 0) light.copy(alpha = 0.16f) else dark.copy(alpha = 0.16f),
            style = Stroke(width = size.width * 0.003f, cap = StrokeCap.Round),
        )
    }

    val seamColor = if (base.luminance() > 0.52f) {
        Color.Black.copy(alpha = 0.36f)
    } else {
        Color.White.copy(alpha = 0.34f)
    }
    val seam = Path().apply {
        moveTo(-size.width * 0.05f, size.height * 0.72f)
        cubicTo(
            size.width * 0.28f, size.height * 0.58f,
            size.width * 0.64f, size.height * 0.86f,
            size.width * 1.05f, size.height * 0.64f,
        )
    }
    drawPath(
        path = seam,
        color = seamColor.copy(alpha = 0.22f),
        style = Stroke(width = size.width * 0.018f),
    )
    drawPath(
        path = seam,
        color = seamColor,
        style = Stroke(
            width = size.width * 0.0025f,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(size.width * 0.009f, size.width * 0.007f),
            ),
        ),
    )

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.width * 0.8f,
        ),
    )
}

private fun DrawScope.drawDarkSilk(
    seed: Int,
    strength: Float,
    vertical: Boolean = false,
) {
    val random = Random(seed)
    repeat(12) { index ->
        val offset = index / 12f
        val path = Path().apply {
            if (vertical) {
                val startX = size.width * (0.08f + offset * 0.78f)
                moveTo(startX - size.width * 0.12f, -size.height * 0.08f)
                cubicTo(
                    startX + size.width * random.nextFloat() * 0.24f, size.height * 0.28f,
                    startX - size.width * random.nextFloat() * 0.30f, size.height * 0.62f,
                    startX + size.width * random.nextFloat() * 0.18f, size.height * 1.08f,
                )
            } else {
                val startY = size.height * (-0.08f + offset * 1.1f)
                moveTo(-size.width * 0.08f, startY)
                cubicTo(
                    size.width * 0.28f, startY + size.height * (random.nextFloat() - 0.5f) * 0.34f,
                    size.width * 0.64f, startY + size.height * (random.nextFloat() - 0.5f) * 0.34f,
                    size.width * 1.08f, startY + size.height * (random.nextFloat() - 0.5f) * 0.22f,
                )
            }
        }
        val alpha = strength * (0.24f + random.nextFloat() * 0.76f)
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.35f),
                    Color.Transparent,
                ),
                start = if (vertical) Offset(0f, 0f) else Offset(0f, size.height * offset),
                end = if (vertical) {
                    Offset(size.width, size.height)
                } else {
                    Offset(size.width, size.height * (offset + 0.24f))
                },
            ),
            style = Stroke(
                width = (size.minDimension * (0.018f + random.nextFloat() * 0.065f)),
                cap = StrokeCap.Round,
            ),
        )
    }
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.9f, size.height),
        ),
    )
}

private fun DrawScope.drawGrain(seed: Int, alpha: Float) {
    val random = Random(seed)
    repeat(520) {
        val start = Offset(
            x = random.nextFloat() * size.width,
            y = random.nextFloat() * size.height,
        )
        drawLine(
            color = if (random.nextBoolean()) {
                Color.White.copy(alpha = random.nextFloat() * alpha)
            } else {
                Color.Black.copy(alpha = random.nextFloat() * alpha)
            },
            start = start,
            end = Offset(
                x = start.x + random.nextFloat() * 1.4f,
                y = start.y + random.nextFloat() * 1.4f,
            ),
            strokeWidth = 0.65f,
        )
    }
}

private data class DetailLine(
    val path: Path,
    val subtle: Boolean = false,
    val dashed: Boolean = false,
    val weight: Float = 1f,
)

private data class GarmentGeometry(
    val body: Path,
    val details: List<DetailLine> = emptyList(),
    val behind: List<Path> = emptyList(),
)

private fun garmentGeometry(
    silhouette: GarmentSilhouette,
    view: ArtworkView,
): GarmentGeometry {
    val body = when (silhouette) {
        GarmentSilhouette.TEE,
        GarmentSilhouette.HOODIE,
        -> teeBody()

        GarmentSilhouette.JACKET,
        GarmentSilhouette.DENIM_JACKET,
        GarmentSilhouette.SHIRT,
        GarmentSilhouette.OVERSHIRT,
        GarmentSilhouette.DENIM_SHIRT,
        -> longSleeveBody()

        GarmentSilhouette.COAT -> coatBody()
        GarmentSilhouette.DRESS,
        GarmentSilhouette.KNIT_DRESS,
        -> dressBody(knit = silhouette == GarmentSilhouette.KNIT_DRESS)

        GarmentSilhouette.TROUSERS,
        GarmentSilhouette.UTILITY_PANTS,
        GarmentSilhouette.JEANS,
        -> trousersBody()

        GarmentSilhouette.SHORTS,
        GarmentSilhouette.DENIM_SHORT,
        -> shortsBody()
    }

    val behind = if (silhouette == GarmentSilhouette.HOODIE) {
        listOf(
            Path().apply {
                moveTo(33f, 25f)
                cubicTo(32f, 2f, 68f, 2f, 67f, 25f)
                cubicTo(61f, 14f, 39f, 14f, 33f, 25f)
                close()
            },
        )
    } else {
        emptyList()
    }

    val details = if (view == ArtworkView.BACK) {
        backDetails(silhouette)
    } else {
        frontDetails(silhouette)
    }

    return GarmentGeometry(body = body, details = details, behind = behind)
}

private fun teeBody(): Path = Path().apply {
    moveTo(37f, 20f)
    cubicTo(41f, 16f, 59f, 16f, 63f, 20f)
    lineTo(70f, 22f)
    cubicTo(78f, 24f, 85f, 28f, 88f, 34f)
    lineTo(95f, 49f)
    lineTo(82f, 58f)
    lineTo(75f, 49f)
    lineTo(76f, 103f)
    cubicTo(67f, 107f, 33f, 107f, 24f, 103f)
    lineTo(25f, 49f)
    lineTo(18f, 58f)
    lineTo(5f, 49f)
    lineTo(12f, 34f)
    cubicTo(15f, 28f, 22f, 24f, 30f, 22f)
    close()
}

private fun longSleeveBody(): Path = Path().apply {
    moveTo(37f, 20f)
    cubicTo(41f, 16f, 59f, 16f, 63f, 20f)
    lineTo(70f, 22f)
    cubicTo(78f, 23f, 84f, 27f, 86f, 33f)
    lineTo(93f, 90f)
    lineTo(84f, 99f)
    lineTo(76f, 50f)
    lineTo(76f, 103f)
    cubicTo(67f, 107f, 33f, 107f, 24f, 103f)
    lineTo(24f, 50f)
    lineTo(16f, 99f)
    lineTo(7f, 90f)
    lineTo(14f, 33f)
    cubicTo(16f, 27f, 22f, 23f, 30f, 22f)
    close()
}

private fun coatBody(): Path = Path().apply {
    moveTo(37f, 20f)
    cubicTo(41f, 16f, 59f, 16f, 63f, 20f)
    lineTo(71f, 22f)
    cubicTo(79f, 23f, 85f, 27f, 87f, 33f)
    lineTo(94f, 101f)
    lineTo(85f, 110f)
    lineTo(78f, 50f)
    lineTo(80f, 120f)
    cubicTo(68f, 124f, 32f, 124f, 20f, 120f)
    lineTo(22f, 50f)
    lineTo(15f, 110f)
    lineTo(6f, 101f)
    lineTo(13f, 33f)
    cubicTo(15f, 27f, 22f, 23f, 29f, 22f)
    close()
}

private fun dressBody(knit: Boolean): Path = if (knit) {
    Path().apply {
        moveTo(37f, 20f)
        cubicTo(41f, 16f, 59f, 16f, 63f, 20f)
        lineTo(70f, 22f)
        cubicTo(78f, 23f, 84f, 27f, 86f, 33f)
        lineTo(91f, 88f)
        lineTo(83f, 96f)
        lineTo(76f, 49f)
        lineTo(76f, 60f)
        cubicTo(72f, 61f, 70f, 62f, 68f, 63f)
        lineTo(69f, 116f)
        cubicTo(58f, 119f, 42f, 119f, 31f, 116f)
        lineTo(32f, 63f)
        cubicTo(30f, 62f, 28f, 61f, 24f, 60f)
        lineTo(24f, 49f)
        lineTo(17f, 96f)
        lineTo(9f, 88f)
        lineTo(14f, 33f)
        cubicTo(16f, 27f, 22f, 23f, 30f, 22f)
        close()
    }
} else {
    Path().apply {
        moveTo(34f, 25f)
        lineTo(38f, 22f)
        cubicTo(45f, 27f, 55f, 27f, 62f, 22f)
        lineTo(66f, 25f)
        lineTo(69f, 57f)
        lineTo(64f, 62f)
        lineTo(67f, 118f)
        cubicTo(56f, 121f, 44f, 121f, 33f, 118f)
        lineTo(36f, 62f)
        lineTo(31f, 57f)
        close()
    }
}

private fun trousersBody(): Path = Path().apply {
    moveTo(22f, 18f)
    lineTo(78f, 18f)
    lineTo(82f, 57f)
    lineTo(68f, 122f)
    lineTo(54f, 122f)
    lineTo(50f, 60f)
    lineTo(46f, 122f)
    lineTo(32f, 122f)
    lineTo(18f, 57f)
    close()
}

private fun shortsBody(): Path = Path().apply {
    moveTo(22f, 18f)
    lineTo(78f, 18f)
    lineTo(84f, 52f)
    lineTo(76f, 69f)
    lineTo(55f, 69f)
    lineTo(50f, 52f)
    lineTo(45f, 69f)
    lineTo(24f, 69f)
    lineTo(16f, 52f)
    close()
}

private fun frontDetails(silhouette: GarmentSilhouette): List<DetailLine> {
    val details = mutableListOf<DetailLine>()
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, subtle: Boolean = false, dashed: Boolean = false) {
        details += DetailLine(
            path = Path().apply {
                moveTo(x1, y1)
                lineTo(x2, y2)
            },
            subtle = subtle,
            dashed = dashed,
        )
    }

    when (silhouette) {
        GarmentSilhouette.JACKET -> {
            line(50f, 24f, 50f, 102f, dashed = true)
            line(40f, 22f, 34f, 33f)
            line(60f, 22f, 66f, 33f)
            line(29f, 77f, 44f, 81f, subtle = true)
            line(71f, 77f, 56f, 81f, subtle = true)
            line(24f, 99f, 33f, 100f, subtle = true)
            line(76f, 99f, 67f, 100f, subtle = true)
        }

        GarmentSilhouette.DENIM_JACKET -> {
            line(50f, 24f, 50f, 102f, dashed = true)
            line(40f, 22f, 34f, 33f)
            line(60f, 22f, 66f, 33f)
            line(31f, 45f, 45f, 45f)
            line(31f, 45f, 31f, 55f)
            line(45f, 45f, 45f, 55f)
            line(55f, 45f, 69f, 45f)
            line(55f, 45f, 55f, 55f)
            line(69f, 45f, 69f, 55f)
            line(24f, 99f, 34f, 100f, subtle = true)
            line(76f, 99f, 66f, 100f, subtle = true)
        }

        GarmentSilhouette.SHIRT,
        GarmentSilhouette.DENIM_SHIRT,
        GarmentSilhouette.OVERSHIRT,
        -> {
            line(50f, 26f, 50f, 102f)
            line(38f, 21f, 44f, 32f)
            line(44f, 32f, 50f, 26f)
            line(62f, 21f, 56f, 32f)
            line(56f, 32f, 50f, 26f)
            repeat(5) { index ->
                val y = 42f + index * 13f
                line(49f, y, 51f, y, subtle = true)
            }
            if (silhouette == GarmentSilhouette.OVERSHIRT) {
                line(31f, 46f, 45f, 46f)
                line(55f, 46f, 69f, 46f)
                line(31f, 46f, 31f, 58f)
                line(45f, 46f, 45f, 58f)
                line(55f, 46f, 55f, 58f)
                line(69f, 46f, 69f, 58f)
            }
            if (silhouette == GarmentSilhouette.DENIM_SHIRT) {
                line(25f, 38f, 42f, 42f, subtle = true)
                line(58f, 42f, 75f, 38f, subtle = true)
            }
        }

        GarmentSilhouette.COAT -> {
            line(42f, 23f, 50f, 54f)
            line(58f, 23f, 50f, 54f)
            line(50f, 54f, 50f, 118f)
            line(29f, 76f, 44f, 78f, subtle = true)
            line(71f, 76f, 56f, 78f, subtle = true)
            repeat(3) { index ->
                val y = 50f + index * 18f
                line(48f, y, 52f, y, subtle = true)
            }
        }

        GarmentSilhouette.HOODIE -> {
            line(33f, 30f, 39f, 42f, subtle = true)
            line(67f, 30f, 61f, 42f, subtle = true)
            line(33f, 78f, 67f, 78f, subtle = true)
            line(33f, 78f, 31f, 91f, subtle = true)
            line(67f, 78f, 69f, 91f, subtle = true)
            line(40f, 22f, 60f, 22f, subtle = true)
        }

        GarmentSilhouette.TEE -> {
            line(24f, 99f, 76f, 99f, subtle = true)
            line(37f, 20f, 42f, 27f, subtle = true)
            line(63f, 20f, 58f, 27f, subtle = true)
        }

        GarmentSilhouette.DRESS -> {
            line(43f, 22f, 40f, 12f, subtle = true)
            line(57f, 22f, 60f, 12f, subtle = true)
            line(36f, 61f, 64f, 61f, subtle = true)
            line(42f, 67f, 39f, 112f, subtle = true)
            line(50f, 64f, 50f, 116f, subtle = true)
            line(58f, 67f, 61f, 112f, subtle = true)
        }

        GarmentSilhouette.KNIT_DRESS -> {
            line(35f, 62f, 65f, 62f, subtle = true)
            repeat(6) { index ->
                val x = 37f + index * 5.2f
                line(x, 66f, x - 1.4f, 113f, subtle = true)
            }
            line(24f, 95f, 33f, 96f, subtle = true)
            line(76f, 95f, 67f, 96f, subtle = true)
        }

        GarmentSilhouette.TROUSERS,
        GarmentSilhouette.UTILITY_PANTS,
        GarmentSilhouette.JEANS,
        -> {
            line(22f, 27f, 78f, 27f)
            line(50f, 27f, 50f, 58f, subtle = true)
            line(36f, 27f, 38f, 120f, subtle = true)
            line(64f, 27f, 62f, 120f, subtle = true)
            if (silhouette == GarmentSilhouette.UTILITY_PANTS) {
                line(25f, 48f, 39f, 48f)
                line(25f, 48f, 25f, 62f)
                line(39f, 48f, 39f, 62f)
                line(61f, 48f, 75f, 48f)
                line(61f, 48f, 61f, 62f)
                line(75f, 48f, 75f, 62f)
            }
            if (silhouette == GarmentSilhouette.JEANS) {
                line(30f, 20f, 30f, 30f, subtle = true)
                line(44f, 20f, 44f, 30f, subtle = true)
                line(56f, 20f, 56f, 30f, subtle = true)
                line(70f, 20f, 70f, 30f, subtle = true)
                line(24f, 31f, 38f, 42f, subtle = true)
                line(76f, 31f, 62f, 42f, subtle = true)
                line(35f, 30f, 35f, 42f, subtle = true)
                line(65f, 30f, 65f, 42f, subtle = true)
            }
        }

        GarmentSilhouette.SHORTS,
        GarmentSilhouette.DENIM_SHORT,
        -> {
            line(22f, 27f, 78f, 27f)
            line(50f, 27f, 50f, 54f, subtle = true)
            line(28f, 31f, 40f, 41f, subtle = true)
            line(72f, 31f, 60f, 41f, subtle = true)
            if (silhouette == GarmentSilhouette.DENIM_SHORT) {
                line(24f, 66f, 45f, 66f, dashed = true)
                line(55f, 66f, 76f, 66f, dashed = true)
            }
        }
    }
    return details
}

private fun backDetails(silhouette: GarmentSilhouette): List<DetailLine> {
    val details = mutableListOf<DetailLine>()
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, subtle: Boolean = false, dashed: Boolean = false) {
        details += DetailLine(
            path = Path().apply {
                moveTo(x1, y1)
                lineTo(x2, y2)
            },
            subtle = subtle,
            dashed = dashed,
        )
    }

    when (silhouette) {
        GarmentSilhouette.DRESS -> {
            line(50f, 26f, 50f, 116f, subtle = true)
            line(43f, 22f, 40f, 12f, subtle = true)
            line(57f, 22f, 60f, 12f, subtle = true)
            line(36f, 61f, 64f, 61f, subtle = true)
        }

        GarmentSilhouette.KNIT_DRESS -> {
            line(50f, 26f, 50f, 113f, subtle = true)
            line(35f, 62f, 65f, 62f, subtle = true)
            line(24f, 95f, 33f, 96f, subtle = true)
            line(76f, 95f, 67f, 96f, subtle = true)
        }

        GarmentSilhouette.COAT,
        GarmentSilhouette.JACKET,
        GarmentSilhouette.DENIM_JACKET,
        GarmentSilhouette.SHIRT,
        GarmentSilhouette.DENIM_SHIRT,
        GarmentSilhouette.OVERSHIRT,
        -> {
            line(50f, 26f, 50f, if (silhouette == GarmentSilhouette.COAT) 118f else 101f, subtle = true)
            line(24f, 38f, 44f, 44f, subtle = true)
            line(76f, 38f, 56f, 44f, subtle = true)
            if (silhouette == GarmentSilhouette.DENIM_JACKET) {
                line(34f, 44f, 34f, 56f, subtle = true)
                line(66f, 44f, 66f, 56f, subtle = true)
                line(34f, 54f, 66f, 54f, subtle = true)
            }
        }

        GarmentSilhouette.HOODIE -> {
            line(50f, 34f, 50f, 94f, subtle = true)
            line(33f, 31f, 39f, 43f, subtle = true)
            line(67f, 31f, 61f, 43f, subtle = true)
        }

        GarmentSilhouette.TEE -> {
            line(50f, 28f, 50f, 100f, subtle = true)
            line(24f, 99f, 76f, 99f, subtle = true)
        }

        GarmentSilhouette.TROUSERS,
        GarmentSilhouette.UTILITY_PANTS,
        GarmentSilhouette.JEANS,
        -> {
            line(50f, 30f, 50f, 56f, subtle = true)
            line(32f, 34f, 42f, 42f, subtle = true)
            line(68f, 34f, 58f, 42f, subtle = true)
            line(22f, 27f, 78f, 27f, subtle = true)
        }

        GarmentSilhouette.SHORTS,
        GarmentSilhouette.DENIM_SHORT,
        -> {
            line(50f, 30f, 50f, 54f, subtle = true)
            line(22f, 27f, 78f, 27f, subtle = true)
            line(30f, 35f, 42f, 44f, subtle = true)
            line(70f, 35f, 58f, 44f, subtle = true)
        }
    }
    return details
}

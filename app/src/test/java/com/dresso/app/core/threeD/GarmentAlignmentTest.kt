package com.dresso.app.core.threeD

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GarmentAlignmentTest {
    @Test
    fun bodyBoundsInSceneCentersOnOrigin() {
        val body = NeutralBounds(0f, 0f, -0.2f, 0.4f, 1.8f, 0.2f)
        val scene = bodyBoundsInScene(body)
        assertEquals(0f, scene.centerX, 0.0001f)
        assertEquals(0f, scene.centerY, 0.0001f)
        assertEquals(0f, scene.centerZ, 0.0001f)
        assertEquals(0.4f, scene.width, 0.0001f)
        assertEquals(1.8f, scene.height, 0.0001f)
        assertEquals(0.4f, scene.depth, 0.0001f)
    }

    @Test
    fun alignGarmentScalesToEnvelopeCenteredBody() {
        val body = NeutralBounds(0f, 0f, -0.2f, 0.4f, 1.8f, 0.2f)
        val garment = NeutralBounds(-50f, 0f, -50f, 50f, 200f, 50f)
        val profile = GarmentAlignProfile(
            yawDegrees = 0f,
            envelopeMargin = 1.0f,
            verticalAnchor = GarmentVerticalAnchor.CENTER,
        )
        val alignment = alignGarmentToAvatar(body, garment, profile)
        assertEquals(0f, alignment.position.x, 0.0001f)
        assertEquals(-0.79f, alignment.position.y, 0.0001f)
        assertEquals(0f, alignment.position.z, 0.0001f)
        assertEquals(0.0079f, alignment.scale.x, 0.0001f)
    }

    @Test
    fun headlessOutfitAnchorsFeetAndCollarHeight() {
        val body = NeutralBounds(
            -0.496f, 0.001f, -0.102f,
            0.496f, 1.667f, 0.321f,
        )
        val garment = NeutralBounds(-0.336f, 0f, -0.118f, 0.336f, 1f, 0.118f)
        val profile = requireNotNull(garmentAlignProfile("garments/nike_garment_02.glb"))
        val alignment = alignGarmentToAvatar(body, garment, profile)
        val sceneBody = bodyBoundsInScene(body)
        val scale = alignment.scale.x
        val garmentBottomY = alignment.position.y + scale * garment.minY
        val targetHeight = sceneBody.height - HEAD_ABOVE_COLLAR_TEST
        val expectedScale = targetHeight / garment.height * profile.envelopeMargin
        assertEquals(sceneBody.minY, garmentBottomY, 0.02f)
        assertEquals(expectedScale, scale, 0.001f)
    }

    @Test
    fun yawRotationAdjustsTranslationForOffCenterGarment() {
        val body = NeutralBounds(0f, 0f, -0.2f, 0.4f, 1.8f, 0.2f)
        val garment = NeutralBounds(10f, 0f, 0f, 30f, 100f, 20f)
        val aligned = alignGarmentToAvatar(
            body,
            garment,
            GarmentAlignProfile(
                yawDegrees = -90f,
                envelopeMargin = 1.0f,
                verticalAnchor = GarmentVerticalAnchor.CENTER,
            ),
        )
        assertTrue(aligned.position.x != 0f || aligned.position.z != 0f)
        assertEquals(-90f, aligned.rotation.y, 0f)
    }

    private companion object {
        const val HEAD_ABOVE_COLLAR_TEST = 0.22f
    }
}

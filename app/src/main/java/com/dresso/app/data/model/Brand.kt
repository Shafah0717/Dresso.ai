package com.dresso.app.data.model

/**
 * A brand entry in the Dresso directory.
 *
 * [imageKey] is an art-direction key, not a bundled asset path, so the visual
 * source can later be swapped for a remote catalogue without touching the UI.
 */
data class Brand(
    val id: String,
    val name: String,
    val monogram: String,
    val tagline: String,
    val description: String,
    val imageKey: String,
)

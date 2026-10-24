package com.dresso.app.data.model

enum class GarmentCategory(val label: String) {
    OUTERWEAR("Outerwear"),
    TOPS("Tops"),
    DRESSES("Dresses"),
    BOTTOMS("Bottoms"),
    SHORTS("Shorts"),
    OUTFITS("Outfits"),
}

/** Used by the placeholder artwork. It is presentation metadata, not a 3D asset id. */
enum class GarmentSilhouette {
    JACKET,
    TEE,
    HOODIE,
    SHORTS,
    COAT,
    DRESS,
    SHIRT,
    TROUSERS,
    OVERSHIRT,
    KNIT_DRESS,
    UTILITY_PANTS,
    DENIM_JACKET,
    JEANS,
    DENIM_SHIRT,
    DENIM_SHORT,
}

data class GarmentColor(
    val id: String,
    val name: String,
    val argb: Long,
)

data class GarmentSize(
    val id: String,
    val label: String,
)

data class GarmentVariant(
    val id: String,
    val colorId: String,
    val sizeId: String,
)

/**
 * A single catalogue piece. Price is stored in whole euros for this demo;
 * a production catalogue should move to minor units and a currency type.
 */
data class Garment(
    val id: String,
    val brandId: String,
    val name: String,
    val priceEur: Int,
    val description: String,
    val category: GarmentCategory,
    val silhouette: GarmentSilhouette,
    val imageKeys: List<String>,
    val colors: List<GarmentColor>,
    val sizes: List<GarmentSize>,
    val variants: List<GarmentVariant>,
    /** Optional original GLB; an asset path does not imply this mesh is fitted or skinned. */
    val modelAssetPath: String? = null,
) {
    fun color(colorId: String?): GarmentColor? = colors.firstOrNull { it.id == colorId }

    fun size(sizeId: String?): GarmentSize? = sizes.firstOrNull { it.id == sizeId }

    fun variant(colorId: String?, sizeId: String?): GarmentVariant? =
        variants.firstOrNull { it.colorId == colorId && it.sizeId == sizeId }
}

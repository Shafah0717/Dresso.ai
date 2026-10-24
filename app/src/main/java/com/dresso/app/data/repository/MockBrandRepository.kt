package com.dresso.app.data.repository

import com.dresso.app.data.model.Brand
import com.dresso.app.data.model.Garment
import com.dresso.app.data.model.GarmentCategory
import com.dresso.app.data.model.GarmentColor
import com.dresso.app.data.model.GarmentSilhouette
import com.dresso.app.data.model.GarmentSize
import com.dresso.app.data.model.GarmentVariant

/**
 * Replaceable in-memory catalogue. The interface is suspend-based so a future
 * backend can implement the same contract without changing screens or view models.
 */
class MockBrandRepository : BrandRepository {

    private val brands = listOf(
        Brand(
            id = "nike",
            name = "Nike",
            monogram = "N",
            tagline = "Move, refined",
            description = "A study of modern movement. Clean layers, technical knits and considered essentials.",
            imageKey = "brand-nike",
        ),
        Brand(
            id = "zara",
            name = "Zara",
            monogram = "Z",
            tagline = "The new tailoring",
            description = "Sculptural coats, quiet shirting and pieces built around line and proportion.",
            imageKey = "brand-zara",
        ),
        Brand(
            id = "hm",
            name = "H&M",
            monogram = "H",
            tagline = "Everyday form",
            description = "Soft natural fibres and relaxed silhouettes, chosen for a slower wardrobe.",
            imageKey = "brand-hm",
        ),
        Brand(
            id = "levis",
            name = "Levi's",
            monogram = "L",
            tagline = "The denim edit",
            description = "A demo denim story. Authentic washes, straight lines and workwear ease.",
            imageKey = "brand-levis",
        ),
    )

    private val garments = listOf(
        garment(
            id = "nike-studio-collar-shirt-demo",
            brandId = "nike",
            name = "Studio Collar Shirt · Demo",
            priceEur = 69,
            description = "A detailed collared shirt source mesh with cuffs and placket. " +
                "Unrigged demo asset; its 3D sample is not fitted to the avatar.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.SHIRT,
            colors = listOf(GarmentColor("original", "Original blue", 0xFFC9DCE8)),
            sizes = emptyList(),
            modelAssetPath = "garments/nike_garment_01.glb",
        ),
        garment(
            id = "nike-layered-outfit-demo",
            brandId = "nike",
            name = "Layered Jacket & Trouser Set · Demo",
            priceEur = 139,
            description = "A jacket, trousers and shoes in one textured source mesh. " +
                "Unrigged demo asset; its 3D sample is not fitted to the avatar.",
            category = GarmentCategory.OUTFITS,
            silhouette = GarmentSilhouette.JACKET,
            colors = listOf(GarmentColor("original", "Original charcoal", 0xFF303030)),
            sizes = emptyList(),
            modelAssetPath = "garments/nike_garment_02.glb",
        ),
        garment(
            id = "zara-atelier-coat",
            brandId = "zara",
            name = "Zara Motion Tee · Rigged",
            priceEur = 189,
            description = "A fitted Zara tee exported from the avatar rig. The garment follows the avatar's movement in the fitting room.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.TEE,
            colors = listOf(CHARCOAL, BONE, ONYX),
            modelAssetPath = "garments/zara_garmet.glb",
        ),
        garment(
            id = "zara-grament-2",
            brandId = "zara",
            name = "Zara Studio Garment 02",
            priceEur = 79,
            description = "A second Zara garment preview from the supplied 3D asset.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.SHIRT,
            colors = listOf(CHARCOAL, BONE, ONYX),
            modelAssetPath = "garments/zara_grament2.glb",
        ),
        garment(
            id = "hm-linen-overshirt",
            brandId = "hm",
            name = "Linen Overshirt",
            priceEur = 59,
            description = "Washed linen with an easy, boxy line. Wear it open over a tee, or closed as a shirt.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.OVERSHIRT,
            colors = listOf(ECRU, STONE, ONYX),
            modelAssetPath = "garments/nike_garment_01.glb",
        ),
        garment(
            id = "hm-ribbed-knit-dress",
            brandId = "hm",
            name = "Ribbed Knit Dress",
            priceEur = 49,
            description = "A fine rib knit that follows the body without clinging. Long sleeve, midi length.",
            category = GarmentCategory.DRESSES,
            silhouette = GarmentSilhouette.KNIT_DRESS,
            colors = listOf(ONYX, ASH),
            sizes = WOMEN_SIZES,
            modelAssetPath = "garments/nike_garment_02.glb",
        ),
        garment(
            id = "hm-utility-pant",
            brandId = "hm",
            name = "Cotton Utility Pant",
            priceEur = 39,
            description = "A relaxed cotton twill pant with a straight leg and discreet utility pockets.",
            category = GarmentCategory.BOTTOMS,
            silhouette = GarmentSilhouette.UTILITY_PANTS,
            colors = listOf(STONE, ONYX, ECRU),
        ),
        garment(
            id = "hm-everyday-tee",
            brandId = "hm",
            name = "Everyday Cotton Tee",
            priceEur = 19,
            description = "A heavyweight organic cotton tee, garment-dyed for a soft, lived-in tone.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.TEE,
            colors = listOf(BONE, ONYX, ASH),
        ),
        garment(
            id = "levis-vintage-denim-jacket",
            brandId = "levis",
            name = "Vintage Denim Jacket",
            priceEur = 110,
            description = "A boxy trucker jacket in a rigid, old-wash denim. Pointed collar and clean metal shanks.",
            category = GarmentCategory.OUTERWEAR,
            silhouette = GarmentSilhouette.DENIM_JACKET,
            colors = listOf(INDIGO, WASH, ONYX),
        ),
        garment(
            id = "levis-straight-jean",
            brandId = "levis",
            name = "Straight Leg Jean",
            priceEur = 98,
            description = "A true straight jean with a mid rise and a full, untapered leg. Cut from honest denim.",
            category = GarmentCategory.BOTTOMS,
            silhouette = GarmentSilhouette.JEANS,
            colors = listOf(INDIGO, WASH, ONYX),
            sizes = DENIM_SIZES,
        ),
        garment(
            id = "levis-western-shirt",
            brandId = "levis",
            name = "Western Denim Shirt",
            priceEur = 75,
            description = "A soft denim shirt with a pointed yoke and pearl-free snap closure. Worn-in from the first wear.",
            category = GarmentCategory.TOPS,
            silhouette = GarmentSilhouette.DENIM_SHIRT,
            colors = listOf(INDIGO, ECRU),
        ),
        garment(
            id = "levis-relaxed-short",
            brandId = "levis",
            name = "Relaxed Denim Short",
            priceEur = 55,
            description = "A longer, relaxed denim short with a raw cut hem and a clean five-pocket line.",
            category = GarmentCategory.SHORTS,
            silhouette = GarmentSilhouette.DENIM_SHORT,
            colors = listOf(WASH, INDIGO),
            sizes = DENIM_SIZES,
        ),
    )

    override suspend fun getBrands(): List<Brand> = brands

    override suspend fun getBrand(brandId: String): Brand? = brands.firstOrNull { it.id == brandId }

    override suspend fun getGarments(brandId: String): List<Garment> =
        garments.filter { it.brandId == brandId }

    override suspend fun getGarment(garmentId: String): Garment? =
        garments.firstOrNull { it.id == garmentId }

    private fun garment(
        id: String,
        brandId: String,
        name: String,
        priceEur: Int,
        description: String,
        category: GarmentCategory,
        silhouette: GarmentSilhouette,
        colors: List<GarmentColor>,
        sizes: List<GarmentSize> = SIZES,
        modelAssetPath: String? = null,
    ): Garment = Garment(
        id = id,
        brandId = brandId,
        name = name,
        priceEur = priceEur,
        description = description,
        category = category,
        silhouette = silhouette,
        imageKeys = listOf("$id-front", "$id-back", "$id-detail"),
        colors = colors,
        sizes = sizes,
        variants = colors.flatMap { color ->
            sizes.map { size ->
                GarmentVariant(
                    id = "$id-${color.id}-${size.id}",
                    colorId = color.id,
                    sizeId = size.id,
                )
            }
        },
        modelAssetPath = modelAssetPath,
    )

    private companion object {
        val SIZES = listOf(
            GarmentSize("xs", "XS"),
            GarmentSize("s", "S"),
            GarmentSize("m", "M"),
            GarmentSize("l", "L"),
            GarmentSize("xl", "XL"),
        )

        val WOMEN_SIZES = listOf(
            GarmentSize("xs", "XS"),
            GarmentSize("s", "S"),
            GarmentSize("m", "M"),
            GarmentSize("l", "L"),
        )

        val DENIM_SIZES = listOf(
            GarmentSize("28", "28"),
            GarmentSize("30", "30"),
            GarmentSize("32", "32"),
            GarmentSize("34", "34"),
            GarmentSize("36", "36"),
        )

        val ONYX = GarmentColor("onyx", "Onyx", 0xFF141414)
        val CHARCOAL = GarmentColor("charcoal", "Charcoal", 0xFF343432)
        val ASH = GarmentColor("ash", "Ash", 0xFF62615D)
        val STONE = GarmentColor("stone", "Stone", 0xFF9B968C)
        val BONE = GarmentColor("bone", "Bone", 0xFFE6E0D5)
        val ECRU = GarmentColor("ecru", "Ecru", 0xFFD6CCBA)
        val PAPER = GarmentColor("paper", "Paper", 0xFFF1EEE7)
        val INDIGO = GarmentColor("indigo", "Indigo", 0xFF334358)
        val WASH = GarmentColor("wash", "Washed indigo", 0xFF7B8797)
    }
}

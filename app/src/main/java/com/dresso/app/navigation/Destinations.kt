package com.dresso.app.navigation

enum class TopLevelDestination(
    val route: String,
    val label: String,
) {
    Discover(route = "discover", label = "Discover"),
    TryOn(route = "tryon", label = "Try-On"),
    Profile(route = "profile", label = "Profile"),
}

object DressoRoutes {
    const val Brand = "brand/{brandId}"
    const val Product = "product/{productId}"
    const val Measurements = "measurements"

    fun brand(brandId: String): String = "brand/$brandId"
    fun product(productId: String): String = "product/$productId"

    const val ARG_BRAND_ID = "brandId"
    const val ARG_PRODUCT_ID = "productId"
}

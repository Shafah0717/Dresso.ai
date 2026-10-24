package com.dresso.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dresso.app.navigation.DressoRoutes
import com.dresso.app.navigation.TopLevelDestination
import com.dresso.app.ui.components.DressoBottomBar
import com.dresso.app.ui.screens.brand.BrandCollectionScreen
import com.dresso.app.ui.screens.discover.DiscoverScreen
import com.dresso.app.ui.screens.measurements.MeasurementsScreen
import com.dresso.app.ui.screens.product.ProductDetailScreen
import com.dresso.app.ui.screens.profile.ProfileScreen
import com.dresso.app.ui.screens.tryon.TryOnScreen
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.SystemBarAppearance
import com.dresso.app.viewmodel.DressoViewModel

@Composable
fun DressoApp(
    viewModel: DressoViewModel,
    container: AppContainer,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelDestination = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }

    val brands by viewModel.brands.collectAsStateWithLifecycle()
    val brandPage by viewModel.brandPage.collectAsStateWithLifecycle()
    val garmentPage by viewModel.garmentPage.collectAsStateWithLifecycle()
    val selectedLook by viewModel.selectedLook.collectAsStateWithLifecycle()
    val bodyProfile by viewModel.bodyProfile.collectAsStateWithLifecycle()
    val avatarMeasurements by viewModel.avatarMeasurements.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val fitRecommendation by viewModel.fitRecommendation.collectAsStateWithLifecycle()

    SystemBarAppearance(useDarkIcons = currentRoute == DressoRoutes.Product)

    fun navigateToTopLevel(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DressoColors.Ink,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (topLevelDestination != null) {
                DressoBottomBar(
                    selected = topLevelDestination,
                    onSelect = { navigateToTopLevel(it) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Discover.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(TopLevelDestination.Discover.route) {
                DiscoverScreen(
                    brands = brands,
                    onBrandClick = { brandId ->
                        navController.navigate(DressoRoutes.brand(brandId))
                    },
                    onProfileClick = {
                        navigateToTopLevel(TopLevelDestination.Profile)
                    },
                )
            }

            composable(
                route = DressoRoutes.Brand,
                arguments = listOf(
                    navArgument(DressoRoutes.ARG_BRAND_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                val brandId = entry.arguments?.getString(DressoRoutes.ARG_BRAND_ID).orEmpty()
                LaunchedEffect(brandId) {
                    viewModel.loadBrand(brandId)
                }
                BrandCollectionScreen(
                    brandId = brandId,
                    state = brandPage,
                    onBack = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate(DressoRoutes.product(productId))
                    },
                )
            }

            composable(
                route = DressoRoutes.Product,
                arguments = listOf(
                    navArgument(DressoRoutes.ARG_PRODUCT_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                val productId = entry.arguments?.getString(DressoRoutes.ARG_PRODUCT_ID).orEmpty()
                LaunchedEffect(productId) {
                    viewModel.openGarment(productId)
                }
                ProductDetailScreen(
                    productId = productId,
                    page = garmentPage,
                    selectedLook = selectedLook,
                    onBack = { navController.popBackStack() },
                    onSelectColor = viewModel::selectColor,
                    onSelectSize = viewModel::selectSize,
                    onTryOn = {
                        navController.navigate(TopLevelDestination.TryOn.route)
                    },
                )
            }

            composable(TopLevelDestination.TryOn.route) {
                TryOnScreen(
                    look = selectedLook,
                    bodyProfile = bodyProfile,
                    avatarMeasurements = avatarMeasurements,
                    fitRecommendation = fitRecommendation,
                    onBodyControlChange = viewModel::updateAvatarControl,
                    onBodyControlFinished = viewModel::saveAvatarMeasurements,
                    onSelectColor = viewModel::selectColor,
                    onSelectSize = viewModel::selectSize,
                    onViewProduct = {
                        val look = viewModel.selectedLook.value
                        if (look != null) {
                            val previousRoute = navController.previousBackStackEntry
                                ?.destination
                                ?.route
                            if (previousRoute == DressoRoutes.Product) {
                                navController.popBackStack()
                            } else {
                                viewModel.openGarment(look.garment.id)
                                navController.navigate(DressoRoutes.product(look.garment.id))
                            }
                        }
                    },
                    onEditMeasurements = {
                        viewModel.clearAvatarDraft()
                        navController.navigate(DressoRoutes.Measurements)
                    },
                    onDiscover = {
                        navigateToTopLevel(TopLevelDestination.Discover)
                    },
                )
            }

            composable(TopLevelDestination.Profile.route) {
                ProfileScreen(
                    bodyProfile = bodyProfile,
                    preferences = preferences,
                    onMeasurements = {
                        navController.navigate(DressoRoutes.Measurements)
                    },
                    onUnitSystemChange = viewModel::setUnitSystem,
                )
            }

            composable(DressoRoutes.Measurements) {
                MeasurementsScreen(
                    container = container,
                    onBack = {
                        viewModel.clearAvatarDraft()
                        navController.popBackStack()
                    },
                    onSaved = {
                        viewModel.clearAvatarDraft()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

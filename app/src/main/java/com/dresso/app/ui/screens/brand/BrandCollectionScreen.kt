package com.dresso.app.ui.screens.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.data.model.Brand
import com.dresso.app.data.model.Garment
import com.dresso.app.data.model.GarmentCategory
import com.dresso.app.ui.components.DressoBackButton
import com.dresso.app.ui.components.DressoWordmark
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.GarmentArtwork
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.SectionLabel
import com.dresso.app.ui.components.priceLabel
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import com.dresso.app.viewmodel.BrandPageState

@Composable
fun BrandCollectionScreen(
    brandId: String,
    state: BrandPageState,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by rememberSaveable(state.brandId) {
        mutableStateOf<GarmentCategory?>(null)
    }

    Column(modifier = modifier.fillMaxSize()) {
        CollectionTopBar(
            onBack = onBack,
            brandName = state.brand?.name,
        )

        when {
            state.isLoading || state.brandId != brandId -> LoadingCollection()
            state.brand == null -> MissingCollection(onBack = onBack)
            else -> {
                val brand = state.brand
                val categories = state.garments.map { it.category }.distinct()
                val visibleGarments = if (selectedCategory == null) {
                    state.garments
                } else {
                    state.garments.filter { it.category == selectedCategory }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 42.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CollectionIntro(
                            brand = brand,
                            pieceCount = state.garments.size,
                        )
                    }

                    if (categories.size > 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategoryFilter(
                                categories = categories,
                                selected = selectedCategory,
                                onSelect = { selectedCategory = it },
                            )
                        }
                    }

                    items(visibleGarments, key = { it.id }) { garment ->
                        CollectionProductCard(
                            garment = garment,
                            onClick = { onProductClick(garment.id) },
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CollectionDisclaimer()
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionTopBar(
    onBack: () -> Unit,
    brandName: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(58.dp)
            .padding(start = 10.dp, end = 18.dp),
    ) {
        DressoBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        DressoWordmark(
            size = 16.dp,
            color = DressoColors.White.copy(alpha = 0.92f),
            modifier = Modifier.align(Alignment.Center),
        )
        Eyebrow(
            text = if (brandName == null) "Demo" else "Demo / Edit",
            color = DressoColors.MutedDeep,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun LoadingCollection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 76.dp),
    ) {
        Eyebrow("Loading collection", color = DressoColors.MutedDeep)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .width(96.dp)
                .height(1.dp)
                .background(DressoColors.Hairline),
        )
    }
}

@Composable
private fun MissingCollection(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 76.dp),
    ) {
        Eyebrow("Collection unavailable", color = DressoColors.Muted)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "This demo collection could not be found.",
            color = DressoColors.Ivory,
            style = DressoTypography.headlineSmall,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Return to the directory to continue.",
            color = DressoColors.Muted,
            style = DressoTypography.bodyMedium,
            modifier = Modifier.clickable(role = Role.Button, onClick = onBack),
        )
    }
}

@Composable
private fun CollectionIntro(
    brand: Brand,
    pieceCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 8.dp),
    ) {
        Eyebrow(
            text = "Selected brand / demo collection",
            color = DressoColors.MutedDeep,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = brand.name,
            color = DressoColors.White,
            style = DressoTypography.displayLarge.copy(
                fontSize = 64.sp,
                lineHeight = 62.sp,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = brand.tagline.uppercase(),
            color = DressoColors.Muted,
            style = EyebrowStyle.copy(letterSpacing = 2.4.sp),
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = brand.description,
            color = DressoColors.Muted,
            style = DressoTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.92f),
        )
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Hairline(
                color = DressoColors.Hairline,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            Eyebrow(
                text = "${pieceCount.toString().padStart(2, '0')} pieces",
                color = DressoColors.MutedDeep,
            )
        }
    }
}

@Composable
private fun CategoryFilter(
    categories: List<GarmentCategory>,
    selected: GarmentCategory?,
    onSelect: (GarmentCategory?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            CategoryLabel(
                label = "All",
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(categories.size) { index ->
            val category = categories[index]
            CategoryLabel(
                label = category.label,
                selected = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}

@Composable
private fun CategoryLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = if (selected) DressoColors.White else DressoColors.MutedDeep,
            style = DressoTypography.labelMedium.copy(
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
            ),
        )
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (selected) DressoColors.White else Color.Transparent),
        )
    }
}

@Composable
private fun CollectionProductCard(
    garment: Garment,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(2.dp)),
        ) {
            GarmentArtwork(
                garment = garment,
                color = garment.colors.first(),
                imageKey = garment.imageKeys.first(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(11.dp))
        Text(
            text = garment.name,
            color = DressoColors.Ivory,
            style = DressoTypography.titleSmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = garment.priceLabel,
            color = DressoColors.Muted,
            style = DressoTypography.bodySmall,
        )
    }
}

@Composable
private fun CollectionDisclaimer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp),
    ) {
        Hairline(color = DressoColors.Hairline)
        Spacer(Modifier.height(18.dp))
        SectionLabel("Demo note", color = DressoColors.MutedDeep)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Pieces, prices and imagery are illustrative. Dresso is not affiliated " +
                "with the brands in this demo directory.",
            color = DressoColors.MutedDeep,
            style = DressoTypography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp),
        )
        Spacer(Modifier.height(20.dp))
        Eyebrow("Dresso / Selected brands", color = DressoColors.MutedDeep)
    }
}

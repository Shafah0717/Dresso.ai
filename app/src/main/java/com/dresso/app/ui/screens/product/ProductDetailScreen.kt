package com.dresso.app.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.data.model.Garment
import com.dresso.app.data.model.GarmentColor
import com.dresso.app.data.model.GarmentSize
import com.dresso.app.ui.components.DressoBackButton
import com.dresso.app.ui.components.DressoButton
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.GarmentArtwork
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.priceLabel
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.viewmodel.GarmentPageState
import com.dresso.app.viewmodel.SelectedLook

@Composable
fun ProductDetailScreen(
    productId: String,
    page: GarmentPageState,
    selectedLook: SelectedLook?,
    onBack: () -> Unit,
    onSelectColor: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onTryOn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val garment = page.garment
    if (page.isLoading || page.productId != productId || garment == null) {
        ProductLoading(
            onBack = onBack,
            modifier = modifier,
        )
        return
    }

    val look = selectedLook?.takeIf { it.garment.id == garment.id }
    val selectedColor = look?.color ?: garment.colors.first()
    val selectedSize = look?.size
    val canTryOn = selectedSize != null || garment.modelAssetPath != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DressoColors.Ivory,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ProductActionBar(
                canTryOn = canTryOn,
                onTryOn = onTryOn,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            item {
                ProductGallery(
                    garment = garment,
                    color = selectedColor,
                    onBack = onBack,
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(Modifier.height(30.dp))
                    Eyebrow(
                        text = page.brand?.name ?: "Dresso selection",
                        color = DressoColors.LightMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = garment.name,
                        color = DressoColors.LightText,
                        style = DressoTypography.displaySmall.copy(
                            fontSize = 34.sp,
                            lineHeight = 37.sp,
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = garment.priceLabel,
                        color = DressoColors.LightText,
                        style = DressoTypography.bodyLarge.copy(
                            fontSize = 16.sp,
                            letterSpacing = 0.3.sp,
                        ),
                    )
                    Spacer(Modifier.height(22.dp))
                    Text(
                        text = garment.description,
                        color = DressoColors.LightMuted,
                        style = DressoTypography.bodyMedium.copy(lineHeight = 22.sp),
                    )
                    Spacer(Modifier.height(30.dp))
                    Hairline(color = DressoColors.LightHairline)
                    Spacer(Modifier.height(26.dp))

                    ColorSelector(
                        colors = garment.colors,
                        selected = selectedColor,
                        onSelect = onSelectColor,
                    )

                    Spacer(Modifier.height(30.dp))

                    SizeSelector(
                        sizes = garment.sizes,
                        selected = selectedSize,
                        onSelect = onSelectSize,
                    )

                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 9.dp)
                                .size(4.dp)
                                .background(DressoColors.LightMuted, CircleShape),
                        )
                        Text(
                            text = "Fit guidance is unavailable for this piece. " +
                                "No verified size chart exists for this demo collection.",
                            color = DressoColors.LightMuted,
                            style = DressoTypography.bodySmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 16.sp,
                            ),
                        )
                    }

                    Spacer(Modifier.height(44.dp))
                    Hairline(color = DressoColors.LightHairline)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Selected colour and size are preserved when you open the " +
                            "fitting room. The avatar is viewable, but garments are not fitted yet.",
                        color = DressoColors.LightMuted,
                        style = DressoTypography.bodySmall.copy(
                            fontSize = 10.sp,
                            lineHeight = 16.sp,
                        ),
                    )
                    Spacer(Modifier.height(40.dp))
                    Eyebrow(
                        text = "Demo piece / illustrative imagery",
                        color = DressoColors.LightMuted,
                    )
                    Spacer(Modifier.height(26.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductGallery(
    garment: Garment,
    color: GarmentColor,
    onBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { garment.imageKeys.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val imageKey = garment.imageKeys.getOrElse(page) { "${garment.id}-front" }
            GarmentArtwork(
                garment = garment,
                color = color,
                imageKey = imageKey,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DressoBackButton(
                onClick = onBack,
                contained = true,
                tint = DressoColors.White,
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(DressoColors.Ink.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                Eyebrow(
                    text = "${pagerState.currentPage + 1} / ${garment.imageKeys.size}",
                    color = DressoColors.White.copy(alpha = 0.85f),
                )
            }
        }

        PageIndicator(
            pageCount = garment.imageKeys.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .width(if (index == currentPage) 18.dp else 5.dp)
                    .height(1.dp)
                    .background(
                        if (index == currentPage) {
                            DressoColors.LightText.copy(alpha = 0.75f)
                        } else {
                            DressoColors.LightText.copy(alpha = 0.25f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun ColorSelector(
    colors: List<GarmentColor>,
    selected: GarmentColor,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Colour",
            color = DressoColors.LightText,
            style = DressoTypography.labelMedium.copy(letterSpacing = 1.5.sp),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = selected.name,
            color = DressoColors.LightMuted,
            style = DressoTypography.bodySmall,
        )
    }
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        colors.forEach { color ->
            val isSelected = color.id == selected.id
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .semantics {
                        contentDescription = color.name
                        this.selected = isSelected
                    }
                    .clickable(role = Role.RadioButton) { onSelect(color.id) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 34.dp else 30.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) {
                                DressoColors.LightText
                            } else {
                                DressoColors.LightHairline
                            },
                            shape = CircleShape,
                        )
                        .padding(3.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(color.argb)),
                    )
                }
            }
        }
    }
}

@Composable
private fun SizeSelector(
    sizes: List<GarmentSize>,
    selected: GarmentSize?,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Size",
            color = DressoColors.LightText,
            style = DressoTypography.labelMedium.copy(letterSpacing = 1.5.sp),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (sizes.isEmpty()) "Not specified" else selected?.label ?: "Select",
            color = DressoColors.LightMuted,
            style = DressoTypography.bodySmall,
        )
    }
    Spacer(Modifier.height(14.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sizes.forEach { size ->
            val isSelected = size.id == selected?.id
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                    .background(
                        color = if (isSelected) DressoColors.LightText else Color.Transparent,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) {
                            DressoColors.LightText
                        } else {
                            DressoColors.LightHairline
                        },
                    )
                    .semantics {
                        contentDescription = "Size ${size.label}"
                        this.selected = isSelected
                    }
                    .clickable(role = Role.RadioButton) { onSelect(size.id) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = size.label,
                    color = if (isSelected) DressoColors.Ivory else DressoColors.LightText,
                    style = DressoTypography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ProductActionBar(
    canTryOn: Boolean,
    onTryOn: () -> Unit,
) {
    Surface(
        color = DressoColors.Ivory,
        contentColor = DressoColors.LightText,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Hairline(color = DressoColors.LightHairline)
            DressoButton(
                label = if (canTryOn) "Try On" else "Select a size",
                enabled = canTryOn,
                light = true,
                showArrow = canTryOn,
                onClick = onTryOn,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ProductLoading(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DressoColors.Ivory),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, top = 8.dp),
        ) {
            DressoBackButton(
                onClick = onBack,
                tint = DressoColors.LightText,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Eyebrow("Loading piece", color = DressoColors.LightMuted)
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(1.dp)
                    .background(DressoColors.LightHairline),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Preparing the fitting room.",
                color = DressoColors.LightMuted,
                style = DressoTypography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

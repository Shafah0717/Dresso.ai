package com.dresso.app.ui.screens.discover

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.data.model.Brand
import com.dresso.app.ui.components.BrandCampaignArtwork
import com.dresso.app.ui.components.DressoButton
import com.dresso.app.ui.components.DressoWordmark
import com.dresso.app.ui.components.Eyebrow
import com.dresso.app.ui.components.Hairline
import com.dresso.app.ui.components.HeroArtwork
import com.dresso.app.ui.components.ProfileShortcut
import com.dresso.app.ui.components.SectionLabel
import com.dresso.app.ui.theme.Cormorant
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
    brands: List<Brand>,
    onBrandClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = if (screenWidth >= 600.dp) {
        320.dp
    } else {
        (screenWidth * 0.72f).coerceAtMost(310.dp)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 10.dp, top = 7.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DressoWordmark(size = 20.dp)
                Spacer(Modifier.weight(1f))
                ProfileShortcut(onClick = onProfileClick)
            }
        }

        item(key = "hero") {
            HeroSection(
                onExploreBrands = {
                    scope.launch { listState.animateScrollToItem(2) }
                },
            )
        }

        item(key = "brands-header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 56.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel("Selected Brands", modifier = Modifier.weight(1f))
                    Eyebrow(
                        text = brands.size.toString().padStart(2, '0'),
                        color = DressoColors.MutedDeep,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Four demo houses, edited for form,\nmaterial and line.",
                    color = DressoColors.Muted,
                    style = DressoTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.86f),
                )
                Spacer(Modifier.height(22.dp))
            }
        }

        item(key = "brands-row") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(brands, key = { _, brand -> brand.id }) { index, brand ->
                    BrandCard(
                        brand = brand,
                        index = index,
                        width = cardWidth,
                        onClick = { onBrandClick(brand.id) },
                    )
                }
            }
        }

        item(key = "editorial") {
            EditorialClosing()
        }

        item(key = "footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp),
            ) {
                Hairline(color = DressoColors.Hairline)
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Eyebrow("Dresso / 2026", color = DressoColors.MutedDeep)
                    Eyebrow("Demo collection", color = DressoColors.MutedDeep)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Demo directory. Dresso is not affiliated with the brands shown. " +
                        "Product imagery is an illustrative placeholder.",
                    color = DressoColors.MutedDeep,
                    style = DressoTypography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp),
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    onExploreBrands: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.76f),
    ) {
        HeroArtwork(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.58f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 26.dp),
        ) {
            Eyebrow(
                text = "The form edit / 01",
                color = DressoColors.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Style, in your\nproportions.",
                color = DressoColors.White,
                style = DressoTypography.displayMedium.copy(
                    fontSize = 46.sp,
                    lineHeight = 45.sp,
                ),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Discover exceptional pieces.\nExperience your fit.",
                color = DressoColors.Muted,
                style = DressoTypography.bodyMedium,
            )
            Spacer(Modifier.height(24.dp))
            DressoButton(
                label = "Explore Brands",
                onClick = onExploreBrands,
                modifier = Modifier.width(212.dp),
            )
        }
    }
}

@Composable
private fun BrandCard(
    brand: Brand,
    index: Int,
    width: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(2.dp))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        BrandCampaignArtwork(
            brand = brand,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = brand.monogram,
            color = DressoColors.White.copy(alpha = 0.09f),
            style = DressoTypography.displayLarge.copy(
                fontFamily = Cormorant,
                fontSize = 170.sp,
                lineHeight = 170.sp,
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 12.dp),
        )

        Eyebrow(
            text = "0${index + 1}",
            color = DressoColors.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 14.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.NorthEast,
                contentDescription = "View ${brand.name} collection",
                tint = DressoColors.White.copy(alpha = 0.88f),
                modifier = Modifier.size(15.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, end = 18.dp, bottom = 17.dp),
        ) {
            Text(
                text = brand.name,
                color = DressoColors.White,
                style = DressoTypography.headlineLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                ),
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(
                    text = brand.tagline,
                    color = DressoColors.White.copy(alpha = 0.55f),
                )
                Spacer(Modifier.width(9.dp))
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.28f)),
                )
            }
        }
    }
}

@Composable
private fun EditorialClosing() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 82.dp),
    ) {
        Hairline(color = DressoColors.Hairline)
        Spacer(Modifier.height(38.dp))
        Eyebrow("The Dresso point of view", color = DressoColors.MutedDeep)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "A wardrobe is not what you own.\nIt is how a piece moves with you.",
            color = DressoColors.Ivory,
            style = DressoTypography.headlineMedium.copy(
                fontSize = 27.sp,
                lineHeight = 33.sp,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "A demo marketplace for discovering form, proportion and fit.",
            color = DressoColors.Muted,
            style = DressoTypography.bodySmall,
            textAlign = TextAlign.Start,
        )
    }
}

package com.dresso.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.data.model.Garment
import com.dresso.app.ui.theme.Cormorant
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.DressoTypography
import com.dresso.app.ui.theme.EyebrowStyle
import com.dresso.app.ui.theme.WordmarkStyle
import java.util.Locale

val Garment.priceLabel: String
    get() = String.format(Locale.US, "€%d", priceEur)

@Composable
fun DressoWordmark(
    modifier: Modifier = Modifier,
    color: Color = DressoColors.White,
    size: Dp = 22.dp,
) {
    Text(
        text = "DRESSO",
        modifier = modifier,
        color = color,
        style = WordmarkStyle.copy(
            fontSize = size.value.sp,
            lineHeight = (size.value + 1).sp,
        ),
    )
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Muted,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        style = EyebrowStyle,
    )
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Muted,
    lineColor: Color = DressoColors.MutedDeep,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(1.dp)
                .background(lineColor),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text.uppercase(),
            color = color,
            style = EyebrowStyle,
        )
    }
}

@Composable
fun Hairline(
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Hairline,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

@Composable
fun DressoButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    light: Boolean = false,
    showArrow: Boolean = true,
) {
    val background = if (light) DressoColors.LightText else DressoColors.Ivory
    val content = if (light) DressoColors.Ivory else DressoColors.Ink
    val animatedBackground by animateColorAsState(
        targetValue = if (enabled) background else background.copy(alpha = 0.26f),
        label = "buttonBackground",
    )
    val animatedContent by animateColorAsState(
        targetValue = if (enabled) content else content.copy(alpha = 0.6f),
        label = "buttonContent",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(animatedBackground)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            color = animatedContent,
            style = DressoTypography.labelLarge.copy(
                fontSize = 11.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (showArrow) {
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = animatedContent,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
fun QuietAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Ivory,
    showArrow: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            color = color,
            style = EyebrowStyle.copy(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (showArrow) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
fun DressoBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DressoColors.White,
    contained: Boolean = false,
) {
    if (contained) {
        Box(
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DressoColors.Ink.copy(alpha = 0.52f))
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
fun ProfileShortcut(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DressoColors.White,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = "Profile" },
    ) {
        Icon(
            imageVector = Icons.Outlined.PersonOutline,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
fun PageHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = DressoColors.White,
    eyebrowColor: Color = DressoColors.Muted,
    titleStyle: androidx.compose.ui.text.TextStyle = DressoTypography.displayMedium,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Eyebrow(eyebrow, color = eyebrowColor)
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = titleColor,
            style = titleStyle.copy(fontFamily = Cormorant),
        )
    }
}

@Composable
fun CenteredNote(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Muted,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = color,
        style = DressoTypography.bodySmall.copy(letterSpacing = 0.35.sp),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun CompletionMeta(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    color: Color = DressoColors.Muted,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$completed OF $total",
            color = color,
            style = EyebrowStyle,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(
                            if (index < completed) color else color.copy(alpha = 0.25f),
                        ),
                )
            }
        }
    }
}

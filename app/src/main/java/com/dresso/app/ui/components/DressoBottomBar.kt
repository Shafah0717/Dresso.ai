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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dresso.app.navigation.TopLevelDestination
import com.dresso.app.ui.theme.DressoColors
import com.dresso.app.ui.theme.EyebrowStyle

@Composable
fun DressoBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DressoColors.Ink.copy(alpha = 0.985f))
            .navigationBarsPadding(),
    ) {
        Hairline(color = DressoColors.Hairline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomBarItem(
                destination = TopLevelDestination.Discover,
                icon = Icons.Outlined.Explore,
                selected = selected == TopLevelDestination.Discover,
                onClick = { onSelect(TopLevelDestination.Discover) },
            )
            BottomBarItem(
                destination = TopLevelDestination.TryOn,
                icon = Icons.Outlined.Checkroom,
                selected = selected == TopLevelDestination.TryOn,
                onClick = { onSelect(TopLevelDestination.TryOn) },
            )
            BottomBarItem(
                destination = TopLevelDestination.Profile,
                icon = Icons.Outlined.PersonOutline,
                selected = selected == TopLevelDestination.Profile,
                onClick = { onSelect(TopLevelDestination.Profile) },
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) DressoColors.White else DressoColors.MutedDeep,
        label = "bottomBarColor",
    )
    Column(
        modifier = Modifier
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = destination.label,
            color = contentColor,
            style = EyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 1.35.sp),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(1.dp)
                .then(
                    if (selected) {
                        Modifier.size(width = 14.dp, height = 1.dp)
                    } else {
                        Modifier.size(width = 14.dp, height = 1.dp)
                    },
                )
                .background(if (selected) Color.White else Color.Transparent),
        )
    }
}

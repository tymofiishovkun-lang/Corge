package org.app.corge.screens.bottomBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.tab_explore_active
import corge.composeapp.generated.resources.tab_explore_inactive
import corge.composeapp.generated.resources.tab_home_active
import corge.composeapp.generated.resources.tab_home_inactive
import corge.composeapp.generated.resources.tab_settings_active
import corge.composeapp.generated.resources.tab_settings_inactive
import org.app.corge.screens.home.HomePalette
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomBar(
    selected: Int,
    onExplore: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF8EFE5),
        tonalElevation = 0.dp
    ) {
        @Composable
        fun item(
            index: Int,
            contentDescription: String,
            activeIcon: Painter,
            inactiveIcon: Painter,
            onClick: () -> Unit
        ) {
            val isSelected = selected == index
            NavigationBarItem(
                selected = isSelected,
                onClick = onClick,
                icon = {
                    Image(
                        painter = if (isSelected) activeIcon else inactiveIcon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(55.dp)
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    selectedTextColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified
                )
            )
        }

        item(
            index = 0,
            contentDescription = "Explore",
            activeIcon = painterResource(Res.drawable.tab_explore_active),
            inactiveIcon = painterResource(Res.drawable.tab_explore_inactive),
            onClick = onExplore
        )
        item(
            index = 1,
            contentDescription = "Home",
            activeIcon = painterResource(Res.drawable.tab_home_active),
            inactiveIcon = painterResource(Res.drawable.tab_home_inactive),
            onClick = onHome
        )
        item(
            index = 2,
            contentDescription = "Settings",
            activeIcon = painterResource(Res.drawable.tab_settings_active),
            inactiveIcon = painterResource(Res.drawable.tab_settings_inactive),
            onClick = onSettings
        )
    }
}

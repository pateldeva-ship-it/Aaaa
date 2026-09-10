package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HomeworkScreen
import com.example.ui.screens.LecturesScreen
import com.example.ui.screens.WidgetSyncScreen
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MainScreen(
    viewModel: PWViewModel,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val missedCount by viewModel.missedLecturesCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingHomeworkCount.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = DarkBg,
        topBar = {
            PWTopBar(missedCount = missedCount, onOpenAlerts = { selectedTab = 2 })
        },
        bottomBar = {
            PWBottomNavBar(
                selectedTab = selectedTab,
                missedCount = missedCount,
                pendingCount = pendingCount,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> LecturesScreen(viewModel = viewModel)
                1 -> HomeworkScreen(viewModel = viewModel)
                2 -> WidgetSyncScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PWTopBar(missedCount: Int, onOpenAlerts: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = DarkBorder),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricViolet),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PW",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "PW Companion",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onOpenAlerts,
                modifier = Modifier.size(36.dp).testTag("top_bar_notif_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (missedCount > 0) {
                            Badge(containerColor = CoralAlert) {
                                Text("$missedCount", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (missedCount > 0) CoralAlert else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PWBottomNavBar(
    selectedTab: Int,
    missedCount: Int,
    pendingCount: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(width = 0.5.dp, color = DarkBorder),
        containerColor = DarkSurface,
        contentColor = TextPrimary
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                BadgedBox(
                    badge = {
                        if (missedCount > 0) {
                            Badge(containerColor = CoralAlert) {
                                Text("$missedCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.School, contentDescription = "Lectures")
                }
            },
            label = { Text("Classes", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricVioletLight,
                selectedTextColor = ElectricVioletLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = DarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_lectures")
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                BadgedBox(
                    badge = {
                        if (pendingCount > 0) {
                            Badge(containerColor = CyberCyan) {
                                Text("$pendingCount", color = Color.Black)
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Assignment, contentDescription = "Homework")
                }
            },
            label = { Text("Homework", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberCyanLight,
                selectedTextColor = CyberCyanLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = DarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_homework")
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = "Widget")
            },
            label = { Text("Widget", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricVioletLight,
                selectedTextColor = ElectricVioletLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = DarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_widget_sync")
        )
    }
}

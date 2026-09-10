package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.PWViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CoralAlertLight
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.NotificationHelper
import com.example.widget.PWDeskWidgetProvider

@Composable
fun WidgetSyncScreen(
    viewModel: PWViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val missedCount by viewModel.missedLecturesCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingHomeworkCount.collectAsStateWithLifecycle()
    val missedLectures by viewModel.missedLectures.collectAsStateWithLifecycle()
    val pendingHomework by viewModel.pendingHomework.collectAsStateWithLifecycle()

    var hasNotifPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPermission = isGranted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(
                title = "Home Screen Widget",
                badgeText = "Live",
                badgeColor = CyberCyanLight
            )
        }

        // Live Widget Preview
        item {
            LiveWidgetCard(
                missedCount = missedCount,
                pendingCount = pendingCount,
                nextDetail = if (pendingHomework.isNotEmpty()) {
                    "Next: ${pendingHomework.first().title}"
                } else if (missedLectures.isNotEmpty()) {
                    "Backlog: ${missedLectures.first().subject} L-${missedLectures.first().lectureNumber}"
                } else {
                    "0 backlogs & 0 pending DPPs"
                },
                onRefresh = {
                    PWDeskWidgetProvider.triggerUpdate(context)
                    Toast.makeText(context, "Widget refreshed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Push Notifications Section
        item {
            SectionHeader(
                title = "Push Alerts",
                badgeText = if (hasNotifPermission) "Active" else "Enable",
                badgeColor = if (hasNotifPermission) EmeraldLight else CoralAlert
            )
        }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = CyberCyanLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Assignment & Backlog Alerts", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = { notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralAlert),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Allow", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ON",
                                    color = EmeraldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerSampleAssignmentAlert(context)
                                Toast.makeText(context, "Alert sent", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_test_assignment_notif"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
                        ) {
                            Text("Test HW Alert", fontSize = 11.sp, color = CyberCyanLight)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerSampleMissedLectureAlert(context)
                                Toast.makeText(context, "Alert sent", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_test_missed_notif"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
                        ) {
                            Text("Test Missed Alert", fontSize = 11.sp, color = CoralAlertLight)
                        }
                    }
                }
            }
        }

        // Calendar Sync Section
        item {
            SectionHeader(
                title = "Calendar Sync",
                badgeText = "Google Calendar",
                badgeColor = ElectricVioletLight
            )
        }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = ElectricVioletLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Sync Deadlines & Backlogs", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (pendingHomework.isNotEmpty()) {
                                viewModel.syncHomeworkToCalendar(pendingHomework.first(), context)
                            } else {
                                Toast.makeText(context, "No pending homework to sync", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_sync_all_calendar"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Next Due DPP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun LiveWidgetCard(
    missedCount: Int,
    pendingCount: Int,
    nextDetail: String,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricVioletLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberCyanLight)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PW STUDY DESK",
                        color = ElectricVioletLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onRefresh, modifier = Modifier.size(26.dp).testTag("btn_refresh_widget")) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyanLight, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "MISSED", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$missedCount", color = Color(0xFFFB7185), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "DUE DPPS", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$pendingCount", color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0x33334155)
            ) {
                Text(
                    text = nextDetail,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

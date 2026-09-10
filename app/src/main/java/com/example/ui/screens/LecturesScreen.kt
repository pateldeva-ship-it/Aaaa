package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import com.example.ui.PWViewModel
import com.example.ui.components.FilterChipRow
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatBadge
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CoralAlertLight
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LecturesScreen(
    viewModel: PWViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allLectures by viewModel.filteredLectures.collectAsStateWithLifecycle()
    val missedLectures by viewModel.missedLectures.collectAsStateWithLifecycle()
    val missedCount by viewModel.missedLecturesCount.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()

    var showAddLectureDialog by remember { mutableStateOf(false) }
    var lectureToMarkMissed by remember { mutableStateOf<Lecture?>(null) }
    var lectureToScheduleBacklog by remember { mutableStateOf<Lecture?>(null) }

    val subjects = listOf("All", "Physics", "Chemistry", "Mathematics", "Biology")

    Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Minimalist Hero Header
                SimpleHeroBanner(missedCount = missedCount, totalCount = allLectures.size)
            }

            // Simple stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBadge(
                        title = "Missed",
                        value = "$missedCount",
                        icon = Icons.Default.Warning,
                        accentColor = CoralAlert,
                        modifier = Modifier.weight(1f).testTag("stat_missed_lectures")
                    )
                    StatBadge(
                        title = "Attended",
                        value = "${allLectures.count { it.status == LectureStatus.ATTENDED }}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f).testTag("stat_attended_lectures")
                    )
                }
            }

            // Subject Filter
            item {
                FilterChipRow(
                    items = subjects,
                    selectedItem = selectedSubject,
                    onItemSelected = { viewModel.setSubjectFilter(it) }
                )
            }

            // Missed Lectures Section
            if (missedLectures.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Missed Classes (${missedLectures.size})",
                        badgeText = "Needs Backlog",
                        badgeColor = CoralAlert
                    )
                }
                items(missedLectures, key = { "missed_${it.id}" }) { lecture ->
                    MissedLectureCard(
                        lecture = lecture,
                        onScheduleBacklog = { lectureToScheduleBacklog = lecture },
                        onMarkAttended = {
                            viewModel.markLectureAttendance(lecture, LectureStatus.ATTENDED, context = context)
                        }
                    )
                }
            }

            // All Lectures
            item {
                SectionHeader(
                    title = "Classes",
                    badgeText = "${allLectures.size}",
                    badgeColor = CyberCyanLight
                )
            }

            if (allLectures.isEmpty()) {
                item {
                    GlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No classes for $selectedSubject",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(allLectures, key = { "lecture_${it.id}" }) { lecture ->
                    LectureItemCard(
                        lecture = lecture,
                        onToggleStatus = { targetStatus ->
                            if (targetStatus == LectureStatus.MISSED && lecture.status != LectureStatus.MISSED) {
                                lectureToMarkMissed = lecture
                            } else {
                                viewModel.markLectureAttendance(lecture, targetStatus, context = context)
                            }
                        },
                        onDelete = {
                            viewModel.deleteLecture(lecture, context = context)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddLectureDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_lecture_fab"),
            containerColor = ElectricViolet,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }
    }

    if (lectureToMarkMissed != null) {
        MarkMissedDialog(
            lecture = lectureToMarkMissed!!,
            onDismiss = { lectureToMarkMissed = null },
            onConfirm = { reason ->
                viewModel.markLectureAttendance(
                    lectureToMarkMissed!!,
                    LectureStatus.MISSED,
                    reason = reason,
                    context = context
                )
                lectureToMarkMissed = null
            }
        )
    }

    if (lectureToScheduleBacklog != null) {
        ScheduleBacklogDialog(
            lecture = lectureToScheduleBacklog!!,
            onDismiss = { lectureToScheduleBacklog = null },
            onSchedule = { plannedDate ->
                viewModel.scheduleBacklogWithCalendar(
                    lectureToScheduleBacklog!!,
                    plannedDate,
                    context
                )
                lectureToScheduleBacklog = null
            }
        )
    }

    if (showAddLectureDialog) {
        AddLectureDialog(
            onDismiss = { showAddLectureDialog = false },
            onAdd = { batch, subject, chapter, num, faculty, date, time ->
                viewModel.addLecture(
                    batchName = batch,
                    subject = subject,
                    chapterName = chapter,
                    lectureNumber = num,
                    facultyName = faculty,
                    scheduledDate = date,
                    scheduledTime = time,
                    context = context
                )
                showAddLectureDialog = false
            }
        )
    }
}

@Composable
private fun SimpleHeroBanner(missedCount: Int, totalCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            Image(
                painter = painterResource(id = R.drawable.pw_study_hero_1788975744930),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFA090D16), Color(0xCC090D16), Color(0x66182236))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Class Backlogs",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (missedCount > 0) "$missedCount classes unattended" else "All classes attended",
                        color = if (missedCount > 0) CoralAlertLight else CyberCyanLight,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (missedCount > 0) CoralAlert else EmeraldSuccess).copy(alpha = 0.2f),
                    modifier = Modifier.border(
                        1.dp,
                        (if (missedCount > 0) CoralAlert else EmeraldSuccess).copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                ) {
                    Text(
                        text = if (missedCount > 0) "$missedCount DUE" else "UP TO DATE",
                        color = if (missedCount > 0) CoralAlertLight else EmeraldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MissedLectureCard(
    lecture: Lecture,
    onScheduleBacklog: () -> Unit,
    onMarkAttended: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CoralAlert.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .testTag("missed_card_${lecture.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E131C))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${lecture.subject.uppercase()} • L-${lecture.lectureNumber}",
                    color = CoralAlertLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lecture.scheduledTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lecture.chapterName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${lecture.facultyName} • ${lecture.missedReason ?: "Unattended"}",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScheduleBacklog,
                    modifier = Modifier.weight(1f).testTag("btn_schedule_backlog_${lecture.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Sync Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMarkAttended,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                    modifier = Modifier.testTag("btn_clear_backlog_${lecture.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Done", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LectureItemCard(
    lecture: Lecture,
    onToggleStatus: (LectureStatus) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (lecture.status) {
        LectureStatus.ATTENDED -> EmeraldSuccess
        LectureStatus.MISSED -> CoralAlert
        LectureStatus.UPCOMING -> CyberCyanLight
    }

    GlassCard(
        modifier = Modifier.testTag("lecture_card_${lecture.id}"),
        borderColor = if (lecture.status == LectureStatus.MISSED) CoralAlert.copy(alpha = 0.4f) else DarkBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${lecture.subject} • L-${lecture.lectureNumber}",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = lecture.scheduledTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lecture.chapterName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = lecture.facultyName,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Simple UI Toggle for Attended / Missed status
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .testTag("attendance_toggle_${lecture.id}"),
                    color = DarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attended side
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onToggleStatus(LectureStatus.ATTENDED) }
                                .testTag("btn_attended_${lecture.id}"),
                            color = if (lecture.status == LectureStatus.ATTENDED) EmeraldSuccess else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (lecture.status == LectureStatus.ATTENDED) Color.White else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Attended",
                                    color = if (lecture.status == LectureStatus.ATTENDED) Color.White else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (lecture.status == LectureStatus.ATTENDED) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // Missed side
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onToggleStatus(LectureStatus.MISSED) }
                                .testTag("btn_missed_${lecture.id}"),
                            color = if (lecture.status == LectureStatus.MISSED) CoralAlert else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (lecture.status == LectureStatus.MISSED) Color.White else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Missed",
                                    color = if (lecture.status == LectureStatus.MISSED) Color.White else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (lecture.status == LectureStatus.MISSED) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_lecture_${lecture.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun MarkMissedDialog(
    lecture: Lecture,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    val presetReasons = listOf("Exam", "Network issue", "Sick", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Why was class missed?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetReasons.forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { reason = preset }
                                .border(1.dp, if (reason == preset) CoralAlert else DarkBorder, RoundedCornerShape(6.dp)),
                            color = if (reason == preset) CoralAlert.copy(alpha = 0.2f) else DarkSurfaceVariant
                        ) {
                            Text(
                                text = preset,
                                color = if (reason == preset) CoralAlertLight else TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralAlert,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.ifBlank { "Unattended" }) },
                colors = ButtonDefaults.buttonColors(containerColor = CoralAlert)
            ) {
                Text("Mark Missed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

@Composable
private fun ScheduleBacklogDialog(
    lecture: Lecture,
    onDismiss: () -> Unit,
    onSchedule: (Long) -> Unit
) {
    var selectedOffsetHours by remember { mutableStateOf(4) }
    val options = listOf(
        Pair("Today (+4h)", 4),
        Pair("Tomorrow (+18h)", 18),
        Pair("Weekend (+48h)", 48)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Schedule Backlog", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (label, hours) ->
                    val isSelected = selectedOffsetHours == hours
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedOffsetHours = hours }
                            .border(1.dp, if (isSelected) ElectricVioletLight else DarkBorder, RoundedCornerShape(8.dp)),
                        color = if (isSelected) ElectricViolet.copy(alpha = 0.2f) else DarkSurfaceVariant
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) ElectricVioletLight else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetMillis = System.currentTimeMillis() + (selectedOffsetHours * 3600 * 1000L)
                    onSchedule(targetMillis)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Text("Sync Calendar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

@Composable
private fun AddLectureDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, String, Long, String) -> Unit
) {
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var lectureNum by remember { mutableStateOf("1") }
    var faculty by remember { mutableStateOf("Rajwant Sir") }
    var timeSlot by remember { mutableStateOf("04:30 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Class", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricVioletLight,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricVioletLight,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = lectureNum,
                        onValueChange = { lectureNum = it.filter { ch -> ch.isDigit() } },
                        label = { Text("L#") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricVioletLight,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty") },
                        modifier = Modifier.weight(1.2f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricVioletLight,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricVioletLight,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chapter.isNotBlank()) {
                        onAdd(
                            "Lakshya JEE",
                            subject,
                            chapter,
                            lectureNum.toIntOrNull() ?: 1,
                            faculty,
                            System.currentTimeMillis(),
                            timeSlot
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

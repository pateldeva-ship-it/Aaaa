package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HomeworkAssignment
import com.example.data.model.HomeworkPriority
import com.example.data.model.HomeworkStatus
import com.example.ui.PWViewModel
import com.example.ui.components.FilterChipRow
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CoralAlertLight
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PdfAttachmentHelper

@Composable
fun HomeworkScreen(
    viewModel: PWViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeworkList by viewModel.filteredHomework.collectAsStateWithLifecycle()
    val allHomework by viewModel.allHomework.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingHomeworkCount.collectAsStateWithLifecycle()
    val currentFilter by viewModel.homeworkFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Pending", "In Progress", "Completed", "High Priority")

    Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Clean Minimal Header
                SimpleHomeworkHeader(pendingCount = pendingCount)
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBadge(
                        title = "Pending",
                        value = "$pendingCount",
                        icon = Icons.Default.Description,
                        accentColor = CyberCyanLight,
                        modifier = Modifier.weight(1f).testTag("stat_pending_hw")
                    )
                    StatBadge(
                        title = "Completed",
                        value = "${allHomework.count { it.status == HomeworkStatus.COMPLETED }}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f).testTag("stat_completed_hw")
                    )
                }
            }

            // Filters
            item {
                FilterChipRow(
                    items = filterOptions,
                    selectedItem = currentFilter,
                    onItemSelected = { viewModel.setHomeworkFilter(it) }
                )
            }

            // Homework items list
            item {
                SectionHeader(
                    title = "Assignments",
                    badgeText = "${homeworkList.size}",
                    badgeColor = ElectricVioletLight
                )
            }

            if (homeworkList.isEmpty()) {
                item {
                    GlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No homework in $currentFilter",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(homeworkList, key = { "hw_${it.id}" }) { homework ->
                    HomeworkCard(
                        homework = homework,
                        onProgressChange = { newCount ->
                            viewModel.updateHomeworkProgress(homework.id, newCount, context)
                        },
                        onSyncCalendar = {
                            viewModel.syncHomeworkToCalendar(homework, context)
                        },
                        onDelete = {
                            viewModel.deleteHomework(homework, context)
                        },
                        onOpenPdf = {
                            PdfAttachmentHelper.openPdf(
                                context,
                                homework.pdfUri,
                                homework.pdfFileName ?: homework.title
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_homework_fab"),
            containerColor = CyberCyan,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }
    }

    if (showAddDialog) {
        ScheduleHomeworkDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, subject, batch, uri, fileName, fileSize, dueDate, dueTime, qCount, priority, notes, syncCal, sendAlert ->
                viewModel.addHomework(
                    title = title,
                    subject = subject,
                    batchName = batch,
                    pdfUri = uri,
                    pdfFileName = fileName,
                    pdfFileSize = fileSize,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    totalQuestions = qCount,
                    priority = priority,
                    notes = notes,
                    syncToCalendar = syncCal,
                    sendPushAlert = sendAlert,
                    context = context
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SimpleHomeworkHeader(pendingCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Homework & DPPs",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$pendingCount pending to solve",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyberCyan.copy(alpha = 0.2f),
                modifier = Modifier.border(1.dp, CyberCyanLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "PDF Attached",
                    color = CyberCyanLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeworkCard(
    homework: HomeworkAssignment,
    onProgressChange: (Int) -> Unit,
    onSyncCalendar: () -> Unit,
    onDelete: () -> Unit,
    onOpenPdf: () -> Unit
) {
    val progressFraction = if (homework.totalQuestions > 0) {
        homework.solvedQuestions.toFloat() / homework.totalQuestions.toFloat()
    } else 0f

    val priorityColor = when (homework.priority) {
        HomeworkPriority.HIGH -> CoralAlert
        HomeworkPriority.MEDIUM -> AmberWarning
        HomeworkPriority.LOW -> CyberCyanLight
    }

    GlassCard(
        modifier = Modifier.testTag("homework_card_${homework.id}"),
        borderColor = if (homework.status == HomeworkStatus.COMPLETED) EmeraldSuccess.copy(alpha = 0.4f) else DarkBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Subject, Priority, Due Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = homework.subject.uppercase(),
                        color = ElectricVioletLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${homework.priority.name}",
                        color = priorityColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = homework.dueTime,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = homework.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // PDF Attachment Box (Simple & Compact)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenPdf() }
                    .border(1.dp, DarkSurfaceHighlight, RoundedCornerShape(8.dp))
                    .testTag("pdf_attachment_${homework.id}"),
                color = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CoralAlertLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = homework.pdfFileName ?: "PW_Attachment.pdf",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Open",
                        color = CyberCyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = CyberCyanLight,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar and Question Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${homework.solvedQuestions} / ${homework.totalQuestions}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "solved",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onProgressChange(homework.solvedQuestions - 1) },
                        enabled = homework.solvedQuestions > 0,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus", tint = TextPrimary, modifier = Modifier.size(12.dp))
                    }

                    IconButton(
                        onClick = { onProgressChange(homework.solvedQuestions + 1) },
                        enabled = homework.solvedQuestions < homework.totalQuestions,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", tint = TextPrimary, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progressFraction >= 1f) EmeraldSuccess else CyberCyan,
                trackColor = DarkSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions row: Sync Cal and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSyncCalendar() }
                        .border(
                            1.dp,
                            if (homework.isSyncedToCalendar) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .testTag("btn_sync_cal_${homework.id}"),
                    color = if (homework.isSyncedToCalendar) EmeraldSuccess.copy(alpha = 0.15f) else DarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (homework.isSyncedToCalendar) EmeraldLight else CyberCyanLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (homework.isSyncedToCalendar) "Synced" else "Calendar",
                            color = if (homework.isSyncedToCalendar) EmeraldLight else CyberCyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp).testTag("delete_hw_${homework.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleHomeworkDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        subject: String,
        batch: String,
        uri: String?,
        fileName: String?,
        fileSize: String?,
        dueDate: Long,
        dueTime: String,
        qCount: Int,
        priority: HomeworkPriority,
        notes: String?,
        syncCal: Boolean,
        sendAlert: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var qCountStr by remember { mutableStateOf("15") }
    var dueTime by remember { mutableStateOf("09:00 PM") }
    var priority by remember { mutableStateOf(HomeworkPriority.HIGH) }
    var syncToCalendar by remember { mutableStateOf(true) }
    var sendPushAlert by remember { mutableStateOf(true) }

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfName by remember { mutableStateOf("PW_Physics_DPP.pdf") }
    var selectedPdfSize by remember { mutableStateOf("1.9 MB") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val (name, size) = PdfAttachmentHelper.getFileNameAndSize(context, uri)
            selectedPdfUri = uri
            selectedPdfName = name
            selectedPdfSize = size
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Schedule Homework / DPP", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / DPP No.") },
                    placeholder = { Text("DPP 07") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
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
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = qCountStr,
                        onValueChange = { qCountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Questions") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due Time") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // PDF Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                    color = DarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = CoralAlertLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = selectedPdfName, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyanLight)
                        ) {
                            Text("Attach", fontSize = 11.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sync to Calendar", color = TextPrimary, fontSize = 12.sp)
                    Switch(
                        checked = syncToCalendar,
                        onCheckedChange = { syncToCalendar = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = DarkSurfaceHighlight)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Push Alert", color = TextPrimary, fontSize = 12.sp)
                    Switch(
                        checked = sendPushAlert,
                        onCheckedChange = { sendPushAlert = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = DarkSurfaceHighlight)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title,
                            subject,
                            "Lakshya JEE",
                            selectedPdfUri?.toString(),
                            selectedPdfName,
                            selectedPdfSize,
                            System.currentTimeMillis() + (3600 * 1000 * 12),
                            dueTime,
                            qCountStr.toIntOrNull() ?: 15,
                            priority,
                            null,
                            syncToCalendar,
                            sendPushAlert
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

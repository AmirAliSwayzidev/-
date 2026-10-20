package com.planora.ai.ui.screens.programs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.ProgramEntity
import com.planora.ai.data.local.entity.ProgramSessionEntity
import com.planora.ai.session.SessionTimerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    database: PlanoraDatabase,
    onBack: () -> Unit
) {
    val programs by database.programDao().getAllPrograms().collectAsState(initial = emptyList())
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // live timer tick
    val active by SessionTimerManager.active.collectAsState()
    val ended by SessionTimerManager.endedAnalysis.collectAsState()
    var remaining by remember { mutableStateOf(0L) }

    LaunchedEffect(active) {
        while (true) {
            remaining = SessionTimerManager.remainingMs()
            if (active != null && !active!!.completed && remaining <= 0L) {
                SessionTimerManager.finish()
                notifySessionEnd(context, active?.title ?: "تمرین")
            }
            delay(1000)
        }
    }

    if (ended != null) {
        SessionEndDialog(
            analysis = ended!!,
            onYes = {
                scope.launch {
                    database.programDao().setSessionDone(ended!!.sessionId, true, System.currentTimeMillis())
                }
                SessionTimerManager.dismissAnalysis()
            },
            onNo = { SessionTimerManager.dismissAnalysis() },
            onSkip = { SessionTimerManager.dismissAnalysis() }
        )
    }

    if (selectedId != null) {
        ProgramDetail(
            database = database,
            programId = selectedId!!,
            onBack = { selectedId = null },
            remainingMs = remaining,
            active = active
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("برنامه‌های تمرین") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (active != null) {
                SessionBar(active!!, remaining)
            }
            if (programs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هنوز برنامه‌ای نیست. + بزنید")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(programs, key = { it.id }) { p ->
                        Card(
                            onClick = { selectedId = p.id },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(p.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${p.type} · ${p.days} روز",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateProgramDialog(
            onDismiss = { showCreate = false },
            onCreate = { title, type, days, durationMin, time ->
                scope.launch {
                    val id = database.programDao().insertProgram(
                        ProgramEntity(
                            title = title,
                            type = type,
                            days = days,
                            startDate = LocalDate.now().toString()
                        )
                    )
                    val sessions = (0 until days).map { i ->
                        ProgramSessionEntity(
                            programId = id,
                            dayIndex = i,
                            title = "$title — روز ${i + 1}",
                            durationMin = durationMin,
                            timeOfDay = time
                        )
                    }
                    database.programDao().insertSessions(sessions)
                    showCreate = false
                    selectedId = id
                }
            }
        )
    }
}

@Composable
private fun SessionBar(active: com.planora.ai.session.ActiveSession, remainingMs: Long) {
    val m = (remainingMs / 60000).toInt()
    val s = ((remainingMs % 60000) / 1000).toInt()
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(active.title, style = MaterialTheme.typography.labelLarge)
            Text(
                "%02d:%02d".format(m, s),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramDetail(
    database: PlanoraDatabase,
    programId: Long,
    onBack: () -> Unit,
    remainingMs: Long,
    active: com.planora.ai.session.ActiveSession?
) {
    val program = produceState<ProgramEntity?>(null, programId) {
        value = database.programDao().getProgram(programId)
    }.value
    val sessions by database.programDao().getSessions(programId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(program?.title ?: "برنامه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (active != null && active.programId == programId) {
                SessionBar(active, remainingMs)
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = s.done,
                                    onCheckedChange = {
                                        scope.launch {
                                            database.programDao().setSessionDone(
                                                s.id, !s.done,
                                                if (!s.done) System.currentTimeMillis() else null
                                            )
                                        }
                                    }
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(s.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${s.durationMin} دقیقه · ${s.timeOfDay}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            if (!s.done) {
                                Button(
                                    onClick = {
                                        SessionTimerManager.start(
                                            programId = programId,
                                            sessionId = s.id,
                                            title = s.title,
                                            durationMin = s.durationMin
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("شروع تایمر ${s.durationMin} دقیقه")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionEndDialog(
    analysis: com.planora.ai.session.SessionAnalysis,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text("پایان جلسه تمرین") },
        text = {
            Column {
                Text(analysis.message)
                Spacer(Modifier.height(8.dp))
                Text(
                    "تماس، آهنگ یا چک کوتاه گوشی طبیعی است و به‌عنوان «کل وقت روی گوشی» حساب نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onYes) { Text("بله، انجام دادم") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onNo) { Text("هنوز نه") }
                TextButton(onClick = onSkip) { Text("رد کن") }
            }
        }
    )
}

@Composable
private fun CreateProgramDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, type: String, days: Int, durationMin: Int, time: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }
    var duration by remember { mutableStateOf("45") }
    var time by remember { mutableStateOf("18:00") }
    var type by remember { mutableStateOf("sport") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("برنامه جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("عنوان") }, singleLine = true)
                OutlinedTextField(days, { days = it.filter { c -> c.isDigit() } }, label = { Text("تعداد روز") }, singleLine = true)
                OutlinedTextField(duration, { duration = it.filter { c -> c.isDigit() } }, label = { Text("دقیقه هر جلسه") }, singleLine = true)
                OutlinedTextField(time, { time = it }, label = { Text("ساعت پیشنهادی") }, singleLine = true)
                Row {
                    FilterChip(selected = type == "sport", onClick = { type = "sport" }, label = { Text("ورزش") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = type == "study", onClick = { type = "study" }, label = { Text("مطالعه") })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(
                            title.trim(),
                            type,
                            days.toIntOrNull()?.coerceIn(1, 90) ?: 7,
                            duration.toIntOrNull()?.coerceIn(1, 300) ?: 45,
                            time.ifBlank { "18:00" }
                        )
                    }
                }
            ) { Text("بساز") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
}

private fun notifySessionEnd(context: Context, title: String) {
    val channelId = "planora_session"
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        nm.createNotificationChannel(
            NotificationChannel(channelId, "جلسات تمرین", NotificationManager.IMPORTANCE_HIGH)
        )
    }
    val n = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("زمان تمرین تمام شد")
        .setContentText("$title — آیا انجام دادی؟")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    nm.notify(9001, n)
}

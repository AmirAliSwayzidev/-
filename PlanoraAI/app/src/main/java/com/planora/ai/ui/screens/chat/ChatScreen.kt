package com.planora.ai.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.ChatMessageEntity
import com.planora.ai.data.local.entity.UserPreferencesEntity
import com.planora.ai.data.remote.AiApiService
import com.planora.ai.data.repository.AiRepository
import com.planora.ai.domain.planner.OfflinePlanningEngine
import com.planora.ai.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    database: PlanoraDatabase,
    offlineEngine: OfflinePlanningEngine, // kept for API compat; not used for fake offline plans
    onPlanReady: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val conversationId = remember { UUID.randomUUID().toString() }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessageEntity>>(emptyList()) }
    val listState = rememberLazyListState()
    val prefs by database.preferencesDao().getPreferences().collectAsState(initial = null)

    val aiRepo = remember {
        AiRepository(
            api = null, // Configure Retrofit when backend is ready
            chatDao = database.chatDao(),
            
            context = context
        )
    }

    val isOnline = aiRepo.isOnline()

    val suggestedPrompts = listOf(
        "برای امتحان ۱۰ روزه یک برنامه مطالعه بساز",
        "کمک کن عادت مطالعه روزانه بسازم",
        "کارهای عقب‌افتاده را اولویت‌بندی کن",
        "یک برنامه هفتگی متعادل پیشنهاد بده"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_assistant))
                    }
                },
                actions = {
                    if (!isOnline) {
                        Icon(
                            Icons.Outlined.WifiOff,
                            contentDescription = stringResource(R.string.offline),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.suggested_prompts),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        suggestedPrompts.forEach { prompt ->
                            SuggestionChip(
                                onClick = { input = prompt },
                                label = { Text(prompt) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.ai_thinking),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Input bar
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.type_message)) },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (input.isBlank() || isLoading) return@FilledIconButton
                            val text = input.trim()
                            input = ""
                            isLoading = true
                            scope.launch {
                                val result = aiRepo.sendMessage(
                                    conversationId = conversationId,
                                    userMessage = text,
                                    history = messages,
                                    preferences = prefs ?: UserPreferencesEntity()
                                )
                                result.onSuccess { response ->
                                    messages = messages + listOf(
                                        ChatMessageEntity(conversationId = conversationId, role = "user", content = text),
                                        ChatMessageEntity(
                                            conversationId = conversationId,
                                            role = "assistant",
                                            content = response.message,
                                            structuredPlanJson = if (response.structuredPlan != null) "plan" else null
                                        )
                                    )
                                    if (response.structuredPlan != null) {
                                        onPlanReady("plan")
                                    }
                                }
                                isLoading = false
                                listState.animateScrollToItem(messages.size)
                            }
                        },
                        enabled = input.isNotBlank() && !isLoading,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = VioletPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

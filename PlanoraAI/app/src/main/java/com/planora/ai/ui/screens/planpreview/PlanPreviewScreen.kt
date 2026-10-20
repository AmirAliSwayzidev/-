package com.planora.ai.ui.screens.planpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.ui.theme.VioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanPreviewScreen(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.plan_preview)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "برنامه پیشنهادی آماده است. پس از تأیید، کارها به لیست شما اضافه می‌شوند.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(5) { i ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        ListItem(
                            headlineContent = { Text("کار پیشنهادی ${i + 1}") },
                            supportingContent = { Text("مدت: ۴۵ دقیقه") }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.reject_plan))
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text(stringResource(R.string.accept_plan))
                }
            }
        }
    }
}

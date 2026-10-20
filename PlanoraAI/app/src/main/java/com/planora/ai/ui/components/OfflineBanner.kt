package com.planora.ai.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.ui.theme.Warning
import kotlinx.coroutines.delay

@Composable
fun OfflineBanner() {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(checkOnline(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = checkOnline(context)
            delay(3000)
        }
    }

    AnimatedVisibility(visible = !isOnline) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Warning.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = Warning)
            Text(
                text = stringResource(R.string.offline_mode_message),
                style = MaterialTheme.typography.bodySmall,
                color = Warning
            )
        }
    }
}

private fun checkOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

package com.nexus.ai.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.components.PuterWebView

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Nexus AI", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            Text("نظام تشغيل الذكاء الاصطناعي", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { webView?.evaluateJavascript("loginPuter()", null) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تسجيل الدخول عبر Puter", fontSize = 16.sp)
            }
            
            if (errorMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }

        // الـ WebView المخفي
        PuterWebView(
            onWebViewCreated = { webView = it },
            onLoginSuccess = { onLoginSuccess() },
            onLoginError = { errorMsg = it },
            onResponse = {}
        )
    }
}

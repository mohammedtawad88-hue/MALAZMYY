package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
actual fun PlatformBannerAd(modifier: Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF1A2E40)),
        contentAlignment = Alignment.Center
    ) {
        Text("Ad Banner Placeholder", color = Color(0xFFD4AF37), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

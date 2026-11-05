package com.phresq.vanguard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun PhilippineFlagWaveHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "flag_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        val width = size.width
        val height = size.height

        // Draw tricolor stripes or animated wave accent
        val stripeHeight = height / 3f
        
        // Blue Stripe
        drawRect(color = Color(0xFF0038A3), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(width, stripeHeight))
        // White Stripe (Middle)
        drawRect(color = Color.White, topLeft = Offset(0f, stripeHeight), size = androidx.compose.ui.geometry.Size(width, stripeHeight))
        // Red Stripe
        drawRect(color = Color(0xFFCE1126), topLeft = Offset(0f, stripeHeight * 2f), size = androidx.compose.ui.geometry.Size(width, stripeHeight))
    }
}

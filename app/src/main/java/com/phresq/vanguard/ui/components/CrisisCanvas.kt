package com.phresq.vanguard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun PhilippineFlagWaveHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "flag_wave_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_anim"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        val width = size.width
        val height = size.height
        val stripeHeight = height / 3f

        // Static background stripes
        drawRect(color = Color(0xFF0038A3), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(width, stripeHeight))
        drawRect(color = Color.White, topLeft = Offset(0f, stripeHeight), size = androidx.compose.ui.geometry.Size(width, stripeHeight))
        drawRect(color = Color(0xFFCE1126), topLeft = Offset(0f, stripeHeight * 2f), size = androidx.compose.ui.geometry.Size(width, stripeHeight))

        // Dynamic Sine-Wave Overlay for fluid wave simulation
        val wavePath = Path().apply {
            moveTo(0f, stripeHeight)
            val waveLength = width / 2f
            val amplitude = 8f
            var x = 0f
            while (x <= width) {
                val y = stripeHeight + (amplitude * sin((x / waveLength * 2f * Math.PI) + phase)).toFloat()
                lineTo(x, y)
                x += 10f
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = wavePath, color = Color(0x33FFFFFF)) // translucent white wave shimmer
    }
}

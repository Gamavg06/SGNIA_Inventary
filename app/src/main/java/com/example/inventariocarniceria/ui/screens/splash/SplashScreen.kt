package com.example.inventariocarniceria.ui.screens.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.inventariocarniceria.R

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val videoUri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.logovideo}")
    }

    var videoViewRef: VideoView? = null

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0D)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoViewRef = this
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.start()
                    }
                    setOnCompletionListener {
                        onSplashFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        onSplashFinished()
                        true
                    }
                }
            }
        )

        // Botón táctico "SALTAR ›" estilo píldora Noir & Crema
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF211E1B).copy(alpha = 0.85f))
                .clickable { onSplashFinished() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "SALTAR ›",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFFEEDDC8)
            )
        }
    }
}

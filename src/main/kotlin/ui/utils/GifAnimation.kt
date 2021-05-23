package ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.Codec

@Composable
fun GifAnimation(codec: Codec, modifier: Modifier) {
    val animation = remember(codec) { GifAnimation(codec) }
    LaunchedEffect(animation) {
        while (true) {
            withFrameNanos {
                animation.update(it)
            }
        }
    }
    Canvas(modifier) {
        drawIntoCanvas {
            animation.draw(it)
        }
    }
}

private class GifAnimation(private val codec: Codec) {
    private val bitmap = Bitmap().apply {
        allocPixels(codec.imageInfo)
    }

    private val frameDuration = codec.framesInfo.first().duration
    private val totalDuration = frameDuration * codec.framesInfo.size

    private var startTime = -1L
    private var frame by mutableStateOf(0)

    fun update(nanoTime: Long) {
        val milliTime = nanoTime / 1_000_000L
        if (startTime == -1L) {
            startTime = milliTime
        }
        frame = ((milliTime - startTime) % totalDuration / frameDuration).toInt()
    }

    fun draw(canvas: Canvas) {
        codec.readPixels(bitmap, frame)
        canvas.drawImage(bitmap.asImageBitmap(), Offset.Zero, Paint())
    }
}
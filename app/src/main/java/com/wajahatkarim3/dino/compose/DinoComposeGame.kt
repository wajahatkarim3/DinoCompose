package com.wajahatkarim3.dino.compose

import android.content.res.Resources
import android.graphics.DashPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.dispatch.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.whenStarted
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.wajahatkarim3.dino.compose.model.CactusState
import com.wajahatkarim3.dino.compose.model.CloudState
import com.wajahatkarim3.dino.compose.model.DinoState
import com.wajahatkarim3.dino.compose.model.EarthState

const val EARTH_Y_POSITION = 500f
private const val EARTH_GROUND_STROKE_WIDTH = 10f
private const val CLOUDS_SPEED = 1 // pixels per frame
private const val MAX_CLOUDS = 3
const val EARTH_OFFSET = 200
const val EARTH_SPEED = 10

var deviceWidthInPixels = 1920

@Composable
fun DinoGameScene()
{
    var cloudsState = remember { CloudState(maxClouds = MAX_CLOUDS, speed = CLOUDS_SPEED) }
    var earthState = remember { EarthState(maxBlocks = 2, speed = EARTH_SPEED) }
    var cactusState = remember { CactusState(cactusSpeed = EARTH_SPEED) }
    var dinoState = remember { DinoState() }
    
    val state = animationTimeMillis {
        // Game Loop
        cloudsState.moveForward()
        earthState.moveForward()
        cactusState.moveForward()
        dinoState.move()
    }

    Column(modifier = Modifier.fillMaxWidth().clickable(
        onClick = { dinoState.jump() },
        indication = null)
    ) {
        HighScoreTextViews()
        GameOverTextView(isGameOver = false)

        Canvas(modifier = Modifier.weight(1f)) {
            val millis = state.value
            EarthView(earthState)
            CloudsView(cloudsState)
            CactusView(cactusState)
            DinoView(dinoState)
        }
    }
}

fun DrawScope.DinoView(dinoState: DinoState) {
    translate(
        left = dinoState.xPos,
        top = dinoState.yPos - (dinoState.path.getBounds().height * dinoState.scale)
    ) {
        scale(scaleX = dinoState.scale, scaleY = dinoState.scale, pivotY = 0f, pivotX = 0f) {
            drawPath(
                path = DinoPath(),
                color = Color(0xFF000000),
                style = Fill
            )
        }
    }
}

fun DrawScope.CloudsView(cloudState: CloudState)
{
    cloudState.cloudsList.forEach {cloud ->
        translate(
            left = cloud.xPos.toFloat(),
            top = cloud.yPos.toFloat()
        ) {
            scale(scaleX = 4f, scaleY = 4f, pivotX = 0f, pivotY = 0f) {
                drawPath(
                    path = CloudPath(),
                    color = Color(0xFFC5C5C5),
                    style = Stroke(2f)
                )
            }
        }
    }
}

fun DrawScope.EarthView(earthState: EarthState)
{
    // Ground Line
    drawLine(
        color = Color.DarkGray,
        start = Offset(x = 0f, y = EARTH_Y_POSITION),
        end = Offset(x = deviceWidthInPixels.toFloat(), y = EARTH_Y_POSITION),
        strokeWidth = EARTH_GROUND_STROKE_WIDTH
    )

    earthState.blocksList.forEach { block ->
        drawLine(
            color = Color.DarkGray,
            start = Offset(x = block.xPos, y = EARTH_Y_POSITION + 20),
            end = Offset(x = block.size, y = EARTH_Y_POSITION + 20),
            strokeWidth = EARTH_GROUND_STROKE_WIDTH / 5,
            pathEffect = DashPathEffect(floatArrayOf(20f, 40f), 0f)
        )
        drawLine(
            color = Color.DarkGray,
            start = Offset(x = block.xPos, y = EARTH_Y_POSITION + 30),
            end = Offset(x = block.size, y = EARTH_Y_POSITION + 30),
            strokeWidth = EARTH_GROUND_STROKE_WIDTH / 5,
            pathEffect = DashPathEffect(floatArrayOf(15f, 50f), 40f)
        )
    }
}

fun DrawScope.CactusView(cactusState: CactusState)
{
    cactusState.cactusList.forEach { cactus ->

        translate(
            left = cactus.xPos.toFloat(),
            top = cactus.yPos.toFloat() - cactus.path.getBounds().height
        ) {
            drawPath(
                path = cactus.path,
                color = Color(0xFF000000),
                style = Fill
            )
        }
    }
}

@Composable
fun HighScoreTextViews()
{
    Spacer(modifier = Modifier.padding(top = 50.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = "HI")
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "00000")
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "00000")
    }
}

@Composable
fun GameOverTextView(isGameOver: Boolean = true)
{
    if (isGameOver)
    {
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Text(
            text = "GAME OVER",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            letterSpacing = 5.sp,
            style = TextStyle(
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun animationTimeMillis(gameloopCallback: () -> Unit): State<Long>
{
    val millisState = remember { mutableStateOf(0L) }
    val lifecycleOwner = LifecycleOwnerAmbient.current
    launchInComposition {
        val startTime = withFrameMillis { it }
        lifecycleOwner.whenStarted {
            while(true) {
                withFrameMillis { frameTimeMillis: Long ->
                    millisState.value = frameTimeMillis - startTime
                }
                gameloopCallback.invoke()
            }
        }
    }
    return millisState
}
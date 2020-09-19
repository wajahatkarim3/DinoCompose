package com.wajahatkarim3.dino.compose

import android.content.res.Resources
import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.dispatch.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.whenStarted
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.wajahatkarim3.dino.compose.model.*

const val EARTH_Y_POSITION = 500f
private const val EARTH_GROUND_STROKE_WIDTH = 10f
private const val CLOUDS_SPEED = 1 // pixels per frame
private const val MAX_CLOUDS = 3
const val EARTH_OFFSET = 200
const val EARTH_SPEED = 10

var deviceWidthInPixels = 1920

var showBounds = mutableStateOf(true)

@Composable
fun DinoGameScene()
{
    var cloudsState = remember { CloudState(maxClouds = MAX_CLOUDS, speed = CLOUDS_SPEED) }
    var earthState = remember { EarthState(maxBlocks = 2, speed = EARTH_SPEED) }
    var cactusState = remember { CactusState(cactusSpeed = EARTH_SPEED) }
    var dinoState = remember { DinoState() }
    var gameState = remember { GameState() }
    
    val state = animationTimeMillis {
        if (!gameState.isGameOver)
        {
            // Game Loop
            gameState.increaseScore()
            cloudsState.moveForward()
            earthState.moveForward()
            cactusState.moveForward()
            dinoState.move()

            // Collision Check
            cactusState.cactusList.forEach {
                if (dinoState.getBounds().deflate(DOUBT_FACTOR).overlaps(it.getBounds().deflate(DOUBT_FACTOR)))
                {
                    gameState.isGameOver = true
                    return@forEach
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().clickable(
        onClick = {
            if (!gameState.isGameOver)
                dinoState.jump()
            else
            {
                cactusState.initCactus()
                dinoState.init()
                gameState.replay()
            }
        },
        indication = null)
    ) {
        val millis = state.value

        ShowBoundsSwitchView()
        HighScoreTextViews(gameState)
        GameOverTextView(gameState.isGameOver)

        Canvas(modifier = Modifier.weight(1f)) {
            EarthView(earthState)
            CloudsView(cloudsState)
            DinoView(dinoState)
            CactusView(cactusState)
        }
    }
}

fun DrawScope.DinoView(dinoState: DinoState) {
    withTransform({
        translate(
            left = dinoState.xPos,
            top = dinoState.yPos - dinoState.path.getBounds().height
        )
    }) {
        drawPath(
            path = dinoState.path,
            color = Color(0xFF000000),
            style = Fill
        )
        drawBoundingBox(color = Color.Green, rect = dinoState.path.getBounds())
    }
}

fun DrawScope.CloudsView(cloudState: CloudState)
{
    cloudState.cloudsList.forEach {cloud ->
        withTransform({
            translate(
                left = cloud.xPos.toFloat(),
                top = cloud.yPos.toFloat()
            )
        })
        {
            drawPath(
                path = cloud.path,
                color = Color(0xFFC5C5C5),
                style = Stroke(2f)
            )
            drawBoundingBox(color = Color.Blue, rect = cloud.path.getBounds())
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
        withTransform({
            translate(
                left = cactus.xPos.toFloat(),
                top = cactus.yPos.toFloat() - cactus.path.getBounds().height
            )
        })
        {
            drawPath(
                path = cactus.path,
                color = Color(0xFF000000),
                style = Fill
            )
            drawBoundingBox(color = Color.Red, rect = cactus.path.getBounds())
        }
    }
}

@Composable
fun HighScoreTextViews(gameState: GameState)
{
    Spacer(modifier = Modifier.padding(top = 50.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = "HI")
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "${gameState.highScore}".padStart(5, '0'))
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "${gameState.currentScore}".padStart(5, '0'))
    }
}

@Composable
fun ShowBoundsSwitchView()
{
    Spacer(modifier = Modifier.padding(top = 20.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = "Show Bounds")
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Switch(checked = showBounds.value, onCheckedChange = {
            showBounds.value = it
        })
    }
}

@Composable
fun GameOverTextView(isGameOver: Boolean = true)
{
    Spacer(modifier = Modifier.padding(top = 20.dp))
    Text(
        text = if (isGameOver) "GAME OVER" else "",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        letterSpacing = 5.sp,
        style = TextStyle(
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (isGameOver) {
            Image(
                asset = vectorResource(id = R.drawable.ic_replay),
                modifier = Modifier.preferredSize(40.dp)
            )
        }
    }
}


fun DrawScope.drawBoundingBox(color: Color, rect: Rect, name: String? = null) {
    name?.let { Log.w("drawBounds", "$name $rect") }
    if (showBounds.value)
    {
        drawRect(color, rect.topLeft, rect.size, style = Stroke(3f))
        drawRect(color, rect.deflate(DOUBT_FACTOR).topLeft, rect.deflate(DOUBT_FACTOR).size, style = Stroke(width = 3f, pathEffect = DashPathEffect(floatArrayOf(2f, 4f), 0f)))
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

fun Rect.collided(other: Rect, doubtFactor: Float = 0f): Boolean
{
    if (right >= (other.left + doubtFactor) && right <= (other.right - doubtFactor))
        return true
//    if (right <= other.left || other.right <= left)
//        return false
//    if (bottom <= other.top || other.bottom <= top)
//        return false
    return false
}
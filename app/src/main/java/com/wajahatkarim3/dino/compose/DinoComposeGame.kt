package com.wajahatkarim3.dino.compose

import android.content.res.Resources
import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope.weight
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.dispatch.withFrameMillis
import androidx.compose.ui.Alignment
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
const val EARTH_SPEED = 9

var deviceWidthInPixels = 1920
var distanceBetweenCactus = 100

var showBounds = mutableStateOf(true)

@Composable
fun DinoGameScene()
{
    var cloudsState = remember { CloudState(maxClouds = MAX_CLOUDS, speed = CLOUDS_SPEED) }
    var earthState = remember { EarthState(maxBlocks = 2, speed = EARTH_SPEED) }
    var cactusState = remember { CactusState(cactusSpeed = EARTH_SPEED) }
    var dinoState = remember { DinoState() }
    var gameState = remember { GameState() }

    val earthColor = MaterialTheme.colors.earthColor
    val cloudsColor = MaterialTheme.colors.cloudColor
    val dinoColor = MaterialTheme.colors.dinoColor
    val cactusColor = MaterialTheme.colors.cactusColor

    val animatedProgress = animatedFloat(initVal = 0f)
    onActive {
        animatedProgress.animateTo(
            targetValue = 1f,
            anim = repeatable(
                iterations = AnimationConstants.Infinite,
                animation = tween(durationMillis = 1000, easing = LinearEasing)
            )
        )
    }

    val millis = animatedProgress.value

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

    Stack {
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
            ShowBoundsSwitchView()
            HighScoreTextViews(gameState)
            Canvas(modifier = Modifier.weight(1f)) {
                EarthView(earthState, color = earthColor)
                CloudsView(cloudsState, color = cloudsColor)
                DinoView(dinoState, color = dinoColor)
                CactusView(cactusState, color = cactusColor)
            }
        }
        GameOverTextView(gameState.isGameOver, modifier = Modifier.align(Alignment.TopCenter).padding(top = 150.dp))
    }
}

fun DrawScope.DinoView(dinoState: DinoState, color: Color) {
    withTransform({
        translate(
            left = dinoState.xPos,
            top = dinoState.yPos - dinoState.path.getBounds().height
        )
    }) {
        Log.w("Dino", "$dinoState.keyframe")
        drawPath(
            path = dinoState.path,
            color = color,
            style = Fill
        )
        drawBoundingBox(color = Color.Green, rect = dinoState.path.getBounds())
    }
}

fun DrawScope.CloudsView(cloudState: CloudState, color: Color)
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
                color = color,
                style = Stroke(2f)
            )
            drawBoundingBox(color = Color.Blue, rect = cloud.path.getBounds())
        }
    }
}

fun DrawScope.EarthView(earthState: EarthState, color: Color)
{
    // Ground Line
    drawLine(
        color = color,
        start = Offset(x = 0f, y = EARTH_Y_POSITION),
        end = Offset(x = deviceWidthInPixels.toFloat(), y = EARTH_Y_POSITION),
        strokeWidth = EARTH_GROUND_STROKE_WIDTH
    )

    earthState.blocksList.forEach { block ->
        drawLine(
            color = color,
            start = Offset(x = block.xPos, y = EARTH_Y_POSITION + 20),
            end = Offset(x = block.size, y = EARTH_Y_POSITION + 20),
            strokeWidth = EARTH_GROUND_STROKE_WIDTH / 5,
            pathEffect = DashPathEffect(floatArrayOf(20f, 40f), 0f)
        )
        drawLine(
            color = color,
            start = Offset(x = block.xPos, y = EARTH_Y_POSITION + 30),
            end = Offset(x = block.size, y = EARTH_Y_POSITION + 30),
            strokeWidth = EARTH_GROUND_STROKE_WIDTH / 5,
            pathEffect = DashPathEffect(floatArrayOf(15f, 50f), 40f)
        )
    }
}

fun DrawScope.CactusView(cactusState: CactusState, color: Color)
{
    cactusState.cactusList.forEach { cactus ->
        withTransform({
            translate(
                left = cactus.xPos.toFloat(),
                top = cactus.yPos.toFloat() - cactus.path.getBounds().height
            )
            scale(cactus.scale, cactus.scale, cactus.path.getBounds().width, cactus.path.getBounds().height)
        })
        {
            drawPath(
                path = cactus.path,
                color = color,
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
        Text(text = "HI", style = TextStyle(color = MaterialTheme.colors.highScoreColor))
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "${gameState.highScore}".padStart(5, '0'), style = TextStyle(color = MaterialTheme.colors.highScoreColor))
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Text(text = "${gameState.currentScore}".padStart(5, '0'), style = TextStyle(color = MaterialTheme.colors.currentScoreColor))
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
fun GameOverTextView(isGameOver: Boolean = true, modifier: Modifier = Modifier)
{
    Column(modifier = modifier) {
        Text(
            text = if (isGameOver) "GAME OVER" else "",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            letterSpacing = 5.sp,
            style = TextStyle(
                color = MaterialTheme.colors.gameOverColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        if (isGameOver) {
            Image(
                asset = vectorResource(id = R.drawable.ic_replay),
                modifier = Modifier.preferredSize(40.dp).padding(top = 10.dp).align(alignment = Alignment.CenterHorizontally)
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
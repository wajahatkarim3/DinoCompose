package com.wajahatkarim3.dino.compose.model

import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.wajahatkarim3.dino.compose.CactusPath
import com.wajahatkarim3.dino.compose.EARTH_SPEED
import com.wajahatkarim3.dino.compose.EARTH_Y_POSITION

data class CactusState(
    val deviceWidthInPixels: Int,
    val cactusList: ArrayList<CactusModel> = ArrayList(),
    val cactusSpeed: Int = EARTH_SPEED
) {

    private val distanceBetweenCactus = (deviceWidthInPixels * 0.4f).toInt()

    init {
        initCactus()
    }

    fun initCactus()
    {
        cactusList.clear()
        var startX = deviceWidthInPixels + 150
        var cactusCount = 3

        for (i in 0 until cactusCount) {
            var cactus = CactusModel(
                count = rand(1, 3),
                scale = rand(0.85f, 1.2f),
                xPos = startX,
                yPos = EARTH_Y_POSITION.toInt() + rand(20, 30)
            )
            Log.w("Cactus", "${cactus.xPos}")
            cactusList.add(cactus)

            startX += distanceBetweenCactus
            startX += rand(0, distanceBetweenCactus)
        }
    }

    fun moveForward()
    {
        cactusList.forEach { cactus ->
            cactus.xPos -= cactusSpeed
        }

        if (cactusList.first().xPos < -250) {
            cactusList.removeAt(0)
            var cactus = CactusModel(
                count = rand(1, 3),
                scale = rand(0.85f, 1.2f),
                xPos = nextCactusX(cactusList.last().xPos),
                yPos = EARTH_Y_POSITION.toInt() + rand(20, 30)
            )
            cactusList.add(cactus)
            Log.e("Cactus", "${cactus.xPos}")
        }
    }

    fun nextCactusX(lastX: Int): Int
    {
        var nextX = lastX + distanceBetweenCactus
        nextX += rand(0, distanceBetweenCactus)
        if (nextX < deviceWidthInPixels)
            nextX += (deviceWidthInPixels - nextX)
        return nextX
    }
}

data class CactusModel(
    val count: Int = 1,
    val scale: Float = 1f,
    var xPos: Int = 0,
    var yPos: Int = 0,
    var path: Path = CactusPath()
) {

    fun getBounds() : Rect
    {
        return Rect(
            left = xPos.toFloat(),
            top = yPos.toFloat() - (path.getBounds().height * scale),
            right = xPos + (path.getBounds().width * scale),
            bottom = yPos.toFloat()
        )
    }
}
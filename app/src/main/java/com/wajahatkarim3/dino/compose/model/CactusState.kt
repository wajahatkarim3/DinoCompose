package com.wajahatkarim3.dino.compose.model

import android.graphics.Matrix
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import com.wajahatkarim3.dino.compose.CactusPath
import com.wajahatkarim3.dino.compose.EARTH_SPEED
import com.wajahatkarim3.dino.compose.EARTH_Y_POSITION
import com.wajahatkarim3.dino.compose.deviceWidthInPixels

data class CactusState(
    val cactusList: ArrayList<CactusModel> = ArrayList(),
    val cactusSpeed: Int = EARTH_SPEED
) {
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
            cactusList.add(cactus)

            startX += (deviceWidthInPixels/3 + rand(10, 200))
        }
    }

    fun moveForward()
    {
        cactusList.forEach { cactus ->
            cactus.xPos -= cactusSpeed

            if (cactus.xPos < -250) {
                cactus.xPos = rand(deviceWidthInPixels, deviceWidthInPixels * rand(1,2))
            }
        }
    }
}

data class CactusModel(
    val count: Int = 1,
    val scale: Float = 1f,
    var xPos: Int = 0,
    var yPos: Int = 0,
    var path: Path = CactusPath()
) {
    init {
        var path = CactusPath()
        var scaleMatrix = Matrix()
        scaleMatrix.setScale(scale * 0.25f, scale * 0.25f)
        var androidPath = path.asAndroidPath()
        androidPath.transform(scaleMatrix)
        this.path = androidPath.asComposePath()
    }

    fun getBounds() : Rect
    {
        return Rect(
            left = xPos.toFloat(),
            top = yPos.toFloat() - path.getBounds().height,
            right = xPos + path.getBounds().width,
            bottom = yPos.toFloat()
        )
    }
}
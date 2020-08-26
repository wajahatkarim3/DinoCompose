package com.wajahatkarim3.dino.compose.model

import androidx.compose.ui.graphics.Path
import com.wajahatkarim3.dino.compose.DinoPath
import com.wajahatkarim3.dino.compose.EARTH_Y_POSITION
import com.wajahatkarim3.dino.compose.deviceWidthInPixels

data class DinoState(
    var xPos: Float = 60f,
    var yPos: Float = EARTH_Y_POSITION,
    var velocityY: Float = 0f,
    var gravity: Float = 0f,
    var scale: Float = 0.4f,
    var path: Path = DinoPath()
)
{
    fun move()
    {
        yPos += velocityY
        velocityY += gravity

        if (yPos > EARTH_Y_POSITION)
        {
            yPos = EARTH_Y_POSITION
            gravity = 0f
            velocityY = 0f
        }
    }

    fun jump()
    {
        if (yPos == EARTH_Y_POSITION)
        {
            velocityY = -40f
            gravity = 3f
        }
    }
}
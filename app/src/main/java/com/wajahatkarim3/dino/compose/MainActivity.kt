package com.wajahatkarim3.dino.compose

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wajahatkarim3.dino.compose.model.GameState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var deviceMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(deviceMetrics)
        deviceWidthInPixels = deviceMetrics.widthPixels
        distanceBetweenCactus = (deviceWidthInPixels * 0.4f).toInt()

        setContent {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkThemeColors else lightThemeColors
            ) {
                DinoGameScene(GameState())
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
    Spacer(modifier = Modifier.padding(start = 20.dp))
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme {
        Greeting("Android")
    }
}
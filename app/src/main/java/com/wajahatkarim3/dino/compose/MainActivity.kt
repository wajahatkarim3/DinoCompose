package com.wajahatkarim3.dino.compose

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var deviceMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(deviceMetrics)
        deviceWidthInPixels = deviceMetrics.widthPixels
        distanceBetweenCactus = (deviceWidthInPixels * 0.4f).toInt()

        setContent {
            MaterialTheme {
                DinoGameScene()
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
package com.hamo.doortech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hamo.doortech.ui.HamoRoot
import com.hamo.doortech.ui.theme.HamoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HamoApplication).container
        setContent {
            HamoTheme {
                HamoRoot(container)
            }
        }
    }
}

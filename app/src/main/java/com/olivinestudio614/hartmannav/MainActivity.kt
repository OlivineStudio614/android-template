package com.olivinestudio614.hartmannav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.olivinestudio614.hartmannav.ui.theme.HartmanNavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HartmanNavTheme {
                Text("SGT. NAV — STAND BY")
            }
        }
    }
}

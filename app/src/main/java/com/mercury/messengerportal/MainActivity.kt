package com.mercury.messengerportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mercury.messengerportal.ui.navigation.MercuryNavGraph
import com.mercury.messengerportal.ui.theme.MercuryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MercuryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MercuryNavGraph()
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainWhatsAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WhatsAppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WhatsAppViewModel = viewModel()
            val darkModePref by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = darkModePref ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainWhatsAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

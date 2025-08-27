package com.example.practicaltrainingproject


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practicaltrainingproject.presentation.ui.mainScreen.MainScreen
import com.example.practicaltrainingproject.presentation.ui.theme.PracticalTrainingProjectTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticalTrainingProjectTheme {
                MainScreen()
            }
        }
    }
}

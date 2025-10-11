package com.sujalkumar.knockme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sujalkumar.knockme.navigation.NavigationRoot
import com.sujalkumar.knockme.ui.theme.KnockMeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KnockMeTheme {
                NavigationRoot()
            }
        }
    }
}
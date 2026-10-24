package com.dresso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dresso.app.ui.theme.DressoTheme
import com.dresso.app.viewmodel.DressoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DressoViewModel by viewModels {
        DressoViewModel.factory((application as DressoApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            DressoTheme {
                DressoApp(
                    viewModel = viewModel,
                    container = (application as DressoApplication).container,
                )
            }
        }
    }
}

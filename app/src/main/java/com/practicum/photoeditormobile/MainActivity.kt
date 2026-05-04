package com.practicum.photoeditormobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.practicum.photoeditormobile.boards.BoardsViewModel
import com.practicum.photoeditormobile.data.ThemeMode
import com.practicum.photoeditormobile.data.ThemePreferences
import com.practicum.photoeditormobile.ui.screens.PhotoEditorAppRoot
import com.practicum.photoeditormobile.ui.theme.PhotoEditorMobileTheme
import com.practicum.photoeditormobile.viewmodel.PhotoEditorViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val themePreferences = remember { ThemePreferences(context.applicationContext) }
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val navController = rememberNavController()

            val photoEditorViewModel: PhotoEditorViewModel = viewModel()
            val boardsViewModel: BoardsViewModel = viewModel()

            PhotoEditorMobileTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                },
                dynamicColor = false
            ) {
                PhotoEditorAppRoot(
                    navController = navController,
                    photoEditorViewModel = photoEditorViewModel,
                    boardsViewModel = boardsViewModel,
                    themeMode = themeMode,
                    onThemeModeChange = { newMode ->
                        scope.launch {
                            themePreferences.setThemeMode(newMode)
                        }
                    }
                )
            }
        }
    }
}

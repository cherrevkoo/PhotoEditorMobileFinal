package com.practicum.photoeditormobile.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.practicum.photoeditormobile.ui.dinamicInterfaces.BoardDetailScreen
import com.practicum.photoeditormobile.ui.dinamicInterfaces.BoardsScreen
import com.practicum.photoeditormobile.boards.BoardsViewModel
import com.practicum.photoeditormobile.data.ThemeMode
import com.practicum.photoeditormobile.ui.dinamicInterfaces.GalleryGridScreen
import com.practicum.photoeditormobile.ui.dinamicInterfaces.SwipeIdeasScreen
import com.practicum.photoeditormobile.ui.dinamicInterfaces.defaultSwipeDeckCards
import com.practicum.photoeditormobile.viewmodel.PhotoEditorViewModel

internal object AppDestinations {
    const val EDITOR = "editor"
    const val IDEAS = "ideas"
    const val FEED = "feed"
    const val BOARDS = "boards"
    const val BOARD_DETAIL = "board_detail/{boardId}"

    fun boardDetailRoute(boardId: String) = "board_detail/$boardId"
}

@Composable
fun PhotoEditorAppRoot(
    navController: NavHostController,
    photoEditorViewModel: PhotoEditorViewModel,
    boardsViewModel: BoardsViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    LaunchedEffect(selectedUri) {
        selectedUri?.let { uri ->
            photoEditorViewModel.loadImage(uri)
            selectedUri = null
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = !currentRoute.startsWith("board_detail")

    val boardsTabSelected =
        currentRoute == AppDestinations.BOARDS || currentRoute.startsWith("board_detail")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == AppDestinations.EDITOR,
                        onClick = {
                            navController.navigate(AppDestinations.EDITOR) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        label = { Text("Редактор") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppDestinations.IDEAS,
                        onClick = {
                            navController.navigate(AppDestinations.IDEAS) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Style, contentDescription = null) },
                        label = { Text("Подборка") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppDestinations.FEED,
                        onClick = {
                            navController.navigate(AppDestinations.FEED) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                        label = { Text("Лента") }
                    )
                    NavigationBarItem(
                        selected = boardsTabSelected,
                        onClick = {
                            if (currentRoute.startsWith("board_detail")) {
                                navController.popBackStack(AppDestinations.BOARDS, inclusive = false)
                            } else {
                                navController.navigate(AppDestinations.BOARDS) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Collections, contentDescription = null) },
                        label = { Text("Доски") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.EDITOR,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppDestinations.EDITOR) {
                PhotoEditorScreen(
                    viewModel = photoEditorViewModel,
                    onImageSelected = { galleryLauncher.launch("image/*") },
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )
            }
            composable(AppDestinations.IDEAS) {
                SwipeIdeasScreen(
                    cards = defaultSwipeDeckCards(),
                    boardsViewModel = boardsViewModel,
                    onOpenBoards = {
                        navController.navigate(AppDestinations.BOARDS) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(AppDestinations.FEED) {
                val deck = defaultSwipeDeckCards()
                GalleryGridScreen(
                    imageUrls = deck.map { it.image },
                    imageTitles = deck.map { it.title }
                )
            }
            composable(AppDestinations.BOARDS) {
                BoardsScreen(
                    viewModel = boardsViewModel,
                    onBack = {},
                    onOpenBoard = { board ->
                        navController.navigate(AppDestinations.boardDetailRoute(board.id))
                    },
                    showTopBarBack = false
                )
            }
            composable(
                route = AppDestinations.BOARD_DETAIL,
                arguments = listOf(
                    navArgument("boardId") { type = NavType.StringType }
                )
            ) { entry ->
                val boardId = entry.arguments?.getString("boardId").orEmpty()
                if (boardId.isNotEmpty()) {
                    BoardDetailScreen(
                        boardId = boardId,
                        boardName = "",
                        viewModel = boardsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

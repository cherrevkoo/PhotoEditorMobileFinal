package com.practicum.photoeditormobile.ui.dinamicInterfaces

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.practicum.photoeditormobile.boards.Board
import com.practicum.photoeditormobile.boards.BoardsViewModel
import com.practicum.photoeditormobile.boards.SavedIdea
import com.practicum.photoeditormobile.ui.theme.PhotoEditorMobileTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SaveToBoardBottomSheet(
    card: Card,
    boards: List<Board>,
    onDismiss: () -> Unit,
    onSaveToExistingBoard: (Board) -> Unit,
    onCreateBoardAndSave: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNewBoardDialog by remember { mutableStateOf(false) }
    var newBoardName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Сохранить в доску",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            GlideImage(
                model = card.image,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))

            if (boards.isEmpty()) {
                Text(
                    "Пока нет досок. Создайте первую.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                boards.forEach { board ->
                    ListItem(
                        headlineContent = { Text(board.name) },
                        supportingContent = { Text("${board.ideas.size} идей") },
                        leadingContent = { BoardMiniStrip(ideas = board.ideas.take(3)) },
                        modifier = Modifier.clickable { onSaveToExistingBoard(board) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showNewBoardDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Создать новую доску")
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showNewBoardDialog) {
        AlertDialog(
            onDismissRequest = { showNewBoardDialog = false },
            title = { Text("Название доски") },
            text = {
                OutlinedTextField(
                    value = newBoardName,
                    onValueChange = { newBoardName = it },
                    singleLine = true,
                    placeholder = { Text("Например: Вдохновение") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newBoardName.trim().ifBlank { "Новая доска" }
                        onCreateBoardAndSave(name)
                        showNewBoardDialog = false
                        newBoardName = ""
                    }
                ) { Text("Создать и сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showNewBoardDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun BoardMiniStrip(ideas: List<SavedIdea>) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { idx ->
            val idea = ideas.getOrNull(idx)
            if (idea != null) {
                GlideImage(
                    model = idea.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
internal fun BoardsScreenLayout(
    boards: List<Board>,
    onBack: () -> Unit,
    onBoardClick: (Board) -> Unit,
    showTopBarBack: Boolean = true,
    showCreateDialog: Boolean,
    onDismissCreateDialog: () -> Unit,
    newBoardName: String,
    onNewBoardNameChange: (String) -> Unit,
    onConfirmCreateBoard: () -> Unit,
    onFabClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Доски") },
                navigationIcon = {
                    if (showTopBarBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Add, contentDescription = "Создать доску")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = boards,
                key = { it.id }
            ) { board ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                        .clickable { onBoardClick(board) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            board.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val previews = board.ideas.take(4)
                            if (previews.isEmpty()) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Пусто", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                previews.forEach { idea ->
                                    GlideImage(
                                        model = idea.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                repeat((4 - previews.size).coerceAtLeast(0)) {
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = onDismissCreateDialog,
            title = { Text("Новая доска") },
            text = {
                OutlinedTextField(
                    value = newBoardName,
                    onValueChange = onNewBoardNameChange,
                    singleLine = true,
                    placeholder = { Text("Название") }
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmCreateBoard) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = onDismissCreateDialog) { Text("Отмена") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun BoardsScreen(
    viewModel: BoardsViewModel,
    onBack: () -> Unit,
    onOpenBoard: (Board) -> Unit,
    showTopBarBack: Boolean = true
) {
    var boards by remember { mutableStateOf<List<Board>>(emptyList()) }
    LaunchedEffect(Unit) {
        viewModel.boards.collect { boards = it }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    BoardsScreenLayout(
        boards = boards,
        onBack = onBack,
        onBoardClick = onOpenBoard,
        showTopBarBack = showTopBarBack,
        showCreateDialog = showCreateDialog,
        onDismissCreateDialog = { showCreateDialog = false },
        newBoardName = newName,
        onNewBoardNameChange = { newName = it },
        onConfirmCreateBoard = {
            val n = newName.trim().ifBlank { "Новая доска" }
            viewModel.createBoard(n)
            showCreateDialog = false
            newName = ""
        },
        onFabClick = { showCreateDialog = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
internal fun BoardDetailScreenLayout(
    titleInTopBar: String,
    board: Board?,
    missingBoardCenterMessage: String,
    onBack: () -> Unit,
    onDeleteIdea: (String) -> Unit,
    detailIdea: SavedIdea?,
    onDismissDetail: () -> Unit,
    onIdeaClick: (SavedIdea) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleInTopBar) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            board == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        missingBoardCenterMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = board.ideas,
                        key = { it.id }
                    ) { idea ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement(
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                                .clickable { onIdeaClick(idea) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box {
                                GlideImage(
                                    model = idea.imageUrl,
                                    contentDescription = idea.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.85f),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onDeleteIdea(idea.id) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    detailIdea?.let { idea ->
        AlertDialog(
            onDismissRequest = onDismissDetail,
            confirmButton = {
                TextButton(onClick = onDismissDetail) { Text("Закрыть") }
            },
            title = { Text(idea.title) },
            text = {
                GlideImage(
                    model = idea.imageUrl,
                    contentDescription = idea.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun BoardDetailScreen(
    boardId: String,
    boardName: String,
    viewModel: BoardsViewModel,
    onBack: () -> Unit
) {
    var boards by remember { mutableStateOf<List<Board>>(emptyList()) }
    LaunchedEffect(Unit) {
        viewModel.boards.collect { boards = it }
    }
    val board = boards.find { it.id == boardId }

    var detailIdea by remember { mutableStateOf<SavedIdea?>(null) }

    val titleInTopBar = board?.name ?: boardName
    val missingMessage =
        if (boards.isEmpty()) "Загрузка…" else "Доска не найдена"

    BoardDetailScreenLayout(
        titleInTopBar = titleInTopBar,
        board = board,
        missingBoardCenterMessage = missingMessage,
        onBack = onBack,
        onDeleteIdea = { ideaId -> viewModel.removeIdeaFromBoard(boardId, ideaId) },
        detailIdea = detailIdea,
        onDismissDetail = { detailIdea = null },
        onIdeaClick = { detailIdea = it }
    )
}

private val previewSavedIdeaA = _root_ide_package_.com.practicum.photoeditormobile.boards.SavedIdea(
    id = "idea-a",
    title = "Референс A",
    imageUrl = "https://picsum.photos/seed/pebrd1/400/500",
    savedAtMillis = 0L
)
private val previewSavedIdeaB = _root_ide_package_.com.practicum.photoeditormobile.boards.SavedIdea(
    id = "idea-b",
    title = "Референс B",
    imageUrl = "https://picsum.photos/seed/pebrd2/400/500",
    savedAtMillis = 0L
)

private val previewBoardsForGrid = listOf(
    Board(
        id = "board-preview-1",
        name = "Мудборд",
        ideas = listOf(previewSavedIdeaA, previewSavedIdeaB)
    ),
    Board(
        id = "board-preview-2",
        name = "Пустая доска",
        ideas = emptyList()
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Доски — сетка")
@Composable
private fun BoardsScreenPreview() {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    PhotoEditorMobileTheme(darkTheme = false) {
        BoardsScreenLayout(
            boards = previewBoardsForGrid,
            onBack = {},
            onBoardClick = {},
            showTopBarBack = false,
            showCreateDialog = showCreate,
            onDismissCreateDialog = { showCreate = false },
            newBoardName = newName,
            onNewBoardNameChange = { newName = it },
            onConfirmCreateBoard = { showCreate = false; newName = "" },
            onFabClick = { showCreate = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Доска — детали")
@Composable
private fun BoardDetailScreenPreview() {
    var detailIdea by remember { mutableStateOf<SavedIdea?>(null) }
    val b = previewBoardsForGrid[0]
    PhotoEditorMobileTheme(darkTheme = false) {
        BoardDetailScreenLayout(
            titleInTopBar = b.name,
            board = b,
            missingBoardCenterMessage = "Загрузка…",
            onBack = {},
            onDeleteIdea = {},
            detailIdea = detailIdea,
            onDismissDetail = { detailIdea = null },
            onIdeaClick = { detailIdea = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Доска — загрузка / не найдена")
@Composable
private fun BoardDetailScreenEmptyPreview() {
    PhotoEditorMobileTheme(darkTheme = false) {
        BoardDetailScreenLayout(
            titleInTopBar = "Новая доска",
            board = null,
            missingBoardCenterMessage = "Загрузка…",
            onBack = {},
            onDeleteIdea = {},
            detailIdea = null,
            onDismissDetail = {},
            onIdeaClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Сохранить в доску — sheet")
@Composable
private fun SaveToBoardBottomSheetPreview() {
    val sampleCard = Card(
        title = "Идея для превью",
        image = "https://picsum.photos/seed/saveboard/200/200"
    )
    PhotoEditorMobileTheme(darkTheme = false) {
        Box(Modifier.fillMaxSize()) {
            SaveToBoardBottomSheet(
                card = sampleCard,
                boards = previewBoardsForGrid,
                onDismiss = {},
                onSaveToExistingBoard = {},
                onCreateBoardAndSave = {}
            )
        }
    }
}

package com.practicum.photoeditormobile.ui.dinamicInterfaces

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp as lerpFloat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.practicum.photoeditormobile.R
import com.practicum.photoeditormobile.boards.Board
import com.practicum.photoeditormobile.boards.BoardsViewModel
import com.practicum.photoeditormobile.boards.SavedIdea
import kotlinx.coroutines.delay

private val SwipeThresholdDp = 120.dp
private val MaxRotationDeg = 20f

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun IdeaCardView(
    card: Card,
    offsetX: Float,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 120),
        label = "cardOffsetX"
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 120),
        label = "cardRotation"
    )

    MaterialCard(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .height(420.dp)
            .padding(16.dp)
            .graphicsLayer {
                translationX = animatedOffsetX
                rotationZ = animatedRotation
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlideImage(
                model = card.image,
                contentDescription = card.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color(0xFF3F51B5))
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = card.title,
                    color = Color.White
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SwipeIdeasScreen(
    cards: List<Card>,
    boardsViewModel: BoardsViewModel? = null,
    onOpenBoards: () -> Unit = {}
) {
    var currentIndex by remember { mutableStateOf(0) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { SwipeThresholdDp.toPx() } }

    var boards by remember { mutableStateOf<List<Board>>(emptyList()) }
    LaunchedEffect(boardsViewModel) {
        if (boardsViewModel == null) return@LaunchedEffect
        boardsViewModel.boards.collect { boards = it }
    }

    var sheetCard by remember { mutableStateOf<Card?>(null) }

    var flyCard by remember { mutableStateOf<Card?>(null) }
    var flyProgress by remember { mutableFloatStateOf(0f) }
    var flyRunning by remember { mutableStateOf(false) }

    LaunchedEffect(flyRunning) {
        if (!flyRunning || flyCard == null) return@LaunchedEffect
        flyProgress = 0f
        val steps = 28
        repeat(steps) { i ->
            flyProgress = (i + 1) / steps.toFloat()
            delay(16)
        }
        currentIndex++
        flyCard = null
        flyProgress = 0f
        flyRunning = false
    }

    var savedToast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(savedToast) {
        if (savedToast != null) {
            delay(1200)
            savedToast = null
        }
    }

    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Нет ссылок на фото",
                fontFamily = FontFamily(Font(R.font.display_medium)),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
        }
        return
    }

    if (currentIndex >= cards.size) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Карточки закончились :(",
                fontFamily = FontFamily(Font(R.font.display_medium)),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
        }
        return
    }

    val card = cards[currentIndex]

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val thumb = 72.dp
        val thumbPx = with(density) { thumb.toPx() }
        val startX = maxWidth / 2 - thumb / 2
        val startY = maxHeight / 2 - thumb / 2
        val endX = maxWidth - thumb - 24.dp
        val endY = 72.dp + 8.dp
        val flyXDp = lerp(startX, endX, flyProgress)
        val flyYDp = lerp(startY, endY, flyProgress)

        if (boardsViewModel != null) {
            FloatingActionButton(
                onClick = onOpenBoards,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Collections, contentDescription = "Мои доски")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentIndex, cards.size) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                            rotation = (offsetX / 25f).coerceIn(-MaxRotationDeg, MaxRotationDeg)
                        },
                        onDragCancel = {
                            offsetX = 0f
                            rotation = 0f
                        },
                        onDragEnd = {
                            when {
                                offsetX > thresholdPx && boardsViewModel != null -> {
                                    sheetCard = card
                                    offsetX = 0f
                                    rotation = 0f
                                }
                                offsetX > thresholdPx -> {
                                    currentIndex++
                                    offsetX = 0f
                                    rotation = 0f
                                }
                                offsetX < -thresholdPx -> {
                                    currentIndex++
                                    offsetX = 0f
                                    rotation = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                    rotation = 0f
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            IdeaCardView(card = card, offsetX = offsetX, rotation = rotation)

            if (offsetX > 24f) {
                Text(
                    text = "Нравится",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(32.dp)
                        .background(Color(0xFF8BC34A))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.display_medium)),
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (offsetX < -24f) {
                Text(
                    text = "Не нравится",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(32.dp)
                        .background(Color(0xFFE91E63))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.display_medium)),
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        flyCard?.let { fc ->
            GlideImage(
                model = fc.image,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = flyXDp, y = flyYDp)
                    .size(thumb)
                    .clip(RoundedCornerShape(12.dp))
                    .graphicsLayer {
                        alpha = 1f - flyProgress * 0.15f
                        scaleX = lerpFloat(1f, 0.55f, flyProgress)
                        scaleY = lerpFloat(1f, 0.55f, flyProgress)
                    },
                contentScale = ContentScale.Crop
            )
        }

        AnimatedVisibility(
            visible = savedToast != null,
            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.92f),
            exit = fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = savedToast.orEmpty(),
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .background(
                        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    val sc = sheetCard
    if (sc != null && boardsViewModel != null) {
        SaveToBoardBottomSheet(
            card = sc,
            boards = boards,
            onDismiss = { sheetCard = null },
            onSaveToExistingBoard = { board ->
                val toSave = sc
                sheetCard = null
                boardsViewModel.addIdeaToBoard(board.id, SavedIdea.fromCard(toSave)) {
                    flyCard = toSave
                    flyRunning = true
                    savedToast = "Сохранено: «${board.name}»"
                }
            },
            onCreateBoardAndSave = { name ->
                val toSave = sc
                sheetCard = null
                boardsViewModel.createBoardAndAddIdea(name, toSave) { newBoard ->
                    flyCard = toSave
                    flyRunning = true
                    savedToast = "Новая доска «${newBoard.name}»"
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Карточка идеи")
@Composable
private fun IdeaCardViewPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            IdeaCardView(
                card = defaultSwipeDeckCards().first(),
                offsetX = 48f,
                rotation = 10f
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "Свайп идей")
@Composable
private fun SwipeIdeasPreview() {
    MaterialTheme {
        SwipeIdeasScreen(defaultSwipeDeckCards())
    }
}

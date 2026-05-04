package com.practicum.photoeditormobile.boards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.photoeditormobile.ui.dinamicInterfaces.Card
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardsRepository(application.applicationContext)

    val boards: StateFlow<List<Board>> = repository.boardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createBoard(name: String, onDone: (Board) -> Unit = {}) {
        viewModelScope.launch {
            val b = repository.createBoard(name)
            onDone(b)
        }
    }

    fun addIdeaToBoard(boardId: String, idea: SavedIdea, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addIdeaToBoard(boardId, idea)
            onDone()
        }
    }

    fun createBoardAndAddIdea(name: String, card: Card, onDone: (Board) -> Unit = {}) {
        viewModelScope.launch {
            val idea = SavedIdea.fromCard(card)
            val b = repository.createBoardAndAddIdea(name, idea)
            onDone(b)
        }
    }

    fun removeIdeaFromBoard(boardId: String, ideaId: String) {
        viewModelScope.launch {
            repository.removeIdeaFromBoard(boardId, ideaId)
        }
    }
}

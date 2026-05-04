package com.practicum.photoeditormobile.boards

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.boardsPreferencesDataStore by preferencesDataStore(name = "idea_boards")

class BoardsRepository(private val appContext: Context) {

    private val dataStore = appContext.boardsPreferencesDataStore

    private object Keys {
        val BOARDS_JSON: Preferences.Key<String> = stringPreferencesKey("boards_json_v1")
    }

    val boardsFlow: Flow<List<Board>> = dataStore.data.map { prefs ->
        boardsFromJson(prefs[Keys.BOARDS_JSON])
    }

    suspend fun replaceAll(boards: List<Board>) {
        dataStore.edit { prefs ->
            prefs[Keys.BOARDS_JSON] = boardsToJson(boards)
        }
    }

    suspend fun createBoard(name: String): Board {
        val trimmed = name.trim().ifBlank { "Новая доска" }
        var created: Board? = null
        dataStore.edit { prefs ->
            val current = boardsFromJson(prefs[Keys.BOARDS_JSON]).toMutableList()
            val board = Board(id = UUID.randomUUID().toString(), name = trimmed)
            current.add(0, board)
            created = board
            prefs[Keys.BOARDS_JSON] = boardsToJson(current)
        }
        return created!!
    }

    suspend fun addIdeaToBoard(boardId: String, idea: SavedIdea) {
        dataStore.edit { prefs ->
            val current = boardsFromJson(prefs[Keys.BOARDS_JSON]).toMutableList()
            val idx = current.indexOfFirst { it.id == boardId }
            if (idx < 0) return@edit
            val b = current[idx]
            if (b.ideas.any { it.imageUrl == idea.imageUrl }) return@edit
            current[idx] = b.copy(ideas = listOf(idea) + b.ideas)
            prefs[Keys.BOARDS_JSON] = boardsToJson(current)
        }
    }

    suspend fun createBoardAndAddIdea(name: String, idea: SavedIdea): Board {
        val board = createBoard(name)
        addIdeaToBoard(board.id, idea)
        return board.copy(ideas = listOf(idea))
    }

    suspend fun removeIdeaFromBoard(boardId: String, ideaId: String) {
        dataStore.edit { prefs ->
            val current = boardsFromJson(prefs[Keys.BOARDS_JSON]).toMutableList()
            val idx = current.indexOfFirst { it.id == boardId }
            if (idx < 0) return@edit
            val b = current[idx]
            current[idx] = b.copy(ideas = b.ideas.filter { it.id != ideaId })
            prefs[Keys.BOARDS_JSON] = boardsToJson(current)
        }
    }
}

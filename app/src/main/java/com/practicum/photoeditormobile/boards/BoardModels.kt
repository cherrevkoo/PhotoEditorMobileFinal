package com.practicum.photoeditormobile.boards

import com.practicum.photoeditormobile.ui.dinamicInterfaces.Card
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedIdea(
    val id: String,
    val title: String,
    val imageUrl: String,
    val savedAtMillis: Long
) {
    companion object {
        fun fromCard(card: Card): SavedIdea = SavedIdea(
            id = UUID.randomUUID().toString(),
            title = card.title,
            imageUrl = card.image,
            savedAtMillis = System.currentTimeMillis()
        )

        fun fromJson(o: JSONObject): SavedIdea = SavedIdea(
            id = o.getString("id"),
            title = o.getString("title"),
            imageUrl = o.getString("imageUrl"),
            savedAtMillis = o.optLong("savedAtMillis", 0L)
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("imageUrl", imageUrl)
        put("savedAtMillis", savedAtMillis)
    }
}

data class Board(
    val id: String,
    val name: String,
    val ideas: List<SavedIdea> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("ideas", JSONArray().apply { ideas.forEach { put(it.toJson()) } })
    }

    companion object {
        fun fromJson(o: JSONObject): Board = Board(
            id = o.getString("id"),
            name = o.getString("name"),
            ideas = buildList {
                val arr = o.optJSONArray("ideas") ?: return@buildList
                for (i in 0 until arr.length()) {
                    add(SavedIdea.fromJson(arr.getJSONObject(i)))
                }
            }
        )
    }
}

internal fun boardsFromJson(json: String?): List<Board> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                add(Board.fromJson(arr.getJSONObject(i)))
            }
        }
    }.getOrElse { emptyList() }
}

internal fun boardsToJson(boards: List<Board>): String {
    val arr = JSONArray()
    boards.forEach { arr.put(it.toJson()) }
    return arr.toString()
}

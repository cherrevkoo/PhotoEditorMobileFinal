package com.practicum.photoeditormobile.ui.dinamicInterfaces

data class Card(
    val title: String,
    val image: String
)

val SwipeDeckImageUrls: List<String> = listOf(
    "https://www.nvcdn.memify.ru/media/qxP2wMZ0Li_bkmoZUzcaew/20260407/5382197856095114647.jpg",
    "https://avatars.mds.yandex.net/i?id=09ceeba48877bfe21148aff60d60484950418821-5515210-images-thumbs&n=13",
    "https://i.pinimg.com/474x/d9/a0/50/d9a050e445dc39d9b3d7a54afbb090cc.jpg",
    "https://i.pinimg.com/1200x/6f/b7/26/6fb726d46f5894ed0c67399b8b42f4c0.jpg",
    "https://i.pinimg.com/736x/0e/e7/70/0ee77083c9fb0eac21ab79df64caad82.jpg",
    "https://i.pinimg.com/736x/3d/d5/4f/3dd54f21ac56490ff0445713bc29ec0c.jpg",
    "https://i.pinimg.com/1200x/90/59/25/905925d127e0461af5a22a3eff64ac05.jpg"
)

fun cardsFromImageUrls(
    urls: List<String>,
    titlePrefix: String
): List<Card> = urls.mapIndexed { index, url ->
    Card(
        title = "$titlePrefix ${index + 1}",
        image = url.trim()
    )
}

fun defaultSwipeDeckCards(): List<Card> =
    cardsFromImageUrls(SwipeDeckImageUrls, titlePrefix = "Фото")
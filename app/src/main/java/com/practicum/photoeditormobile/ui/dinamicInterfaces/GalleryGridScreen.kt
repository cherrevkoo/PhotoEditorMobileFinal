package com.practicum.photoeditormobile.ui.dinamicInterfaces

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.practicum.photoeditormobile.ui.theme.PhotoEditorMobileTheme

@SuppressLint("ShowToast")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryGridScreen(
    imageUrls: List<String>,
    imageTitles: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = imageUrls,
            key = { index, url -> "$index:$url" }
        ) { index, url ->
            val title = imageTitles.getOrNull(index)?.takeIf { it.isNotBlank() }
                ?: "Изображение ${index + 1}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, title, Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                GlideImage(
                    model = url,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Preview(showBackground = true, name = "GalleryGrid")
@Composable
private fun GalleryGridScreenPreview() {
    PhotoEditorMobileTheme(darkTheme = false) {
        GalleryGridScreen(
            imageUrls = listOf(
                "https://picsum.photos/seed/g1/400/400",
                "https://picsum.photos/seed/g2/400/400",
                "https://picsum.photos/seed/g3/400/400",
                "https://picsum.photos/seed/g4/400/400",
                "https://picsum.photos/seed/g5/400/400",
                "https://picsum.photos/seed/g6/400/400"
            ),
            imageTitles = listOf(
                "Рассвет",
                "Лес",
                "Город",
                "Море",
                "Пустыня",
                "Снег"
            )
        )
    }
}

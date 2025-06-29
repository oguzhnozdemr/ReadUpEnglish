package com.oguzhanozdemir.readupenglish.view

import android.service.autofill.OnClickAction
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.oguzhanozdemir.readupenglish.model.Author
import com.oguzhanozdemir.readupenglish.model.Book
import com.oguzhanozdemir.readupenglish.viewmodel.BookViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    navController: NavController,
    viewModel: BookViewModel = hiltViewModel()
) {
    val books = viewModel.books
    val isLoading = viewModel.isLoading
    val snackbarMessage: String? by viewModel.snackbarMessage // Türü netleştir

    val snackbarHostState = remember { SnackbarHostState() }
    val allBookmarks by produceState<List<BookmarkEntity>>(initialValue = emptyList()) {
        viewModel.repository.getAllBookmarks().collect {
            value = it
        }
    }

    var selectedFilter by remember {mutableStateOf("Mystery")}
    val filterOption = listOf("Mystery","Historical Fiction","Horror","Science Fiction","Children's Literature","Best Books Ever Listings","Fantasy","Adventure")

    // Snackbar'ı sadece yeni mesaj varsa göster
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(
                message = it
            )
            viewModel.clearSnackbarMessage() // Mesajı sıfırla
        }
    }

    Scaffold(

        topBar = { TopAppBar(title = { Text("ReadUp English") }) },
        snackbarHost = { SnackbarHost(snackbarHostState)

        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            // 🔽 Chip grubu
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOption) { option ->
                    FilterChip(
                        selected = selectedFilter == option,
                        onClick = {
                            selectedFilter = option
                            viewModel.loadBooks(option.toString())

                        },
                        label = { Text(option) }
                    )
                }
            }


            if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {


                // 🔽 Kitap listesi
                LazyColumn {
                    items(books) { book ->
                        val bookmark = allBookmarks.find { it.bookId == book.id }

                        BookItem(
                            book = book,
                            bookmark = bookmark,
                            onClick = { navController.navigate("book_detail/${book.id}") },
                            onFavoriteClick = { viewModel.saveFavorite(book) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    bookmark: BookmarkEntity? = null  // Bookmark bilgisini ekle
) {
    val imageUrl = book.formats["image/jpeg"]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = book.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
            )
        }

        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = book.authors.joinToString { author ->
                    val parts = author.name.split(",").map{it.trim()}
                    if (parts.size == 2) "${parts[1]} ${parts[0]}" else author.name
                },
                style = MaterialTheme.typography.bodySmall
            )
            // İlerleme göstergesi
            bookmark?.let {
                Spacer(modifier = Modifier.height(4.dp))

                // İlerleme çubuğu
                LinearProgressIndicator(
                    progress = it.percentage, // ✔️ Doğru tür: Float
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                // Son okuma zamanı
                val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                val lastReadDate = remember(it.lastReadTimestamp) {
                    dateFormat.format(Date(it.lastReadTimestamp))
                }

                Text(
                    text = "Son okuma: $lastReadDate · %${(it.percentage * 100).toInt()} tamamlandı",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "⭐", // Basit bir favori ikonu
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onFavoriteClick()
                    }
            )
        }
    }
}


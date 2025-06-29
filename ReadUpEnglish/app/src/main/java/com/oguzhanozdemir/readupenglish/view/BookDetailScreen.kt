package com.oguzhanozdemir.readupenglish.view

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.oguzhanozdemir.readupenglish.viewmodel.BookViewModel
import com.oguzhanozdemir.readupenglish.viewmodel.TranslateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: Int,
    viewModel: BookViewModel = hiltViewModel(),
    translateViewModel: TranslateViewModel = hiltViewModel(),
) {

    val currentBookmark by viewModel.currentBookmark
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val apiKey = "AIzaSyA0L9gQxJL4WZJeW6YK3rxYO4BExng6xN4"
    val isListLoading = viewModel.isLoading
    val book = viewModel.getBookById(bookId)
    var bookText by remember { mutableStateOf<String?>(null) }
    var isLoadingFromCache by remember { mutableStateOf(false) }
    var isLoadingFromNetwork by remember { mutableStateOf(false) }

    // Use better state management for translation UI
    val translatedText = translateViewModel.translatedText
    val isTranslating = translateViewModel.isTranslating
    val translationError = translateViewModel.translationError

    // Dialog and selection state
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        // Load bookmark for this book
        viewModel.loadBookmark(bookId)
        
        // Load book content if book exists
        if (book != null) {
            // FIRST: Check memory cache immediately (synchronous - no loading spinner)
            val memoryCachedContent = viewModel.getCachedBookContentFromMemory(bookId)
            if (memoryCachedContent != null) {
                Log.d("BookDetail", "Using memory cached content for book $bookId (instant)")
                bookText = memoryCachedContent
                isLoadingFromCache = true
                viewModel.showSnackbarMessage("Book loaded from memory cache - instant access!")
            } else {
                // SECOND: Check database cache (asynchronous)
                try {
                    val cachedContent = viewModel.getCachedBookContent(bookId)
                    if (cachedContent != null) {
                        Log.d("BookDetail", "Using database cached content for book $bookId")
                        bookText = cachedContent
                        isLoadingFromCache = true
                        viewModel.showSnackbarMessage("Book loaded from cache!")
                    } else {
                        // THIRD: Load from network if no cache
                        val textUrl = book.formats["text/plain; charset=utf-8"]
                            ?: book.formats["text/plain; charset=us-ascii"]
                            ?: book.formats["text/plain"]
                            ?: book.formats.entries.firstOrNull {
                                it.key.contains("text/plain", ignoreCase = true)
                            }?.value

                        if (textUrl != null) {
                            Log.d("BookDetail", "Kitap içeriği indiriliyor: $textUrl")
                            viewModel.loadBookTextContent(bookId, textUrl) { loaded ->
                                if (loaded.isNotBlank()) {
                                    Log.d("BookDetail", "Kitap içeriği başarıyla yüklendi: ${loaded.length} karakter")
                                    bookText = loaded
                                    isLoadingFromNetwork = true
                                } else {
                                    Log.e("BookDetail", "Kitap içeriği boş geldi")
                                    bookText = "Kitap içeriği yüklenemedi. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."
                                }
                            }
                        } else {
                            Log.e("BookDetail", "Kitap için metin URL'si bulunamadı")
                            bookText = "Bu kitap için metin içeriği bulunamadı."
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookDetail", "Error checking cached content: ${e.message}")
                    // Fallback to network loading
                    val textUrl = book.formats["text/plain; charset=utf-8"]
                        ?: book.formats["text/plain; charset=us-ascii"]
                        ?: book.formats["text/plain"]
                        ?: book.formats.entries.firstOrNull {
                            it.key.contains("text/plain", ignoreCase = true)
                        }?.value

                    if (textUrl != null) {
                        viewModel.loadBookTextContent(bookId, textUrl) { loaded ->
                            if (loaded.isNotBlank()) {
                                bookText = loaded
                                isLoadingFromNetwork = true
                            }
                        }
                    }
                }
            }
        } else {
            // Try to load book from cache if not in memory
            try {
                val cachedBook = viewModel.repository.getCachedBook(bookId)
                if (cachedBook != null) {
                    Log.d("BookDetail", "Found book $bookId in cache")
                    // Update the books list in ViewModel
                    viewModel.addBookToList(cachedBook)
                    
                    // Check memory cache first for instant loading
                    val memoryCachedContent = viewModel.getCachedBookContentFromMemory(bookId)
                    if (memoryCachedContent != null) {
                        Log.d("BookDetail", "Using memory cached content for cached book $bookId (instant)")
                        bookText = memoryCachedContent
                        isLoadingFromCache = true
                        viewModel.showSnackbarMessage("Book and content loaded from memory cache!")
                    } else {
                        // Then check database cache
                        val cachedContent = viewModel.getCachedBookContent(bookId)
                        if (cachedContent != null) {
                            Log.d("BookDetail", "Using database cached content for cached book $bookId")
                            bookText = cachedContent
                            isLoadingFromCache = true
                            viewModel.showSnackbarMessage("Book and content loaded from offline cache!")
                        } else {
                            viewModel.showSnackbarMessage("Book found in cache, but content needs to be downloaded")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BookDetail", "Error loading cached book: ${e.message}")
            }
        }
    }

    BackHandler(enabled = true) {

        coroutineScope.launch {

            if (listState.firstVisibleItemIndex > 0) {
                val firstVisibleItem = listState.firstVisibleItemIndex
                val totalItems = listState.layoutInfo.totalItemsCount
                val percentage = if (totalItems > 0) {
                    firstVisibleItem.toFloat() / totalItems
                } else 0f

                viewModel.saveBookmark(bookId, firstVisibleItem, percentage)

                viewModel.showSnackbarMessage("Bookmark saved at position: ${(percentage * 100).toInt()}%")
            }
        }
        navController.popBackStack()
    }

    // Debug logging
    LaunchedEffect(translatedText, selectedWord) {
        Log.d("BookDetailScreen", "Translation updated: $translatedText for word: $selectedWord")
    }

    LaunchedEffect(showDialog, selectedWord) {
        Log.d("BookDetailScreen", "State changed - ShowDialog: $showDialog, Selected Word: $selectedWord")
    }


    val paragraphs = remember(bookText) {
        bookText
            ?.split(Regex("\n\\s*\n"))
            ?: emptyList()
    }


    LaunchedEffect(currentBookmark, bookText) {
        val hasContent = bookText != null && !bookText.toString().isNullOrEmpty()
        if (hasContent && !listState.isScrollInProgress) {
            currentBookmark?.let { bookmark ->
                if (bookmark.position > 0) {
                    try {
                        listState.scrollToItem(bookmark.position)
                        Log.d("BookDetailScreen", "Scrolled to initial bookmark position: ${bookmark.position}")
                    } catch (e: Exception) {
                        Log.e("BookDetailScreen", "Error scrolling to bookmark: ${e.message}")
                    }
                }
            }
        }
    }
    


    val handleWordClick = { word: String ->
        Log.d("BookDetails", "Processing word: $word")
        selectedWord = word
        translateViewModel.translate(word, apiKey)
        showDialog = true
    }

    val closeDialog = {
        Log.d("BookDetailScreen", "Dialog closed")
        showDialog = false
        selectedWord = null
        translateViewModel.clearTranslation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (listState.firstVisibleItemIndex > 0) {
                                val firstVisibleItem = listState.firstVisibleItemIndex
                                val totalItems = listState.layoutInfo.totalItemsCount
                                val percentage = if (totalItems > 0) {
                                    firstVisibleItem.toFloat() / totalItems
                                } else 0f
                                
                                viewModel.saveBookmark(bookId, firstVisibleItem, percentage)
                                Log.d("BookDetailScreen", "Saving position before navigation: $firstVisibleItem")
                            }
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (listState.firstVisibleItemIndex > 0) {
                                val firstVisibleItem = listState.firstVisibleItemIndex
                                val totalItems = listState.layoutInfo.totalItemsCount
                                val percentage = if (totalItems > 0) {
                                    firstVisibleItem.toFloat() / totalItems
                                } else 0f
                                
                                viewModel.saveBookmark(bookId, firstVisibleItem, percentage)
                                Log.d("BookDetailScreen", "Manual bookmark saved at position: $firstVisibleItem")
                                viewModel.showSnackbarMessage("Bookmark saved at position: ${(percentage * 100).toInt()}%")
                            }
                        }
                    }) {
                        Icon(Icons.Default.AddCircle, "İlerlemeyi Kaydet")
                    }
                    
                    // Debug button to clear cache for this book
                    IconButton(onClick = {
                        viewModel.clearBookContentCache(bookId)
                        bookText = null
                        isLoadingFromCache = false
                        isLoadingFromNetwork = false
                        viewModel.showSnackbarMessage("Cache cleared for this book")
                    }) {
                        Icon(Icons.Default.Clear, "Clear Cache")
                    }
                }
            )
        }
    ){ padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isListLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading book information...",
                                modifier = Modifier.padding(top = 16.dp),
                                color = Color.Gray
                            )
                        }
                    }
                }
                book == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Book not found",
                                color = Color.Red,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This book may not be cached for offline reading.\nPlease check your internet connection and try again.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    // Try to reload the books
                                    viewModel.loadBooks("Mystery")
                                }
                            ) {
                                Text("Retry Loading Books")
                            }
                        }
                    }
                }
                bookText.isNullOrBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = when {
                                    isLoadingFromNetwork -> "Downloading book content..."
                                    isLoadingFromCache -> "Loading from cache..."
                                    else -> "Loading book content..."
                                },
                                modifier = Modifier.padding(top = 16.dp),
                                color = Color.Gray
                            )
                            if (isLoadingFromNetwork) {
                                Text(
                                    text = "This may take a moment for large books",
                                    modifier = Modifier.padding(top = 8.dp),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(paragraphs.size) { index ->
                                val para = paragraphs[index]
                                val words = para.trim().split(Regex("\\s+"))

                                val annotatedText = buildAnnotatedString {
                                    words.forEachIndexed { i, word ->
                                        // Clean the word
                                        val originalWord = word.trim()

                                        if (originalWord.isNotEmpty()) {
                                            // Create unique tag for each word
                                            val tag = "WORD_${originalWord}"

                                            // Tag the word
                                            pushStringAnnotation(tag = tag, annotation = originalWord)

                                            withStyle(style = SpanStyle(
                                                color = Color.Black,
                                                fontSize = 24.sp,
                                            )) {
                                                append(originalWord)
                                            }
                                            // Close annotation tag
                                            pop()
                                        } else {
                                            // For empty words, use normal style
                                            append(word)
                                        }

                                        // Add space after each word (except the last one)
                                        if (i < words.size - 1) {
                                            append(" ")
                                        }
                                    }
                                }

                                ClickableText(
                                    text = annotatedText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = { offset ->

                                        Log.d("BookDetails", "Text clicked at offset: $offset")

                                        try {

                                            annotatedText.getStringAnnotations(
                                                start = offset,
                                                end = offset
                                            ).firstOrNull()?.let { annotation ->
                                                val clickedWord = annotation.item
                                                Log.d("BookDetails", "Found annotation with tag: ${annotation.tag}, word: $clickedWord")

                                                if (clickedWord.isNotEmpty()) {
                                                    handleWordClick(clickedWord)
                                                }
                                            } ?: run {
                                                Log.d("BookDetails", "No annotation found at offset: $offset")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("BookDetails", "Error in click handling", e)
                                        }
                                    }
                                )
                            }
                        }

                        // Custom scrollbar
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 2.dp)
                                .width(8.dp)
                                .fillMaxHeight()
                        ) {
                            // Scrollbar background
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .align(Alignment.Center)
                            )

                            // Scrollbar thumb
                            val firstVisibleItemIndex = listState.firstVisibleItemIndex
                            val totalItems = paragraphs.size
                            val visibleItems = listState.layoutInfo.visibleItemsInfo.size

                            if (totalItems > 0) {
                                val scrollRatio = firstVisibleItemIndex.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
                                val thumbHeight = (visibleItems.toFloat() / totalItems.coerceAtLeast(1)).coerceIn(0.05f, 1f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(thumbHeight)
                                        .width(6.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = (scrollRatio * (1f - thumbHeight) * 100).dp)
                                        .background(
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDialog && selectedWord != null) {
        Log.d("BookDetailScreen", "Displaying dialog for word: $selectedWord")

        AlertDialog(
            onDismissRequest = closeDialog,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedWord!!,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            },
            text = {
                Column {
                    if (translationError != null) {
                        Text(
                            text = "Çeviri hatası: $translationError",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    } else if (isTranslating) {
                        Text(
                            text = "Çeviri yükleniyor...",
                            fontSize = 16.sp
                        )
                    } else if (translatedText.isNotBlank()) {
                        Text(
                            text = "Çeviri: $translatedText",
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "Çeviri bekleniyor...",
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("BookDetailScreen", "Saving word: $selectedWord")
                        if (selectedWord != null && translatedText.isNotBlank()) {
                            translateViewModel.saveWord(selectedWord!!)
                            closeDialog()
                        }
                    },
                    enabled = !isTranslating && translatedText.isNotBlank()
                ) {
                    Text("+ Kaydet", color = if (!isTranslating && translatedText.isNotBlank()) Color.Blue else Color.Gray)
                }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) {
                    Text("Kapat")
                }
            }
        )
    }
}
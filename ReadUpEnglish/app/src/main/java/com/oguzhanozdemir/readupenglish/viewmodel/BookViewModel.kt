package com.oguzhanozdemir.readupenglish.viewmodel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.oguzhanozdemir.readupenglish.model.Book
import com.oguzhanozdemir.readupenglish.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.oguzhanozdemir.readupenglish.database.FavoriteBookDao
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import com.oguzhanozdemir.readupenglish.model.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class BookViewModel @Inject constructor(
    internal val repository: BookRepository,
    private val favoriteBookDao: FavoriteBookDao
) : BaseViewModel() {

    private val _currentBookmark = mutableStateOf<BookmarkEntity?>(null)
    val currentBookmark: State<BookmarkEntity?> = _currentBookmark
    private var selectedFilter = mutableStateOf("Mystery")
    private val _snackbarMessage = mutableStateOf<String?>(null)
    val snackbarMessage: State<String?> = _snackbarMessage

    private val _favoriteBooks = mutableStateOf<List<FavoriteBookEntity>>(emptyList())
    val favoriteBooks: State<List<FavoriteBookEntity>> = _favoriteBooks

    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)

    init {
        loadBooks("Mystery")
        observeFavorites()
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
    
    fun showSnackbarMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun loadBookmark(bookId: Int) {
        viewModelScope.launch {
            val bookmark = repository.getBookmark(bookId)
            _currentBookmark.value = bookmark
        }
    }

    fun saveBookmark(bookId: Int, position: Int, percentage: Float) {
        viewModelScope.launch {
            if (position > 0) {
                repository.saveBookmark(bookId, position, percentage)
                loadBookmark(bookId)
                Log.d("BookViewModel", "Bookmark saved at position: $position, percentage: $percentage")
            } else {
                Log.d("BookViewModel", "Skipping bookmark save at position 0")
            }
        }
    }

    fun saveFavorite(book: Book) {
        viewModelScope.launch {
            repository.saveFavoriteBook(book)
            Log.d("BookScreen", "Saved Favorite Book = ${book.title}")
        }
    }

    fun loadBooks(topic : String) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d("BookViewModel", "Starting to fetch history books")
                val fetchedBooks = repository.fetchHistoryBooks(topic)
                if (fetchedBooks.isEmpty()) {
                    _snackbarMessage.value = "No history books found"
                    Log.w("BookViewModel", "No history books were found")
                } else {
                    // Cache each book's metadata
                    fetchedBooks.forEach { book ->
                        viewModelScope.launch {
                            repository.cacheBook(book)
                        }
                    }

                    // Preserve books that already have content
                    val existingBooksWithContent = books.filter { it.textContent != null }
                    Log.d("BookViewModel", "Before loading new books: ${existingBooksWithContent.size} books have content")
                    
                    val updatedBooks = fetchedBooks.map { newBook ->
                        // Check if we already have this book with content
                        existingBooksWithContent.find { it.id == newBook.id }?.let { existingBook ->
                            // Merge the new book data with existing content
                            Log.d("BookViewModel", "Preserving content for book ${newBook.id}")
                            newBook.copy(textContent = existingBook.textContent)
                        } ?: newBook
                    }
                    
                    books = updatedBooks
                    Log.d("BookViewModel", "Successfully loaded ${fetchedBooks.size} history books (preserved ${existingBooksWithContent.size} with content)")
                    Log.d("BookViewModel", "After loading: ${books.count { it.textContent != null }} books have content")
                    
                    // Preload cached content for the new books (in background, don't block loading)
                    viewModelScope.launch {
                        preloadCachedContentForBooks()
                    }
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Failed to fetch history books, trying cached books", e)
                // If network fails, try to load cached books
                try {
                    val cachedBooks = repository.getCachedBooks()
                    if (cachedBooks.isNotEmpty()) {
                        Log.d("BookViewModel", "Loading ${cachedBooks.size} books from cache - immediate display")
                        books = cachedBooks
                        _snackbarMessage.value = "Loaded books from offline cache"
                        
                        // For cached books, preload content immediately but don't block UI
                        viewModelScope.launch {
                            preloadCachedContentForBooksOptimized()
                        }
                    } else {
                        _snackbarMessage.value = "Failed to load books: ${e.localizedMessage}"
                        Log.e("BookViewModel", "No cached books available")
                    }
                } catch (cacheError: Exception) {
                    _snackbarMessage.value = "Failed to load books: ${e.localizedMessage}"
                    Log.e("BookViewModel", "Failed to load cached books", cacheError)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun getBookById(id: Int): Book? {
        // First check in memory
        val memoryBook = books.find { it.id == id }
        if (memoryBook != null) {
            return memoryBook
        }

        // If not in memory, check cache and load it
        viewModelScope.launch {
            try {
                val cachedBook = repository.getCachedBook(id)
                if (cachedBook != null) {
                    Log.d("BookViewModel", "Found book $id in cache, adding to memory")
                    books = books + cachedBook
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error loading cached book: ${e.message}")
            }
        }

        return books.find { it.id == id }
    }

    fun preloadCachedContentForBooks() {
        viewModelScope.launch {
            try {
                val booksWithoutContent = books.filter { it.textContent == null }
                Log.d("BookViewModel", "Preloading cached content for ${booksWithoutContent.size} books")
                
                booksWithoutContent.forEach { book ->
                    val cachedContent = repository.getCachedBookContent(book.id)
                    if (cachedContent != null) {
                        // Update the book with cached content
                        val updatedBooks = books.map {
                            if (it.id == book.id) it.copy(textContent = cachedContent) else it
                        }
                        books = updatedBooks
                        Log.d("BookViewModel", "Preloaded cached content for book ${book.id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error preloading cached content: ${e.message}")
            }
        }
    }

    fun preloadCachedContentForBooksOptimized() {
        viewModelScope.launch {
            try {
                val booksWithoutContent = books.filter { it.textContent == null }
                Log.d("BookViewModel", "Fast preloading cached content for ${booksWithoutContent.size} books")
                
                // Process books in batches for better performance
                val bookIds = booksWithoutContent.map { it.id }
                val cachedContents = repository.getBulkCachedBookContent(bookIds)
                
                // Update all books at once
                val updatedBooks = books.map { book ->
                    cachedContents[book.id]?.let { content ->
                        Log.d("BookViewModel", "Applied cached content for book ${book.id}")
                        book.copy(textContent = content)
                    } ?: book
                }
                
                books = updatedBooks
                Log.d("BookViewModel", "Fast preloaded content for ${cachedContents.size} books")
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error in optimized preloading: ${e.message}")
                // Fallback to normal preloading
                preloadCachedContentForBooks()
            }
        }
    }

    fun loadBookTextContent(
        bookId: Int,
        textUrl: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // First, check if content is already cached
                val cachedContent = repository.getCachedBookContent(bookId)
                if (cachedContent != null) {
                    Log.d("BookViewModel", "Using cached content for book $bookId")
                    onResult(cachedContent)
                    return@launch
                }

                // If not cached, load from network
                Log.d("BookViewModel", "Loading content from network for book $bookId")
                val fullText = repository.fetchBookTextContent(textUrl)

                // Show partial content immediately while processing
                onResult("Loading and processing content...\n\n${fullText.take(500)}...")

                // Process text in background for better performance
                withContext(Dispatchers.Default) {
                    val processedText = processBookText(fullText)
                    
                    withContext(Dispatchers.Main) {
                        // Cache the processed content
                        repository.cacheBookContent(bookId, processedText)

                        // Also cache the book metadata if not already cached
                        val book = getBookById(bookId)
                        if (book != null) {
                            repository.cacheBook(book)
                        }

                        onResult(processedText)

                        // Update the book
                        val updatedBooks = books.map {
                            if (it.id == bookId) it.copy(textContent = processedText) else it
                        }
                        books = updatedBooks
                        Log.d("BookViewModel", "Updated book ${bookId} with content in memory. Books list now has ${books.count { it.textContent != null }} books with content.")
                    }
                }

            } catch (e: Exception) {
                Log.e("BookViewModel", "Failed to load book content: ${e.message}", e)
                onResult("Failed to load book content: ${e.localizedMessage}")
            }
        }
    }

    private fun processBookText(fullText: String): String {
        try {
            Log.d("BookViewModel", "Processing text of ${fullText.length} characters")
            
            // Find Project Gutenberg markers more efficiently
            val startMarkerIndex = findStartMarker(fullText)
            val textAfterStart = if (startMarkerIndex > 0) {
                fullText.substring(startMarkerIndex)
            } else fullText

            val endMarkerIndex = findEndMarker(textAfterStart)
            val mainContent = if (endMarkerIndex > 0) {
                textAfterStart.substring(0, endMarkerIndex)
            } else textAfterStart

            // Find chapter start more efficiently
            val chapterStart = findChapterStart(mainContent)
            val bookContent = if (chapterStart > 0) {
                mainContent.substring(chapterStart)
            } else mainContent

            // Clean and format text efficiently
            val cleanText = cleanAndFormatText(bookContent)
            
            Log.d("BookViewModel", "Text processing completed: ${cleanText.length} characters")
            return cleanText
            
        } catch (e: Exception) {
            Log.e("BookViewModel", "Error processing text: ${e.message}")
            // Return raw text if processing fails
            return fullText.trim()
        }
    }

    private fun findStartMarker(text: String): Int {
        val markers = listOf(
            "*** START OF THIS PROJECT GUTENBERG",
            "*** START OF THE PROJECT GUTENBERG",
            "***START OF THIS PROJECT GUTENBERG",
            "***START OF THE PROJECT GUTENBERG"
        )
        
        for (marker in markers) {
            val index = text.indexOf(marker, ignoreCase = true)
            if (index >= 0) {
                // Find end of line after marker
                val lineEnd = text.indexOf('\n', index)
                return if (lineEnd > index) lineEnd + 1 else index + marker.length
            }
        }
        return 0
    }

    private fun findEndMarker(text: String): Int {
        val markers = listOf(
            "*** END OF THIS PROJECT GUTENBERG",
            "*** END OF THE PROJECT GUTENBERG",
            "***END OF THIS PROJECT GUTENBERG",
            "***END OF THE PROJECT GUTENBERG"
        )
        
        for (marker in markers) {
            val index = text.indexOf(marker, ignoreCase = true)
            if (index >= 0) return index
        }
        return text.length
    }

    private fun findChapterStart(text: String): Int {
        val lines = text.split('\n')
        for (i in lines.indices) {
            val line = lines[i].trim().uppercase()
            if (line.startsWith("CHAPTER 1") || 
                line.startsWith("CHAPTER I") ||
                line == "I" || line == "1" ||
                line.matches(Regex("^[IVXLCDM]+\\.$"))) {
                // Return position in original text
                return text.indexOf(lines[i])
            }
        }
        return 0
    }

    private fun cleanAndFormatText(text: String): String {
        return text
            .replace(Regex("[-_]{2,}"), " ")  // Replace multiple dashes/underscores
            .replace(Regex("\\s{3,}"), "  ") // Replace multiple spaces
            .replace(Regex("\\n{4,}"), "\n\n\n") // Limit excessive line breaks
            .split("\n\n")
            .filter { it.trim().isNotEmpty() && it.trim().length > 10 } // Filter very short paragraphs
            .joinToString("\n\n") { it.trim() }
            .trim()
    }

    suspend fun getCachedBookContent(bookId: Int): String? {
        return try {
            repository.getCachedBookContent(bookId)
        } catch (e: Exception) {
            Log.e("BookViewModel", "Error getting cached content: ${e.message}")
            null
        }
    }

    fun getCachedBookContentFromMemory(bookId: Int): String? {
        return try {
            repository.getCachedBookContentFromMemory(bookId)
        } catch (e: Exception) {
            Log.e("BookViewModel", "Error getting cached content from memory: ${e.message}")
            null
        }
    }

    fun clearBookContentCache(bookId: Int) {
        viewModelScope.launch {
            try {
                repository.clearBookContentCache(bookId)
                Log.d("BookViewModel", "Cleared cache for book $bookId")
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error clearing cache: ${e.message}")
            }
        }
    }

    fun clearAllBookContentCache() {
        viewModelScope.launch {
            try {
                repository.clearAllBookContentCache()
                // Also clear from memory
                val updatedBooks = books.map { it.copy(textContent = null) }
                books = updatedBooks
                Log.d("BookViewModel", "Cleared all book content cache")
                _snackbarMessage.value = "All cached book content cleared"
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error clearing all cache: ${e.message}")
            }
        }
    }

    fun addBookToList(book: Book) {
        if (books.none { it.id == book.id }) {
            books = books + book
            Log.d("BookViewModel", "Added book ${book.id} to books list")
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteBooks().collect {
                _favoriteBooks.value = it
            }
        }
    }
}
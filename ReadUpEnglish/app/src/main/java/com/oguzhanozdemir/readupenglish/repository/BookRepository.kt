package com.oguzhanozdemir.readupenglish.repository

import android.util.Log
import com.oguzhanozdemir.readupenglish.cache.BookContentCache
import com.oguzhanozdemir.readupenglish.database.BookContentDao
import com.oguzhanozdemir.readupenglish.database.BookmarkDao
import com.oguzhanozdemir.readupenglish.database.FavoriteBookDao
import com.oguzhanozdemir.readupenglish.model.Book
import com.oguzhanozdemir.readupenglish.model.BookContentEntity
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import com.oguzhanozdemir.readupenglish.model.FavoriteBookEntity
import com.oguzhanozdemir.readupenglish.service.BookApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val api: BookApiService,
    private val favoriteBookDao: FavoriteBookDao,
    private val bookmarkDao: BookmarkDao,
    private val bookContentDao: BookContentDao,
    private val bookContentCache: BookContentCache
) {
    suspend fun fetchBooks(): List<Book> = api.getBooks().result
    
    suspend fun fetchHistoryBooks(topic: String): List<Book> = withContext(Dispatchers.IO) {
        var retryCount = 0
        val maxRetries = 3
        
        while (retryCount < maxRetries) {
            try {
                Log.d("BookRepository", "Fetching history books... (Attempt ${retryCount + 1}/$maxRetries)")
                val response = withTimeout(30000) { // 30 second timeout
                    api.getHistoryBooks(topic)
                }
                
                if (response.result.isEmpty()) {
                    Log.w("BookRepository", "No history books found in the primary search")
                    throw IOException("No history books found")
                } else {
                    Log.d("BookRepository", "Successfully fetched ${response.result.size} history books")
                    return@withContext response.result
                }
            } catch (e: Exception) {
                retryCount++
                Log.e("BookRepository", "Error fetching history books (Attempt $retryCount/$maxRetries): ${e.message}")
                
                if (retryCount >= maxRetries) {
                    throw IOException("Failed to fetch history books: ${e.localizedMessage}")
                }
                
                // Exponential backoff delay
                kotlinx.coroutines.delay(1000L * (1 shl retryCount))
            }
        }
        
        throw IOException("Maximum retry attempts reached")
    }
    
    suspend fun fetchBookTextContent(textUrl: String): String =
        withContext(Dispatchers.IO) {
            var retryCount = 0
            val maxRetries = 3
            
            while (retryCount < maxRetries) {
                try {
                    Log.d("BookRepository", "Downloading book content: $textUrl (Attempt ${retryCount + 1}/$maxRetries)")
                    val response: Response<ResponseBody> = withTimeout(45000) { // 45 second timeout
                        api.getBookText(textUrl)
                    }
                    
                    if (response.isSuccessful) {
                        val content = response.body()?.string()
                        if (!content.isNullOrBlank()) {
                            Log.d("BookRepository", "Successfully downloaded book content: ${content.length} characters")
                            return@withContext content
                        } else {
                            Log.e("BookRepository", "Book content is empty")
                            throw IOException("Book content is empty")
                        }
                    } else {
                        Log.e("BookRepository", "Failed to download book content: ${response.code()}")
                        throw IOException("Failed to download book content: ${response.code()}")
                    }
                } catch (e: Exception) {
                    retryCount++
                    Log.e("BookRepository", "Error downloading book content (Attempt $retryCount/$maxRetries): ${e.message}")
                    
                    if (retryCount >= maxRetries) {
                        throw IOException("Failed to download book content: ${e.localizedMessage}")
                    }
                    
                    // Exponential backoff delay
                    kotlinx.coroutines.delay(1000L * (1 shl retryCount))
                }
            }
            
            throw IOException("Maximum retry attempts reached")
        }

    suspend fun saveFavoriteBook(book: Book) {
        val favorite = FavoriteBookEntity(
            id = book.id,
            title = book.title,
            imageUrl = book.formats["image/jpeg"],
            authors = book.authors.joinToString { it.name }
        )
        favoriteBookDao.insertFavorite(favorite)
    }

    fun getFavoriteBooks(): Flow<List<FavoriteBookEntity>> {
        return favoriteBookDao.getAllFavorites()
    }

    suspend fun saveBookmark(bookId: Int, position: Int, percentage: Float) {
        val bookmark = BookmarkEntity(
            bookId = bookId,
            position = position,
            lastReadTimestamp = System.currentTimeMillis(),
            percentage = percentage
        )
        bookmarkDao.saveBookmark(bookmark)
    }

    suspend fun getBookmark(bookId: Int): BookmarkEntity? {
        return bookmarkDao.getBookmarkForBook(bookId)
    }

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }

    suspend fun getCachedBookContent(bookId: Int): String? {
        return bookContentCache.getBookContent(bookId)
    }

    suspend fun cacheBookContent(bookId: Int, content: String) {
        bookContentCache.cacheBookContent(bookId, content)
    }

    suspend fun clearBookContentCache(bookId: Int) {
        bookContentCache.clearBookFromAllCaches(bookId)
    }

    suspend fun clearAllBookContentCache() {
        bookContentCache.clearAllCaches()
    }
}
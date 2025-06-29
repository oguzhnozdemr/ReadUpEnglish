package com.oguzhanozdemir.readupenglish.cache

import android.util.Log
import com.oguzhanozdemir.readupenglish.database.BookContentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookContentCache @Inject constructor(
    private val bookContentDao: BookContentDao
) {
    private val memoryCache = mutableMapOf<Int, String>()

    suspend fun getBookContent(bookId: Int): String? {
        // First check memory cache
        memoryCache[bookId]?.let { content ->
            Log.d("BookContentCache", "Found content in memory cache for book $bookId")
            return content
        }

        // Then check database cache
        return withContext(Dispatchers.IO) {
            try {
                val cachedContent = bookContentDao.getBookContent(bookId)
                if (cachedContent != null) {
                    Log.d("BookContentCache", "Found content in database cache for book $bookId")
                    // Store in memory cache for faster access
                    memoryCache[bookId] = cachedContent.content
                    cachedContent.content
                } else {
                    Log.d("BookContentCache", "No cached content found for book $bookId")
                    null
                }
            } catch (e: Exception) {
                Log.e("BookContentCache", "Error getting cached content: ${e.message}")
                null
            }
        }
    }

    fun getBookContentFromMemory(bookId: Int): String? {
        return memoryCache[bookId]?.also {
            Log.d("BookContentCache", "Found content in memory cache for book $bookId (sync)")
        }
    }

    suspend fun getBulkBookContent(bookIds: List<Int>): Map<Int, String> {
        val result = mutableMapOf<Int, String>()
        
        // First check memory cache for all books
        bookIds.forEach { bookId ->
            memoryCache[bookId]?.let { content ->
                result[bookId] = content
                Log.d("BookContentCache", "Found content in memory cache for book $bookId")
            }
        }
        
        // Get remaining books from database
        val remainingBookIds = bookIds.filter { it !in result.keys }
        if (remainingBookIds.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    remainingBookIds.forEach { bookId ->
                        val cachedContent = bookContentDao.getBookContent(bookId)
                        if (cachedContent != null) {
                            result[bookId] = cachedContent.content
                            // Store in memory cache for faster access next time
                            memoryCache[bookId] = cachedContent.content
                            Log.d("BookContentCache", "Found content in database cache for book $bookId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookContentCache", "Error getting bulk cached content: ${e.message}")
                }
            }
        }
        
        Log.d("BookContentCache", "Bulk loaded ${result.size} contents out of ${bookIds.size} requested")
        return result
    }

    suspend fun cacheBookContent(bookId: Int, content: String) {
        // Store in memory cache immediately
        memoryCache[bookId] = content
        Log.d("BookContentCache", "Cached content in memory for book $bookId")

        // Store in database cache
        withContext(Dispatchers.IO) {
            try {
                val bookContent = com.oguzhanozdemir.readupenglish.model.BookContentEntity(
                    bookId = bookId,
                    content = content
                )
                bookContentDao.insertBookContent(bookContent)
                Log.d("BookContentCache", "Cached content in database for book $bookId")
            } catch (e: Exception) {
                Log.e("BookContentCache", "Error caching content in database: ${e.message}")
            }
        }
    }

    fun clearBookFromMemoryCache(bookId: Int) {
        memoryCache.remove(bookId)
        Log.d("BookContentCache", "Removed book $bookId from memory cache")
    }

    suspend fun clearBookFromAllCaches(bookId: Int) {
        clearBookFromMemoryCache(bookId)
        withContext(Dispatchers.IO) {
            try {
                bookContentDao.deleteBookContent(bookId)
                Log.d("BookContentCache", "Removed book $bookId from database cache")
            } catch (e: Exception) {
                Log.e("BookContentCache", "Error removing book from database cache: ${e.message}")
            }
        }
    }

    suspend fun clearAllCaches() {
        memoryCache.clear()
        withContext(Dispatchers.IO) {
            try {
                bookContentDao.deleteAllBookContent()
                Log.d("BookContentCache", "Cleared all caches")
            } catch (e: Exception) {
                Log.e("BookContentCache", "Error clearing all caches: ${e.message}")
            }
        }
    }

    fun getMemoryCacheSize(): Int = memoryCache.size
} 
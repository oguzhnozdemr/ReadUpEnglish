package com.oguzhanozdemir.readupenglish.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM book_bookmarks WHERE bookId = :bookId")
    suspend fun getBookmarkForBook(bookId: Int): BookmarkEntity?

    @Query("SELECT * FROM book_bookmarks")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
}

package com.oguzhanozdemir.readupenglish.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oguzhanozdemir.readupenglish.model.BookContentEntity

@Dao
interface BookContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookContent(bookContent: BookContentEntity)

    @Query("SELECT * FROM book_content WHERE bookId = :bookId")
    suspend fun getBookContent(bookId: Int): BookContentEntity?

    @Query("DELETE FROM book_content WHERE bookId = :bookId")
    suspend fun deleteBookContent(bookId: Int)

    @Query("DELETE FROM book_content")
    suspend fun deleteAllBookContent()
} 
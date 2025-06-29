package com.oguzhanozdemir.readupenglish.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oguzhanozdemir.readupenglish.model.Book
import com.oguzhanozdemir.readupenglish.model.BookResponse
import com.oguzhanozdemir.readupenglish.model.WordEntity

@Dao
interface BookDao {

    @Insert
    suspend fun insertAll(vararg books: Book): List<Long>

    @Query("SELECT * FROM book")
    suspend fun getAllBooks(): List<Book>

    @Query("SELECT * FROM book WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    @Query("DELETE FROM book")
    suspend fun deleteAllBook()

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(wordEntity: WordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    // BookResponse ile işlem yapmak için yeni metodlar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookResponse(bookResponse: BookResponse): Long

    @Query("SELECT * FROM book_response WHERE uuid = :uuid")
    suspend fun getBookResponseById(uuid: Int): BookResponse
}

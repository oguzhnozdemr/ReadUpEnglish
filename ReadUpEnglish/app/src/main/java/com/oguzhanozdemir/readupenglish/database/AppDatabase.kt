package com.oguzhanozdemir.readupenglish.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oguzhanozdemir.readupenglish.model.Book
import com.oguzhanozdemir.readupenglish.model.BookContentEntity
import com.oguzhanozdemir.readupenglish.model.BookResponse
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import com.oguzhanozdemir.readupenglish.model.FavoriteBookEntity
import com.oguzhanozdemir.readupenglish.model.WordEntity
import com.oguzhanozdemir.readupenglish.util.Converters

@Database(entities = [Book::class, BookResponse::class, WordEntity::class, FavoriteBookEntity::class, BookmarkEntity::class, BookContentEntity::class], version = 11, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun wordDao(): WordDao
    abstract fun favoriteBookDao(): FavoriteBookDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun bookContentDao(): BookContentDao
}
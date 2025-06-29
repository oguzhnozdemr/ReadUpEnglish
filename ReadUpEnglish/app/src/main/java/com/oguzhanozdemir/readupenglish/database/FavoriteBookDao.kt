package com.oguzhanozdemir.readupenglish.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oguzhanozdemir.readupenglish.model.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(book: FavoriteBookEntity)

    @Query("SELECT * FROM favorite_books")
    fun getAllFavorites(): Flow<List<FavoriteBookEntity>>

    @Delete
    suspend fun deleteFavorite(book: FavoriteBookEntity)
}
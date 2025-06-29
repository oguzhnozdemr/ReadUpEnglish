package com.oguzhanozdemir.readupenglish.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oguzhanozdemir.readupenglish.model.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>
}
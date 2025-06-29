package com.oguzhanozdemir.readupenglish.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val translation: String,
    val timestamp: Long = System.currentTimeMillis() // Added timestamp instead of text
)


package com.oguzhanozdemir.readupenglish.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_content")
data class BookContentEntity(
    @PrimaryKey val bookId: Int,
    val content: String,
    val lastUpdated: Long = System.currentTimeMillis()
) 
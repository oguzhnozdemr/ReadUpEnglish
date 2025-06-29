package com.oguzhanozdemir.readupenglish.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_bookmarks")
data class BookmarkEntity(
    @PrimaryKey val bookId: Int,
    val position: Int,        // Sayfa pozisyonu veya scroll pozisyonu
    val lastReadTimestamp: Long, // Son okuma zamanı
    val percentage: Float     // Kitaptaki ilerleme yüzdesi
)
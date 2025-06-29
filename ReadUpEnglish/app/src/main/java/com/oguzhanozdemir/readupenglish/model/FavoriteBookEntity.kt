package com.oguzhanozdemir.readupenglish.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String?, // Görsel gerekiyorsa
    val authors: String     // Join edilmiş yazarlar
)
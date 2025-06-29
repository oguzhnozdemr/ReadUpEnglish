package com.oguzhanozdemir.readupenglish.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "book")
data class Book(
    @PrimaryKey
    @ColumnInfo(name="id")
    val id: Int,

    @ColumnInfo(name="title")
    val title: String,

    @ColumnInfo(name="authors")
    val authors: List<Author>,

    @ColumnInfo(name="formats")
    val formats: Map<String, String>,

    @ColumnInfo(name="text_content")
    val textContent: String? = null
)

data class Author(
    val name: String,
)
@Entity(tableName = "book_response")
data class BookResponse(
    @ColumnInfo(name = "result")
    @SerializedName("results")
    val result: List<Book>,

    @PrimaryKey(autoGenerate = true)
    var uuid: Int = 0
)
package com.oguzhanozdemir.readupenglish.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oguzhanozdemir.readupenglish.model.Author
import com.oguzhanozdemir.readupenglish.model.Book

class Converters {

    @TypeConverter
    fun fromAuthorList(value: List<Author>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAuthorList(value: String): List<Author> {
        val type = object : TypeToken<List<Author>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromFormatsMap(value: Map<String, String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFormatsMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromBookList(value: List<Book>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toBookList(value: String): List<Book> {
        val type = object : TypeToken<List<Book>>() {}.type
        return Gson().fromJson(value, type)
    }

}
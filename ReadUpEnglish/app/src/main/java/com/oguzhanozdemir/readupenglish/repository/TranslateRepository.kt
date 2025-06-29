package com.oguzhanozdemir.readupenglish.repository

import android.util.Log
import com.oguzhanozdemir.readupenglish.database.WordDao
import com.oguzhanozdemir.readupenglish.model.BookmarkEntity
import com.oguzhanozdemir.readupenglish.model.WordEntity
import com.oguzhanozdemir.readupenglish.service.TranslationApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TranslateRepository @Inject constructor(
    private val api: TranslationApiService,
    private val dao: WordDao
) {
    suspend fun translateWord(word: String, apiKey: String): String {
        return try {
            val response = api.translate(word, "tr", apiKey)
            response.data.translations.firstOrNull()?.translatedText ?: "Çeviri bulunamadı"
        } catch (e: Exception) {
            Log.e("TranslateRepository", "Translation error", e)
            throw e
        }
    }

    suspend fun saveWord(word: String, translation: String) {
        dao.insertWord(WordEntity(
            word = word,
            translation = translation,
            // No need for TODOs - id is auto-generated and we use timestamp
        ))
    }


    fun getSavedWords(): Flow<List<WordEntity>> = dao.getAllWords()


}
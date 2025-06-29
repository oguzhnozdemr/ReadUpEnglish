package com.oguzhanozdemir.readupenglish.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oguzhanozdemir.readupenglish.repository.TranslateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oguzhanozdemir.readupenglish.model.TranslationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val repository: TranslateRepository
) : ViewModel() {

    // State for translation
    private val _translationState = MutableStateFlow(TranslationState())
    val translationState = _translationState.asStateFlow()

    // Simple translated text for direct access
    var translatedText by mutableStateOf("")
        private set

    // State to track if translation is loading
    var isTranslating by mutableStateOf(false)
        private set

    // Error state
    var translationError by mutableStateOf<String?>(null)
        private set

    // Saved words from repository
    val savedWords = repository.getSavedWords().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    // Translate a word
    fun translate(word: String, apiKey: String) {
        translationError = null
        isTranslating = true
        translatedText = "" // Clear previous translation

        viewModelScope.launch {
            try {
                val result = repository.translateWord(word, apiKey)
                translatedText = result
                _translationState.update {
                    it.copy(
                        isLoading = false,
                        translatedText = result,
                        error = null
                    )
                }
            } catch (e: Exception) {
                translationError = e.message ?: "Translation failed"
                _translationState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Translation failed"
                    )
                }
                Log.e("TranslateViewModel", "Translation error", e)
            } finally {
                isTranslating = false
            }
        }
    }

    // Save a word and its translation
    fun saveWord(word: String) {

        viewModelScope.launch {
            Log.d("TranslateViewModel", "Kelime kaydetme başlatıldı: $word - Çevirisi: $translatedText")
            try {
                repository.saveWord(word, translatedText)
                Log.d("TranslateViewModel", "Kelime başarıyla kaydedildi: $word")

                repository.getSavedWords().collect { savedWords ->
                    Log.d("TranslateViewModel", "Tüm kaydedilen kelimeler:")
                    savedWords.forEach { wordEntity ->
                        Log.d("TranslateViewModel", "${wordEntity.word} - ${wordEntity.translation}")
                    }
                }

            } catch (e: Exception) {
                Log.e("TranslateViewModel", "Error saving word", e)
            }
        }
    }

    // Clear states when dialog is dismissed
    fun clearTranslation() {
        translatedText = ""
        translationError = null
        _translationState.update { TranslationState() }
    }
}
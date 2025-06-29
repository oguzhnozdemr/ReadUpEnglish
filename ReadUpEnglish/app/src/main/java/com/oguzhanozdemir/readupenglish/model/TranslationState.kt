package com.oguzhanozdemir.readupenglish.model

data class TranslationState(
    val isLoading: Boolean = false,
    val translatedText: String = "",
    val error: String? = null
)

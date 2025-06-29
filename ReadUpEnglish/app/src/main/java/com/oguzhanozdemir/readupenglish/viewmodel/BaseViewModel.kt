package com.oguzhanozdemir.readupenglish.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    // Hata yönetimi için temel coroutine handler
    private val handler = CoroutineExceptionHandler { _, exception ->
        onError(exception)
    }

    // Hata mesajı göstermek isteyen alt sınıflar override edebilir
    open fun onError(exception: Throwable) {
        // Log ya da UI gösterimi yapılabilir
        exception.printStackTrace()
    }

    // Güvenli bir şekilde coroutine başlatır
    fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch(handler) {
            block()
        }
    }
}
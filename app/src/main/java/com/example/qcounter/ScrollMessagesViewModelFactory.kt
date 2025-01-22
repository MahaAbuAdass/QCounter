package com.example.qcounter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

class GenericViewModelFactory<T : ViewModel>(
    private val modelClass: KClass<T>,
    private val creator: () -> T
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (this.modelClass.java.isAssignableFrom(modelClass)) {
            return creator() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

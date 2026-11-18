package com.tecsup.centrotareas.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.centrotareas.data.TareasRepositoryLocal

class TareasViewModelFactory(
    context: Context? = null
) : ViewModelProvider.Factory {

    private val appContext = context?.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            return TareasViewModel(
                repo = TareasRepositoryLocal(appContext)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

package com.hamo.doortech.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamo.doortech.AppContainer
import com.hamo.doortech.data.ai.AiImporter
import com.hamo.doortech.data.ai.AiOutcome
import com.hamo.doortech.data.repo.Outcome
import kotlinx.coroutines.launch

class AiSearchViewModel(private val container: AppContainer, initialQuery: String) : ViewModel() {
    var query by mutableStateOf(initialQuery)
    var apiKey by mutableStateOf(container.aiKey)
    var loading by mutableStateOf(false)
        private set
    var outcome by mutableStateOf<AiOutcome?>(null)
        private set
    var categoryId by mutableStateOf<Long?>(null)
    var saving by mutableStateOf(false)
        private set
    val categories = container.repo.categories.asUiState(viewModelScope)

    fun saveKey() { container.aiKey = apiKey }
    fun clearKey() { apiKey = ""; container.aiKey = "" }

    fun search() {
        if (loading) return
        container.aiKey = apiKey
        loading = true
        outcome = null
        viewModelScope.launch {
            outcome = container.ai.search(apiKey, query)
            loading = false
        }
    }

    fun save(onDone: (Outcome) -> Unit) {
        val o = outcome as? AiOutcome.Success ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            val r = try { AiImporter.save(container.repo, o.result, o.sources, categoryId) }
            catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر الحفظ") }
            saving = false
            onDone(r)
        }
    }
}

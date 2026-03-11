package com.example.melodist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.pages.HomePage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class HomeState {
    data class Success(val page: HomePage, val isLoadingMore: Boolean = false) : HomeState()
    data class Error(val message: String) : HomeState()
    object Loading : HomeState()
}

/**
 * @param loginState Flow que emite true/false al cambiar la sesión.
 *                   Se usa el valor ACTUAL para la carga inicial y se escuchan
 *                   cambios posteriores (drop(1)) para recargar al login/logout.
 */
class HomeViewModel(
    loginState: StateFlow<Boolean>? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _currentParams = MutableStateFlow<String?>(null)
    val currentParams: StateFlow<String?> = _currentParams.asStateFlow()

    init {
        // Carga inicial — el valor actual de loginState ya refleja si hay cookie
        loadHome()

        // Escuchar SOLO cambios futuros (drop(1) omite el valor actual ya procesado)
        // para recargar al hacer login o logout
        loginState?.drop(1)?.onEach {
            forceReload()
        }?.launchIn(viewModelScope)
    }

    /** Fuerza recarga ignorando la guarda de params. */
    fun forceReload(params: String? = _currentParams.value) {
        _currentParams.value = null // limpiar para que la guarda no bloquee
        loadHome(params)
    }

    fun loadHome(params: String? = null) {
        if (_currentParams.value == params && _uiState.value is HomeState.Success) return
        _currentParams.value = params
        _uiState.value = HomeState.Loading
        viewModelScope.launch {
            YouTube.home(params = params)
                .onSuccess { _uiState.value = HomeState.Success(it) }
                .onFailure { _uiState.value = HomeState.Error(it.message ?: "Error al cargar el home") }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState !is HomeState.Success || currentState.isLoadingMore) return
        val continuation = currentState.page.continuation ?: return
        _uiState.value = currentState.copy(isLoadingMore = true)
        viewModelScope.launch {
            YouTube.home(continuation = continuation)
                .onSuccess { newPage ->
                    _uiState.value = HomeState.Success(
                        page = newPage.copy(
                            sections = currentState.page.sections + newPage.sections,
                            chips = currentState.page.chips
                        ),
                        isLoadingMore = false
                    )
                }
                .onFailure { _uiState.value = currentState.copy(isLoadingMore = false) }
        }
    }
}
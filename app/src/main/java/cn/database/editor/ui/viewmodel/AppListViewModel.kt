package cn.database.editor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.service.AppInfo
import cn.database.editor.data.service.FileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppListUiState(
    val isLoading: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class AppListViewModel(
    private val fileService: FileService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            fileService.getInstalledAppsWithDatabases()
                .onSuccess { apps ->
                    _uiState.value = _uiState.value.copy(
                        apps = apps,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredApps(): List<AppInfo> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.apps
        } else {
            _uiState.value.apps.filter {
                it.appName.lowercase().contains(query) || 
                it.packageName.lowercase().contains(query)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

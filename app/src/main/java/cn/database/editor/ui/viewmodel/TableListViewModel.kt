package cn.database.editor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.model.TableInfo
import cn.database.editor.data.service.DatabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TableListUiState(
    val isLoading: Boolean = false,
    val dbPath: String = "",
    val tables: List<TableInfo> = emptyList(),
    val errorMessage: String? = null
)

class TableListViewModel(
    private val databaseService: DatabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableListUiState())
    val uiState: StateFlow<TableListUiState> = _uiState.asStateFlow()

    fun loadDatabase(dbPath: String) {
        if (_uiState.value.dbPath == dbPath && _uiState.value.tables.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, dbPath = dbPath)
            databaseService.openDatabase(dbPath)
                .onSuccess {
                    databaseService.getTables()
                        .onSuccess { tables ->
                            _uiState.value = _uiState.value.copy(
                                tables = tables,
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
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun refreshTables() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            databaseService.getTables()
                .onSuccess { tables ->
                    _uiState.value = _uiState.value.copy(
                        tables = tables,
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        databaseService.closeDatabase()
    }
}

package cn.database.editor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.model.ColumnInfo
import cn.database.editor.data.service.DatabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TableStructureUiState(
    val isLoading: Boolean = false,
    val tableName: String = "",
    val columns: List<ColumnInfo> = emptyList(),
    val errorMessage: String? = null
)

class TableStructureViewModel(
    private val databaseService: DatabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableStructureUiState())
    val uiState: StateFlow<TableStructureUiState> = _uiState.asStateFlow()

    fun loadTableStructure(dbPath: String, tableName: String) {
        if (_uiState.value.tableName == tableName && _uiState.value.columns.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, tableName = tableName, errorMessage = null)
            if (!databaseService.isDatabaseOpen()) {
                databaseService.openDatabase(dbPath)
            }
            databaseService.getTableColumns(tableName)
                .onSuccess { columns ->
                    _uiState.value = _uiState.value.copy(
                        columns = columns,
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
}

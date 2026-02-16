package cn.database.editor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.model.RowData
import cn.database.editor.data.service.DatabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QueryEditorUiState(
    val query: String = "",
    val results: List<RowData> = emptyList(),
    val columnNames: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val rowsAffected: Int = 0
)

class QueryEditorViewModel(
    private val databaseService: DatabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueryEditorUiState())
    val uiState: StateFlow<QueryEditorUiState> = _uiState.asStateFlow()

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun executeQuery() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "请输入SQL语句")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            if (query.lowercase().startsWith("select")) {
                databaseService.executeQuery(query)
                    .onSuccess { results ->
                        val columnNames = if (results.isNotEmpty()) {
                            results.first().values.keys.toList()
                        } else {
                            emptyList()
                        }
                        _uiState.value = _uiState.value.copy(
                            results = results,
                            columnNames = columnNames,
                            isLoading = false,
                            successMessage = "查询成功，返回 ${results.size} 行"
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message,
                            isLoading = false
                        )
                    }
            } else {
                databaseService.executeUpdate(query)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "执行成功",
                            results = emptyList(),
                            columnNames = emptyList()
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
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun initDatabase(dbPath: String) {
        if (!databaseService.isDatabaseOpen()) {
            viewModelScope.launch {
                databaseService.openDatabase(dbPath)
            }
        }
    }
}

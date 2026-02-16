package cn.database.editor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.model.ColumnInfo
import cn.database.editor.data.model.TableInfo
import cn.database.editor.data.service.DatabaseService
import cn.database.editor.ui.screen.DatabaseSchemaUiState
import cn.database.editor.ui.screen.TableSchemaInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DatabaseSchemaViewModel(
    private val databaseService: DatabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatabaseSchemaUiState())
    val uiState: StateFlow<DatabaseSchemaUiState> = _uiState.asStateFlow()

    fun loadSchema(dbPath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            if (!databaseService.isDatabaseOpen()) {
                databaseService.openDatabase(dbPath)
            }
            
            databaseService.getTables()
                .onSuccess { tables ->
                    val schemaInfos = mutableListOf<TableSchemaInfo>()
                    for (table in tables) {
                        databaseService.getTableColumns(table.name)
                            .onSuccess { columns ->
                                schemaInfos.add(TableSchemaInfo(table, columns))
                            }
                    }
                    _uiState.value = _uiState.value.copy(
                        tables = schemaInfos,
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

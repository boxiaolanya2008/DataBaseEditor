package cn.database.editor.ui.viewmodel

import android.content.ContentValues
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.database.editor.data.model.ColumnInfo
import cn.database.editor.data.model.RowData
import cn.database.editor.data.service.DatabaseService
import cn.database.editor.util.DatabaseOperation
import cn.database.editor.util.OperationHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TableDataUiState(
    val isLoading: Boolean = false,
    val tableName: String = "",
    val columns: List<ColumnInfo> = emptyList(),
    val rows: List<RowData> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val totalRows: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val canUndo: Boolean = false,
    val undoMessage: String? = null,
    val searchQuery: String = "",
    val searchColumn: String? = null,
    val isSearching: Boolean = false
)

class TableDataViewModel(
    private val databaseService: DatabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableDataUiState())
    val uiState: StateFlow<TableDataUiState> = _uiState.asStateFlow()
    
    private val operationHistory = OperationHistory()
    private var searchJob: Job? = null

    fun loadTableData(dbPath: String, tableName: String) {
        if (_uiState.value.tableName == tableName && _uiState.value.rows.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, tableName = tableName, errorMessage = null)
            if (!databaseService.isDatabaseOpen()) {
                databaseService.openDatabase(dbPath)
            }
            databaseService.getTableColumns(tableName)
                .onSuccess { columns ->
                    _uiState.value = _uiState.value.copy(columns = columns)
                    loadPage(tableName, 0)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
        }
    }

    private suspend fun loadPage(tableName: String, page: Int) {
        val pageSize = _uiState.value.pageSize
        val offset = page * pageSize
        val searchQuery = _uiState.value.searchQuery
        val searchColumn = _uiState.value.searchColumn
        
        databaseService.getTableData(tableName, pageSize, offset, searchQuery, searchColumn)
            .onSuccess { rows ->
                _uiState.value = _uiState.value.copy(
                    rows = rows,
                    currentPage = page,
                    isLoading = false,
                    totalRows = rows.size,
                    canUndo = operationHistory.canUndo(),
                    isSearching = searchQuery.isNotEmpty()
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message,
                    isLoading = false
                )
            }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadPage(_uiState.value.tableName, 0)
        }
    }

    fun setSearchColumn(column: String?) {
        _uiState.value = _uiState.value.copy(searchColumn = column)
        viewModelScope.launch {
            loadPage(_uiState.value.tableName, 0)
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", searchColumn = null, isSearching = false)
        viewModelScope.launch {
            loadPage(_uiState.value.tableName, 0)
        }
    }

    fun nextPage() {
        val current = _uiState.value
        if (current.rows.size == current.pageSize) {
            viewModelScope.launch {
                loadPage(current.tableName, current.currentPage + 1)
            }
        }
    }

    fun previousPage() {
        val current = _uiState.value
        if (current.currentPage > 0) {
            viewModelScope.launch {
                loadPage(current.tableName, current.currentPage - 1)
            }
        }
    }

    fun insertRow(values: Map<String, Any?>) {
        viewModelScope.launch {
            val tableName = _uiState.value.tableName
            databaseService.insertRowRaw(tableName, values)
                .onSuccess { rowId ->
                    operationHistory.addOperation(
                        DatabaseOperation.InsertOperation(tableName, values, rowId)
                    )
                    _uiState.value = _uiState.value.copy(
                        successMessage = "添加成功",
                        canUndo = true
                    )
                    loadPage(tableName, 0)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun deleteRow(row: RowData) {
        viewModelScope.launch {
            val tableName = _uiState.value.tableName
            val columns = _uiState.value.columns
            val pkColumn = columns.find { it.primaryKey }
            
            if (pkColumn != null) {
                val pkValue = row.values[pkColumn.name]
                val whereClause = if (pkValue is String) {
                    "${pkColumn.name} = '$pkValue'"
                } else {
                    "${pkColumn.name} = $pkValue"
                }
                
                databaseService.deleteRow(tableName, whereClause)
                    .onSuccess {
                        operationHistory.addOperation(
                            DatabaseOperation.DeleteOperation(tableName, row.values, whereClause)
                        )
                        _uiState.value = _uiState.value.copy(
                            successMessage = "删除成功",
                            canUndo = true
                        )
                        loadPage(tableName, _uiState.value.currentPage)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(errorMessage = error.message)
                    }
            } else {
                val rowid = row.values["rowid"]
                if (rowid != null) {
                    databaseService.deleteByRowId(tableName, rowid as Long)
                        .onSuccess {
                            operationHistory.addOperation(
                                DatabaseOperation.DeleteOperation(tableName, row.values, "rowid = $rowid")
                            )
                            _uiState.value = _uiState.value.copy(
                                successMessage = "删除成功",
                                canUndo = true
                            )
                            loadPage(tableName, _uiState.value.currentPage)
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(errorMessage = error.message)
                        }
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "无法定位此行")
                }
            }
        }
    }

    fun updateRow(oldValues: Map<String, Any?>, newValues: Map<String, Any?>) {
        viewModelScope.launch {
            val tableName = _uiState.value.tableName
            val columns = _uiState.value.columns
            val pkColumn = columns.find { it.primaryKey }
            
            val contentValues = ContentValues()
            newValues.forEach { (key, value) ->
                when (value) {
                    null -> contentValues.putNull(key)
                    is String -> contentValues.put(key, value)
                    is Int -> contentValues.put(key, value)
                    is Long -> contentValues.put(key, value)
                    is Float -> contentValues.put(key, value)
                    is Double -> contentValues.put(key, value)
                    is Boolean -> contentValues.put(key, if (value) 1 else 0)
                    else -> contentValues.put(key, value.toString())
                }
            }
            
            val whereClause = if (pkColumn != null) {
                val pkValue = oldValues[pkColumn.name]
                if (pkValue is String) {
                    "${pkColumn.name} = '$pkValue'"
                } else {
                    "${pkColumn.name} = $pkValue"
                }
            } else {
                val rowid = oldValues["rowid"]
                "rowid = $rowid"
            }
            
            databaseService.updateRow(tableName, contentValues, whereClause)
                .onSuccess {
                    operationHistory.addOperation(
                        DatabaseOperation.UpdateOperation(tableName, oldValues, newValues, whereClause)
                    )
                    _uiState.value = _uiState.value.copy(
                        successMessage = "更新成功",
                        canUndo = true
                    )
                    loadPage(tableName, _uiState.value.currentPage)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun undo() {
        viewModelScope.launch {
            val operation = operationHistory.removeLastOperation() ?: return@launch
            val tableName = _uiState.value.tableName
            
            when (operation) {
                is DatabaseOperation.InsertOperation -> {
                    databaseService.deleteByRowId(tableName, operation.rowId)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                successMessage = "已撤销添加操作",
                                canUndo = operationHistory.canUndo()
                            )
                            loadPage(tableName, _uiState.value.currentPage)
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(errorMessage = "撤销失败: ${error.message}")
                        }
                }
                is DatabaseOperation.DeleteOperation -> {
                    databaseService.insertRowRaw(tableName, operation.deletedValues)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                successMessage = "已撤销删除操作",
                                canUndo = operationHistory.canUndo()
                            )
                            loadPage(tableName, _uiState.value.currentPage)
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(errorMessage = "撤销失败: ${error.message}")
                        }
                }
                is DatabaseOperation.UpdateOperation -> {
                    val contentValues = ContentValues()
                    operation.oldValues.forEach { (key, value) ->
                        when (value) {
                            null -> contentValues.putNull(key)
                            is String -> contentValues.put(key, value)
                            is Int -> contentValues.put(key, value)
                            is Long -> contentValues.put(key, value)
                            is Float -> contentValues.put(key, value)
                            is Double -> contentValues.put(key, value)
                            is Boolean -> contentValues.put(key, if (value) 1 else 0)
                            else -> contentValues.put(key, value.toString())
                        }
                    }
                    databaseService.updateRow(tableName, contentValues, operation.whereClause)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                successMessage = "已撤销更新操作",
                                canUndo = operationHistory.canUndo()
                            )
                            loadPage(tableName, _uiState.value.currentPage)
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(errorMessage = "撤销失败: ${error.message}")
                        }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null, undoMessage = null)
    }
}

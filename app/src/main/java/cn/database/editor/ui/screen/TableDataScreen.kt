package cn.database.editor.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.database.editor.data.service.DatabaseService
import cn.database.editor.ui.component.DataRowCard
import cn.database.editor.ui.viewmodel.TableDataViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(
    dbPath: String,
    tableName: String,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val databaseService = remember { DatabaseService(context) }
    val viewModel = remember { TableDataViewModel(databaseService) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRow by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(dbPath, tableName) {
        viewModel.loadTableData(dbPath, tableName)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessages()
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessages()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tableName)
                        Text(
                            text = if (uiState.isSearching) "搜索结果" else "第 ${uiState.currentPage + 1} 页",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "搜索",
                            tint = if (showSearch || uiState.isSearching) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo, 
                            contentDescription = "撤销",
                            tint = if (uiState.canUndo) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加记录")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.rows.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { viewModel.previousPage() },
                            enabled = uiState.currentPage > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一页")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.canUndo) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "可撤销",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.undo() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Undo,
                                                contentDescription = "撤销",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "第 ${uiState.currentPage + 1} 页",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { viewModel.nextPage() },
                            enabled = uiState.rows.size == uiState.pageSize
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一页")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showSearch || uiState.isSearching) {
                SearchBar(
                    columns = uiState.columns.map { it.name },
                    searchQuery = uiState.searchQuery,
                    searchColumn = uiState.searchColumn,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onColumnChange = { viewModel.setSearchColumn(it) },
                    onClear = { 
                        viewModel.clearSearch()
                        showSearch = false
                    }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.rows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.isSearching) "未找到匹配的数据" else "表中没有数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!uiState.isSearching) {
                            androidx.compose.material3.FilledTonalButton(
                                onClick = { showAddDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("添加记录")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.rows) { row ->
                        DataRowCard(
                            values = row.values,
                            columnNames = uiState.columns.map { it.name },
                            onClick = {
                                selectedRow = row.values
                                showEditDialog = true
                            },
                            onLongClick = {
                                viewModel.deleteRow(row)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedRow != null) {
        EditRowDialog(
            columns = uiState.columns,
            currentValues = selectedRow!!,
            onDismiss = { showEditDialog = false },
            onSave = { newValues ->
                viewModel.updateRow(selectedRow!!, newValues)
                showEditDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddRowDialog(
            columns = uiState.columns,
            onDismiss = { showAddDialog = false },
            onAdd = { values ->
                viewModel.insertRow(values)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    columns: List<String>,
    searchQuery: String,
    searchColumn: String?,
    onQueryChange: (String) -> Unit,
    onColumnChange: (String?) -> Unit,
    onClear: () -> Unit
) {
    var columnExpanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = columnExpanded,
                onExpandedChange = { columnExpanded = it }
            ) {
                OutlinedTextField(
                    value = searchColumn ?: "所有列",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = columnExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .width(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = columnExpanded,
                    onDismissRequest = { columnExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("所有列") },
                        onClick = {
                            onColumnChange(null)
                            columnExpanded = false
                        }
                    )
                    columns.forEach { column ->
                        DropdownMenuItem(
                            text = { Text(column) },
                            onClick = {
                                onColumnChange(column)
                                columnExpanded = false
                            }
                        )
                    }
                }
            }
            
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = "关闭搜索")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRowDialog(
    columns: List<cn.database.editor.data.model.ColumnInfo>,
    onDismiss: () -> Unit,
    onAdd: (Map<String, Any?>) -> Unit
) {
    val editedValues = remember {
        mutableStateOf(
            columns.associate { column ->
                column.name to column.defaultValue
            }.toMutableMap()
        )
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加记录") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "填写以下字段信息",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                columns.forEach { column ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = column.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (column.primaryKey) FontWeight.Bold else FontWeight.Normal
                            )
                            if (column.primaryKey) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "主键",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (column.notNull) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "*",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = column.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = editedValues.value[column.name]?.toString() ?: "",
                            onValueChange = { newValue ->
                                editedValues.value[column.name] = if (newValue.isEmpty()) null else newValue
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = {
                                Text(
                                    text = column.defaultValue ?: "输入${column.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onAdd(editedValues.value.toMap()) }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRowDialog(
    columns: List<cn.database.editor.data.model.ColumnInfo>,
    currentValues: Map<String, Any?>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any?>) -> Unit
) {
    val editedValues = remember { mutableStateOf(currentValues.toMutableMap()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑数据") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                columns.forEach { column ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = column.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (column.primaryKey) FontWeight.Bold else FontWeight.Normal
                            )
                            if (column.primaryKey) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "主键",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = column.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = editedValues.value[column.name]?.toString() ?: "",
                            onValueChange = { newValue ->
                                editedValues.value[column.name] = if (newValue.isEmpty()) null else newValue
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            enabled = !column.primaryKey
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onSave(editedValues.value.toMap()) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

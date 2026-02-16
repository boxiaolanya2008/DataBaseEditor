package cn.database.editor.data.model

data class TableInfo(
    val name: String,
    val rowCount: Int = 0
)

data class ColumnInfo(
    val name: String,
    val type: String,
    val notNull: Boolean,
    val defaultValue: String?,
    val primaryKey: Boolean
)

data class RowData(
    val values: Map<String, Any?>
)

data class DatabaseFile(
    val path: String,
    val name: String,
    val size: Long,
    val isReadable: Boolean = true
)

data class AppDatabase(
    val packageName: String,
    val appName: String,
    val databases: List<String>
)

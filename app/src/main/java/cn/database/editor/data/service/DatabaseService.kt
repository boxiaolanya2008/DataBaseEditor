package cn.database.editor.data.service

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import cn.database.editor.data.model.ColumnInfo
import cn.database.editor.data.model.RowData
import cn.database.editor.data.model.TableInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DatabaseService(private val context: Context) {

    private var database: SQLiteDatabase? = null
    private var currentPath: String? = null

    suspend fun openDatabase(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            closeDatabase()
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("数据库文件不存在"))
            }
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
            currentPath = path
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun closeDatabase() {
        database?.close()
        database = null
        currentPath = null
    }

    suspend fun getTables(): Result<List<TableInfo>> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val tables = mutableListOf<TableInfo>()
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                null
            )
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                val countCursor = db.rawQuery("SELECT COUNT(*) FROM \"$tableName\"", null)
                val rowCount = if (countCursor.moveToFirst()) countCursor.getInt(0) else 0
                countCursor.close()
                tables.add(TableInfo(tableName, rowCount))
            }
            cursor.close()
            Result.success(tables.sortedBy { it.name.lowercase() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTableColumns(tableName: String): Result<List<ColumnInfo>> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val columns = mutableListOf<ColumnInfo>()
            val cursor = db.rawQuery("PRAGMA table_info(\"$tableName\")", null)
            while (cursor.moveToNext()) {
                columns.add(
                    ColumnInfo(
                        name = cursor.getString(1),
                        type = cursor.getString(2),
                        notNull = cursor.getInt(3) == 1,
                        defaultValue = cursor.getString(4),
                        primaryKey = cursor.getInt(5) == 1
                    )
                )
            }
            cursor.close()
            Result.success(columns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTableData(
        tableName: String,
        limit: Int = 100,
        offset: Int = 0,
        searchQuery: String? = null,
        searchColumn: String? = null
    ): Result<List<RowData>> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val rows = mutableListOf<RowData>()
            
            val sql = if (!searchQuery.isNullOrEmpty() && !searchColumn.isNullOrEmpty()) {
                "SELECT * FROM \"$tableName\" WHERE \"$searchColumn\" LIKE '%$searchQuery%' LIMIT $limit OFFSET $offset"
            } else if (!searchQuery.isNullOrEmpty()) {
                val columns = getTableColumnsSync(tableName)
                if (columns.isNotEmpty()) {
                    val whereClause = columns.joinToString(" OR ") { "\"${it.name}\" LIKE '%$searchQuery%'" }
                    "SELECT * FROM \"$tableName\" WHERE $whereClause LIMIT $limit OFFSET $offset"
                } else {
                    "SELECT * FROM \"$tableName\" LIMIT $limit OFFSET $offset"
                }
            } else {
                "SELECT * FROM \"$tableName\" LIMIT $limit OFFSET $offset"
            }
            
            val cursor = db.rawQuery(sql, null)
            val columnNames = cursor.columnNames
            while (cursor.moveToNext()) {
                val values = mutableMapOf<String, Any?>()
                columnNames.forEachIndexed { index, name ->
                    values[name] = when (cursor.getType(index)) {
                        android.database.Cursor.FIELD_TYPE_NULL -> null
                        android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        android.database.Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                        android.database.Cursor.FIELD_TYPE_BLOB -> "[BLOB ${cursor.getBlob(index).size} bytes]"
                        else -> null
                    }
                }
                rows.add(RowData(values))
            }
            cursor.close()
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTableColumnsSync(tableName: String): List<ColumnInfo> {
        val db = database ?: return emptyList()
        val columns = mutableListOf<ColumnInfo>()
        val cursor = db.rawQuery("PRAGMA table_info(\"$tableName\")", null)
        while (cursor.moveToNext()) {
            columns.add(
                ColumnInfo(
                    name = cursor.getString(1),
                    type = cursor.getString(2),
                    notNull = cursor.getInt(3) == 1,
                    defaultValue = cursor.getString(4),
                    primaryKey = cursor.getInt(5) == 1
                )
            )
        }
        cursor.close()
        return columns
    }

    suspend fun executeQuery(sql: String): Result<List<RowData>> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val rows = mutableListOf<RowData>()
            val cursor = db.rawQuery(sql, null)
            val columnNames = cursor.columnNames
            while (cursor.moveToNext()) {
                val values = mutableMapOf<String, Any?>()
                columnNames.forEachIndexed { index, name ->
                    values[name] = when (cursor.getType(index)) {
                        android.database.Cursor.FIELD_TYPE_NULL -> null
                        android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        android.database.Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                        android.database.Cursor.FIELD_TYPE_BLOB -> "[BLOB]"
                        else -> null
                    }
                }
                rows.add(RowData(values))
            }
            cursor.close()
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeUpdate(sql: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            db.execSQL(sql)
            Result.success(1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertRow(
        tableName: String,
        values: ContentValues
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val id = db.insert(tableName, null, values)
            if (id == -1L) {
                Result.failure(Exception("插入失败"))
            } else {
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRow(tableName: String, whereClause: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val count = db.delete(tableName, whereClause, null)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRow(
        tableName: String,
        values: ContentValues,
        whereClause: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val count = db.update(tableName, values, whereClause, null)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertRowRaw(tableName: String, values: Map<String, Any?>): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val contentValues = ContentValues()
            values.forEach { (key, value) ->
                when (value) {
                    null -> contentValues.putNull(key)
                    is String -> contentValues.put(key, value)
                    is Int -> contentValues.put(key, value)
                    is Long -> contentValues.put(key, value)
                    is Float -> contentValues.put(key, value)
                    is Double -> contentValues.put(key, value)
                    is Boolean -> contentValues.put(key, if (value) 1 else 0)
                    is ByteArray -> contentValues.put(key, value)
                    else -> contentValues.put(key, value.toString())
                }
            }
            val id = db.insert(tableName, null, contentValues)
            if (id == -1L) {
                Result.failure(Exception("插入失败"))
            } else {
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteByRowId(tableName: String, rowId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val count = db.delete(tableName, "rowid = ?", arrayOf(rowId.toString()))
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRowByRowId(tableName: String, rowId: Long): Result<RowData?> = withContext(Dispatchers.IO) {
        try {
            val db = database ?: return@withContext Result.failure(Exception("数据库未打开"))
            val cursor = db.rawQuery("SELECT * FROM \"$tableName\" WHERE rowid = ?", arrayOf(rowId.toString()))
            var row: RowData? = null
            if (cursor.moveToFirst()) {
                val values = mutableMapOf<String, Any?>()
                val columnNames = cursor.columnNames
                columnNames.forEachIndexed { index, name ->
                    values[name] = when (cursor.getType(index)) {
                        android.database.Cursor.FIELD_TYPE_NULL -> null
                        android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        android.database.Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                        android.database.Cursor.FIELD_TYPE_BLOB -> "[BLOB]"
                        else -> null
                    }
                }
                row = RowData(values)
            }
            cursor.close()
            Result.success(row)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentPath(): String? = currentPath
    fun isDatabaseOpen(): Boolean = database?.isOpen == true
}

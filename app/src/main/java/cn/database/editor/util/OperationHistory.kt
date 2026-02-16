package cn.database.editor.util

sealed class DatabaseOperation {
    abstract val tableName: String
    
    data class InsertOperation(
        override val tableName: String,
        val insertedValues: Map<String, Any?>,
        val rowId: Long
    ) : DatabaseOperation()
    
    data class UpdateOperation(
        override val tableName: String,
        val oldValues: Map<String, Any?>,
        val newValues: Map<String, Any?>,
        val whereClause: String
    ) : DatabaseOperation()
    
    data class DeleteOperation(
        override val tableName: String,
        val deletedValues: Map<String, Any?>,
        val whereClause: String
    ) : DatabaseOperation()
}

class OperationHistory {
    private val operations = mutableListOf<DatabaseOperation>()
    private val maxSize = 50
    
    fun addOperation(operation: DatabaseOperation) {
        if (operations.size >= maxSize) {
            operations.removeAt(0)
        }
        operations.add(operation)
    }
    
    fun getLastOperation(): DatabaseOperation? = operations.lastOrNull()
    
    fun removeLastOperation(): DatabaseOperation? {
        return if (operations.isNotEmpty()) operations.removeLast() else null
    }
    
    fun canUndo(): Boolean = operations.isNotEmpty()
    
    fun clear() {
        operations.clear()
    }
    
    fun getOperationCount(): Int = operations.size
}

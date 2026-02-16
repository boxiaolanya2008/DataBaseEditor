package cn.database.editor.ui.navigation

sealed class Screen(val route: String) {
    object Tutorial : Screen("tutorial")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object AppList : Screen("app_list")
    object TableList : Screen("table_list/{dbPath}") {
        fun createRoute(dbPath: String) = "table_list/${dbPath.encode()}"
    }
    object TableData : Screen("table_data/{dbPath}/{tableName}") {
        fun createRoute(dbPath: String, tableName: String) =
            "table_data/${dbPath.encode()}/${tableName.encode()}"
    }
    object TableStructure : Screen("table_structure/{dbPath}/{tableName}") {
        fun createRoute(dbPath: String, tableName: String) =
            "table_structure/${dbPath.encode()}/${tableName.encode()}"
    }
    object QueryEditor : Screen("query_editor/{dbPath}") {
        fun createRoute(dbPath: String) = "query_editor/${dbPath.encode()}"
    }
    object DatabaseSchema : Screen("database_schema/{dbPath}") {
        fun createRoute(dbPath: String) = "database_schema/${dbPath.encode()}"
    }
}

fun String.encode(): String = java.net.URLEncoder.encode(this, "UTF-8")
fun String.decode(): String = java.net.URLDecoder.decode(this, "UTF-8")

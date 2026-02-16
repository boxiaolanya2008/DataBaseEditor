package cn.database.editor.data.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import cn.database.editor.data.model.AppDatabase
import cn.database.editor.data.model.DatabaseFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable,
    val databases: List<DatabaseFile>
)

class FileService(private val context: Context) {

    suspend fun getLocalDatabases(): Result<List<DatabaseFile>> = withContext(Dispatchers.IO) {
        try {
            val databases = mutableListOf<DatabaseFile>()
            val dbDir = File(context.applicationInfo.dataDir, "databases")
            if (dbDir.exists() && dbDir.isDirectory) {
                dbDir.listFiles()?.filter { it.isFile && !it.name.endsWith("-journal") && !it.name.endsWith("-wal") }
                    ?.forEach { file ->
                        databases.add(
                            DatabaseFile(
                                path = file.absolutePath,
                                name = file.name,
                                size = file.length(),
                                isReadable = file.canRead()
                            )
                        )
                    }
            }
            Result.success(databases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInstalledAppsWithDatabases(): Result<List<AppInfo>> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .mapNotNull { appInfo ->
                    val databases = getAppDatabasesAsFiles(appInfo.packageName)
                    if (databases.isNotEmpty()) {
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo),
                            databases = databases
                        )
                    } else {
                        null
                    }
                }
            Result.success(apps.sortedBy { it.appName.lowercase() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getAppDatabasesAsFiles(packageName: String): List<DatabaseFile> {
        return try {
            val dbPath = "/data/data/$packageName/databases"
            val dir = File(dbPath)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()
                    ?.filter { it.isFile && !it.name.endsWith("-journal") && !it.name.endsWith("-wal") }
                    ?.map { file ->
                        DatabaseFile(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            isReadable = file.canRead()
                        )
                    }
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            os.write("exit\n".toByteArray())
            os.flush()
            os.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun copyDatabaseWithRoot(dbPath: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "root_db_${System.currentTimeMillis()}.db")
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat \"$dbPath\""))
            val input = process.inputStream
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            input.close()
            process.waitFor()
            if (tempFile.exists() && tempFile.length() > 0) {
                Result.success(tempFile)
            } else {
                tempFile.delete()
                Result.failure(Exception("无法读取数据库文件"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package cn.database.editor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class WorkMode {
    NORMAL,
    ROOT
}

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val HAS_SHOWN_TUTORIAL = booleanPreferencesKey("has_shown_tutorial")
        val WORK_MODE = stringPreferencesKey("work_mode")
    }

    val hasShownTutorial: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SHOWN_TUTORIAL] ?: false
        }

    val workMode: Flow<WorkMode> = context.dataStore.data
        .map { preferences ->
            when (preferences[PreferencesKeys.WORK_MODE]) {
                "ROOT" -> WorkMode.ROOT
                else -> WorkMode.NORMAL
            }
        }

    suspend fun setTutorialShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SHOWN_TUTORIAL] = shown
        }
    }

    suspend fun setWorkMode(mode: WorkMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORK_MODE] = mode.name
        }
    }
}

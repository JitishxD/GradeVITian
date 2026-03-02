package me.jitish.gradevitian.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.jitish.gradevitian.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    }

    override fun observeDarkMode(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[DARK_MODE_KEY] ?: false }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = enabled }
    }

    override fun observeDynamicColor(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[DYNAMIC_COLOR_KEY] ?: true }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DYNAMIC_COLOR_KEY] = enabled }
    }
}


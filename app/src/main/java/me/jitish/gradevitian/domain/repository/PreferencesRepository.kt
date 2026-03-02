package me.jitish.gradevitian.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    fun observeDynamicColor(): Flow<Boolean>
    suspend fun setDynamicColor(enabled: Boolean)
}


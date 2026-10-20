package com.example.macmovilcare.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_USUARIO_ID = stringPreferencesKey("usuario_id")
        val KEY_ROL = stringPreferencesKey("usuario_rol")
    }

    val usuarioRol: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ROL]
    }

    suspend fun guardarSesion(usuarioId: String, rol: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USUARIO_ID] = usuarioId
            preferences[KEY_ROL] = rol
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
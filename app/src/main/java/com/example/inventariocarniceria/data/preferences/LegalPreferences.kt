package com.example.inventariocarniceria.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.legalDataStore: DataStore<Preferences> by preferencesDataStore(name = "sgnia_legal_prefs")

class LegalPreferences(private val context: Context) {

    companion object {
        val KEY_TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val KEY_TERMS_VERSION = stringPreferencesKey("terms_version")
        val KEY_ACCEPTANCE_DATE = stringPreferencesKey("acceptance_date")
        val KEY_ACCEPTANCE_ROLE = stringPreferencesKey("acceptance_role")
    }

    val isTermsAccepted: Flow<Boolean> = context.legalDataStore.data.map { preferences ->
        preferences[KEY_TERMS_ACCEPTED] ?: false
    }

    val termsVersion: Flow<String> = context.legalDataStore.data.map { preferences ->
        preferences[KEY_TERMS_VERSION] ?: ""
    }

    val acceptanceDate: Flow<String> = context.legalDataStore.data.map { preferences ->
        preferences[KEY_ACCEPTANCE_DATE] ?: ""
    }

    val acceptanceRole: Flow<String> = context.legalDataStore.data.map { preferences ->
        preferences[KEY_ACCEPTANCE_ROLE] ?: ""
    }

    suspend fun saveAcceptance(version: String, date: String, role: String) {
        context.legalDataStore.edit { preferences ->
            preferences[KEY_TERMS_ACCEPTED] = true
            preferences[KEY_TERMS_VERSION] = version
            preferences[KEY_ACCEPTANCE_DATE] = date
            preferences[KEY_ACCEPTANCE_ROLE] = role
        }
    }
}

package com.example.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val ADDRESS_KEY = stringPreferencesKey("address")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val ACCOUNT_ID_KEY = stringPreferencesKey("account_id")
    }

    val tokenFlow: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val addressFlow: Flow<String?> = dataStore.data.map { it[ADDRESS_KEY] }
    val passwordFlow: Flow<String?> = dataStore.data.map { it[PASSWORD_KEY] }
    val accountIdFlow: Flow<String?> = dataStore.data.map { it[ACCOUNT_ID_KEY] }

    suspend fun saveAccount(address: String, password: String, token: String, accountId: String) {
        dataStore.edit { prefs ->
            prefs[ADDRESS_KEY] = address
            prefs[PASSWORD_KEY] = password
            prefs[TOKEN_KEY] = token
            prefs[ACCOUNT_ID_KEY] = accountId
        }
    }

    suspend fun clearAccount() {
        dataStore.edit { prefs ->
            prefs.remove(ADDRESS_KEY)
            prefs.remove(PASSWORD_KEY)
            prefs.remove(TOKEN_KEY)
            prefs.remove(ACCOUNT_ID_KEY)
        }
    }
}

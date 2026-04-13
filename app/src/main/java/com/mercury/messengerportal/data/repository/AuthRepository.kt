package com.mercury.messengerportal.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mercury.messengerportal.data.dummy.DUMMY_MESSENGER
import com.mercury.messengerportal.domain.model.Messenger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val MESSENGER_ID = stringPreferencesKey("messenger_id")
        val MESSENGER_NAME = stringPreferencesKey("messenger_name")
        val EMPLOYEE_ID = stringPreferencesKey("employee_id")
        val PHONE = stringPreferencesKey("phone")
        val TOKEN = stringPreferencesKey("token")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TOKEN] != null
    }

    val currentMessenger: Flow<Messenger?> = context.dataStore.data.map { prefs ->
        val id = prefs[Keys.MESSENGER_ID] ?: return@map null
        Messenger(
            id = id,
            name = prefs[Keys.MESSENGER_NAME] ?: "",
            employeeId = prefs[Keys.EMPLOYEE_ID] ?: "",
            phone = prefs[Keys.PHONE] ?: ""
        )
    }

    /**
     * Dummy login — accepts any non-empty credentials.
     * Replace body with real API call when backend is ready.
     */
    suspend fun login(employeeId: String, password: String): Result<Messenger> {
        if (employeeId.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Employee ID and password required"))
        }

        // TODO: Replace with real API call
        val messenger = DUMMY_MESSENGER.copy(employeeId = employeeId)

        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = "dummy-jwt-token"
            prefs[Keys.MESSENGER_ID] = messenger.id
            prefs[Keys.MESSENGER_NAME] = messenger.name
            prefs[Keys.EMPLOYEE_ID] = messenger.employeeId
            prefs[Keys.PHONE] = messenger.phone
        }

        return Result.success(messenger)
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getMessengerId(): String? {
        var id: String? = null
        context.dataStore.data.collect { prefs ->
            id = prefs[Keys.MESSENGER_ID]
            return@collect
        }
        return id
    }
}

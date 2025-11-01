package ru.netology.nework.auth

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)
        val avatar = prefs.getString(AVATAR_KEY, null)

        if (token == null || id == 0L) {
            _authState.value = AuthState()
            prefs.edit().clear().apply()
        } else {
            _authState.value = AuthState(id, token, avatar)
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String, avatar: String?) {
        _authState.value = AuthState(id, token, avatar)
        prefs.edit()
            .putLong(ID_KEY, id)
            .putString(TOKEN_KEY, token)
            .putString(AVATAR_KEY, avatar)
            .apply()
    }

    @Synchronized
    fun setAvatar(avatar: String) {
        val currentState = _authState.value
        _authState.value = currentState.copy(avatar = avatar)
        prefs.edit()
            .putString(AVATAR_KEY, avatar)
            .apply()
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        prefs.edit().clear().apply()
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val AVATAR_KEY = "AVATAR_KEY"
    }

    data class AuthState(val id: Long = 0, val token: String? = null, val avatar: String? = null)
}

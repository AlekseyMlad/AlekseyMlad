package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject
import ru.netology.nework.error.AppError
import ru.netology.nework.util.SingleLiveEvent

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _data = MutableLiveData<SignInState>()
    val data: LiveData<SignInState> = _data

    private val _toastEvent = SingleLiveEvent<String>()
    val toastEvent: LiveData<String> = _toastEvent

    fun login(login: String, pass: String) {
        viewModelScope.launch {
            _data.value = SignInState(loading = true)
            try {
                val token = userRepository.login(login, pass)
                appAuth.setAuth(token.id, token.token, token.avatar)
                _data.value = SignInState(success = true)
            } catch (e: AppError) {
                if (e is AppError.ApiError && e.status == 400) {
                    _toastEvent.value = "Неправильный логин или пароль"
                }
                _data.value = SignInState(error = true)
            } catch (_: Exception) {
                _data.value = SignInState(error = true)
            }
        }
    }
}

data class SignInState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false
)

package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.repository.UserRepository
import java.io.File
import javax.inject.Inject
import ru.netology.nework.error.AppError
import ru.netology.nework.util.SingleLiveEvent

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _data = MutableLiveData<SignUpState>()
    val data: LiveData<SignUpState> = _data

    private val _avatar = MutableLiveData<Avatar>()
    val avatar: LiveData<Avatar> = _avatar

    private val _toastEvent = SingleLiveEvent<String>()
    val toastEvent: LiveData<String> = _toastEvent

    fun register(login: String, pass: String, name: String) {
        if (login.isBlank() || pass.isBlank() || name.isBlank()) {
            _toastEvent.value = "Все поля должны быть заполнены"
            return
        }
        viewModelScope.launch {
            _data.value = SignUpState(loading = true)
            try {
                val avatarPart = avatar.value?.file?.let {
                    val requestBody = it.readBytes().toRequestBody("*/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", it.name, requestBody)
                }
                val token = userRepository.register(login, pass, name, avatarPart)
                appAuth.setAuth(token.id, token.token, token.avatar)
                _data.value = SignUpState(success = true)
            } catch (e: AppError) {
                if (e is AppError.ApiError && e.status == 400) {
                    _toastEvent.value = "Пользователь с таким логином уже зарегистрирован"
                }
                _data.value = SignUpState(error = true)
            } catch (_: Exception) {
                _data.value = SignUpState(error = true)
            }
        }
    }

    fun setAvatar(uri: Uri, file: File) {
        _avatar.value = Avatar(uri, file)
    }
}

data class SignUpState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false
)

data class Avatar(
    val uri: Uri,
    val file: File
)

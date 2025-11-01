package ru.netology.nework.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject
import ru.netology.nework.error.AppError

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val repository: UserRepository,
    private val postRepository: PostRepository,
    private val appAuth: AppAuth,
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> = _user

    private val _error = MutableLiveData<AppError>()
    val error: LiveData<AppError> = _error

    init {
        loadUser()
    }

    fun isCurrentUser(userId: Long): Boolean {
        return appAuth.authState.value.id == userId
    }

    fun changeAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = application.contentResolver.openInputStream(uri)
                val requestBody = inputStream?.readBytes()?.toRequestBody("*/*".toMediaTypeOrNull())
                val file = requestBody?.let {
                    MultipartBody.Part.createFormData(
                        "file", "avatar.jpg", it
                    )
                }
                if (file != null) {
                    val media = postRepository.saveMedia(file)
                    appAuth.setAvatar(media.url)
                    loadUser()
                }
            } catch (e: Exception) {
                _error.value = AppError.from(e)
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userId = savedStateHandle.get<Long>("userId") ?: return@launch
            try {
                val user = repository.getUserById(userId)
                _user.value = user
            } catch (e: Exception) {
                _error.value = AppError.from(e)
            }
        }
    }
}

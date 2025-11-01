package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.util.SingleLiveEvent
import ru.netology.nework.error.AppError

data class FeedModelState(
    val items: List<DisplayableItem> = emptyList(),
    val loading: Boolean = false,
    val error: AppError? = null,
    val empty: Boolean = false,
)

abstract class BaseViewModel : ViewModel() {

    protected val _data = MutableLiveData(FeedModelState())
    val data: LiveData<FeedModelState> = _data

    abstract fun load()

    abstract fun likeById(id: Long)
    abstract fun unlikeById(id: Long)
    abstract fun removeById(id: Long)

    protected val _errorEvent = SingleLiveEvent<AppError>()
    val errorEvent: LiveData<AppError> = _errorEvent

    protected fun <T> observeData(flow: Flow<List<T>>, mapper: (T) -> DisplayableItem) {
        viewModelScope.launch {
            flow.collectLatest {
                _data.value = FeedModelState(items = it.map(mapper), empty = it.isEmpty())
            }
        }
    }

    protected fun <T> performUpdate(
        action: suspend () -> T,
        update: (List<DisplayableItem>, T) -> List<DisplayableItem>
    ) {
        viewModelScope.launch {
            try {
                val result = action()
                _data.value = _data.value?.copy(items = update(_data.value?.items.orEmpty(), result))
            } catch (e: AppError) {
                _errorEvent.value = e
            } catch (e: Exception) {
                _errorEvent.value = AppError.from(e)
            }
        }
    }

    protected fun <T> loadData(
        loader: suspend () -> List<T>,
        mapper: (T) -> DisplayableItem
    ) {
        viewModelScope.launch {
            _data.value = FeedModelState(loading = true)
            try {
                val result = loader()
                _data.value = FeedModelState(items = result.map(mapper), empty = result.isEmpty())
            } catch (e: AppError) {
                _errorEvent.value = e
            } catch (e: Exception) {
                _errorEvent.value = AppError.from(e)
            }
        }
    }
}

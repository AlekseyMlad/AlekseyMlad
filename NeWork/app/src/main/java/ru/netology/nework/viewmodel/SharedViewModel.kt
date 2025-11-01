package ru.netology.nework.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.dto.FabAction
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private val _fabAction = MutableStateFlow(FabAction.NONE)
    val fabAction = _fabAction.asStateFlow()

    fun setFabAction(action: FabAction) {
        _fabAction.value = action
    }
}
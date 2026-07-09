package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.MessageDetail
import com.example.api.MessageIntro
import com.example.data.AuthManager
import com.example.data.EmailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.firstOrNull

class MainViewModel(
    private val repository: EmailRepository,
    val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                if (authManager.tokenFlow.firstOrNull() != null) {
                    refreshInbox()
                }
                delay(5000)
            }
        }
    }

    suspend fun checkToken() {
        authManager.tokenFlow.collect {
            if (it != null) {
                refreshInbox()
            }
        }
    }

    fun generateNewEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.generateNewAccount()
                refreshInbox()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun refreshInbox() {
        viewModelScope.launch {
            try {
                val messages = repository.getMessages()
                _uiState.value = _uiState.value.copy(messages = messages, isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun openMessage(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMessageLoading = true, error = null)
            try {
                val detail = repository.getMessageDetail(id)
                _uiState.value = _uiState.value.copy(selectedMessage = detail, isMessageLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isMessageLoading = false)
            }
        }
    }

    fun closeMessage() {
        _uiState.value = _uiState.value.copy(selectedMessage = null)
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(id)
                if (_uiState.value.selectedMessage?.id == id) {
                    closeMessage()
                }
                refreshInbox()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteCurrentAccount() {
        viewModelScope.launch {
            authManager.clearAccount()
            _uiState.value = UiState()
        }
    }
}

data class UiState(
    val isLoading: Boolean = false,
    val messages: List<MessageIntro> = emptyList(),
    val selectedMessage: MessageDetail? = null,
    val isMessageLoading: Boolean = false,
    val error: String? = null
)

class MainViewModelFactory(
    private val repository: EmailRepository,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.practicaltrainingproject.presentation.ui.loginScreen.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practicaltrainingproject.data.auth.AuthRepository
import com.example.practicaltrainingproject.data.auth.AuthState
import com.example.practicaltrainingproject.data.auth.Gender
import com.example.practicaltrainingproject.data.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Temporary user data for registration
    private val _tempUserData = MutableStateFlow(
        User(
            email = "",
            name = "",
            age = 0,
            gender = Gender.PREFER_NOT_TO_SAY,
            profession = "",
            healthConditions = emptyList(),
            emergencyContact = ""
        )
    )
    val tempUserData: StateFlow<User> = _tempUserData.asStateFlow()

    init {
        _authState.value = AuthState.Unauthenticated
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Login failed")
            }
        }
    }

    fun startSignup() {
        _authState.value = AuthState.SignupInProgress
    }

    fun register(password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(_tempUserData.value, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                // Clear temporary data after successful registration
                resetTempData()
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Registration failed")
            }
        }
    }

    // Methods to update temporary user data
    fun updateTempEmail(email: String) {
        _tempUserData.value = _tempUserData.value.copy(email = email)
    }

    fun updateTempName(name: String) {
        _tempUserData.value = _tempUserData.value.copy(name = name)
    }

    fun updateTempAge(age: Int) {
        _tempUserData.value = _tempUserData.value.copy(age = age)
    }

    fun updateTempGender(gender: Gender) {
        _tempUserData.value = _tempUserData.value.copy(gender = gender)
    }

    fun updateTempProfession(profession: String) {
        _tempUserData.value = _tempUserData.value.copy(profession = profession)
    }

    fun updateTempHealthConditions(conditions: List<String>) {
        _tempUserData.value = _tempUserData.value.copy(healthConditions = conditions)
    }

    fun updateTempEmergencyContact(contact: String) {
        _tempUserData.value = _tempUserData.value.copy(emergencyContact = contact)
    }

    /**
     * Update current user profile with new data
     */
    fun updateUserProfile(
        name: String,
        age: Int,
        gender: Gender,
        profession: String,
        healthConditions: List<String>,
        emergencyContact: String,
        emergencyContacts: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val currentUserData = _currentUser.value ?: return@launch

            val updatedUser = currentUserData.copy(
                name = name,
                age = age,
                gender = gender,
                profession = profession,
                healthConditions = healthConditions,
                emergencyContact = emergencyContact,
                emergencyContacts = emergencyContacts
            )

            val result = authRepository.updateUserProfile(updatedUser)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Update failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            resetTempData()
        }
    }

    fun cancelSignup() {
        _authState.value = AuthState.Unauthenticated
        resetTempData()
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetTempData() {
        _tempUserData.value = User(
            email = "",
            name = "",
            age = 0,
            gender = Gender.PREFER_NOT_TO_SAY,
            profession = "",
            healthConditions = emptyList(),
            emergencyContact = ""
        )
    }
}
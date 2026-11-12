package com.example.practicaltrainingproject.data.auth

class AuthRepository {

    suspend fun login(email: String, password: String): Result<User> {
        kotlinx.coroutines.delay(1000)

        return if (isValidCredentials(email, password)) {
            // In real app, fetch user data from backend
            Result.success(
                User(
                    email = email,
                    name = "John Doe", // This would come from backend
                    age = 30,
                    gender = Gender.MALE,
                    profession = "Teacher",
                    healthConditions = listOf("Hypertension"),
                    emergencyContact = "+212672987878",
                    emergencyContacts = listOf("+212672987878", "+1234567890"), // Multiple contacts
                    isLoggedIn = true
                )
            )
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun register(userData: User, password: String): Result<User> {
        kotlinx.coroutines.delay(1000)

        return if (isValidRegistration(userData.email, password)) {
            Result.success(
                userData.copy(isLoggedIn = true)
            )
        } else {
            Result.failure(Exception("Registration failed"))
        }
    }

    suspend fun updateUserProfile(updatedUser: User): Result<User> {
        kotlinx.coroutines.delay(500) // Simulate network delay

        // In real app, this would send data to backend
        return Result.success(updatedUser.copy(isLoggedIn = true))
    }

    fun logout(): Result<Unit> {
        return Result.success(Unit)
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        return email.isNotBlank() && password == "password123"
    }

    private fun isValidRegistration(email: String, password: String): Boolean {
        return email.isNotBlank() && password.length >= 6
    }
}
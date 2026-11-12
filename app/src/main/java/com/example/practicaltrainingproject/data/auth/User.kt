package com.example.practicaltrainingproject.data.auth


data class User(
    val email: String,
    val name: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.OTHER,
    val profession: String = "",
    val healthConditions: List<String> = emptyList(),
    val emergencyContact: String = "", // Keep for backward compatibility
    val emergencyContacts: List<String> = emptyList(), // New: Multiple contacts
    val isLoggedIn: Boolean = false
) {
    val displayName: String
        get() = if (name.isNotBlank()) name else email.substringBefore("@")

    // Helper to get all emergency contacts (both old and new format)
    val allEmergencyContacts: List<String>
        get() = buildList {
            if (emergencyContact.isNotBlank() && emergencyContact !in emergencyContacts) {
                add(emergencyContact)
            }
            addAll(emergencyContacts)
        }.distinct()
}

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}
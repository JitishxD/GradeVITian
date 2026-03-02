package me.jitish.gradevitian.domain.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val university: String = "VIT",
    val registrationNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)


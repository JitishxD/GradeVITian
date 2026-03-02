package me.jitish.gradevitian.domain.repository

import kotlinx.coroutines.flow.Flow
import me.jitish.gradevitian.domain.model.UserProfile
import me.jitish.gradevitian.domain.util.Resource

interface AuthRepository {
    val currentUserId: String?
    val isAuthenticated: Boolean
    fun observeAuthState(): Flow<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Resource<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Resource<UserProfile>
    suspend fun signInWithGoogle(idToken: String): Resource<UserProfile>
    suspend fun signOut()
    suspend fun getCurrentProfile(): Resource<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Resource<Unit>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
}


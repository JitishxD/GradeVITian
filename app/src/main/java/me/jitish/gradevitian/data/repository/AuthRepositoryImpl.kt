package me.jitish.gradevitian.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import me.jitish.gradevitian.domain.model.UserProfile
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isAuthenticated: Boolean
        get() = auth.currentUser != null

    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Resource<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Resource.Error("Sign in failed")
            val profile = fetchOrCreateProfile(user.uid, user.displayName, user.email, user.photoUrl?.toString())
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Sign in failed", e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Resource<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Resource.Error("Sign up failed")
            user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
            val profile = fetchOrCreateProfile(user.uid, displayName, email, null)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Sign up failed", e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Resource.Error("Google sign in failed")
            val profile = fetchOrCreateProfile(user.uid, user.displayName, user.email, user.photoUrl?.toString())
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google sign in failed", e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getCurrentProfile(): Resource<UserProfile> {
        val uid = currentUserId ?: return Resource.Error("Not authenticated")
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java) ?: UserProfile(uid = uid)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get profile", e)
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Resource<Unit> {
        return try {
            firestore.collection("users").document(profile.uid).set(profile).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send reset email", e)
        }
    }

    private suspend fun fetchOrCreateProfile(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): UserProfile {
        val docRef = firestore.collection("users").document(uid)
        val doc = docRef.get().await()
        return if (doc.exists()) {
            doc.toObject(UserProfile::class.java) ?: UserProfile(uid = uid)
        } else {
            val profile = UserProfile(
                uid = uid,
                displayName = displayName ?: "",
                email = email ?: "",
                photoUrl = photoUrl ?: ""
            )
            docRef.set(profile).await()
            profile
        }
    }
}


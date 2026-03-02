package me.jitish.gradevitian.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import me.jitish.gradevitian.domain.model.AttendanceRecord
import me.jitish.gradevitian.domain.model.CgpaRecord
import me.jitish.gradevitian.domain.model.GpaRecord
import me.jitish.gradevitian.domain.repository.RecordsRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecordsRepository {

    // ─── GPA ────────────────────────────────────────────────────────────

    override fun observeGpaRecords(userId: String): Flow<Resource<List<GpaRecord>>> = callbackFlow {
        val ref = firestore.collection("users").document(userId)
            .collection("gpa_records")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Failed to load GPA records"))
                return@addSnapshotListener
            }
            val records = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GpaRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Resource.Success(records))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun saveGpaRecord(userId: String, record: GpaRecord): Resource<String> {
        return try {
            val ref = firestore.collection("users").document(userId)
                .collection("gpa_records")
            val docRef = if (record.id.isBlank()) {
                ref.add(record).await()
            } else {
                ref.document(record.id).set(record).await()
                ref.document(record.id)
            }
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save GPA record", e)
        }
    }

    override suspend fun deleteGpaRecord(userId: String, recordId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("gpa_records").document(recordId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete GPA record", e)
        }
    }

    // ─── CGPA ───────────────────────────────────────────────────────────

    override fun observeCgpaRecords(userId: String): Flow<Resource<List<CgpaRecord>>> = callbackFlow {
        val ref = firestore.collection("users").document(userId)
            .collection("cgpa_records")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Failed to load CGPA records"))
                return@addSnapshotListener
            }
            val records = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(CgpaRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Resource.Success(records))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun saveCgpaRecord(userId: String, record: CgpaRecord): Resource<String> {
        return try {
            val ref = firestore.collection("users").document(userId)
                .collection("cgpa_records")
            val docRef = if (record.id.isBlank()) {
                ref.add(record).await()
            } else {
                ref.document(record.id).set(record).await()
                ref.document(record.id)
            }
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save CGPA record", e)
        }
    }

    override suspend fun deleteCgpaRecord(userId: String, recordId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("cgpa_records").document(recordId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete CGPA record", e)
        }
    }

    // ─── Attendance ─────────────────────────────────────────────────────

    override fun observeAttendanceRecords(userId: String): Flow<Resource<List<AttendanceRecord>>> = callbackFlow {
        val ref = firestore.collection("users").document(userId)
            .collection("attendance_records")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Failed to load attendance records"))
                return@addSnapshotListener
            }
            val records = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(AttendanceRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Resource.Success(records))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun saveAttendanceRecord(userId: String, record: AttendanceRecord): Resource<String> {
        return try {
            val ref = firestore.collection("users").document(userId)
                .collection("attendance_records")
            val docRef = if (record.id.isBlank()) {
                ref.add(record).await()
            } else {
                ref.document(record.id).set(record).await()
                ref.document(record.id)
            }
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save attendance record", e)
        }
    }

    override suspend fun deleteAttendanceRecord(userId: String, recordId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("attendance_records").document(recordId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete attendance record", e)
        }
    }
}


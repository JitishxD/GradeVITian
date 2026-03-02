package me.jitish.gradevitian.domain.repository

import kotlinx.coroutines.flow.Flow
import me.jitish.gradevitian.domain.model.AttendanceRecord
import me.jitish.gradevitian.domain.model.CgpaRecord
import me.jitish.gradevitian.domain.model.GpaRecord
import me.jitish.gradevitian.domain.util.Resource

interface RecordsRepository {
    // GPA Records
    fun observeGpaRecords(userId: String): Flow<Resource<List<GpaRecord>>>
    suspend fun saveGpaRecord(userId: String, record: GpaRecord): Resource<String>
    suspend fun deleteGpaRecord(userId: String, recordId: String): Resource<Unit>

    // CGPA Records
    fun observeCgpaRecords(userId: String): Flow<Resource<List<CgpaRecord>>>
    suspend fun saveCgpaRecord(userId: String, record: CgpaRecord): Resource<String>
    suspend fun deleteCgpaRecord(userId: String, recordId: String): Resource<Unit>

    // Attendance Records
    fun observeAttendanceRecords(userId: String): Flow<Resource<List<AttendanceRecord>>>
    suspend fun saveAttendanceRecord(userId: String, record: AttendanceRecord): Resource<String>
    suspend fun deleteAttendanceRecord(userId: String, recordId: String): Resource<Unit>
}


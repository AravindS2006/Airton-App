package com.example.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AirTonDao {

    // --- Clinics & Clinicians ---
    @Query("SELECT * FROM clinics LIMIT 1")
    fun observeClinic(): Flow<ClinicEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinic(clinic: ClinicEntity)

    @Query("SELECT * FROM clinicians ORDER BY fullName ASC")
    fun observeClinicians(): Flow<List<ClinicianEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicians(clinicians: List<ClinicianEntity>)

    // --- Patients ---
    @Query("SELECT * FROM patients ORDER BY fullName ASC")
    fun observePatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE subjectId = :subjectId LIMIT 1")
    fun observePatient(subjectId: String): Flow<PatientEntity?>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun countPatients(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Query(
        "UPDATE patients SET assignedDeviceId = :assignedDeviceId, enrolmentStatus = :enrolmentStatus, trainingStatus = :trainingStatus WHERE subjectId = :subjectId"
    )
    suspend fun updatePatientAssignment(
        subjectId: String,
        assignedDeviceId: String?,
        enrolmentStatus: String,
        trainingStatus: String
    )

    // --- Care Plans ---
    @Query("SELECT * FROM care_plans")
    fun observeCarePlans(): Flow<List<CarePlanEntity>>

    @Query("SELECT * FROM care_plans WHERE subjectId = :subjectId LIMIT 1")
    fun observeCarePlanForSubject(subjectId: String): Flow<CarePlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCarePlan(plan: CarePlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarePlans(plans: List<CarePlanEntity>)

    // --- Immutable Measurements ---
    @Query("SELECT * FROM measurements ORDER BY capturedAt DESC")
    fun observeMeasurements(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE subjectId = :subjectId ORDER BY capturedAt DESC")
    fun observeMeasurementsForSubject(subjectId: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE measurementId = :measurementId LIMIT 1")
    suspend fun findMeasurementById(measurementId: String): MeasurementEntity?

    @Query("SELECT * FROM measurements WHERE measurementId = :measurementId LIMIT 1")
    fun observeMeasurementById(measurementId: String): Flow<MeasurementEntity?>

    // Strictly IGNORE so an existing immutable record is never silently overwritten
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeasurementImmutable(entity: MeasurementEntity): Long

    // --- Transport States (Independent State Machine) ---
    @Query("SELECT * FROM transport_states")
    fun observeTransportStates(): Flow<List<TransportStateEntity>>

    @Query("SELECT * FROM transport_states WHERE measurementId = :measurementId LIMIT 1")
    suspend fun findTransportState(measurementId: String): TransportStateEntity?

    @Query("SELECT * FROM transport_states WHERE transportStatus IN ('LOCAL_ONLY', 'PENDING', 'FAILED')")
    suspend fun findUnsyncedTransportStates(): List<TransportStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransportState(entity: TransportStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransportStates(entities: List<TransportStateEntity>)

    // --- Append-Only Review Events ---
    @Query("SELECT * FROM review_events ORDER BY createdAt ASC")
    fun observeReviewEvents(): Flow<List<ReviewEventEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviewEventAppendOnly(event: ReviewEventEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviewEvents(events: List<ReviewEventEntity>)

    // --- Conflict Quarantine ---
    @Query("SELECT * FROM conflict_records ORDER BY detectedAt DESC")
    fun observeConflicts(): Flow<List<ConflictRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConflict(conflict: ConflictRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConflicts(conflicts: List<ConflictRecordEntity>)

    // --- Devices & Append-Only Service History ---
    @Query("SELECT * FROM devices ORDER BY serialNumber ASC")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun findDeviceById(deviceId: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("SELECT * FROM service_events ORDER BY performedAt DESC")
    fun observeServiceEvents(): Flow<List<ServiceEventEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertServiceEventAppendOnly(event: ServiceEventEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertServiceEvents(events: List<ServiceEventEntity>)

    // --- Local Notifications ---
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :notificationId")
    suspend fun markNotificationRead(notificationId: String)

    // --- Transactional Demo Reset ---
    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    @Query("DELETE FROM service_events")
    suspend fun clearServiceEvents()

    @Query("DELETE FROM devices")
    suspend fun clearDevices()

    @Query("DELETE FROM conflict_records")
    suspend fun clearConflicts()

    @Query("DELETE FROM review_events")
    suspend fun clearReviewEvents()

    @Query("DELETE FROM transport_states")
    suspend fun clearTransportStates()

    @Query("DELETE FROM measurements")
    suspend fun clearMeasurements()

    @Query("DELETE FROM care_plans")
    suspend fun clearCarePlans()

    @Query("DELETE FROM patients")
    suspend fun clearPatients()

    @Query("DELETE FROM clinicians")
    suspend fun clearClinicians()

    @Query("DELETE FROM clinics")
    suspend fun clearClinics()
}

package com.example.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClinicEntity::class,
        ClinicianEntity::class,
        PatientEntity::class,
        CarePlanEntity::class,
        MeasurementEntity::class,
        TransportStateEntity::class,
        ReviewEventEntity::class,
        ConflictRecordEntity::class,
        DeviceEntity::class,
        ServiceEventEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AirTonDatabase : RoomDatabase() {
    abstract fun airTonDao(): AirTonDao

    companion object {
        @Volatile
        private var INSTANCE: AirTonDatabase? = null

        fun getInstance(context: Context): AirTonDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AirTonDatabase::class.java,
                    "airton_demo_local.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

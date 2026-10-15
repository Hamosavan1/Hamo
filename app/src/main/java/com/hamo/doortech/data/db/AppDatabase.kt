package com.hamo.doortech.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DoorCategory::class, Brand::class, MachineModel::class, Fault::class,
        FaultSymptom::class, ErrorCode::class, Solution::class, SolutionStep::class,
        TechnicalGuide::class, GuideStep::class, MediaAttachment::class,
        SparePart::class, MaintenanceJob::class, JobPart::class, Favorite::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun faultDao(): FaultDao
    abstract fun guideDao(): GuideDao
    abstract fun jobDao(): JobDao
    abstract fun mediaDao(): MediaDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "hamo_door_tech.db")
                .build()
    }
}

package com.example.auditapplication5.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates

@Database(entities = [AInfo5::class, AInfo5Templates::class], version = 1, exportSchema = false)
abstract class AInfo5Database : RoomDatabase() {
    abstract fun getAInfo5Dao(): AInfo5Dao
}
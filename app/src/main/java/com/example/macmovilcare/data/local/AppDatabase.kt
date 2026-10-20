package com.example.macmovilcare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.macmovilcare.data.local.dao.OrdenDao
import com.example.macmovilcare.data.local.entity.OrdenEntity

@Database(entities = [OrdenEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ordenDao(): OrdenDao
}
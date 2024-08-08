package com.example.auditapplication5.presentation.di

import android.app.Application
import androidx.room.Room
import com.example.auditapplication5.data.db.AInfo5Dao
import com.example.auditapplication5.data.db.AInfo5Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AInfo5DatabaseModule {

    @Singleton
    @Provides
    fun provideAInfo5Database(app: Application) : AInfo5Database{
        return Room.databaseBuilder(
            app,
            AInfo5Database::class.java,
            "audit_info_db_5"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideAInfo5Dao(aInfo5Database: AInfo5Database): AInfo5Dao{
        return aInfo5Database.getAInfo5Dao()
    }

}
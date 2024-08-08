package com.example.auditapplication5.presentation.di

import com.example.auditapplication5.data.db.AInfo5Dao
import com.example.auditapplication5.data.repository.datasource.AInfo5DataSource
import com.example.auditapplication5.data.repository.datasourceimpl.AInfo5DataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AInfo5DataSourceModule {

    @Singleton
    @Provides
    fun provideAInfo5DataSource(aInfo5Dao: AInfo5Dao) : AInfo5DataSource{
        return AInfo5DataSourceImpl(aInfo5Dao)
    }
}
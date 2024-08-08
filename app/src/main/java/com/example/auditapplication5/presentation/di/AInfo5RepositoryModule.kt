package com.example.auditapplication5.presentation.di

import com.example.auditapplication5.data.repository.datasource.AInfo5DataSource
import com.example.auditapplication5.domain.repository.AInfo5Repository
import com.example.auditapplication5.domain.repository.AInfo5RepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AInfo5RepositoryModule {
    @Singleton
    @Provides
    fun provideAInfo5Repository(aInfo5DataSource: AInfo5DataSource): AInfo5Repository{
        return AInfo5RepositoryImpl(aInfo5DataSource)
    }
}
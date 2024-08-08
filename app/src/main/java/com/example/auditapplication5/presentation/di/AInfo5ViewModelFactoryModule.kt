package com.example.auditapplication5.presentation.di

import android.app.Application
import com.example.auditapplication5.domain.usecase.*
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AInfo5ViewModelFactoryModule {

    @Singleton
    @Provides
    fun provideAInfo5ViewModelFactory(
        app: Application,
        insertAInfo5UseCase: InsertAInfo5UseCase,
        insertAInfo5TemplatesUseCase: InsertAInfo5TemplatesUseCase,
        deleteAInfo5UseCase: DeleteAInfo5UseCase,
        deleteAInfo5TemplatesUseCase: DeleteAInfo5TemplatesUseCase,
        deleteAllAInfo5UseCase: DeleteAllAInfo5UseCase,
        deleteAllAInfo5TemplatesUseCase: DeleteAllAInfo5TemplatesUseCase,
        getAInfo5ByIdsUseCase: GetAInfo5ByIdsUseCase,
        getAInfo5TemplatesByIdsUseCase: GetAInfo5TemplatesByIdsUseCase
    ): AInfo5ViewModelFactory {
        return AInfo5ViewModelFactory(
            app,
            insertAInfo5UseCase,
            insertAInfo5TemplatesUseCase,
            deleteAInfo5UseCase,
            deleteAInfo5TemplatesUseCase,
            deleteAllAInfo5UseCase,
            deleteAllAInfo5TemplatesUseCase,
            getAInfo5ByIdsUseCase,
            getAInfo5TemplatesByIdsUseCase
        )
    }
}
package com.example.auditapplication5.presentation.di

import com.example.auditapplication5.domain.repository.AInfo5Repository
import com.example.auditapplication5.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Singleton
    @Provides
    fun provideInsertAInfo5UseCase(aInfo5Repository: AInfo5Repository) : InsertAInfo5UseCase{
        return InsertAInfo5UseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideInsertAInfo5TemplatesUseCase(aInfo5Repository: AInfo5Repository): InsertAInfo5TemplatesUseCase{
        return InsertAInfo5TemplatesUseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideDeleteAInfo5UseCase(aInfo5Repository: AInfo5Repository): DeleteAInfo5UseCase{
        return DeleteAInfo5UseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideDeleteAInfo5TemplatesUseCase(aInfo5Repository: AInfo5Repository): DeleteAInfo5TemplatesUseCase{
        return DeleteAInfo5TemplatesUseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideDeleteAllAInfo5UseCase(aInfo5Repository: AInfo5Repository): DeleteAllAInfo5UseCase{
        return DeleteAllAInfo5UseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideDeleteAllAInfo5TemplatesUseCase(aInfo5Repository: AInfo5Repository): DeleteAllAInfo5TemplatesUseCase{
        return DeleteAllAInfo5TemplatesUseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideGetAInfo5ByIdsUseCase(aInfo5Repository: AInfo5Repository): GetAInfo5ByIdsUseCase{
        return GetAInfo5ByIdsUseCase(aInfo5Repository)
    }
    @Singleton
    @Provides
    fun provideGetAInfo5TemplatesByIdsUseCase(aInfo5Repository: AInfo5Repository): GetAInfo5TemplatesByIdsUseCase{
        return GetAInfo5TemplatesByIdsUseCase(aInfo5Repository)
    }
}
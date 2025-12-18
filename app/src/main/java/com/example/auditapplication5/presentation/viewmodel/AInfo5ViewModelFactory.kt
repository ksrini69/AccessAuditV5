package com.example.auditapplication5.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auditapplication5.domain.usecase.*

class AInfo5ViewModelFactory(
    val app: Application,
    private val insertAInfo5UseCase: InsertAInfo5UseCase,
    private val insertAInfo5TemplatesUseCase: InsertAInfo5TemplatesUseCase,
    private val deleteAInfo5UseCase: DeleteAInfo5UseCase,
    private val deleteAInfo5TemplatesUseCase: DeleteAInfo5TemplatesUseCase,
    private val deleteAllAInfo5UseCase: DeleteAllAInfo5UseCase,
    private val deleteAllAInfo5TemplatesUseCase: DeleteAllAInfo5TemplatesUseCase,
    private val getAInfo5ByIdsUseCase: GetAInfo5ByIdsUseCase,
    private val getAInfo5TemplatesByIdsUseCase: GetAInfo5TemplatesByIdsUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //return super.create(modelClass)
        return AInfo5ViewModel(
            app,
            insertAInfo5UseCase,
            insertAInfo5TemplatesUseCase,
            deleteAInfo5UseCase,
            deleteAInfo5TemplatesUseCase,
            deleteAllAInfo5UseCase,
            deleteAllAInfo5TemplatesUseCase,
            getAInfo5ByIdsUseCase,
            getAInfo5TemplatesByIdsUseCase
        ) as T
    }

}
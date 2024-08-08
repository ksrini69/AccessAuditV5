package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.data.model.AInfo5Templates
import com.example.auditapplication5.domain.repository.AInfo5Repository

class InsertAInfo5TemplatesUseCase(private val aInfo5Repository: AInfo5Repository) {
    suspend fun execute(aInfo5Templates: AInfo5Templates) = aInfo5Repository.insertAInfo5Templates(aInfo5Templates)
}
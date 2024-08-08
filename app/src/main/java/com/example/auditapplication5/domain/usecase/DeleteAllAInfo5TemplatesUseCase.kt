package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.domain.repository.AInfo5Repository

class DeleteAllAInfo5TemplatesUseCase(private val aInfo5Repository: AInfo5Repository) {
    suspend fun execute() = aInfo5Repository.deleteAllAInfo5Templates()
}
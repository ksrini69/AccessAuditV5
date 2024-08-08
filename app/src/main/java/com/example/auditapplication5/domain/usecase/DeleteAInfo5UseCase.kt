package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.domain.repository.AInfo5Repository

class DeleteAInfo5UseCase(private val aInfo5Repository: AInfo5Repository) {
    suspend fun execute(aInfo5: AInfo5) = aInfo5Repository.deleteAInfo5(aInfo5)
}
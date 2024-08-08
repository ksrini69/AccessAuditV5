package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.domain.repository.AInfo5Repository

class InsertAInfo5UseCase(private val aInfo5Repository: AInfo5Repository) {
    suspend fun execute(aInfo5: AInfo5) = aInfo5Repository.insertAInfo5(aInfo5)
}
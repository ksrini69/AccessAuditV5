package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.domain.repository.AInfo5Repository
import kotlinx.coroutines.flow.Flow


class GetAInfo5ByIdsUseCase(private val aInfo5Repository: AInfo5Repository) {
    fun execute(ids: MutableList<String>) : Flow<MutableList<AInfo5>>{
        return aInfo5Repository.getAInfo5ByIds(ids)
    }
}
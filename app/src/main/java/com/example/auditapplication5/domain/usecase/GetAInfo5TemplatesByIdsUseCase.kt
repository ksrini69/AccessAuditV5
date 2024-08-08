package com.example.auditapplication5.domain.usecase

import com.example.auditapplication5.data.model.AInfo5Templates
import com.example.auditapplication5.domain.repository.AInfo5Repository
import kotlinx.coroutines.flow.Flow


class GetAInfo5TemplatesByIdsUseCase(private val aInfo5Repository: AInfo5Repository) {
    fun execute(ids: MutableList<String>): Flow<MutableList<AInfo5Templates>>{
        return aInfo5Repository.getAInfo5TemplatesByIds(ids)
    }
}
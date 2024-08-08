package com.example.auditapplication5.domain.repository

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import kotlinx.coroutines.flow.Flow


interface AInfo5Repository {
    suspend fun insertAInfo5(aInfo5: AInfo5)
    suspend fun insertAInfo5Templates(aInfo5Templates: AInfo5Templates)
    suspend fun deleteAInfo5(aInfo5: AInfo5)
    suspend fun deleteAInfo5Templates(aInfo5Templates: AInfo5Templates)
    fun getAInfo5ByIds(ids: MutableList<String>) : Flow<MutableList<AInfo5>>
    fun getAInfo5TemplatesByIds(ids: MutableList<String>): Flow<MutableList<AInfo5Templates>>
    suspend fun deleteAllAInfo5()
    suspend fun deleteAllAInfo5Templates()
}
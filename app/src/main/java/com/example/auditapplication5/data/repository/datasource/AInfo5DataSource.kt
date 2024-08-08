package com.example.auditapplication5.data.repository.datasource

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import kotlinx.coroutines.flow.Flow


interface AInfo5DataSource {
    suspend fun insertAInfo5Data(aInfo5: AInfo5)
    suspend fun insertAInfo5TemplatesData(aInfo5Templates: AInfo5Templates)
    suspend fun deleteAInfo5Data(aInfo5: AInfo5)
    suspend fun deleteAInfo5TemplatesData(aInfo5Templates: AInfo5Templates)
    fun getAInfo5ByIdsData(ids: MutableList<String>) : Flow<MutableList<AInfo5>>
    fun getAInfo5TemplatesByIdsData(ids: MutableList<String>) : Flow<MutableList<AInfo5Templates>>
    suspend fun deleteAllAInfo5Data()
    suspend fun deleteAllAInfo5TemplatesData()
}
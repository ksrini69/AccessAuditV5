package com.example.auditapplication5.domain.repository

import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import com.example.auditapplication5.data.repository.datasource.AInfo5DataSource
import kotlinx.coroutines.flow.Flow

class AInfo5RepositoryImpl(private val aInfo5DataSource: AInfo5DataSource): AInfo5Repository {
    override suspend fun insertAInfo5(aInfo5: AInfo5) {
       return aInfo5DataSource.insertAInfo5Data(aInfo5)
    }

    override suspend fun insertAInfo5Templates(aInfo5Templates: AInfo5Templates) {
        return aInfo5DataSource.insertAInfo5TemplatesData(aInfo5Templates)
    }

    override suspend fun deleteAInfo5(aInfo5: AInfo5) {
        return aInfo5DataSource.deleteAInfo5Data(aInfo5)
    }

    override suspend fun deleteAInfo5Templates(aInfo5Templates: AInfo5Templates) {
        return aInfo5DataSource.deleteAInfo5TemplatesData(aInfo5Templates)
    }

    override fun getAInfo5ByIds(ids: MutableList<String>): Flow<MutableList<AInfo5>> {
        return aInfo5DataSource.getAInfo5ByIdsData(ids)
    }

    override fun getAInfo5TemplatesByIds(ids: MutableList<String>): Flow<MutableList<AInfo5Templates>> {
        return aInfo5DataSource.getAInfo5TemplatesByIdsData(ids)
    }

    override suspend fun deleteAllAInfo5() {
        return aInfo5DataSource.deleteAllAInfo5Data()
    }

    override suspend fun deleteAllAInfo5Templates() {
        return aInfo5DataSource.deleteAllAInfo5TemplatesData()
    }
}
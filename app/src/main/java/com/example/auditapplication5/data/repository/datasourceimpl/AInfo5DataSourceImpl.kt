package com.example.auditapplication5.data.repository.datasourceimpl

import com.example.auditapplication5.data.db.AInfo5Dao
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import com.example.auditapplication5.data.repository.datasource.AInfo5DataSource
import kotlinx.coroutines.flow.Flow

class AInfo5DataSourceImpl(private val aInfo5Dao: AInfo5Dao): AInfo5DataSource {
    override suspend fun insertAInfo5Data(aInfo5: AInfo5) {
        aInfo5Dao.insertAInfo5(aInfo5)
    }

    override suspend fun insertAInfo5TemplatesData(aInfo5Templates: AInfo5Templates) {
        aInfo5Dao.insertAInfo5Templates(aInfo5Templates)
    }

    override suspend fun deleteAInfo5Data(aInfo5: AInfo5) {
        aInfo5Dao.deleteAInfo5(aInfo5)
    }

    override suspend fun deleteAInfo5TemplatesData(aInfo5Templates: AInfo5Templates) {
        aInfo5Dao.deleteAInfo5Templates(aInfo5Templates)
    }

    override fun getAInfo5ByIdsData(ids: MutableList<String>): Flow<MutableList<AInfo5>> {
        return aInfo5Dao.getAInfo5ByIds(ids)
    }

    override fun getAInfo5TemplatesByIdsData(ids: MutableList<String>): Flow<MutableList<AInfo5Templates>> {
        return aInfo5Dao.getAInfo5TemplatesByIds(ids)
    }

    override suspend fun deleteAllAInfo5Data() {
        aInfo5Dao.deleteAllAInfo5()
    }

    override suspend fun deleteAllAInfo5TemplatesData() {
        aInfo5Dao.deleteAllAInfo5Templates()
    }
}
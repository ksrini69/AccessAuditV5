package com.example.auditapplication5.data.db

import androidx.room.*
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import kotlinx.coroutines.flow.Flow

@Dao
interface AInfo5Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAInfo5(aInfo5: AInfo5)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAInfo5Templates(aInfo5Templates: AInfo5Templates)
    @Delete
    suspend fun deleteAInfo5(aInfo5: AInfo5)
    @Delete
    suspend fun deleteAInfo5Templates(aInfo5Templates: AInfo5Templates)
    @Query("SELECT * FROM audit_information_table_5 WHERE audit5_id in (:ids)")
    fun getAInfo5ByIds(ids: MutableList<String>): Flow<MutableList<AInfo5>>
    @Query("SELECT * FROM audit_templates_table_5 WHERE audit5_template_id in (:ids)")
    fun getAInfo5TemplatesByIds(ids: MutableList<String>) : Flow<MutableList<AInfo5Templates>>
    @Query("DELETE FROM audit_information_table_5")
    suspend fun deleteAllAInfo5()
    @Query("DELETE FROM audit_templates_table_5")
    suspend fun deleteAllAInfo5Templates()
}
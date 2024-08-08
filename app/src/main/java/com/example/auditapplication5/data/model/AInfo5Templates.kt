package com.example.auditapplication5.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_templates_table_5")
data class AInfo5Templates(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "audit5_template_id")
    var id: String,
    @ColumnInfo(name = "audit5_template_string")
    var template_string: String? = null,
    @ColumnInfo(name = "audit5_template_extra")
    var extra: String? = null
)

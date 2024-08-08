package com.example.auditapplication5.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_information_table_5")
data class AInfo5(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "audit5_id")
    var id: String,
    @ColumnInfo(name = "audit5_framework")
    var framework: String? = null,
    @ColumnInfo(name = "audit5_data")
    var data: String? = null,
    @ColumnInfo(name = "audit5_reports")
    var reports: String? = null,
    @ColumnInfo(name = "audit5_boq")
    var boq: String? = null,
    @ColumnInfo(name = "audit5_extra")
    var extra: String? = null
)

package com.example.auditapplication5.data.model

data class CompanyReportDC(
    var companyCode: String = "",
    var companyName: String = "",
    var companyAuditDate: String = "",
    var companyIntroduction: String = "",
    var sectionReportList: MutableList<SectionReportDC> = mutableListOf()
)

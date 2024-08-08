package com.example.auditapplication5.data.model

data class CheckboxesFrameworkDataItemDC(
    var checkboxesFrameworkTitle: String = "",
    var pageCode: String = "",
    var checkboxDataItemML: MutableList<CheckboxDataItemDC> = mutableListOf()
)

package com.example.auditapplication5.data.model

data class RVParentChildParentItemDC(
    var title: String = "",
    var pageGroupCode: String = "",
    var childItemList: MutableList<String> = mutableListOf(),
    var isExpandable: Boolean = false
)

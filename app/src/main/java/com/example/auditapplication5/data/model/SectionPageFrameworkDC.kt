package com.example.auditapplication5.data.model
//This class indicates the structure of a single page in a section.
data class SectionPageFrameworkDC(
    var pageTitle: String = "",
    var pageCode: String = "",
    var pageNumber: Int = 1,
    var questionsFrameworkList: MutableList<QuestionsFrameworkItemDC> = mutableListOf(),
    var observationsFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf(),
    var recommendationsFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf(),
    var standardsFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf()
)

package com.example.auditapplication5.data.model
//This class indicates the structure of a single page in a section.
data class SectionPageFrameworkDC(
    var pageTitle: String = "",
    var pageCode: String = "",
    var pageNumber: Int = 1,
    var questionsFrameworkList: MutableList<QuestionsFrameworkItemDC> = mutableListOf(),
    var observationsCheckboxesFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf(),
    var recommendationsCheckboxesFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf(),
    var standardsCheckboxesFrameworkList: MutableList<CheckboxesFrameworkItemDC> = mutableListOf()
)

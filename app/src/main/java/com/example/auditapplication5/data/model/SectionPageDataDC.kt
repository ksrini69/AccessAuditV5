package com.example.auditapplication5.data.model
//This class indicates the structure of a single data page in a section
data class SectionPageDataDC(
    var pageTitle: String = "",
    var pageNumber: Int = 1,
    var observations: String = "",
    var photoPaths: String = "",
    var recommendations: String = "",
    var standards: String = "",
    var questionsFrameworkDataItemList: MutableList<QuestionsFrameworkDataItemDC> = mutableListOf(),
    var observationsFrameworkDataItemList: MutableList<CheckboxesFrameworkDataItemDC> = mutableListOf(),
    var recommendationsFrameworkDataItemList: MutableList<CheckboxesFrameworkDataItemDC> = mutableListOf(),
    var standardsFrameworkDataItemList: MutableList<CheckboxesFrameworkDataItemDC> = mutableListOf()

    )

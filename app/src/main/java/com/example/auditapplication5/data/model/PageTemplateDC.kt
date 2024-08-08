package com.example.auditapplication5.data.model

data class PageTemplateDC(
    var pageCode: String = "",
    var questionsList: MutableList<QuestionTemplateItemDC> = mutableListOf(),
    var observationsList : MutableList<CheckboxTemplateItemDC> = mutableListOf(),
    var recommendationsList: MutableList<CheckboxTemplateItemDC> = mutableListOf(),
    var standardsList: MutableList<CheckboxTemplateItemDC> = mutableListOf()
)

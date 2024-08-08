package com.example.auditapplication5.data.model

data class QuestionsFrameworkDataItemDC(
    var questionsFrameworkTitle: String = "",
    var pageCode: String = "",
    var questionDataItemList: MutableList<QuestionDataItemDC> = mutableListOf()
)

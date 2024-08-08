package com.example.auditapplication5.data.model

data class QuestionTemplateItemDC(
    var blockNumber: String = "",
    var question: String = "",
    var message: String = "",
    var mandatory: String = "M",
    var data1Visibility: Boolean = true,
    var data1Hint: String = "",
    var data1Type: String = "N",
    var data1Label: String = "",
    var data1Sentence1: String = "",
    var data1Sentence2: String = "",
    var data2Visibility: Boolean = true,
    var data2Hint: String = "",
    var data2Type: String = "N",
    var data2Label: String = "",
    var data2Sentence1: String = "",
    var data2Sentence2: String = "",
    var data3Visibility: Boolean = true,
    var data3Hint: String = "",
    var data3Type: String = "N",
    var data3Label: String = "",
    var data3Sentence1: String = "",
    var data3Sentence2: String = "",
    var buttonVisibility: Boolean = true,
    var buttonOptionsList: MutableList<String> = mutableListOf()



)

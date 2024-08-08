package com.example.auditapplication5.data.model

data class SectionAllDataDC(
    var introduction : String = "",
    var picturePathsInIntroductions : String = "",
    var sectionAllPagesData : SectionAllPagesDataDC = SectionAllPagesDataDC(mutableListOf())
)

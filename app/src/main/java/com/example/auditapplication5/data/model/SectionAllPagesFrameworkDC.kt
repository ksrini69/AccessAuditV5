package com.example.auditapplication5.data.model
//This class defines the framework of all pages in a given section.
// This is saved to the database along with the corresponding data file.
data class SectionAllPagesFrameworkDC(
    var sectionPageFrameworkList : MutableList<SectionPageFrameworkDC> = mutableListOf()
)

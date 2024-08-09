package com.example.auditapplication5.presentation.viewmodel

import android.app.Application
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import com.example.auditapplication5.Event
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.R
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.domain.usecase.*
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class AInfo5ViewModel(
    private val app: Application,
    private val insertAInfo5UseCase: InsertAInfo5UseCase,
    private val insertAInfo5TemplatesUseCase: InsertAInfo5TemplatesUseCase,
    private val deleteAInfo5UseCase: DeleteAInfo5UseCase,
    private val deleteAInfo5TemplatesUseCase: DeleteAInfo5TemplatesUseCase,
    private val deleteAllAInfo5UseCase: DeleteAllAInfo5UseCase,
    private val deleteAllAInfo5TemplatesUseCase: DeleteAllAInfo5TemplatesUseCase,
    private val getAInfo5ByIdsUseCase: GetAInfo5ByIdsUseCase,
    private val getAInfo5TemplatesByIdsUseCase: GetAInfo5TemplatesByIdsUseCase
) : AndroidViewModel(app), LifecycleObserver {

//Database Related Functions

    fun insertAInfo5(aInfo5: AInfo5) = viewModelScope.launch(Dispatchers.IO) {
        insertAInfo5UseCase.execute(aInfo5)
    }

    fun insertAInfo5Templates(aInfo5Templates: AInfo5Templates) =
        viewModelScope.launch(Dispatchers.IO) {
            insertAInfo5TemplatesUseCase.execute(aInfo5Templates)
        }

    fun deleteAInfo5(aInfo5: AInfo5) = viewModelScope.launch(Dispatchers.IO) {
        deleteAInfo5UseCase.execute(aInfo5)
    }

    fun deleteAInfo5Templates(aInfo5Templates: AInfo5Templates) =
        viewModelScope.launch(Dispatchers.IO) {
            deleteAInfo5TemplatesUseCase.execute(aInfo5Templates)
        }

    fun deleteAllAInfo5() = viewModelScope.launch(Dispatchers.IO) {
        deleteAllAInfo5UseCase.execute()
    }

    fun deleteAllAInfo5Templates() = viewModelScope.launch(Dispatchers.IO) {
        deleteAllAInfo5TemplatesUseCase.execute()
    }

    fun getAInfo5ByIds(ids: MutableList<String>): LiveData<MutableList<AInfo5>> {
        return liveData {
            getAInfo5ByIdsUseCase.execute(ids).collect {
                emit(it)
            }
        }
    }

    fun getAInfo5TemplatesByIds(ids: MutableList<String>): LiveData<MutableList<AInfo5Templates>> {
        return liveData {
            getAInfo5TemplatesByIdsUseCase.execute(ids).collect {
                emit(it)
            }
        }
    }

//Status Message using Event

    val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage

    fun setStatusMessage(input: String) {
        statusMessage.value = Event(input)
    }
//Company, Audit Date, Intro and Section related Variables and Functions


    // Getting the Parent Folder URI from the database
    private var parentFolderURIIDML: MutableList<String> =
        mutableListOf<String>(MainActivity.PARENT_FOLDER_URI_ID)
    val getParentFolderURIStringLD = getAInfo5ByIds(parentFolderURIIDML)

    private var parentFolderURIString = ""
    fun getTheParentFolderURIString(): String {
        return parentFolderURIString
    }

    fun setTheParentFolderURIString(input: String) {
        parentFolderURIString = input
    }

    //Mutable List to be used for Edit by the SimpleRecyclerView
    private var editML = mutableListOf<String>()
    fun getTheEditML(): MutableList<String> {
        return editML
    }

    fun setTheEditML(input: MutableList<String>) {
        editML = input
    }

    //Mutable List to be used for Deleting by the SimpleRecyclerView
    private var deleteML = mutableListOf<String>()
    fun getTheDeleteML(): MutableList<String> {
        return deleteML
    }

    fun setTheDeleteML(input: MutableList<String>) {
        deleteML = input
    }

    // Get the list of Companies and Corresponding Codes from the database
    var companyCodesAndNamesID = mutableListOf<String>(MainActivity.COMPANY_CODES_NAMES_ID)
    val getMLOfCompanyCodesAndNamesLD = getAInfo5ByIds(companyCodesAndNamesID)

    //Company related Variables
    private var companyCodeAndDisplayNameML = mutableListOf<CodeNameAndDisplayNameDC>()
    fun getTheCompanyCodeAndDisplayNameML(): MutableList<CodeNameAndDisplayNameDC> {
        return companyCodeAndDisplayNameML
    }

    fun setTheCompanyCodeAndDisplayNameML(input: MutableList<CodeNameAndDisplayNameDC>) {
        companyCodeAndDisplayNameML = input
    }

    fun addToCompanyCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companyCodeAndDisplayNameML.add(input)
    }

    fun deleteCompanyInCompanyCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companyCodeAndDisplayNameML.remove(input)
    }

    fun modifyDisplayNameOfSpecificCompanyInML(newDisplayName: String, companyCode: String) {
        for (item in companyCodeAndDisplayNameML) {
            if (item.uniqueCodeName == companyCode) {
                item.displayName = newDisplayName
                break
            }
        }
    }

    //Present Company CodeAndDisplayName
    private var presentCompanyCodeAndDisplayName = CodeNameAndDisplayNameDC()
    fun setThePresentCompanyCodeAndDisplayName(input: CodeNameAndDisplayNameDC) {
        presentCompanyCodeAndDisplayName = input
    }

    fun getThePresentCompanyCodeAndDisplayName(): CodeNameAndDisplayNameDC {
        return presentCompanyCodeAndDisplayName
    }

    fun getPresentCompanyName(): String {
        return presentCompanyCodeAndDisplayName.displayName
    }

    fun getPresentCompanyCode(): String {
        return presentCompanyCodeAndDisplayName.uniqueCodeName
    }

    fun modifyPresentCompanyName(input: String) {
        presentCompanyCodeAndDisplayName.displayName = input
    }

    var companyNameToBeUpdatedFlag: Boolean = false
    fun getTheCompanyNameToBeUpdatedFlag(): Boolean {
        return companyNameToBeUpdatedFlag
    }

    fun setTheCompanyNameToBeUpdatedFlag(input: Boolean) {
        companyNameToBeUpdatedFlag = input
    }

    //Present Company All IDs variable and functions
    private var presentCompanyAllIds = mutableListOf<String>()
    fun setThePresentCompanyAllIds(input: MutableList<String>) {
        presentCompanyAllIds = input
    }

    fun setThePresentCompanyAllIdsUsingString(input: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val mutableList = stringToMLUsingDlimiter1(input)

            withContext(Dispatchers.Main) {
                presentCompanyAllIds = mutableList
            }
        }
    }

    fun getThePresentCompanyAllIds(): MutableList<String> {
        return presentCompanyAllIds
    }

    fun addUniqueItemToPresentCompanyAllIds(input: String) {
        var flagPresent = false
        if (presentCompanyAllIds.isNotEmpty()) {
            for (index in 0 until presentCompanyAllIds.size) {
                if (presentCompanyAllIds[index].trim() == input.trim()) {
                    flagPresent = true
                    break
                }
            }
            if (flagPresent == false) {
                presentCompanyAllIds.add(input.trim())
            }
        } else {
            setThePresentCompanyAllIds(mutableListOf(input))
        }

    }

    //Save Present company all ids into the database
    fun savePresentCompanyAllIdsIntoDB(presentCompanyCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val presentCompanyAllIdsId =
                presentCompanyCode + MainActivity.PRESENT_COMPANY_ALL_IDs_ID
            val presentCompanyAllIdsString = mlToStringUsingDelimiter1(presentCompanyAllIds)
            val aInfo5 = AInfo5(presentCompanyAllIdsId, presentCompanyAllIdsString)
            insertAInfo5(aInfo5)
        }

    }

    fun clearPresentCompanyAllIds() {
        presentCompanyAllIds = mutableListOf()
    }

    var auditDateToBeUpdatedFlag: Boolean = false
    fun getTheAuditDateToBeUpdatedFlag(): Boolean {
        return auditDateToBeUpdatedFlag
    }

    fun setTheAuditDateToBeUpdatedFlag(input: Boolean) {
        auditDateToBeUpdatedFlag = input
    }

    //Introductions and Section OBSRECOSTDS related variables for company and sections
    var whichIntroductionsOrObservationsToBeUploaded: String = ""
    fun getTheWhichIntroductionsOrObservationsToBeUploadedVariable(): String {
        return whichIntroductionsOrObservationsToBeUploaded
    }

    fun setTheWhichIntroductionsOrObservationsToBeUploadedVariable(input: String) {
        whichIntroductionsOrObservationsToBeUploaded = input
    }

    //Section Related Variables
    var templateSectionListID = mutableListOf(MainActivity.TEMPLATE_SECTION_LIST)
    val getDefaultSectionList = getAInfo5TemplatesByIds(templateSectionListID)

    private var sectionCodeAndDisplayNameML = mutableListOf<CodeNameAndDisplayNameDC>()
    fun getTheSectionCodeAndDisplayNameML(): MutableList<CodeNameAndDisplayNameDC> {
        return sectionCodeAndDisplayNameML
    }

    fun setTheSectionCodeAndDisplayNameML(input: MutableList<CodeNameAndDisplayNameDC>) {
        sectionCodeAndDisplayNameML = input
    }

    fun addToSectionCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        sectionCodeAndDisplayNameML.add(input)
    }

    fun deleteSectionInCompanyCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companyCodeAndDisplayNameML.remove(input)
    }

    fun modifyDisplayNameOfSpecificSectionInML(newDisplayName: String, sectionCode: String) {
        for (item in sectionCodeAndDisplayNameML) {
            if (item.uniqueCodeName == sectionCode) {
                item.displayName = newDisplayName
                break
            }
        }
    }

    //Present Section CodeAndDisplayName
    private var presentSectionCodeAndDisplayName = CodeNameAndDisplayNameDC()
    fun setThePresentSectionCodeAndDisplayName(input: CodeNameAndDisplayNameDC) {
        presentSectionCodeAndDisplayName = input
    }

    fun getThePresentSectionCodeAndDisplayName(): CodeNameAndDisplayNameDC {
        return presentSectionCodeAndDisplayName
    }

    fun getPresentSectionName(): String {
        return presentSectionCodeAndDisplayName.displayName
    }

    fun getPresentSectionCode(): String {
        return presentSectionCodeAndDisplayName.uniqueCodeName
    }

    fun modifyPresentSectionName(input: String) {
        presentSectionCodeAndDisplayName.displayName = input
    }

    var sectionNameToBeUpdatedFlag: Boolean = false
    fun getFlagForSectionNameToBeUpdated(): Boolean {
        return sectionNameToBeUpdatedFlag
    }

    fun setFlagForSectionNameToBeUpdated(input: Boolean) {
        sectionNameToBeUpdatedFlag = input
    }

    // Holding variables for Undo operation in Introductions etc
    private var holdingVariableInIntroductions = ""
    fun setTheHoldingVariableInIntroductions(input: String) {
        holdingVariableInIntroductions = input
    }

    fun getTheHoldingVariableInIntroductions(): String {
        return holdingVariableInIntroductions
    }

    private var holdingVariableForPhotosPathsInIntroductions = ""
    fun setTheHoldingVariableForPhotoPathsInIntroductions(input: String) {
        holdingVariableForPhotosPathsInIntroductions = input
    }

    fun getTheHoldingVariableForPhotoPathsInIntroductions(): String {
        return holdingVariableForPhotosPathsInIntroductions
    }


    //Framework Related Variables and Functions
    //Define the Default Framework Page which is the General Entry Page
    var defaultSectionPageFramework = SectionPageFrameworkDC(
        "General Entry", "PC_General_Entry_01_PC", 1,
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
    )

    fun getTheDefaultSectionPageFramework(): SectionPageFrameworkDC {
        return defaultSectionPageFramework
    }

    //Set the Section All Pages Framework Variables and Functions
    var presentSectionAllPagesFramework = SectionAllPagesFrameworkDC()
    fun getThePresentSectionAllPagesFramework(): SectionAllPagesFrameworkDC {
        return presentSectionAllPagesFramework
    }

    fun setThePresentSectionAllPagesFramework(input: SectionAllPagesFrameworkDC) {
        presentSectionAllPagesFramework = input
    }

    fun addPageToPresentSectionAllPagesFramework(input: SectionPageFrameworkDC) {
        presentSectionAllPagesFramework.sectionPageFrameworkList.add(input)
    }

    fun resetThePageNumbersOfPresentSectionAllPagesFramework() {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
            val pageList = presentSectionAllPagesFramework.sectionPageFrameworkList
            for (pageIndex in 0 until pageList.size) {
                if (pageList[pageIndex].pageNumber != pageIndex + 1) {
                    pageList[pageIndex].pageNumber = pageIndex + 1
                }
            }
        }
    }

    fun clearThePresentSectionAllPagesFramework() {
        presentSectionAllPagesFramework.sectionPageFrameworkList = mutableListOf()
        sectionAllPagesFrameworkLoadedFlagMLD.value = false
    }

    fun loadThePresentSectionAllPagesFrameworkAndAllDataUsingStrings(
        sectionAllPagesFrameworkString: String,
        sectionAllDataString: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            var sectionAllPagesFramework = SectionAllPagesFrameworkDC()
            var sectionAllData = SectionAllDataDC()
            if (sectionAllPagesFrameworkString != "") {
                sectionAllPagesFramework =
                    stringToSectionAllPagesFramework(sectionAllPagesFrameworkString)
                if (sectionAllDataString == "") {
                    sectionAllData = createPresentSectionAllDataUsingSectionAllPagesFramework(
                        sectionAllPagesFramework
                    )
                } else {
                    sectionAllData = stringToSectionAllData(sectionAllDataString)
                }
            } else {
                sectionAllPagesFramework =
                    stringToSectionAllPagesFramework(sectionAllPagesFrameworkString)
                sectionAllPagesFramework.sectionPageFrameworkList.add(defaultSectionPageFramework)
                sectionAllData = createPresentSectionAllDataUsingSectionAllPagesFramework(
                    sectionAllPagesFramework
                )
            }
            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                sectionAllPagesFramework,
                sectionAllData,
                sectionPagesFrameworkAndDataID
            )

            withContext(Dispatchers.Main) {
                this@AInfo5ViewModel.presentSectionAllPagesFramework = sectionAllPagesFramework
                this@AInfo5ViewModel.presentSectionAllData = sectionAllData
                sectionAllPagesFrameworkLoadedFlagMLD.value = true
                sectionAllDataLoadedFlagMLD.value = true
                //Log.d(MainActivity.TESTING_TAG, "loadThePresentSectionAllPagesFrameworkAndAllDataUsingStrings: Monica ")
            }
        }
    }

    //This live data flag is meant to indicate if the All Pages Framework is uploaded or not
    //False means no. True means yes.
    var sectionAllPagesFrameworkLoadedFlagMLD = MutableLiveData<Boolean?>()


    fun questionsFrameworkItemToML(questionsFrameworkItem: QuestionsFrameworkItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(questionsFrameworkItem.questionsFrameworkTitle)
        result.add(questionsFrameworkItem.pageCode)
        result.add(questionsFrameworkItem.serialStatus)
        result.add(questionsFrameworkItem.isExpandable.toString())
        return result
    }

    //delimiterLevel1 is used here
    fun questionsFrameworkItemToString(questionsFrameworkItem: QuestionsFrameworkItemDC): String {
        var questionsFrameworkItemString = ""
        val questionsFrameworkItemML = questionsFrameworkItemToML(questionsFrameworkItem)
        questionsFrameworkItemString = mlToStringUsingDelimiter1(questionsFrameworkItemML)
        return questionsFrameworkItemString
    }

    fun stringToQuestionsFrameworkItem(input: String): QuestionsFrameworkItemDC {
        val questionsFrameworkItem = QuestionsFrameworkItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        questionsFrameworkItem.questionsFrameworkTitle = ""
                        questionsFrameworkItem.pageCode = ""
                        questionsFrameworkItem.serialStatus = ""
                        questionsFrameworkItem.isExpandable = false
                    }
                    1 -> {
                        questionsFrameworkItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkItem.pageCode = ""
                        questionsFrameworkItem.serialStatus = ""
                        questionsFrameworkItem.isExpandable = false
                    }
                    2 -> {
                        questionsFrameworkItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkItem.pageCode = stringList[1]
                        questionsFrameworkItem.serialStatus = ""
                        questionsFrameworkItem.isExpandable = false
                    }
                    3 -> {
                        questionsFrameworkItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkItem.pageCode = stringList[1]
                        questionsFrameworkItem.serialStatus = stringList[2]
                        questionsFrameworkItem.isExpandable = false
                    }
                    4 -> {
                        questionsFrameworkItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkItem.pageCode = stringList[1]
                        questionsFrameworkItem.serialStatus = stringList[2]
                        questionsFrameworkItem.isExpandable = stringList[3].toBoolean()
                    }
                    else -> {
                        questionsFrameworkItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkItem.pageCode = stringList[1]
                        questionsFrameworkItem.serialStatus = stringList[2]
                        questionsFrameworkItem.isExpandable = stringList[3].toBoolean()
                    }
                }
            } else {
                questionsFrameworkItem.questionsFrameworkTitle = input
                questionsFrameworkItem.pageCode = ""
                questionsFrameworkItem.serialStatus = ""
                questionsFrameworkItem.isExpandable = false
            }
        } else {
            questionsFrameworkItem.questionsFrameworkTitle = ""
            questionsFrameworkItem.pageCode = ""
            questionsFrameworkItem.serialStatus = ""
            questionsFrameworkItem.isExpandable = false
        }
        return questionsFrameworkItem
    }

    fun checkboxesFrameworkItemToML(checkboxesFrameworkItem: CheckboxesFrameworkItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxesFrameworkItem.checkboxesFrameworkTitle)
        result.add(checkboxesFrameworkItem.pageCode)
        result.add(checkboxesFrameworkItem.serialStatus)
        result.add(checkboxesFrameworkItem.isExpandable.toString())
        return result
    }

    fun checkboxesFrameworkItemToString(checkboxesFrameworkItem: CheckboxesFrameworkItemDC): String {
        var checkboxesFrameworkItemString = ""
        val checkboxesFrameworkItemML = checkboxesFrameworkItemToML(checkboxesFrameworkItem)
        checkboxesFrameworkItemString = mlToStringUsingDelimiter1(checkboxesFrameworkItemML)
        return checkboxesFrameworkItemString
    }

    fun stringToCheckboxesFrameworkItem(input: String): CheckboxesFrameworkItemDC {
        val checkboxesFrameworkItem = CheckboxesFrameworkItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = ""
                        checkboxesFrameworkItem.pageCode = ""
                        checkboxesFrameworkItem.serialStatus = ""
                        checkboxesFrameworkItem.isExpandable = false
                    }
                    1 -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkItem.pageCode = ""
                        checkboxesFrameworkItem.serialStatus = ""
                        checkboxesFrameworkItem.isExpandable = false
                    }
                    2 -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkItem.pageCode = stringList[1]
                        checkboxesFrameworkItem.serialStatus = ""
                        checkboxesFrameworkItem.isExpandable = false
                    }
                    3 -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkItem.pageCode = stringList[1]
                        checkboxesFrameworkItem.serialStatus = stringList[2]
                        checkboxesFrameworkItem.isExpandable = false
                    }
                    4 -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkItem.pageCode = stringList[1]
                        checkboxesFrameworkItem.serialStatus = stringList[2]
                        checkboxesFrameworkItem.isExpandable = stringList[3].toBoolean()
                    }
                    else -> {
                        checkboxesFrameworkItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkItem.pageCode = stringList[1]
                        checkboxesFrameworkItem.serialStatus = stringList[2]
                        checkboxesFrameworkItem.isExpandable = stringList[3].toBoolean()
                    }
                }
            } else {
                checkboxesFrameworkItem.checkboxesFrameworkTitle = input
                checkboxesFrameworkItem.pageCode = ""
                checkboxesFrameworkItem.serialStatus = ""
                checkboxesFrameworkItem.isExpandable = false
            }
        } else {
            checkboxesFrameworkItem.checkboxesFrameworkTitle = ""
            checkboxesFrameworkItem.pageCode = ""
            checkboxesFrameworkItem.serialStatus = ""
            checkboxesFrameworkItem.isExpandable = false
        }
        return checkboxesFrameworkItem
    }

    //deLimiterLevel2 is used here
    fun questionsFrameworkListToString(input: MutableList<QuestionsFrameworkItemDC>): String {
        var result = ""
        if (input.isNotEmpty()) {
            val inputMLToStringML = mutableListOf<String>()
            for (index in 0 until input.size) {
                val itemToString = questionsFrameworkItemToString(input[index])
                inputMLToStringML.add(itemToString)
            }
            result = mlToStringUsingDelimiter2(inputMLToStringML)
        }
        return result
    }

    fun stringToQuestionsFrameworkList(input: String): MutableList<QuestionsFrameworkItemDC> {
        val result = mutableListOf<QuestionsFrameworkItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val questionsFrameworkItem = stringToQuestionsFrameworkItem(inputML[index])
                result.add(questionsFrameworkItem)
            }
        }
        return result
    }

    fun checkboxesFrameworkListToString(input: MutableList<CheckboxesFrameworkItemDC>): String {
        var result = ""
        if (input.isNotEmpty()) {
            val inputMLToStringML = mutableListOf<String>()
            for (index in 0 until input.size) {
                val itemToString = checkboxesFrameworkItemToString(input[index])
                inputMLToStringML.add(itemToString)
            }
            result = mlToStringUsingDelimiter2(inputMLToStringML)
        }
        return result
    }

    fun stringToCheckboxesFrameworkList(input: String): MutableList<CheckboxesFrameworkItemDC> {
        val result = mutableListOf<CheckboxesFrameworkItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val checkboxesFrameworkItem = stringToCheckboxesFrameworkItem(inputML[index])
                result.add(checkboxesFrameworkItem)
            }
        }
        return result
    }

    //DelimiterLevel3 is used here
    fun sectionPageFrameworkToML(sectionPageFramework: SectionPageFrameworkDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(sectionPageFramework.pageTitle)
        result.add(sectionPageFramework.pageCode)
        result.add(sectionPageFramework.pageNumber.toString())
        result.add(questionsFrameworkListToString(sectionPageFramework.questionsFrameworkList))
        result.add(checkboxesFrameworkListToString(sectionPageFramework.observationsCheckboxesFrameworkList))
        result.add(checkboxesFrameworkListToString(sectionPageFramework.recommendationsCheckboxesFrameworkList))
        result.add(checkboxesFrameworkListToString(sectionPageFramework.standardsCheckboxesFrameworkList))
        return result
    }

    fun sectionPageFrameworkToString(sectionPageFramework: SectionPageFrameworkDC): String {
        var result = ""
        val sectionPageFrameworkMl = sectionPageFrameworkToML(sectionPageFramework)
        result = mlToStringUsingDelimiter3(sectionPageFrameworkMl)
        return result
    }

    fun stringToSectionPageFramework(input: String): SectionPageFrameworkDC {
        val sectionPageFramework = SectionPageFrameworkDC()
        if (input != "") {
            if (input.contains(delimiterLevel3)) {
                val stringList = input.split(delimiterLevel3)
                when (stringList.size) {
                    0 -> {
                        sectionPageFramework.pageTitle = ""
                        sectionPageFramework.pageCode = ""
                        sectionPageFramework.pageNumber = "".toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    1 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = ""
                        sectionPageFramework.pageNumber = "".toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    2 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = "".toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    3 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    4 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    5 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    6 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
                    }
                    7 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[6])
                    }
                    else -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsCheckboxesFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[6])
                    }
                }
            } else {
                sectionPageFramework.pageTitle = input
                sectionPageFramework.pageCode = ""
                sectionPageFramework.pageNumber = "".toInt()
                sectionPageFramework.questionsFrameworkList = mutableListOf()
                sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
                sectionPageFramework.recommendationsCheckboxesFrameworkList = mutableListOf()
                sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
            }
        } else {
            sectionPageFramework.pageTitle = ""
            sectionPageFramework.pageCode = ""
            sectionPageFramework.pageNumber = "".toInt()
            sectionPageFramework.questionsFrameworkList = mutableListOf()
            sectionPageFramework.observationsCheckboxesFrameworkList = mutableListOf()
            sectionPageFramework.recommendationsCheckboxesFrameworkList = mutableListOf()
            sectionPageFramework.standardsCheckboxesFrameworkList = mutableListOf()
        }
        return sectionPageFramework
    }

    //DeLimiterLevel4 is used here
    fun sectionAllPagesFrameworkToString(input: SectionAllPagesFrameworkDC): String {
        var result = ""
        if (input.sectionPageFrameworkList.isNotEmpty()) {
            val inputMLToStringML = mutableListOf<String>()
            for (index in 0 until input.sectionPageFrameworkList.size) {
                val itemToString =
                    sectionPageFrameworkToString(input.sectionPageFrameworkList[index])
                inputMLToStringML.add(itemToString)
            }
            result = mlToStringUsingDelimiter4(inputMLToStringML)
        }

        return result
    }

    fun stringToSectionAllPagesFramework(input: String): SectionAllPagesFrameworkDC {
        val result = SectionAllPagesFrameworkDC()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter4(input)
            for (index in 0 until inputML.size) {
                val sectionPageFramework = stringToSectionPageFramework(inputML[index])
                result.sectionPageFrameworkList.add(sectionPageFramework)
            }
        }
        return result
    }

//Template related Variables and Functions

    private var templateString: String? = ""
    fun setTheTemplateString(input: String?) {
        templateString = input
    }

    fun getTheTemplateString(): String? {
        return templateString
    }

    //This flag is meant to check if the templates have been loaded into the DB or not
    //If templates have been loaded, it is true else false
    private var templatesHaveBeenLoadedIntoDatabaseFlag = false
    fun getTheTemplatesHaveBeenLoadedIntoDBFlag(): Boolean {
        return templatesHaveBeenLoadedIntoDatabaseFlag
    }

    fun setTheTemplatesHaveBeenLoadedIntoDBFlag(input: Boolean) {
        templatesHaveBeenLoadedIntoDatabaseFlag = input
    }

    //This flag ensures that the templateHaveBeenLoadedIntoDatabase flag
    // from db is put into the ViewModel only once after the app is started
    private var ObserveAndActOnceForTemplatesLoadedFlag: Boolean = false
    fun setTheObserveAndActOnceForTemplatesLoadedFlag(input: Boolean) {
        ObserveAndActOnceForTemplatesLoadedFlag = input
    }

    fun getTheObserveAndActOnceForTemplatesLoadedFlag(): Boolean {
        return ObserveAndActOnceForTemplatesLoadedFlag
    }

    fun loadDefaultTemplatesIntoTemplateDatabase(templateString: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val fileString = templateString
                val fileStringList: List<String>
                val templateIDList = mutableListOf<String>()
                var pageCode = ""
                val pageContentsML = mutableListOf<String>()
                if (fileString != "") {
                    fileStringList = fileString.split("\n")
                    for (line in fileStringList) {
                        if (line.contains(",")) {
                            val lineList = line.split(",")

                            if (lineList[0].contains("Page Group ID") || lineList[0].contains("Add Page Menu ID") || lineList[0].contains(
                                    "Page Code ID"
                                )
                            ) {
                                continue
                            } else if (lineList[0].contains("PG_") || lineList[0].contains("APM_") || lineList[0].contains(
                                    "Default_Section_List"
                                )
                            ) {
                                if (!templateIDList.contains(lineList[0])) {
                                    templateIDList.add(lineList[0])
                                }
                                val lineItemList = mutableListOf<String>()
                                for (index in 1 until lineList.size) {
                                    if (lineList[index] != "") {
                                        lineItemList.add(lineList[index])
                                    }
                                }

                                val lineItemListString = mlToStringUsingDelimiter1(lineItemList)
                                val aInfo5Template = AInfo5Templates(
                                    lineList[0],
                                    lineItemListString
                                )
                                insertAInfo5Templates(aInfo5Template)
                            } else if (lineList[0].contains("PC_")) {
                                if (!templateIDList.contains(lineList[0])) {
                                    templateIDList.add(lineList[0])
                                }
                                val blockContentsML = mutableListOf<String>()
                                for (index in 1 until lineList.size) {
                                    if (lineList[index] != "") {
                                        blockContentsML.add(lineList[index])
                                    }
                                }
                                val blockContentsMLString =
                                    mlToStringUsingDelimiter1(blockContentsML)
                                if (pageCode == "") {
                                    pageCode = lineList[0]
                                    pageContentsML.add(blockContentsMLString)
                                } else {
                                    if (pageCode == lineList[0]) {
                                        pageContentsML.add(blockContentsMLString)
                                        if (fileStringList.indexOf(line) == fileStringList.lastIndex - 1) {
                                            val pageBlockListString =
                                                mlToStringUsingDelimiter2(pageContentsML)
                                            val aInfo5Template = AInfo5Templates(
                                                pageCode,
                                                pageBlockListString
                                            )
                                            insertAInfo5Templates(aInfo5Template)
                                        }
                                    } else {
                                        val pageBlockListString =
                                            mlToStringUsingDelimiter2(pageContentsML)
                                        val aInfo3Template = AInfo5Templates(
                                            pageCode,
                                            pageBlockListString
                                        )
                                        insertAInfo5Templates(aInfo3Template)
                                        pageContentsML.clear()
                                        pageCode = lineList[0]
                                        pageContentsML.add(blockContentsMLString)
                                    }
                                }
                            }
                        }
                    }
                }

                //Load the Template ID List into the Template Database
                val aInfo3TemplateListID = AInfo5Templates(
                    MainActivity.TEMPLATE_IDs_LIST_ID,
                    mlToStringUsingDelimiter1(templateIDList)
                )
                insertAInfo5Templates(aInfo3TemplateListID)
            }
        }
    }

    //Get the list of template ids from the template database
    var templateIDListsID = mutableListOf(MainActivity.TEMPLATE_IDs_LIST_ID)
    val getTemplateIdsListStringFromTemplateDB = getAInfo5TemplatesByIds(templateIDListsID)

    private var templateIDList = mutableListOf<String>()
    fun setTheTemplateIDList(input: MutableList<String>) {
        templateIDList = input
    }

    fun getTheTemplateIDList(): MutableList<String> {
        return templateIDList
    }

    //Page group IDs List
    private var pageGroupIDsList = mutableListOf<String>()
    fun setThePageGroupIDsList(input: MutableList<String>) {
        pageGroupIDsList = input
    }

    fun getThePageGroupIDsList(): MutableList<String> {
        return pageGroupIDsList
    }

    fun tasksToDoWithTemplateIDsListString(templateIDsListString: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val templateIDsList = stringToMLUsingDlimiter1(templateIDsListString)
                val pageGroupIDsList = collectTermsForPageGroup(templateIDsList)
                withContext(Dispatchers.Main) {
                    setTheTemplateIDList(templateIDsList)
                    setThePageGroupIDsList(pageGroupIDsList)
                    templateIDsUploadingCompletedMLD.value = templateIDsList.isNotEmpty()
                    pageGroupIDsUploadingCompletedMLD.value = pageGroupIDsList.isNotEmpty()
                }
            }
        }
    }

    fun tasksToDoWithPageGroupIDs(
        pageGroupItemString: String,
        idsListForPageGroup: MutableList<String>,
        indexOfPageGroupItem: Int
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val pageGroupItemList = stringToMLUsingDlimiter1(pageGroupItemString)
                val rvParentChildParentItem = RVParentChildParentItemDC()

                rvParentChildParentItem.pageCode = idsListForPageGroup[indexOfPageGroupItem]
                when (pageGroupItemList.size) {
                    0 -> {
                        rvParentChildParentItem.title = ""
                        rvParentChildParentItem.childItemList = mutableListOf()
                    }
                    1 -> {
                        rvParentChildParentItem.title = pageGroupItemList[0]
                        rvParentChildParentItem.childItemList =
                            mutableListOf("No Pages Present")
                    }
                    else -> {
                        rvParentChildParentItem.title = pageGroupItemList[0]
                        rvParentChildParentItem.childItemList = mutableListOf()
                        for (index2 in 1 until pageGroupItemList.size) {
                            rvParentChildParentItem.childItemList.add(
                                pageGroupItemList[index2]
                            )
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    addToTheParentChildParentItemML(rvParentChildParentItem)
                }
            }
        }
    }

    fun collectTermsForPageGroup(input: MutableList<String>): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input.isNotEmpty()) {
            for (index in input.indices) {
                if (input[index].contains("PG_") && input[index].contains("_PG")) {
                    resultList.add(input[index])
                }
            }
        } else {
            resultList = mutableListOf()
        }
        return resultList
    }

    private var parentChildParentItemML = mutableListOf<RVParentChildParentItemDC>()
    fun setTheParentChildParentItemML(input: MutableList<RVParentChildParentItemDC>) {
        parentChildParentItemML = input
    }

    fun getTheParentChildParentItemML(): MutableList<RVParentChildParentItemDC> {
        return parentChildParentItemML
    }

    fun addToTheParentChildParentItemML(input: RVParentChildParentItemDC) {
        parentChildParentItemML.add(input)
    }

    var templateIDsUploadingCompletedMLD = MutableLiveData<Boolean?>()
    var pageGroupIDsUploadingCompletedMLD = MutableLiveData<Boolean?>()
    var parentChildParentItemListMLUploadingCompletedMLD = MutableLiveData<Boolean?>()

    //This function gets the Display Name from the Page Code
    fun extractDisplayNameFromPageCode(pageCode: String): String {
        var result = ""
        if (pageCode != "") {
            if (pageCode.contains("_PC") && pageCode.contains("PC_")) {
                result = pageCode.replace("_PC", "")
                result = result.replace("PC_", "")
                result = result.dropLast(3)
                result = result.replace("_", " ").trim()
            } else {
                result = pageCode
            }
        }
        return result
    }

    //This function gets the page code from the given display name
    fun returnPageCodeFromName(input: String): String {
        var result = ""
        val allIDsList = getTheTemplateIDList()
        //val pageCodeAndPageStringList = getAllDownloadedPageCodeAndPCString()
        if (input != "") {
            if (allIDsList.isNotEmpty()) {
                for (item in allIDsList) {
                    val displayName = extractDisplayNameFromPageCode(item)
                    if (input.contains(displayName)) {
                        result = item
                        break
                    }
                }
            }
        }
        return result
    }

    //Database string converted to QuestionTemplateItem and CheckboxTemplateItem
    fun dbStringToQuestionTemplateItemList(pageCodeString: String): MutableList<QuestionTemplateItemDC> {
        val result = mutableListOf<QuestionTemplateItemDC>()
        if (pageCodeString != "" && pageCodeString.contains(delimiterLevel2)) {
            val delimiter2Items = pageCodeString.split(delimiterLevel2)

            for (itemLevel2 in delimiter2Items) {
                if (itemLevel2 != "" && itemLevel2.contains(delimiterLevel1)) {
                    val delimiter1Items = itemLevel2.split(delimiterLevel1)

                    val size = delimiter1Items.size
                    val checkItem = delimiter1Items[0]
                    if (size >= 22) {
                        if (checkItem.first() == 'B' && checkItem.substring(1)
                                .toIntOrNull() != null
                        ) {
                            val questionItem = QuestionTemplateItemDC()
                            questionItem.blockNumber = delimiter1Items[0]
                            questionItem.question = delimiter1Items[1].replace("#", ",")
                            questionItem.message =
                                delimiter1Items[2].replace("#", ",").replace("\\", "\n")
                            questionItem.mandatory = delimiter1Items[3]
                            questionItem.data1Visibility = delimiter1Items[4].toBoolean()
                            questionItem.data1Hint = delimiter1Items[5].replace("#", ",")
                            questionItem.data1Type = delimiter1Items[6]
                            questionItem.data1Label =
                                delimiter1Items[7].replace("#", ",").replace("\\", "\n")
                            questionItem.data1Sentence1 = delimiter1Items[8].replace("#", ",")
                            questionItem.data1Sentence2 = delimiter1Items[9].replace("#", ",")

                            questionItem.data2Visibility = delimiter1Items[10].toBoolean()
                            questionItem.data2Hint = delimiter1Items[11].replace("#", ",")
                            questionItem.data2Type = delimiter1Items[12]
                            questionItem.data2Label =
                                delimiter1Items[13].replace("#", ",").replace("\\", "\n")
                            questionItem.data2Sentence1 = delimiter1Items[14].replace("#", ",")
                            questionItem.data2Sentence2 = delimiter1Items[15].replace("#", ",")

                            questionItem.data3Visibility = delimiter1Items[16].toBoolean()
                            questionItem.data3Hint = delimiter1Items[17].replace("#", ",")
                            questionItem.data3Type = delimiter1Items[18]
                            questionItem.data3Label =
                                delimiter1Items[19].replace("#", ",").replace("\\", "\n")
                            questionItem.data3Sentence1 = delimiter1Items[20].replace("#", ",")
                            questionItem.data3Sentence2 = delimiter1Items[21].replace("#", ",")

                            questionItem.buttonVisibility = delimiter1Items[22].toBoolean()
                            if (size > 23) {
                                for (index in 23 until size) {
                                    questionItem.buttonOptionsList.add(delimiter1Items[index])
                                }
                            }
                            result.add(questionItem)
                        }
                    }

                }
            }
        }
        return result
    }

    fun dbStringtoObsCheckboxTemplateItemList(input: String): MutableList<CheckboxTemplateItemDC> {
        val result = mutableListOf<CheckboxTemplateItemDC>()
        if (input != "" && input.contains(delimiterLevel2)) {
            val delimiter2Items = input.split(delimiterLevel2)
            for (itemLevel2 in delimiter2Items) {
                if (itemLevel2 != "" && itemLevel2.contains(delimiterLevel1)) {
                    val delimiter1Items = itemLevel2.split(delimiterLevel1)
                    val size = delimiter1Items.size
                    val checkItem = delimiter1Items[0]
                    if (checkItem.contains("Obs")) {
                        if (size >= 2) {
                            val checkboxItem = CheckboxTemplateItemDC()
                            checkboxItem.checkboxLabel = delimiter1Items[0]
                            checkboxItem.checkboxVisibility = delimiter1Items[1].toBoolean()
                            result.add(checkboxItem)
                        }
                    }
                }
            }
        }

        return result
    }

    fun dbStringtoRecoCheckboxTemplateItemList(input: String): MutableList<CheckboxTemplateItemDC> {
        val result = mutableListOf<CheckboxTemplateItemDC>()
        if (input != "" && input.contains(delimiterLevel2)) {
            val delimiter2Items = input.split(delimiterLevel2)
            for (itemLevel2 in delimiter2Items) {
                if (itemLevel2 != "" && itemLevel2.contains(delimiterLevel1)) {
                    val delimiter1Items = itemLevel2.split(delimiterLevel1)
                    val size = delimiter1Items.size
                    val checkItem = delimiter1Items[0]
                    if (checkItem.contains("Reco")) {
                        if (size >= 2) {
                            val checkboxItem = CheckboxTemplateItemDC()
                            checkboxItem.checkboxLabel = delimiter1Items[0]
                            checkboxItem.checkboxVisibility = delimiter1Items[1].toBoolean()
                            result.add(checkboxItem)
                        }
                    }
                }
            }
        }

        return result
    }

    fun dbStringtoStdsCheckboxTemplateItemList(input: String): MutableList<CheckboxTemplateItemDC> {
        val result = mutableListOf<CheckboxTemplateItemDC>()
        if (input != "" && input.contains(delimiterLevel2)) {
            val delimiter2Items = input.split(delimiterLevel2)
            for (itemLevel2 in delimiter2Items) {
                if (itemLevel2 != "" && itemLevel2.contains(delimiterLevel1)) {
                    val delimiter1Items = itemLevel2.split(delimiterLevel1)
                    val size = delimiter1Items.size
                    val checkItem = delimiter1Items[0]
                    if (checkItem.contains("Standard")) {
                        if (size >= 2) {
                            val checkboxItem = CheckboxTemplateItemDC()
                            checkboxItem.checkboxLabel = delimiter1Items[0]
                            checkboxItem.checkboxVisibility = delimiter1Items[1].toBoolean()
                            result.add(checkboxItem)
                        }
                    }
                }
            }
        }

        return result
    }

    //This variable and associated functions stores a list of template pages gotten from the db
    private var downloadedTemplateItemList = mutableListOf<PageTemplateDC>()
    fun addUniqueItemToDownloadedTemplateItems(input: PageTemplateDC) {
        var isItemPresentFlag = false
        for (item in downloadedTemplateItemList) {
            if (item.pageCode == input.pageCode) {
                isItemPresentFlag = true
            }
        }
        if (isItemPresentFlag == false) {
            downloadedTemplateItemList.add(input)
        }

    }

    fun isItemPresentInDownloadedTemplateItemList(pageCode: String): Boolean {
        var result = false
        for (item in downloadedTemplateItemList) {
            if (item.pageCode == pageCode) {
                result = true
                break
            }
        }

        return result
    }

    fun getItemFromDownloadedTemplateItemList(pageCode: String): PageTemplateDC {
        var result = PageTemplateDC()

        for (item in downloadedTemplateItemList) {
            if (item.pageCode == pageCode) {
                result = item
            }
        }

        return result
    }

//Data Related Variables and Functions

    //Mutable Live Data variables for the EditTexts and TextViews that Matter
    //Some are two way binding variables. Others are one way binding only
    var etIntroductionsMLD = MutableLiveData<String>()

    var tvPhotoPathsInIntroductionsFragmentMLD = MutableLiveData<String>()

    var tvPhotoPathsInObservationsFragmentMLD = MutableLiveData<String>()

    var etObservationsFragmentMLD = MutableLiveData<String>()

    var etRecommendationsMLD = MutableLiveData<String>()

    var tvStandardsMLD = MutableLiveData<String>()

    var etPageNameMLD = MutableLiveData<String>()

    var pageCountMLD = MutableLiveData<Int>()


    //Variables to store Company Intro Data
    var companyIntroData = CompanyIntroDataDC()
    fun setTheCompanyIntroData(input: CompanyIntroDataDC) {
        companyIntroData = input
    }

    fun getTheCompanyIntroData(): CompanyIntroDataDC {
        return companyIntroData
    }

    //Variables to store section data
    var presentSectionAllData = SectionAllDataDC()
    fun setThePresentSectionAllData(input: SectionAllDataDC) {
        presentSectionAllData = input
    }

    fun createPresentSectionAllDataUsingSectionAllPagesFramework(input: SectionAllPagesFrameworkDC): SectionAllDataDC {
        val presentSectionAllData = SectionAllDataDC()
        val sectionAllPagesData = presentSectionAllData.sectionAllPagesData
        if (input.sectionPageFrameworkList.isNotEmpty()) {
            for (index in 0 until input.sectionPageFrameworkList.size) {
                val pageFramework = input.sectionPageFrameworkList[index]
                val sectionPageData = SectionPageDataDC()
                sectionPageData.pageTitle = pageFramework.pageTitle
                sectionPageData.pageNumber = pageFramework.pageNumber

                if (pageFramework.questionsFrameworkList.isNotEmpty()) {
                    val questionsFrameworkDataItemList =
                        mutableListOf<QuestionsFrameworkDataItemDC>()
                    val questionsFrameworkList = pageFramework.questionsFrameworkList
                    for (qIndex in 0 until questionsFrameworkList.size) {
                        val questionsFrameworkDataItem = QuestionsFrameworkDataItemDC()
                        questionsFrameworkDataItem.questionsFrameworkTitle =
                            questionsFrameworkList[qIndex].questionsFrameworkTitle
                        questionsFrameworkDataItem.pageCode =
                            questionsFrameworkList[qIndex].pageCode
                        questionsFrameworkDataItemList.add(questionsFrameworkDataItem)
                    }
                    sectionPageData.questionsFrameworkDataItemList = questionsFrameworkDataItemList
                }

                if (pageFramework.observationsCheckboxesFrameworkList.isNotEmpty()) {
                    val observationsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val observationsCheckboxesFrameworkList =
                        pageFramework.observationsCheckboxesFrameworkList
                    for (oIndex in 0 until observationsCheckboxesFrameworkList.size) {
                        val checkboxesFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle =
                            observationsCheckboxesFrameworkList[oIndex].checkboxesFrameworkTitle
                        checkboxesFrameworkDataItem.pageCode =
                            observationsCheckboxesFrameworkList[oIndex].pageCode
                        observationsFrameworkDataItemList.add(checkboxesFrameworkDataItem)
                    }
                    sectionPageData.observationsFrameworkDataItemList =
                        observationsFrameworkDataItemList
                }

                if (pageFramework.recommendationsCheckboxesFrameworkList.isNotEmpty()) {
                    val recommendationsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val recommendationsCheckboxesFrameworkList =
                        pageFramework.recommendationsCheckboxesFrameworkList
                    for (rIndex in 0 until recommendationsCheckboxesFrameworkList.size) {
                        val checkboxesFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle =
                            recommendationsCheckboxesFrameworkList[rIndex].checkboxesFrameworkTitle
                        checkboxesFrameworkDataItem.pageCode =
                            recommendationsCheckboxesFrameworkList[rIndex].pageCode
                        recommendationsFrameworkDataItemList.add(checkboxesFrameworkDataItem)
                    }
                    sectionPageData.recommendationsFrameworkDataItemList =
                        recommendationsFrameworkDataItemList
                }
                if (pageFramework.standardsCheckboxesFrameworkList.isNotEmpty()) {
                    val standardsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val standardsCheckboxesFrameworkList =
                        pageFramework.standardsCheckboxesFrameworkList
                    for (rIndex in 0 until standardsCheckboxesFrameworkList.size) {
                        val checkboxesFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle =
                            standardsCheckboxesFrameworkList[rIndex].checkboxesFrameworkTitle
                        checkboxesFrameworkDataItem.pageCode =
                            standardsCheckboxesFrameworkList[rIndex].pageCode
                        standardsFrameworkDataItemList.add(checkboxesFrameworkDataItem)
                    }
                    sectionPageData.standardsFrameworkDataItemList = standardsFrameworkDataItemList
                }
                sectionAllPagesData.sectionPageDataList.add(sectionPageData)
            }
        }
        return presentSectionAllData
    }

    fun setThePresentSectionAllDataUsingString(input: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val sectionAllData = stringToSectionAllData(input)
            withContext(Dispatchers.Main) {
                presentSectionAllData = sectionAllData
            }
        }
    }

    fun convertThePresentSectionAllDataToString(input: SectionAllDataDC): String {
        var result = ""
        viewModelScope.launch(Dispatchers.Default) {
            val sectionAllDataString = sectionAllDataToString(input)
            withContext(Dispatchers.Main) {
                result = sectionAllDataString
            }
        }
        return result
    }

    fun saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
        sectionAllPagesFramework: SectionAllPagesFrameworkDC,
        sectionAllData: SectionAllDataDC,
        sectionIDForDB: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val sectionAllPagesFrameworkString =
                sectionAllPagesFrameworkToString(sectionAllPagesFramework)
            val sectionAllDataString = sectionAllDataToString(sectionAllData)
            val aInfo5 =
                AInfo5(sectionIDForDB, sectionAllPagesFrameworkString, sectionAllDataString)
            insertAInfo5(aInfo5)
        }
    }

    fun getThePresentSectionAllData(): SectionAllDataDC {
        return presentSectionAllData
    }

    fun getIntroFromThePresentSectionAllData(): String {
        return presentSectionAllData.introduction
    }

    fun getPicturePathsFromThePresentSectionAllData(): String {
        return presentSectionAllData.picturePathsInIntroductions
    }

    fun updateIntroForThePresentSectionAllData(input: String) {
        presentSectionAllData.introduction = input
    }

    fun updatePicturePathsForThePresentSectionAllData(input: String) {
        presentSectionAllData.picturePathsInIntroductions = input
    }

    var sectionAllDataLoadedFlagMLD = MutableLiveData<Boolean?>()


    fun questionDataItemToML(questionDataItem: QuestionDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(questionDataItem.blockNumber)
        result.add(questionDataItem.mandatoryValue)
        result.add(questionDataItem.data1Value)
        result.add(questionDataItem.data2Value)
        result.add(questionDataItem.data3Value)
        result.add(questionDataItem.buttonOptionChosen)
        return result
    }

    //DelimiterLevel1 is used here
    fun questionDataItemToString(questionDataItem: QuestionDataItemDC): String {
        var result = ""
        val resultML = questionDataItemToML(questionDataItem)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToQuestionDataItem(input: String): QuestionDataItemDC {
        val questionDataItem = QuestionDataItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        questionDataItem.blockNumber = ""
                        questionDataItem.mandatoryValue = ""
                        questionDataItem.data1Value = ""
                        questionDataItem.data2Value = ""
                        questionDataItem.data3Value = ""
                        questionDataItem.buttonOptionChosen = ""
                    }
                    1 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = ""
                        questionDataItem.data1Value = ""
                        questionDataItem.data2Value = ""
                        questionDataItem.data3Value = ""
                        questionDataItem.buttonOptionChosen = ""
                    }
                    2 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = ""
                        questionDataItem.data2Value = ""
                        questionDataItem.data3Value = ""
                        questionDataItem.buttonOptionChosen = ""
                    }
                    3 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = stringList[2]
                        questionDataItem.data2Value = ""
                        questionDataItem.data3Value = ""
                        questionDataItem.buttonOptionChosen = ""
                    }
                    4 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = stringList[2]
                        questionDataItem.data2Value = stringList[3]
                        questionDataItem.data3Value = ""
                        questionDataItem.buttonOptionChosen = ""
                    }
                    5 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = stringList[2]
                        questionDataItem.data2Value = stringList[3]
                        questionDataItem.data3Value = stringList[4]
                        questionDataItem.buttonOptionChosen = ""
                    }
                    6 -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = stringList[2]
                        questionDataItem.data2Value = stringList[3]
                        questionDataItem.data3Value = stringList[4]
                        questionDataItem.buttonOptionChosen = stringList[5]
                    }
                    else -> {
                        questionDataItem.blockNumber = stringList[0]
                        questionDataItem.mandatoryValue = stringList[1]
                        questionDataItem.data1Value = stringList[2]
                        questionDataItem.data2Value = stringList[3]
                        questionDataItem.data3Value = stringList[4]
                        questionDataItem.buttonOptionChosen = stringList[5]
                    }
                }
            } else {
                questionDataItem.blockNumber = input
                questionDataItem.mandatoryValue = ""
                questionDataItem.data1Value = ""
                questionDataItem.data2Value = ""
                questionDataItem.data3Value = ""
                questionDataItem.buttonOptionChosen = ""
            }
        } else {
            questionDataItem.blockNumber = ""
            questionDataItem.mandatoryValue = ""
            questionDataItem.data1Value = ""
            questionDataItem.data2Value = ""
            questionDataItem.data3Value = ""
            questionDataItem.buttonOptionChosen = ""
        }
        return questionDataItem
    }

    //DelimiterLevel2 is used here
    fun questionDataItemListToString(questionDataItemList: MutableList<QuestionDataItemDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (questionDataItemList.isNotEmpty()) {
            for (index in 0 until questionDataItemList.size) {
                val questionDataItemString = questionDataItemToString(questionDataItemList[index])
                resultML.add(questionDataItemString)
            }
            result = mlToStringUsingDelimiter2(resultML)
        }
        return result
    }

    fun stringToQuestionDataItemList(input: String): MutableList<QuestionDataItemDC> {
        val result = mutableListOf<QuestionDataItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val questionDataItem = stringToQuestionDataItem(inputML[index])
                result.add(questionDataItem)
            }
        }
        return result
    }

    //DelimiterLevel1 is used here
    fun checkboxDataItemToML(checkboxDataItem: CheckboxDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxDataItem.checkboxTickedValue.toString())
        result.add(checkboxDataItem.priorityValues)
        return result
    }

    fun checkboxDataItemToString(checkboxDataItem: CheckboxDataItemDC): String {
        var result = ""
        val resultML = checkboxDataItemToML(checkboxDataItem)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToCheckboxDataItem(input: String): CheckboxDataItemDC {
        val checkboxDataItem = CheckboxDataItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        checkboxDataItem.checkboxTickedValue = false
                        checkboxDataItem.priorityValues = ""
                    }
                    1 -> {
                        checkboxDataItem.checkboxTickedValue = stringList[0].toBoolean()
                        checkboxDataItem.priorityValues = ""
                    }
                    2 -> {
                        checkboxDataItem.checkboxTickedValue = stringList[0].toBoolean()
                        checkboxDataItem.priorityValues = stringList[1]

                    }
                    else -> {
                        checkboxDataItem.checkboxTickedValue = stringList[0].toBoolean()
                        checkboxDataItem.priorityValues = stringList[1]
                    }
                }
            } else {
                checkboxDataItem.checkboxTickedValue = input.toBoolean()
                checkboxDataItem.priorityValues = ""
            }
        } else {
            checkboxDataItem.checkboxTickedValue = false
            checkboxDataItem.priorityValues = ""
        }
        return checkboxDataItem
    }

    //DelimiterLevel2 is used here
    fun checkboxDataItemListToString(checkboxDataItemList: MutableList<CheckboxDataItemDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (checkboxDataItemList.isNotEmpty()) {
            for (index in 0 until checkboxDataItemList.size) {
                val checkboxDataItemString = checkboxDataItemToString(checkboxDataItemList[index])
                resultML.add(checkboxDataItemString)
            }
            result = mlToStringUsingDelimiter2(resultML)
        }
        return result
    }

    fun stringToCheckboxDataItemList(input: String): MutableList<CheckboxDataItemDC> {
        val result = mutableListOf<CheckboxDataItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val checkboxDataItem = stringToCheckboxDataItem(inputML[index])
                result.add(checkboxDataItem)
            }
        }
        return result
    }

    //DelimiterLevel2 is used here
    fun questionsFrameworkDataItemToML(questionsFrameworkDataItem: QuestionsFrameworkDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(questionsFrameworkDataItem.questionsFrameworkTitle)
        result.add(questionsFrameworkDataItem.pageCode)
        result.add(questionDataItemListToString(questionsFrameworkDataItem.questionDataItemList))
        return result
    }

    //DelimiterLevel 3 is used here
    fun questionsFrameworkDataItemToString(questionsFrameworkDataItem: QuestionsFrameworkDataItemDC): String {
        var result = ""
        val resultML = questionsFrameworkDataItemToML(questionsFrameworkDataItem)
        result = mlToStringUsingDelimiter3(resultML)
        return result
    }

    fun stringToQuestionsFrameworkDataItem(input: String): QuestionsFrameworkDataItemDC {
        val questionsFrameworkDataItem = QuestionsFrameworkDataItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel3)) {
                val stringList = input.split(delimiterLevel3)
                when (stringList.size) {
                    0 -> {
                        questionsFrameworkDataItem.questionsFrameworkTitle = ""
                        questionsFrameworkDataItem.pageCode = ""
                        questionsFrameworkDataItem.questionDataItemList = mutableListOf()
                    }
                    1 -> {
                        questionsFrameworkDataItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkDataItem.pageCode = ""
                        questionsFrameworkDataItem.questionDataItemList = mutableListOf()
                    }
                    2 -> {
                        questionsFrameworkDataItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkDataItem.pageCode = stringList[1]
                        questionsFrameworkDataItem.questionDataItemList = mutableListOf()

                    }
                    3 -> {
                        questionsFrameworkDataItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkDataItem.pageCode = stringList[1]
                        questionsFrameworkDataItem.questionDataItemList =
                            stringToQuestionDataItemList(stringList[2])
                    }
                    else -> {
                        questionsFrameworkDataItem.questionsFrameworkTitle = stringList[0]
                        questionsFrameworkDataItem.pageCode = stringList[1]
                        questionsFrameworkDataItem.questionDataItemList =
                            stringToQuestionDataItemList(stringList[2])
                    }
                }
            } else {
                questionsFrameworkDataItem.questionsFrameworkTitle = input
                questionsFrameworkDataItem.pageCode = ""
                questionsFrameworkDataItem.questionDataItemList = mutableListOf()
            }
        } else {
            questionsFrameworkDataItem.questionsFrameworkTitle = ""
            questionsFrameworkDataItem.pageCode = ""
            questionsFrameworkDataItem.questionDataItemList = mutableListOf()
        }
        return questionsFrameworkDataItem
    }

    fun checkboxesFrameworkDataItemToML(checkboxesFrameworkDataItem: CheckboxesFrameworkDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxesFrameworkDataItem.checkboxesFrameworkTitle)
        result.add(checkboxesFrameworkDataItem.pageCode)
        result.add(checkboxDataItemListToString(checkboxesFrameworkDataItem.checkboxDataItemML))
        return result
    }

    fun checkboxesFrameworkDataItemToString(checkboxesFrameworkDataItem: CheckboxesFrameworkDataItemDC): String {
        var result = ""
        val resultML = checkboxesFrameworkDataItemToML(checkboxesFrameworkDataItem)
        result = mlToStringUsingDelimiter3(resultML)
        return result
    }

    fun stringToCheckboxesFrameworkDataItem(input: String): CheckboxesFrameworkDataItemDC {
        val checkboxesFrameworkDataItem = CheckboxesFrameworkDataItemDC()
        if (input != "") {
            if (input.contains(delimiterLevel3)) {
                val stringList = input.split(delimiterLevel3)
                when (stringList.size) {
                    0 -> {
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle = ""
                        checkboxesFrameworkDataItem.pageCode = ""
                        checkboxesFrameworkDataItem.checkboxDataItemML = mutableListOf()
                    }
                    1 -> {
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkDataItem.pageCode = ""
                        checkboxesFrameworkDataItem.checkboxDataItemML = mutableListOf()
                    }
                    2 -> {
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkDataItem.pageCode = stringList[1]
                        checkboxesFrameworkDataItem.checkboxDataItemML = mutableListOf()

                    }
                    3 -> {
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkDataItem.pageCode = stringList[1]
                        checkboxesFrameworkDataItem.checkboxDataItemML =
                            stringToCheckboxDataItemList(stringList[2])
                    }
                    else -> {
                        checkboxesFrameworkDataItem.checkboxesFrameworkTitle = stringList[0]
                        checkboxesFrameworkDataItem.pageCode = stringList[1]
                        checkboxesFrameworkDataItem.checkboxDataItemML =
                            stringToCheckboxDataItemList(stringList[2])
                    }
                }
            } else {
                checkboxesFrameworkDataItem.checkboxesFrameworkTitle = input
                checkboxesFrameworkDataItem.pageCode = ""
                checkboxesFrameworkDataItem.checkboxDataItemML = mutableListOf()
            }
        } else {
            checkboxesFrameworkDataItem.checkboxesFrameworkTitle = ""
            checkboxesFrameworkDataItem.pageCode = ""
            checkboxesFrameworkDataItem.checkboxDataItemML = mutableListOf()
        }
        return checkboxesFrameworkDataItem
    }

    //DelimiterLevel4 is used here
    fun questionsFrameworkDataItemListToString(questionsFrameworkDataItemList: MutableList<QuestionsFrameworkDataItemDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (questionsFrameworkDataItemList.isNotEmpty()) {
            for (index in 0 until questionsFrameworkDataItemList.size) {
                val questionsFrameworkDataItemString =
                    questionsFrameworkDataItemToString(questionsFrameworkDataItemList[index])
                resultML.add(questionsFrameworkDataItemString)
            }
            result = mlToStringUsingDelimiter4(resultML)
        }
        return result
    }

    fun stringToQuestionsFrameworkDataItemList(input: String): MutableList<QuestionsFrameworkDataItemDC> {
        val questionsFrameworkDataItemList = mutableListOf<QuestionsFrameworkDataItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter4(input)
            for (index in 0 until inputML.size) {
                val questionsFrameworkDataItem = stringToQuestionsFrameworkDataItem(inputML[index])
                questionsFrameworkDataItemList.add(questionsFrameworkDataItem)
            }
        }
        return questionsFrameworkDataItemList
    }

    fun checkboxesFrameworkDataItemListToString(checkboxesFrameworkDataItemList: MutableList<CheckboxesFrameworkDataItemDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (checkboxesFrameworkDataItemList.isNotEmpty()) {
            for (index in 0 until checkboxesFrameworkDataItemList.size) {
                val checkboxesFrameworkDataItemString =
                    checkboxesFrameworkDataItemToString(checkboxesFrameworkDataItemList[index])
                resultML.add(checkboxesFrameworkDataItemString)
            }
            result = mlToStringUsingDelimiter4(resultML)
        }
        return result
    }

    fun stringToCheckboxesFrameworkDataItemList(input: String): MutableList<CheckboxesFrameworkDataItemDC> {
        val checkboxesFrameworkDataItemList = mutableListOf<CheckboxesFrameworkDataItemDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter4(input)
            for (index in 0 until inputML.size) {
                val checkboxesFrameworkDataItem =
                    stringToCheckboxesFrameworkDataItem(inputML[index])
                checkboxesFrameworkDataItemList.add(checkboxesFrameworkDataItem)
            }
        }
        return checkboxesFrameworkDataItemList
    }


    fun sectionPageDataToML(sectionPageData: SectionPageDataDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(sectionPageData.pageTitle)
        result.add(sectionPageData.pageNumber.toString())
        result.add(sectionPageData.observations)
        result.add(sectionPageData.picturePaths)
        result.add(sectionPageData.recommendations)
        result.add(sectionPageData.standards)
        result.add(questionsFrameworkDataItemListToString(sectionPageData.questionsFrameworkDataItemList))
        result.add(checkboxesFrameworkDataItemListToString(sectionPageData.observationsFrameworkDataItemList))
        result.add(checkboxesFrameworkDataItemListToString(sectionPageData.recommendationsFrameworkDataItemList))
        result.add(checkboxesFrameworkDataItemListToString(sectionPageData.standardsFrameworkDataItemList))
        return result
    }

    //DelimiterLevel5 is used here
    fun sectionPageDataToString(sectionPageData: SectionPageDataDC): String {
        var result = ""
        val resultML = sectionPageDataToML(sectionPageData)
        result = mlToStringUsingDelimiter5(resultML)
        return result
    }

    fun stringToSectionPageData(input: String): SectionPageDataDC {
        val sectionPageData = SectionPageDataDC()
        if (input != "") {
            if (input.contains(delimiterLevel5)) {
                val stringList = input.split(delimiterLevel5)
                when (stringList.size) {
                    0 -> {
                        sectionPageData.pageTitle = ""
                        sectionPageData.pageNumber = 0
                        sectionPageData.observations = ""
                        sectionPageData.picturePaths = ""
                        sectionPageData.recommendations = ""
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    1 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = 0
                        sectionPageData.observations = ""
                        sectionPageData.picturePaths = ""
                        sectionPageData.recommendations = ""
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    2 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = ""
                        sectionPageData.picturePaths = ""
                        sectionPageData.recommendations = ""
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    3 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = ""
                        sectionPageData.recommendations = ""
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    4 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = ""
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    5 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = ""
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    6 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    7 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList =
                            stringToQuestionsFrameworkDataItemList(stringList[6])
                        sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    8 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList =
                            stringToQuestionsFrameworkDataItemList(stringList[6])
                        sectionPageData.observationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[7])
                        sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    9 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList =
                            stringToQuestionsFrameworkDataItemList(stringList[6])
                        sectionPageData.observationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[7])
                        sectionPageData.recommendationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[8])
                        sectionPageData.standardsFrameworkDataItemList = mutableListOf()
                    }
                    10 -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList =
                            stringToQuestionsFrameworkDataItemList(stringList[6])
                        sectionPageData.observationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[7])
                        sectionPageData.recommendationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[8])
                        sectionPageData.standardsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[9])
                    }
                    else -> {
                        sectionPageData.pageTitle = stringList[0]
                        sectionPageData.pageNumber = stringList[1].toInt()
                        sectionPageData.observations = stringList[2]
                        sectionPageData.picturePaths = stringList[3]
                        sectionPageData.recommendations = stringList[4]
                        sectionPageData.standards = stringList[5]
                        sectionPageData.questionsFrameworkDataItemList =
                            stringToQuestionsFrameworkDataItemList(stringList[6])
                        sectionPageData.observationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[7])
                        sectionPageData.recommendationsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[8])
                        sectionPageData.standardsFrameworkDataItemList =
                            stringToCheckboxesFrameworkDataItemList(stringList[9])
                    }
                }
            } else {
                sectionPageData.pageTitle = input
                sectionPageData.pageNumber = 0
                sectionPageData.observations = ""
                sectionPageData.picturePaths = ""
                sectionPageData.recommendations = ""
                sectionPageData.standards = ""
                sectionPageData.questionsFrameworkDataItemList = mutableListOf()
                sectionPageData.observationsFrameworkDataItemList = mutableListOf()
                sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
                sectionPageData.standardsFrameworkDataItemList = mutableListOf()
            }
        } else {
            sectionPageData.pageTitle = ""
            sectionPageData.pageNumber = 0
            sectionPageData.observations = ""
            sectionPageData.picturePaths = ""
            sectionPageData.recommendations = ""
            sectionPageData.standards = ""
            sectionPageData.questionsFrameworkDataItemList = mutableListOf()
            sectionPageData.observationsFrameworkDataItemList = mutableListOf()
            sectionPageData.recommendationsFrameworkDataItemList = mutableListOf()
            sectionPageData.standardsFrameworkDataItemList = mutableListOf()
        }
        return sectionPageData
    }

    //DelimiterLevel6 is used here
    fun sectionPageDataListToString(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (sectionPageDataList.isNotEmpty()) {
            for (index in 0 until sectionPageDataList.size) {
                val sectionPageDataString = sectionPageDataToString(sectionPageDataList[index])
                resultML.add(sectionPageDataString)
            }
            result = mlToStringUsingDelimiter6(resultML)
        }
        return result
    }

    fun stringToSectionPageDataList(input: String): MutableList<SectionPageDataDC> {
        val sectionPageDataList = mutableListOf<SectionPageDataDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter6(input)
            for (index in 0 until inputML.size) {
                val sectionPageData = stringToSectionPageData(inputML[index])
                sectionPageDataList.add(sectionPageData)
            }
        }

        return sectionPageDataList
    }


    fun sectionAllDataToML(sectionAllData: SectionAllDataDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(sectionAllData.introduction)
        result.add(sectionAllData.picturePathsInIntroductions)
        result.add(sectionPageDataListToString(sectionAllData.sectionAllPagesData.sectionPageDataList))
        return result
    }

    //DelimiterLevel7 is used here
    fun sectionAllDataToString(sectionAllData: SectionAllDataDC): String {
        var result = ""
        val resultML = sectionAllDataToML(sectionAllData)
        result = mlToStringUsingDelimiter7(resultML)
        return result
    }

    fun stringToSectionAllData(input: String): SectionAllDataDC {
        val sectionAllData = SectionAllDataDC()
        if (input != "") {
            if (input.contains(delimiterLevel7)) {
                val stringList = input.split(delimiterLevel7)
                when (stringList.size) {
                    0 -> {
                        sectionAllData.introduction = ""
                        sectionAllData.picturePathsInIntroductions = ""
                        sectionAllData.sectionAllPagesData.sectionPageDataList = mutableListOf()
                    }
                    1 -> {
                        sectionAllData.introduction = stringList[0]
                        sectionAllData.picturePathsInIntroductions = ""
                        sectionAllData.sectionAllPagesData.sectionPageDataList = mutableListOf()
                    }
                    2 -> {
                        sectionAllData.introduction = stringList[0]
                        sectionAllData.picturePathsInIntroductions = stringList[1]
                        sectionAllData.sectionAllPagesData.sectionPageDataList = mutableListOf()
                    }
                    3 -> {
                        sectionAllData.introduction = stringList[0]
                        sectionAllData.picturePathsInIntroductions = stringList[1]
                        sectionAllData.sectionAllPagesData.sectionPageDataList =
                            stringToSectionPageDataList(stringList[2])
                    }
                    else -> {
                        sectionAllData.introduction = stringList[0]
                        sectionAllData.picturePathsInIntroductions = stringList[1]
                        sectionAllData.sectionAllPagesData.sectionPageDataList =
                            stringToSectionPageDataList(stringList[2])
                    }
                }
            } else {
                sectionAllData.introduction = input
                sectionAllData.picturePathsInIntroductions = ""
                sectionAllData.sectionAllPagesData.sectionPageDataList = mutableListOf()
            }
        } else {
            sectionAllData.introduction = ""
            sectionAllData.picturePathsInIntroductions = ""
            sectionAllData.sectionAllPagesData.sectionPageDataList = mutableListOf()
        }
        return sectionAllData
    }

    //DelimiterLevel1 is used here
    fun companyIntroDataToML(companyIntroData: CompanyIntroDataDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(companyIntroData.introduction)
        result.add(companyIntroData.picturePathsInIntroductions)
        return result
    }

    fun companyIntroDataToString(companyIntroData: CompanyIntroDataDC): String {
        var result = ""
        val resultML = companyIntroDataToML(companyIntroData)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToCompanyIntroData(input: String): CompanyIntroDataDC {
        val companyIntroData = CompanyIntroDataDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        companyIntroData.introduction = ""
                        companyIntroData.picturePathsInIntroductions = ""
                    }
                    1 -> {
                        companyIntroData.introduction = stringList[0]
                        companyIntroData.picturePathsInIntroductions = ""
                    }
                    2 -> {
                        companyIntroData.introduction = stringList[0]
                        companyIntroData.picturePathsInIntroductions = stringList[1]

                    }
                    else -> {
                        companyIntroData.introduction = stringList[0]
                        companyIntroData.picturePathsInIntroductions = stringList[1]
                    }
                }
            } else {
                companyIntroData.introduction = input
                companyIntroData.picturePathsInIntroductions = ""
            }
        } else {
            companyIntroData.introduction = ""
            companyIntroData.picturePathsInIntroductions = ""
        }
        return companyIntroData
    }

//Reports Related Variables and Functions for reports of different types

    private var reportsToBeGeneratedList = mutableListOf<String>()
    fun getTheReportsToBeGeneratedList(): MutableList<String> {
        return reportsToBeGeneratedList
    }

    fun setTheReportsToBeGeneratedList(input: MutableList<String>) {
        reportsToBeGeneratedList = input
    }

    fun generateReports(inputList: MutableList<String>) {
        val reportsList =
            Resources.getSystem().getStringArray(R.array.Report_Choices).toMutableList()
        if (reportsList.size >= 6) {
            if (inputList.isNotEmpty()) {
                for (index in 0 until inputList.size) {
                    if (reportsList.contains(inputList[index])) {
                        if (reportsList.indexOf(inputList[index]) == 0) {
                            generateThreeColumnWordReport()
                        } else if (reportsList.indexOf(inputList[index]) == 1) {
                            generateThreeColumnExcelReport()
                        } else if (reportsList.indexOf(inputList[index]) == 2) {
                            generateSixColumnWordReport()
                        } else if (reportsList.indexOf(inputList[index]) == 3) {
                            generateSixColumnExcelReport()
                        } else if (reportsList.indexOf(inputList[index]) == 4) {
                            generateChecklistWordReport()
                        } else if (reportsList.indexOf(inputList[index]) == 5) {
                            generateChecklistExcelReport()
                        }
                    }
                }
            }
        }
    }


    fun generateThreeColumnWordReport() {

    }

    fun generateThreeColumnExcelReport() {

    }

    fun generateSixColumnWordReport() {

    }

    fun generateSixColumnExcelReport() {

    }

    fun generateChecklistWordReport() {

    }

    fun generateChecklistExcelReport() {

    }

//Screen Related Variables

    private var screen = ""
    fun setTheScreenVariable(input: String) {
        screen = input
    }

    fun getTheScreenVariable(): String {
        return screen
    }

    private var previousScreen = ""
    fun setThePreviousScreenVariable(input: String) {
        previousScreen = input
    }

    fun getThePreviousScreenVariable(): String {
        return previousScreen
    }

    private var previousScreen2 = ""
    fun setThePreviousScreen2Variable(input: String) {
        previousScreen2 = input
    }

    fun getThePreviousScreen2Variable(): String {
        return previousScreen2
    }

//Photos and Camera related Variable and Functions


    //The below function changes the photo name to photoName_M. M stands for Modified
    fun modifyPhotoName(selectedPhotoName: String): String {
        var result = ""
        if (selectedPhotoName != "") {
            if (selectedPhotoName.contains(".jpg")) {
                if (selectedPhotoName.contains("_M.jpg")) {
                    result = selectedPhotoName
                } else {
                    result = selectedPhotoName.replace(".jpg", "_M.jpg")
                }

            } else {
                result = selectedPhotoName + "_M.jpg"
            }
        } else {
            result = "Default.jpg"
        }
        return result
    }

    private var locationForPhotosTaken = ""
    fun setLocationForPhotos(item: String) {
        locationForPhotosTaken = item
    }

    fun getLocationForPhotos(): String {
        return locationForPhotosTaken
    }

    fun makeLocationForPhotos(locationVariable: String): String {
        var result = ""
        when (locationVariable) {
            MainActivity.COMPANY_INTRODUCTION -> {
                result = getPresentCompanyCode() + "_" + MainActivity.INTRODUCTIONS_PAGE_NAME
            }
            MainActivity.SECTION_INTRODUCTION -> {
                result = getPresentSectionCode() + "_" + MainActivity.INTRODUCTIONS_PAGE_NAME
            }
            MainActivity.SECTION_OBSERVATIONS -> {
                //result = getPresentSectionCode() + "Present Page Code" + "_Page_" + pageCountMLD.value.toString()
            }
        }
        return result
    }


//DeLimiters and String to * and * to String Functions
    //DeLimiters

    var delimiterLevel1 = "{%DASPL#1}"

    var delimiterLevel2 = "{%DASPL#2}"

    var delimiterLevel3 = "{%DASPL#3}"

    var delimiterLevel4 = "{%DASPL#4}"

    var delimiterLevel5 = "{%DASPL#5}"

    var delimiterLevel6 = "{%DASPL#6}"

    var delimiterLevel7 = "{%DASPL#7}"

    fun mlToStringUsingDelimiter1(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel1 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDlimiter1(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel1)) {
                val inputList = input.split(delimiterLevel1)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter2(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel2 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter2(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel2)) {
                val inputList = input.split(delimiterLevel2)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter3(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel3 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter3(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel3)) {
                val inputList = input.split(delimiterLevel3)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter4(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel4 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter4(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel4)) {
                val inputList = input.split(delimiterLevel4)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter5(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel5 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter5(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel5)) {
                val inputList = input.split(delimiterLevel5)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter6(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel6 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter6(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel6)) {
                val inputList = input.split(delimiterLevel6)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun mlToStringUsingDelimiter7(inputList: MutableList<String>): String {
        var result = ""
        if (inputList.isNotEmpty()) {
            for (index in 0 until inputList.size) {
                if (index == 0) {
                    result = inputList[0]
                } else {
                    result += delimiterLevel7 + inputList[index]
                }
            }
        } else {
            result = ""
        }
        return result
    }

    fun stringToMLUsingDelimiter7(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel6)) {
                val inputList = input.split(delimiterLevel7)
                for (index in 0 until inputList.size) {
                    resultList.add(inputList[index])
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    fun makeLinkedHashMapFromML(input: MutableList<String>): LinkedHashMap<String, Boolean> {
        var result = linkedMapOf<String, Boolean>()
        if (input.isNotEmpty()) {
            if (input.size >= 3) {
                for (index in 0 until input.size) {
                    if (index == 0 || index == 2) {
                        result[input[index]] = false
                    } else {
                        result[input[index]] = false
                    }
                }
            }
        } else {
            result = linkedMapOf()
        }
        return result
    }

    // Code and Display Related functions for Company and Sections
    fun stringToCodeAndDisplayCollection(
        input: String,
        companyOrSectionFlag: String
    ): MutableList<CodeNameAndDisplayNameDC> {
        val codeAndDisplayML = mutableListOf<CodeNameAndDisplayNameDC>()
        if (input == "") {
            codeAndDisplayML.clear()
        } else {
            if (input.contains(delimiterLevel2) == true) {
                val listLevel2 = input.split(delimiterLevel2)
                for (item in listLevel2) {
                    if (item.contains(delimiterLevel1) == true) {
                        val listLevel1 = item.split(delimiterLevel1)
                        val codeAndDisplay = CodeNameAndDisplayNameDC(listLevel1[0], listLevel1[1])
                        codeAndDisplayML.add(codeAndDisplay)
                    } else {
                        val codeAndDisplay = generateUniqueCodeFromCDCollection(
                            codeAndDisplayML,
                            item,
                            companyOrSectionFlag
                        )
                        codeAndDisplayML.add(codeAndDisplay)
                    }
                }
            } else {
                if (input.contains(delimiterLevel1) == true) {
                    val listLevel1 = input.split(delimiterLevel1)
                    if (listLevel1.size > 2) {
                        for (item in listLevel1) {
                            val codeAndDisplay = generateUniqueCodeFromCDCollection(
                                codeAndDisplayML,
                                item,
                                companyOrSectionFlag
                            )
                            if (codeAndDisplay != CodeNameAndDisplayNameDC("", "")) {
                                codeAndDisplayML.add(codeAndDisplay)
                            }
                        }
                    } else {
                        val codeAndDisplay = CodeNameAndDisplayNameDC(listLevel1[0], listLevel1[1])
                        if (codeAndDisplay != CodeNameAndDisplayNameDC("", "")) {
                            codeAndDisplayML.add(codeAndDisplay)
                        }
                    }
                } else {
                    val codeAndDisplay = generateUniqueCodeFromCDCollection(
                        codeAndDisplayML,
                        input,
                        companyOrSectionFlag
                    )
                    codeAndDisplayML.add(codeAndDisplay)
                }
            }
        }
        return codeAndDisplayML
    }

    fun codeAndDisplayCollectionToString(input: MutableList<CodeNameAndDisplayNameDC>): String {
        var codeAndDisplayString = ""
        if (input.isEmpty()) {
            codeAndDisplayString = ""
        } else {
            codeAndDisplayString = ""
            for (item in input) {
                if (input.indexOf(item) == 0) {
                    if (item.uniqueCodeName != "" && (item.uniqueCodeName.contains("Comp_") && item.uniqueCodeName.contains(
                            "_Comp"
                        ) || item.uniqueCodeName.contains("Section_") && item.uniqueCodeName.contains(
                            "_Section"
                        )) && item.displayName != ""
                    ) {
                        val level1String = item.uniqueCodeName + delimiterLevel1 + item.displayName
                        codeAndDisplayString = level1String
                    } else {
                        codeAndDisplayString = ""
                    }
                } else {
                    if (item.uniqueCodeName != "" && (item.uniqueCodeName.contains("Comp_") && item.uniqueCodeName.contains(
                            "_Comp"
                        ) || item.uniqueCodeName.contains("Section_") && item.uniqueCodeName.contains(
                            "_Section"
                        )) && item.displayName != ""
                    ) {
                        val level1String = item.uniqueCodeName + delimiterLevel1 + item.displayName
                        codeAndDisplayString += delimiterLevel2 + level1String
                    } else {
                        continue
                    }
                }
            }
        }
        return codeAndDisplayString
    }

    fun generateUniqueCodeFromCDCollection(
        codeAndDisplayML: MutableList<CodeNameAndDisplayNameDC>,
        displayName: String,
        companyOrSectionFlag: String
    ): CodeNameAndDisplayNameDC {
        val codeNameAndDisplay: CodeNameAndDisplayNameDC
        val maxNumber = extractMaxNumberFromCDCollection(codeAndDisplayML)
        val codeNumber = generateCodeNumberStringFromInt(maxNumber)
        var itemCode = ""
        if (companyOrSectionFlag == MainActivity.FLAG_VALUE_COMPANY) {
            itemCode = "Comp_" + displayName.replace("\\s".toRegex(), "")
                .trim() + "_" + codeNumber + "_Comp"
        } else if (companyOrSectionFlag == MainActivity.FLAG_VALUE_SECTION) {
            itemCode = "Section_" + displayName.replace("\\s".toRegex(), "")
                .trim() + "_" + codeNumber + "_Section"
        }

        //This shows whether the item code exists in the codeAndDisplayML
        var codeNameIsUniqueFlag = true
        if (codeAndDisplayML.isNotEmpty()) {
            for (item in codeAndDisplayML) {
                if (item.uniqueCodeName == itemCode) {
                    codeNameIsUniqueFlag = false
                }
            }
        } else {
            codeNameIsUniqueFlag = true
        }

        if (codeNameIsUniqueFlag == false) {
            codeNameAndDisplay = CodeNameAndDisplayNameDC("", "")
        } else {
            codeNameAndDisplay = CodeNameAndDisplayNameDC(itemCode, displayName)
        }
        return codeNameAndDisplay
    }

    fun extractMaxNumberFromCDCollection(codeAndDisplayML: MutableList<CodeNameAndDisplayNameDC>): Int {
        var maxNumber = 0
        if (codeAndDisplayML.isNotEmpty()) {
            for (item in codeAndDisplayML) {
                val list = item.uniqueCodeName.split("_")
                if (list.isNotEmpty() && list.size > 3) {
                    val intNumber = list[2].toIntOrNull()
                    if (intNumber != null) {
                        if (intNumber >= maxNumber) {
                            maxNumber = intNumber
                        }
                    } else {
                        continue
                    }
                } else {
                    maxNumber = 0
                }
            }
        } else {
            maxNumber = 0
        }
        return maxNumber
    }

    fun generateCodeNumberStringFromInt(maxNumber: Int): String {
        var codeNumber = ""
        var maxNumberPositive = 0
        if (maxNumber < 0) {
            maxNumberPositive = -maxNumber
        } else {
            maxNumberPositive = maxNumber
        }
        val codeNumberString = (maxNumberPositive + 1).toString()
        when (codeNumberString.length) {
            1 -> codeNumber = "000" + codeNumberString
            2 -> codeNumber = "00" + codeNumberString
            3 -> codeNumber = "0" + codeNumberString
            4 -> codeNumber = codeNumberString
        }
        return codeNumber
    }

    // Check for uniqueness of a name in a names and codes list string
    // This function returns true if the name is not present in the list submitted.
    fun uniquenessCheckInCodesAndNames(
        namesAndCodesML: MutableList<CodeNameAndDisplayNameDC>,
        name: String
    ): Boolean {
        var uniquenessFlag = true
        if (namesAndCodesML.isNotEmpty()) {
            for (item in namesAndCodesML) {
                if (item.displayName == name.trim()) {
                    uniquenessFlag = false
                    break
                }
            }
        } else {
            uniquenessFlag = true
        }
        return uniquenessFlag
    }


    //File and Directory related Functions and Variables
    private var companyDirectoryUriId: String = ""
    fun setTheCompanyDirectoryUriId(companyCode: String) {
        companyDirectoryUriId = companyCode + MainActivity
            .COMPANY_DIRECTORY_URI_ID
    }

    fun getTheCompanyDirectoryUriId(): String {
        return companyDirectoryUriId
    }

    private var companyDirectoryURIString: String = ""
    fun getTheCompanyDirectoryURIString(): String {
        return companyDirectoryURIString
    }

    fun setTheCompanyDirectoryURIString(input: String) {
        companyDirectoryURIString = input
    }

    //Flag is meant for choosing between directory choosing and
    //template document uploading
    private var fileFlag: String? = null
    fun setTheFileFlag(input: String?) {
        fileFlag = input
    }

    fun getTheFileFlag(): String? {
        return fileFlag
    }


    fun makeAChildDirectory(companyName: String, companyCode: String = "", dirUri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val companyDirectory =
                    DocumentFile.fromTreeUri(context, dirUri)?.createDirectory(companyName)
                val companyUri = companyDirectory?.uri
                val companyDirectoryUriId = companyCode + MainActivity.COMPANY_DIRECTORY_URI_ID
                val aInfo5 =
                    AInfo5(companyDirectoryUriId, companyUri.toString())
                insertAInfo5(aInfo5)
                addUniqueItemToPresentCompanyAllIds(companyDirectoryUriId)
            }
        }
    }

    fun directoryExists(companyName: String, dirUri: Uri?): Boolean? {
        val context = getApplication<Application>().applicationContext
        val fileExists =
            DocumentFile.fromTreeUri(context, dirUri!!)?.findFile(companyName)?.exists()
        return if (fileExists == true) {
            DocumentFile.fromTreeUri(context, dirUri)?.findFile(companyName)?.isDirectory
        } else {
            false
        }
    }


    fun renamingFilesAfterSectionNameChange(newSectionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            TODO()
        }
    }

    fun remakeCompanyDirectoryAndMovingFiles(dirUri: Uri, newCompanyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            TODO("to be developed yet")
        }

    }

    fun fileExists(name: String, dirUri: Uri?): Boolean? {
        val context = getApplication<Application>().applicationContext
        return DocumentFile.fromTreeUri(context, dirUri!!)?.findFile(name)?.exists()
    }

    fun createTextFileWithExtension(name: String, dirUri: Uri?, extension: String) {
        viewModelScope.launch {
            val fullFileName = name + extension
            val context = getApplication<Application>().applicationContext
            val file3Column = DocumentFile.fromTreeUri(context, dirUri!!)
                ?.createFile("*/txt", "$fullFileName")
            if (file3Column != null && file3Column.canWrite()) {
                statusMessage.value = Event("The file was created successfully")
                val aInfo5 = AInfo5("$fullFileName", file3Column.uri.toString())
                insertAInfo5(aInfo5)
            } else {
                statusMessage.value = Event("Write error! Please check your permissions")
            }
        }

    }

    fun writeToTextFile(dirUri: Uri?, fullFileName: String, content: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val dir = DocumentFile.fromTreeUri(context, dirUri!!)
                val fileExists = dir?.findFile(fullFileName)
                if (fileExists != null) {
                    if (fileExists.canWrite()) {
                        alterDocument(
                            fileExists.uri, content
                                .toByteArray()
                        )
                    } else {
                        Log.d("LOGTAG", "Cannot write into file")
                        //consider showing some more appropriate error message
                        statusMessage.value = Event("Error: Cannot write into file")
                    }
                } else {
                    val file = dir?.createFile("*/txt", fullFileName)
                    if (file != null && file.canWrite()) {
                        Log.d("LOGTAG", "file.uri = ${file.uri.toString()}")
                        alterDocument(
                            file.uri, content
                                .toByteArray()
                        )

                    } else {
                        Log.d("LOGTAG", "no file or cannot write")
                        statusMessage.value = Event("Error: No file present or cannot write")
                    }
                }
            }
        }
    }

    private suspend fun alterDocument(uri: Uri, data: ByteArray) {
        val context = getApplication<Application>().applicationContext
        try {
            val contentResolver = context.contentResolver
            if (contentResolver != null) {
                contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).use {
                        it.write(
                            data
                        )
                        withContext(Dispatchers.Main) {
                            statusMessage.value = Event("File Write is Successful!")
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            withContext(Dispatchers.Main) {
                statusMessage.value = Event("Error: File not found!")
            }
            e.printStackTrace()
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                statusMessage.value = Event("Error: File Write Failed!")
            }
            e.printStackTrace()
        }
    }


}
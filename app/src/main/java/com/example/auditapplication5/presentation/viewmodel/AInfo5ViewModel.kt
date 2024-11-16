package com.example.auditapplication5.presentation.viewmodel

import android.app.Application
import android.database.SQLException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import com.example.auditapplication5.Event
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.domain.usecase.*
import kotlinx.coroutines.*
import java.io.*

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
    var companyCodesAndNamesID = MainActivity.COMPANY_CODES_NAMES_ID
    val getMLOfCompanyCodesAndNamesLD = getAInfo5ByIds(mutableListOf(companyCodesAndNamesID))

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

    fun saveCompanyCodeAndDisplayIntoDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val companyCodeAndDisplayMLString =
                codeAndDisplayNameListToString(companyCodeAndDisplayNameML)
            val aInfo5 = AInfo5(companyCodesAndNamesID, companyCodeAndDisplayMLString)
            insertAInfo5(aInfo5)
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

    var companyAuditDate = ""
    fun setTheCompanyAuditDate(input: String) {
        companyAuditDate = input
    }

    fun getTheCompanyAuditDate(): String {
        return companyAuditDate
    }


    var auditDateToBeUpdatedFlag: Boolean = false
    fun getTheAuditDateToBeUpdatedFlag(): Boolean {
        return auditDateToBeUpdatedFlag
    }

    fun setTheAuditDateToBeUpdatedFlag(input: Boolean) {
        auditDateToBeUpdatedFlag = input
    }

    //Introductions and Section OBS,RECO, STDS related variables for company and sections
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

    //Present Company Section List
    private var companySectionCodeAndDisplayNameML = mutableListOf<CodeNameAndDisplayNameDC>()
    fun getTheCompanySectionCodeAndDisplayNameML(): MutableList<CodeNameAndDisplayNameDC> {
        return companySectionCodeAndDisplayNameML
    }

    fun setTheCompanySectionCodeAndDisplayNameML(input: MutableList<CodeNameAndDisplayNameDC>) {
        companySectionCodeAndDisplayNameML = input
    }

    fun addToTheCompanySectionCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companySectionCodeAndDisplayNameML.add(input)
    }

    fun deleteSectionInTheCompanySectionCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companySectionCodeAndDisplayNameML.remove(input)
    }

    fun modifyDisplayNameOfSpecificSectionInSectionCDML(
        newDisplayName: String,
        sectionCode: String
    ) {
        for (item in companySectionCodeAndDisplayNameML) {
            if (item.uniqueCodeName == sectionCode) {
                item.displayName = newDisplayName
                break
            }
        }
    }

    fun changePagesPresentToTrueInPresentSectionCodeAndDisplayNameList(sectionCode: String) {
        for (item in companySectionCodeAndDisplayNameML) {
            if (item.uniqueCodeName == sectionCode) {
                item.pagesPresent = true
                break
            }
        }
    }

    fun saveTheCompanySectionCodeAndDisplayMLIntoDB(sectionCodeAndDisplayID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionCodeAndDisplayString =
                codeAndDisplayNameListToString(companySectionCodeAndDisplayNameML)
            val aInfo5 = AInfo5(sectionCodeAndDisplayID, sectionCodeAndDisplayString)
            insertAInfo5(aInfo5)
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

    fun changePagesPresentToTrueInPresentSectionCodeAndDisplayName() {
        presentSectionCodeAndDisplayName.pagesPresent = true
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

    fun addPageFrameworkToPresentSectionAllPagesFramework(
        sectionPageFramework: SectionPageFrameworkDC,
        indexAt: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList.add(indexAt, sectionPageFramework)
    }

    fun deletePageFrameworkInPresentSectionAllPagesFramework(indexAt: Int) {
        presentSectionAllPagesFramework.sectionPageFrameworkList.removeAt(indexAt)

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

    fun updatePageFrameworkTitleInPresentSectionAllPagesFramework(
        pageTitle: String,
        pageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].pageTitle = pageTitle
    }

    fun updatePageFrameworkNumberInPresentSectionAllPagesFramework(
        pageNumber: Int,
        pageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].pageNumber = pageNumber
    }

    fun addPageFrameworkQuestionsInPresentSectionAllPagesFramework(
        input: QuestionsFrameworkItemDC,
        currentPageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].questionsFrameworkList.add(
            input
        )

    }

    fun deletePageFrameworkQuestionsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        questionsListPosition: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].questionsFrameworkList.removeAt(
            questionsListPosition
        )
    }

    fun addPageFrameworkObservationsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].observationsFrameworkList.add(
            input
        )
    }

    fun deletePageFrameworkObservationsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        observationsListPosition: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].observationsFrameworkList.removeAt(
            observationsListPosition
        )
    }

    fun addPageFrameworkRecommendationsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList.add(
            input
        )
    }

    fun deletePageFrameworkRecommendationsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        recommendationsListPosition: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList.removeAt(
            recommendationsListPosition
        )
    }

    fun addPageFrameworkStandardsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList.add(
            input
        )
    }

    fun deletePageFrameworkStandardsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        standardsListPosition: Int
    ) {
        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList.removeAt(
            standardsListPosition
        )
    }

    fun clearThePresentSectionAllPagesFramework() {
        presentSectionAllPagesFramework.sectionPageFrameworkList = mutableListOf()
        setTheSectionAllPagesFrameworkLoadedFlagMLD(false)
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
                //this@AInfo5ViewModel.presentSectionAllPagesFramework = sectionAllPagesFramework
                this@AInfo5ViewModel.setThePresentSectionAllPagesFramework(sectionAllPagesFramework)
                this@AInfo5ViewModel.setThePresentSectionAllData(sectionAllData)
                //this@AInfo5ViewModel.presentSectionAllData = sectionAllData
                setThePageCountMLD(sectionAllPagesFramework.sectionPageFrameworkList.size)
                etPageNameMLD.value =
                    sectionAllPagesFramework.sectionPageFrameworkList[sectionAllPagesFramework.sectionPageFrameworkList.size - 1].pageTitle
                setTheSectionAllPagesFrameworkLoadedFlagMLD(true)
                sectionAllDataLoadedFlagMLD.value = true
            }
        }
    }

    //This Indicates the current index value of the Section Pages Framework that is on display
    private var presentSectionAllPagesFrameworkIndex: Int = 0
    fun setThePresentSectionAllPagesFrameworkIndex(input: Int) {
        presentSectionAllPagesFrameworkIndex = input
    }

    fun getThePresentSectionAllPagesFrameworkIndex(): Int {
        return presentSectionAllPagesFrameworkIndex
    }

    //This is meant to set the Observations View upon entry into the fragment
    private var observationsViewUponEntryFlag: Boolean? = false
    fun setTheObservationsViewUponEntryFlag(input: Boolean?) {
        observationsViewUponEntryFlag = input
    }

    fun getTheObservationsViewUponEntryFlag(): Boolean? {
        return observationsViewUponEntryFlag
    }

    //This live data flag is meant to indicate if the All Pages Framework is uploaded or not
    //False means no. True means yes.
    private var sectionAllPagesFrameworkLoadedFlagMLD = MutableLiveData<Boolean?>()
    val sectionAllPagesFrameworkLoadedFlagLD: LiveData<Boolean?>
        get() = sectionAllPagesFrameworkLoadedFlagMLD

    fun setTheSectionAllPagesFrameworkLoadedFlagMLD(input: Boolean?) {
        sectionAllPagesFrameworkLoadedFlagMLD.value = input
    }

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
        result.add(checkboxesFrameworkListToString(sectionPageFramework.observationsFrameworkList))
        result.add(checkboxesFrameworkListToString(sectionPageFramework.recommendationsFrameworkList))
        result.add(checkboxesFrameworkListToString(sectionPageFramework.standardsFrameworkList))
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
                        sectionPageFramework.observationsFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    1 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = ""
                        sectionPageFramework.pageNumber = "".toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    2 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = "".toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    3 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList = mutableListOf()
                        sectionPageFramework.observationsFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    4 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsFrameworkList = mutableListOf()
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    5 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsFrameworkList =
                            mutableListOf()
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    6 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsFrameworkList = mutableListOf()
                    }
                    7 -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[6])
                    }
                    else -> {
                        sectionPageFramework.pageTitle = stringList[0]
                        sectionPageFramework.pageCode = stringList[1]
                        sectionPageFramework.pageNumber = stringList[2].toInt()
                        sectionPageFramework.questionsFrameworkList =
                            stringToQuestionsFrameworkList(stringList[3])
                        sectionPageFramework.observationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[4])
                        sectionPageFramework.recommendationsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[5])
                        sectionPageFramework.standardsFrameworkList =
                            stringToCheckboxesFrameworkList(stringList[6])
                    }
                }
            } else {
                sectionPageFramework.pageTitle = input
                sectionPageFramework.pageCode = ""
                sectionPageFramework.pageNumber = "".toInt()
                sectionPageFramework.questionsFrameworkList = mutableListOf()
                sectionPageFramework.observationsFrameworkList = mutableListOf()
                sectionPageFramework.recommendationsFrameworkList = mutableListOf()
                sectionPageFramework.standardsFrameworkList = mutableListOf()
            }
        } else {
            sectionPageFramework.pageTitle = ""
            sectionPageFramework.pageCode = ""
            sectionPageFramework.pageNumber = "".toInt()
            sectionPageFramework.questionsFrameworkList = mutableListOf()
            sectionPageFramework.observationsFrameworkList = mutableListOf()
            sectionPageFramework.recommendationsFrameworkList = mutableListOf()
            sectionPageFramework.standardsFrameworkList = mutableListOf()
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

    private var ObserveAndActOnceForTemplatesIDsListFlag: Boolean = false
    fun setTheObserveAndActOnceForTemplatesIDsListFlag(input: Boolean) {
        ObserveAndActOnceForTemplatesIDsListFlag = input
    }

    fun getTheObserveAndActOnceForTemplatesIDsListFlag(): Boolean {
        return ObserveAndActOnceForTemplatesIDsListFlag
    }

    private var ObserveAndActOnceForPageGroupsIDsFlag: Boolean = false
    fun setTheObserveAndActOnceForPageGroupIDsFlag(input: Boolean) {
        ObserveAndActOnceForPageGroupsIDsFlag = input
    }

    fun getTheObserveAndActOnceForPageGroupIDsFlag(): Boolean {
        return ObserveAndActOnceForPageGroupsIDsFlag
    }


    fun loadDefaultTemplatesIntoTemplateDatabase(templateString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val fileString = templateString
            val fileStringList: List<String>
            val templateIDList = mutableListOf<String>()
            val rvParentChildParentItemML = mutableListOf<RVParentChildParentItemDC>()
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
                            val rvParentChildParentItem = RVParentChildParentItemDC()
                            if (!templateIDList.contains(lineList[0])) {
                                templateIDList.add(lineList[0])
                            }
                            if (lineList[0].contains("PG_")) {
                                rvParentChildParentItem.pageGroupCode = lineList[0]
                            }

                            val lineItemList = mutableListOf<String>()
                            rvParentChildParentItem.childItemList = mutableListOf()
                            for (index in 1 until lineList.size) {
                                if (lineList[index] != "") {
                                    lineItemList.add(lineList[index])
                                    if (index == 1) {
                                        rvParentChildParentItem.title = lineList[index]
                                    } else {
                                        rvParentChildParentItem.childItemList.add(lineList[index])
                                    }
                                }
                            }
                            if (rvParentChildParentItem.pageGroupCode.contains("PG_")) {
                                if (rvParentChildParentItem.title != "") {
                                    if (rvParentChildParentItem.childItemList.isEmpty()) {
                                        rvParentChildParentItem.childItemList =
                                            mutableListOf("No Pages Present")
                                    }
                                    if (!rvParentChildParentItemML.contains(rvParentChildParentItem)) {
                                        rvParentChildParentItemML.add(rvParentChildParentItem)
                                    }
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

            //Load the templateLoadedIntoDBFlag into Template DB
            val templateLoadedIntoDBFlagID = MainActivity.TEMPLATES_LOADED_INTO_DB_ID
            val aInfo5Template = AInfo5Templates(templateLoadedIntoDBFlagID, true.toString())
            insertAInfo5Templates(aInfo5Template)
            withContext(Dispatchers.Main) {
                setTheTemplatesHaveBeenLoadedIntoDBFlag(true)
                //Load the ParentChildParentItemML Variable
                setTheParentChildParentItemML(rvParentChildParentItemML)
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

    fun clearTheTemplateIDList() {
        templateIDList = mutableListOf()
    }

    //Page group IDs List
    private var pageGroupIDsList = mutableListOf<String>()
    fun setThePageGroupIDsList(input: MutableList<String>) {
        pageGroupIDsList = input
    }

    fun getThePageGroupIDsList(): MutableList<String> {
        return pageGroupIDsList
    }

    fun clearThePageGroupIDsList() {
        pageGroupIDsList = mutableListOf()
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

                rvParentChildParentItem.pageGroupCode = idsListForPageGroup[indexOfPageGroupItem]
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
                    addUniqueItemToTheParentChildParentItemML(rvParentChildParentItem)
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

    fun addUniqueItemToTheParentChildParentItemML(input: RVParentChildParentItemDC) {
        if (!parentChildParentItemML.contains(input)) {
            parentChildParentItemML.add(input)
        }
    }

    fun clearTheParentChildParentItemML() {
        parentChildParentItemML.clear()
    }

    var defaultRVParentChildParentItem = RVParentChildParentItemDC(
        "General Entry",
        "PC_General_Entry_01_PC",
        mutableListOf("PC_General_Entry_01_PC")
    )

    var templateIDsUploadingCompletedMLD = MutableLiveData<Boolean?>()
    var pageGroupIDsUploadingCompletedMLD = MutableLiveData<Boolean?>()

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

    //Database string converted to Page Template and
    //setting the Template MLDs also
    fun dbStringToPageTemplateAndAddingToTemplatesList(
        pageCode: String,
        pageTemplateString: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            var pageTemplate = PageTemplateDC()
            pageTemplate.pageCode = pageCode
            pageTemplate.questionsList = dbStringToQuestionTemplateItemList(pageTemplateString)
            if (pageTemplateString != "") {
                if (pageTemplateString.contains(delimiterLevel2)) {
                    val delimiter2Items = pageTemplateString.split(delimiterLevel2)
                    for (itemLevel2 in delimiter2Items) {
                        val takeFirst13Characters = itemLevel2.take(13)
                        if (takeFirst13Characters != "") {
                            if (takeFirst13Characters.contains("Obs Remarks")) {
                                pageTemplate.observationsList =
                                    dbStringtoCheckboxTemplateItemList(itemLevel2)
                            } else if (takeFirst13Characters.contains("Reco Remarks")) {
                                pageTemplate.recommendationsList =
                                    dbStringtoCheckboxTemplateItemList(itemLevel2)
                            } else if (takeFirst13Characters.contains("Standards")) {
                                pageTemplate.standardsList =
                                    dbStringtoCheckboxTemplateItemList(itemLevel2)
                            }
                        }
                    }
                }
            } else {
                pageTemplate = getTheDefaultPageTemplate()
            }
            withContext(Dispatchers.Main) {
                addUniquePageToPageTemplateList(pageTemplate)
                setTheQuestionsListMLD(pageTemplate.questionsList)
                setTheObservationsListMLD(pageTemplate.observationsList)
                setTheRecommendationsListMLD(pageTemplate.recommendationsList)
                setTheStandardsListMLD(pageTemplate.standardsList)
                presentTemplateUploadedFlagMLD.value = true
            }
        }
    }

    //Flag to check if the template has been uploaded or not
    //True means uploaded and false not uploaded
    var templatesUploadedFlagMLD = MutableLiveData<Boolean>()

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
                            questionItem.data1Visibility = delimiter1Items[4] == "1"
                            questionItem.data1Hint = delimiter1Items[5].replace("#", ",")
                            questionItem.data1Type = delimiter1Items[6]
                            questionItem.data1Label =
                                delimiter1Items[7].replace("#", ",").replace("\\", "\n")
                            questionItem.data1Sentence1 = delimiter1Items[8].replace("#", ",")
                            questionItem.data1Sentence2 = delimiter1Items[9].replace("#", ",")

                            questionItem.data2Visibility = delimiter1Items[10] == "1"
                            questionItem.data2Hint = delimiter1Items[11].replace("#", ",")
                            questionItem.data2Type = delimiter1Items[12]
                            questionItem.data2Label =
                                delimiter1Items[13].replace("#", ",").replace("\\", "\n")
                            questionItem.data2Sentence1 = delimiter1Items[14].replace("#", ",")
                            questionItem.data2Sentence2 = delimiter1Items[15].replace("#", ",")

                            questionItem.data3Visibility = delimiter1Items[16] == "1"
                            questionItem.data3Hint = delimiter1Items[17].replace("#", ",")
                            questionItem.data3Type = delimiter1Items[18]
                            questionItem.data3Label =
                                delimiter1Items[19].replace("#", ",").replace("\\", "\n")
                            questionItem.data3Sentence1 = delimiter1Items[20].replace("#", ",")
                            questionItem.data3Sentence2 = delimiter1Items[21].replace("#", ",")

                            questionItem.buttonVisibility = delimiter1Items[22] == "1"
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

    fun dbStringtoCheckboxTemplateItemList(input: String): MutableList<CheckboxTemplateItemDC> {
        val result = mutableListOf<CheckboxTemplateItemDC>()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val delimiter1Items = input.split(delimiterLevel1)
                val size = delimiter1Items.size
                for (index in 1 until size) {
                    val checkboxItem = CheckboxTemplateItemDC()
                    checkboxItem.checkboxLabel = delimiter1Items[index]
                    checkboxItem.checkboxVisibility = delimiter1Items[index] != ""
                    result.add(checkboxItem)
                }
            }
        }

//        if (input != "" && input.contains(delimiterLevel2)) {
//            val delimiter2Items = input.split(delimiterLevel2)
//            for (itemLevel2 in delimiter2Items) {
//                if (itemLevel2 != "" && itemLevel2.contains(delimiterLevel1)) {
//                    val delimiter1Items = itemLevel2.split(delimiterLevel1)
//                    val size = delimiter1Items.size
//                    val checkItem = delimiter1Items[0]
//                    if (checkItem.contains("Obs")) {
//                        if (size >= 2) {
//                            val checkboxItem = CheckboxTemplateItemDC()
//                            checkboxItem.checkboxLabel = delimiter1Items[0]
//                            checkboxItem.checkboxVisibility = delimiter1Items[1].toBoolean()
//                            result.add(checkboxItem)
//                        }
//                    }
//                }
//            }
//        }

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

    //This variable holds the Default Page Template
    var defaultPageTemplate = PageTemplateDC(
        "PC_General_Entry_01_PC",
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableListOf()
    )

    fun getTheDefaultPageTemplate(): PageTemplateDC {
        return defaultPageTemplate
    }

    //Current page template that is being displayed
    private var currentPageTemplate = PageTemplateDC()
    fun setTheCurrentPageTemplate(input: PageTemplateDC) {
        currentPageTemplate = input
    }


    //This variable and associated functions stores a list of template pages gotten from the db
    private var pageTemplateList = mutableListOf<PageTemplateDC>()

    fun addUniquePageToPageTemplateList(input: PageTemplateDC) {
        var isItemPresentFlag = false
        for (item in pageTemplateList) {
            if (item.pageCode == input.pageCode) {
                isItemPresentFlag = true
            }
        }
        if (isItemPresentFlag == false) {
            pageTemplateList.add(input)
        }

    }

    //True represents that the item is present
    fun isItemPresentInPageTemplateList(pageCode: String): Boolean {
        var result = false
        if (pageCode != ""){
            for (item in pageTemplateList) {
                if (item.pageCode == pageCode) {
                    result = true
                    break
                }
            }
        }

        return result
    }

    fun getItemFromPageTemplateList(pageCode: String): PageTemplateDC {
        var result = PageTemplateDC()

        for (item in pageTemplateList) {
            if (item.pageCode == pageCode) {
                result = item
            }
        }

        return result
    }

    fun clearThePageTemplateList() {
        pageTemplateList = mutableListOf()
    }

    //Mutable Live Data for Template Lists for Child RV to access
    private var questionsListMLD = MutableLiveData<MutableList<QuestionTemplateItemDC>>()
    val questionsList_LD: LiveData<MutableList<QuestionTemplateItemDC>>
        get() = questionsListMLD

    fun setTheQuestionsListMLD(input: MutableList<QuestionTemplateItemDC>) {
        questionsListMLD.value = input
    }

    private var observationsListMLD = MutableLiveData<MutableList<CheckboxTemplateItemDC>>()
    val observationsList_LD: LiveData<MutableList<CheckboxTemplateItemDC>>
        get() = observationsListMLD

    fun setTheObservationsListMLD(input: MutableList<CheckboxTemplateItemDC>) {
        observationsListMLD.value = input
    }

    private var recommendationsListMLD = MutableLiveData<MutableList<CheckboxTemplateItemDC>>()
    val recommendationsList_LD: LiveData<MutableList<CheckboxTemplateItemDC>>
        get() = recommendationsListMLD

    fun setTheRecommendationsListMLD(input: MutableList<CheckboxTemplateItemDC>) {
        recommendationsListMLD.value = input
    }

    private var standardsListMLD = MutableLiveData<MutableList<CheckboxTemplateItemDC>>()
    val standardsList_LD: LiveData<MutableList<CheckboxTemplateItemDC>>
        get() = standardsListMLD

    fun setTheStandardsListMLD(input: MutableList<CheckboxTemplateItemDC>) {
        standardsListMLD.value = input
    }

    //This flag is meant to indicate that the present Template required has been uploaded
    //False indicates that it has not been uploaded yet.
    var presentTemplateUploadedFlagMLD = MutableLiveData<Boolean?>()

//Data Related Variables and Functions

    //Mutable Live Data variables for the EditTexts and TextViews that Matter
    //Some are two way binding variables. Others are one way binding only
    var etIntroductionsMLD = MutableLiveData<String>()

    var tvPhotoPathsInIntroductionsFragmentMLD = MutableLiveData<String>()

    var tvPhotoPathsInObservationsFragmentMLD = MutableLiveData<String>()

    var etObservationsMLD = MutableLiveData<String>()

    var etRecommendationsMLD = MutableLiveData<String>()

    var tvStandardsMLD = MutableLiveData<String>()

    var etPageNameMLD = MutableLiveData<String>()

    //    val etPageNameLD : LiveData<String>
//    get() = etPageNameMLD
    fun setTheEtPageNameMLD(input: String) {
        etPageNameMLD.value = input
    }

    var pageCountMLD = MutableLiveData<Int>()
    val pageCountLD: LiveData<Int>
        get() = pageCountMLD

    fun setThePageCountMLD(input: Int) {
        pageCountMLD.value = input
    }

    val etTextCaptionsMLD = MutableLiveData<String>()

    var sectionNameMLD = MutableLiveData<String>()
    val sectionNameLD: LiveData<String>
        get() = sectionNameMLD

    fun setSectionNameMLD(input: String) {
        sectionNameMLD.value = input
    }

    fun sectionNameFormatForDisplay(sectionName: String?): String {
        var result = "Section Name:\n"
        result = "Section Name: $sectionName \n"
        return result
    }

    fun pageCountFormatForDisplay(count: Int): String {
        val result = "Page $count  "
        return result
    }

    //Variables to store Company Intro Data
    var companyIntroData = CompanyIntroDataDC()
    fun setTheCompanyIntroData(input: CompanyIntroDataDC) {
        companyIntroData = input
    }

    fun getTheCompanyIntroData(): CompanyIntroDataDC {
        return companyIntroData
    }

    fun updateThePhotoPathsInCompanyIntroData(input: String) {
        companyIntroData.picturePathsInIntroductions = input
    }

    fun updateTheIntroInTheCompanyIntroData(input: String) {
        companyIntroData.introduction = input
    }

    fun saveTheCompanyIntroDataIntoDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val companyIntroID =
                    getPresentCompanyCode() + MainActivity.COMPANY_INTRO_ID
                setTheCompanyIntroData(getTheCompanyIntroData())
                val companyIntroDataString = companyIntroDataToString(companyIntroData)
                val aInfo5Intro = AInfo5(companyIntroID, companyIntroDataString)
                insertAInfo5(aInfo5Intro)
                addUniqueItemToPresentCompanyAllIds(companyIntroID)
            }
        }
    }

    //Variables to store section data

    val defaultSectionPageData = SectionPageDataDC("General Entry", 1)
    fun getTheDefaultSectionPageData(): SectionPageDataDC {
        return defaultSectionPageData
    }

    var presentSectionAllData = SectionAllDataDC()
    fun setThePresentSectionAllData(input: SectionAllDataDC) {
        presentSectionAllData = input
    }

    fun addSectionPageDataToPresentSectionAllData(
        sectionPageData: SectionPageDataDC,
        indexAt: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList.add(indexAt, sectionPageData)
    }

    fun deleteSectionPageDataInPresentSectionAllData(currentPageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList.removeAt(currentPageIndex)
    }

    fun resetThePageNumbersOfPresentSectionAllData() {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
            val dataPagesList = presentSectionAllData.sectionAllPagesData.sectionPageDataList
            for (pageIndex in 0 until dataPagesList.size) {
                if (dataPagesList[pageIndex].pageNumber != pageIndex + 1) {
                    dataPagesList[pageIndex].pageNumber = pageIndex + 1
                }
            }
        }
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

                if (pageFramework.observationsFrameworkList.isNotEmpty()) {
                    val observationsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val observationsCheckboxesFrameworkList =
                        pageFramework.observationsFrameworkList
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

                if (pageFramework.recommendationsFrameworkList.isNotEmpty()) {
                    val recommendationsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val recommendationsCheckboxesFrameworkList =
                        pageFramework.recommendationsFrameworkList
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
                if (pageFramework.standardsFrameworkList.isNotEmpty()) {
                    val standardsFrameworkDataItemList =
                        mutableListOf<CheckboxesFrameworkDataItemDC>()
                    val standardsCheckboxesFrameworkList =
                        pageFramework.standardsFrameworkList
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

    fun getPicturePathsInIntroFromThePresentSectionAllData(): String {
        return presentSectionAllData.picturePathsInIntroductions
    }

    fun updateIntroInThePresentSectionAllData(input: String) {
        presentSectionAllData.introduction = input
    }

    fun updatePicturePathsInIntroForThePresentSectionAllData(input: String) {
        presentSectionAllData.picturePathsInIntroductions = input
    }

    fun updatePageTitleInSectionPageDataInPresentSectionAllData(pageTitle: String, pageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].pageTitle =
            pageTitle
    }

    fun updatePageNumberInSectionPageDataInPresentSectionAllData(pageNumber: Int, pageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].pageNumber =
            pageNumber
    }

    fun updateObservationsInObsForThePresentSectionAllData(observations: String, pageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observations =
            observations
    }

    fun updatePicturePathsInObsForThePresentSectionAllData(picturePaths: String, pageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].photoPaths =
            picturePaths
    }

    fun updateRecommendationsInObsForThePresentSectionAllData(
        recommendations: String,
        pageIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendations =
            recommendations
    }

    fun updateStandardsInObsForThePresentSectionAllData(standards: String, pageIndex: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standards =
            standards
    }

    fun addQuestionsFrameworkDataItemInThePresentSectionAllData(
        questionsFrameworkDataItem: QuestionsFrameworkDataItemDC,
        pageIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].questionsFrameworkDataItemList.add(
            questionsFrameworkDataItem
        )
    }

    fun deleteQuestionsFrameworkDataItemInThePresentSectionAllData(pageIndex: Int, indexAt: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].questionsFrameworkDataItemList.removeAt(
            indexAt
        )
    }

    fun addObservationsFrameworkDataItemInThePresentSectionAllData(
        observationsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observationsFrameworkDataItemList.add(
            observationsFrameworkDataItem
        )
    }

    fun deleteObservationsFrameworkDataItemInThePresentSectionAllData(
        pageIndex: Int,
        indexAt: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observationsFrameworkDataItemList.removeAt(
            indexAt
        )
    }

    fun addRecommendationsFrameworkDataItemInThePresentSectionAllData(
        recommendationsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.add(
            recommendationsFrameworkDataItem
        )
    }

    fun deleteRecommendationsFrameworkDataItemInThePresentSectionAllData(
        pageIndex: Int,
        indexAt: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.removeAt(
            indexAt
        )
    }

    fun addStandardsFrameworkDataItemInThePresentSectionAllData(
        standardsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standardsFrameworkDataItemList.add(
            standardsFrameworkDataItem
        )
    }

    fun deleteStandardsFrameworkDataItemInThePresentSectionAllData(pageIndex: Int, indexAt: Int) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standardsFrameworkDataItemList.removeAt(
            indexAt
        )
    }


    fun isQuestionDataItemListUpdatedInPresentSectionAllData(
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionTemplateList: MutableList<QuestionTemplateItemDC>
    ): Boolean {
        var result = false
        if (questionTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size >= questionsFrameworkIndex) {
                    val questionsDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList
                    if (questionsDataItemList.isEmpty()) {
                        result = false
                    } else {
                        if (questionsDataItemList.size != questionTemplateList.size) {
                            result = false
                        } else {
                            var anyItemMismatched = false
                            for (indexT in 0 until questionTemplateList.size) {
                                if (questionTemplateList[indexT].blockNumber != questionsDataItemList[indexT].blockNumber) {
                                    anyItemMismatched = true
                                    break
                                }
                            }
                            result = anyItemMismatched != true
                        }
                    }
                } else {
                    result = false
                }
            } else {
                result = false
            }
        }
        return result
    }

    fun updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionTemplateList: MutableList<QuestionTemplateItemDC>
    ) {
        if (questionTemplateList.isNotEmpty()) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size >= questionsFrameworkIndex) {
                    val questionsDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList
                    if (questionsDataItemList.size != questionTemplateList.size) {
                        if (questionsDataItemList.isEmpty()) {
                            val questionsDataItemList1 = mutableListOf<QuestionDataItemDC>()
                            for (index in 0 until questionTemplateList.size) {
                                val questionDataItemDC = QuestionDataItemDC()
                                questionDataItemDC.blockNumber =
                                    questionTemplateList[index].blockNumber
                                questionDataItemDC.mandatoryValue =
                                    questionTemplateList[index].mandatory
                                questionsDataItemList1.add(questionDataItemDC)
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList =
                                questionsDataItemList1

                        } else {
                            val questionsDataItemList1 = mutableListOf<QuestionDataItemDC>()
                            for (indexT in 0 until questionTemplateList.size) {
                                val tBlockNumber = questionTemplateList[indexT].blockNumber
                                var itemPresentFlag = false
                                var indexDPresent = 0
                                for (indexD in 0 until questionsDataItemList.size) {
                                    if (questionsDataItemList[indexD].blockNumber == tBlockNumber) {
                                        itemPresentFlag = true
                                        indexDPresent = indexD
                                        break
                                    }
                                }
                                if (itemPresentFlag == true) {
                                    val questionDataItem = questionsDataItemList[indexDPresent]
                                    questionsDataItemList1.add(questionDataItem)
                                } else {
                                    val questionDataItem = QuestionDataItemDC()
                                    questionDataItem.blockNumber = tBlockNumber
                                    questionsDataItemList1.add(questionDataItem)
                                }
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList =
                                questionsDataItemList1

                        }
                    } else {
                        val questionsDataItemList1 = mutableListOf<QuestionDataItemDC>()
                        for (indexT in 0 until questionTemplateList.size) {
                            val tBlockNumber = questionTemplateList[indexT].blockNumber
                            var itemPresentFlag = false
                            var indexDPresent = 0
                            for (indexD in 0 until questionsDataItemList.size) {
                                if (questionsDataItemList[indexD].blockNumber == tBlockNumber) {
                                    itemPresentFlag = true
                                    indexDPresent = indexD
                                    break
                                }
                            }
                            if (itemPresentFlag == true) {
                                val questionDataItem = questionsDataItemList[indexDPresent]
                                questionsDataItemList1.add(questionDataItem)
                            } else {
                                val questionDataItem = QuestionDataItemDC()
                                questionDataItem.blockNumber = tBlockNumber
                                questionsDataItemList1.add(questionDataItem)
                            }
                        }
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList =
                            questionsDataItemList1
                    }
                }
            }
        }
    }

    fun isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
        currentPageIndex: Int,
        observationsFrameworkIndex: Int,
        obsCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ): Boolean {
        var result = false
        if (obsCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList.size >= observationsFrameworkIndex) {
                    val obsCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[observationsFrameworkIndex].checkboxDataItemML
                    if (obsCheckboxesDataItemList.isEmpty()) {
                        result = false
                    } else {
                        result = obsCheckboxesDataItemList.size == obsCheckboxTemplateList.size
                    }
                }
            }
        }

        return result
    }

    fun updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
        currentPageIndex: Int,
        observationsFrameworkIndex: Int,
        obsCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ) {
        if (obsCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList.size >= observationsFrameworkIndex) {
                    val obsCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[observationsFrameworkIndex].checkboxDataItemML
                    if (obsCheckboxesDataItemList.size != obsCheckboxTemplateList.size) {
                        if (obsCheckboxesDataItemList.isEmpty()) {
                            val obsCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (index in 0 until obsCheckboxTemplateList.size) {
                                val obsCheckboxDataItem = CheckboxDataItemDC()
                                obsCheckboxesDataItemList1.add(obsCheckboxDataItem)
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[observationsFrameworkIndex].checkboxDataItemML =
                                obsCheckboxesDataItemList1
                        } else {
                            val obsCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (tIndex in 0 until obsCheckboxTemplateList.size) {
                                if (tIndex <= obsCheckboxesDataItemList.size - 1) {
                                    obsCheckboxesDataItemList1.add(obsCheckboxesDataItemList[tIndex])
                                } else {
                                    val obsCheckboxDataItem = CheckboxDataItemDC()
                                    obsCheckboxesDataItemList1.add(obsCheckboxDataItem)
                                }
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[observationsFrameworkIndex].checkboxDataItemML =
                                obsCheckboxesDataItemList1
                        }
                    }
                }
            }
        }
    }

    fun isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
        currentPageIndex: Int,
        recommendationsFrameworkIndex: Int,
        recoCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ): Boolean {
        var result = false
        if (recoCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList.size >= recommendationsFrameworkIndex) {
                    val recoCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recommendationsFrameworkIndex].checkboxDataItemML
                    if (recoCheckboxesDataItemList.isEmpty()) {
                        result = false
                    } else {
                        result = recoCheckboxesDataItemList.size == recoCheckboxTemplateList.size
                    }
                }
            }
        }
        return result
    }

    fun updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
        currentPageIndex: Int,
        recommendationsFrameworkIndex: Int,
        recoCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ) {
        if (recoCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList.size >= recommendationsFrameworkIndex) {
                    val recoCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recommendationsFrameworkIndex].checkboxDataItemML
                    if (recoCheckboxesDataItemList.size != recoCheckboxTemplateList.size) {
                        if (recoCheckboxesDataItemList.isEmpty()) {
                            val recoCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (index in 0 until recoCheckboxTemplateList.size) {
                                val recoCheckboxDataItem = CheckboxDataItemDC()
                                recoCheckboxesDataItemList1.add(recoCheckboxDataItem)
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recommendationsFrameworkIndex].checkboxDataItemML =
                                recoCheckboxesDataItemList1
                        } else {
                            val recoCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (tIndex in 0 until recoCheckboxTemplateList.size) {
                                if (tIndex <= recoCheckboxesDataItemList.size - 1) {
                                    recoCheckboxesDataItemList1.add(recoCheckboxesDataItemList[tIndex])
                                } else {
                                    val recoCheckboxDataItem = CheckboxDataItemDC()
                                    recoCheckboxesDataItemList1.add(recoCheckboxDataItem)
                                }
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recommendationsFrameworkIndex].checkboxDataItemML =
                                recoCheckboxesDataItemList1
                        }
                    }
                }
            }
        }
    }

    fun isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
        currentPageIndex: Int,
        standardsFrameworkIndex: Int,
        stdsCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ): Boolean {
        var result = false
        if (stdsCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList.size >= standardsFrameworkIndex) {
                    val stdsCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[standardsFrameworkIndex].checkboxDataItemML
                    if (stdsCheckboxesDataItemList.isEmpty()) {
                        result = false
                    } else {
                        result = stdsCheckboxesDataItemList.size == stdsCheckboxTemplateList.size
                    }
                }
            }
        }
        return result
    }

    fun updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
        currentPageIndex: Int,
        standardsFrameworkIndex: Int,
        stdsCheckboxTemplateList: MutableList<CheckboxTemplateItemDC>
    ) {
        if (stdsCheckboxTemplateList.isNotEmpty()) {
            val presentSectionAllData = getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size >= currentPageIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList.size >= standardsFrameworkIndex) {
                    val stdsCheckboxesDataItemList =
                        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[standardsFrameworkIndex].checkboxDataItemML
                    if (stdsCheckboxesDataItemList.size != stdsCheckboxTemplateList.size) {
                        if (stdsCheckboxesDataItemList.isEmpty()) {
                            val stdsCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (index in 0 until stdsCheckboxTemplateList.size) {
                                val stdsCheckboxDataItem = CheckboxDataItemDC()
                                stdsCheckboxesDataItemList1.add(stdsCheckboxDataItem)
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[standardsFrameworkIndex].checkboxDataItemML =
                                stdsCheckboxesDataItemList1
                        } else {
                            val stdsCheckboxesDataItemList1 = mutableListOf<CheckboxDataItemDC>()
                            for (tIndex in 0 until stdsCheckboxTemplateList.size) {
                                if (tIndex <= stdsCheckboxesDataItemList.size - 1) {
                                    stdsCheckboxesDataItemList1.add(stdsCheckboxesDataItemList[tIndex])
                                } else {
                                    val stdsCheckboxDataItem = CheckboxDataItemDC()
                                    stdsCheckboxesDataItemList1.add(stdsCheckboxDataItem)
                                }
                            }
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[standardsFrameworkIndex].checkboxDataItemML =
                                stdsCheckboxesDataItemList1
                        }
                    }
                }
            }
        }
    }

    fun updateData1ValueInPresentSectionAllData(
        data1Value: String,
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data1Value =
            data1Value
    }

    fun updateData1ValueInObservations(
        oldData1Value: String,
        newData1Value: String,
        s11: String,
        s12: String,
        currentPageIndex: Int
    ) {
        var oldDataValueSentence = ""
        var newDataValueSentence = ""
        if (oldData1Value != "") {
            oldDataValueSentence = s11.trim() + " " + oldData1Value + s12 + "\n"
        }
        if (newData1Value != "") {
            newDataValueSentence = s11.trim() + " " + newData1Value + s12 + "\n"
        }
        if (oldDataValueSentence != "") {
            if (newDataValueSentence != "") {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                } else {
                    etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
                }
            } else {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                }
            }
        } else {
            if (newDataValueSentence != "") {
                etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
            }
        }
        updateObservationsInObsForThePresentSectionAllData(
            etObservationsMLD.value.toString(),
            currentPageIndex
        )
    }

    fun updateData2ValueInPresentSectionAllData(
        data2Value: String,
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data2Value =
            data2Value
    }

    fun updateData2ValueInObservations(
        oldData2Value: String,
        newData2Value: String,
        s21: String,
        s22: String,
        currentPageIndex: Int
    ) {
        var oldDataValueSentence = ""
        var newDataValueSentence = ""
        if (oldData2Value != "") {
            oldDataValueSentence = s21.trim() + " " + oldData2Value + s22 + "\n"
        }
        if (newData2Value != "") {
            newDataValueSentence = s21.trim() + " " + newData2Value + s22 + "\n"
        }
        if (oldDataValueSentence != "") {
            if (newDataValueSentence != "") {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                } else {
                    etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
                }
            } else {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                }
            }
        } else {
            if (newDataValueSentence != "") {
                etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
            }
        }
        updateObservationsInObsForThePresentSectionAllData(
            etObservationsMLD.value.toString(),
            currentPageIndex
        )
    }

    fun updateData3ValueInPresentSectionAllData(
        data3Value: String,
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data3Value =
            data3Value
    }

    fun updateData3ValueInObservations(
        oldData3Value: String,
        newData3Value: String,
        s31: String,
        s32: String,
        currentPageIndex: Int
    ) {
        var oldDataValueSentence = ""
        var newDataValueSentence = ""
        if (oldData3Value != "") {
            oldDataValueSentence = s31.trim() + " " + oldData3Value + s32 + "\n"
        }
        if (newData3Value != "") {
            newDataValueSentence = s31.trim() + " " + newData3Value + s32 + "\n"
        }
        if (oldDataValueSentence != "") {
            if (newDataValueSentence != "") {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                } else {
                    etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
                }
            } else {
                if (etObservationsMLD.value?.contains(oldDataValueSentence) == true) {
                    etObservationsMLD.value =
                        etObservationsMLD.value!!.replace(
                            oldDataValueSentence,
                            newDataValueSentence
                        )
                }
            }
        } else {
            if (newDataValueSentence != "") {
                etObservationsMLD.value = etObservationsMLD.value + newDataValueSentence
            }
        }
        updateObservationsInObsForThePresentSectionAllData(
            etObservationsMLD.value.toString(),
            currentPageIndex
        )
    }


    fun updateButtonChoiceInThePresentSectionAllData(
        buttonChoice: String,
        currentPageIndex: Int,
        questionsFrameworkIndex: Int,
        questionDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].buttonOptionChosen =
            buttonChoice
    }

    fun updateButtonChoiceTextInObservations(
        oldButtonChoiceTextValue: String,
        buttonChoiceTextValue: String, currentPageIndex: Int
    ) {
        val oldValue = oldButtonChoiceTextValue
        val newValue = buttonChoiceTextValue
        if (etObservationsMLD.value?.contains(oldValue) == true) {
            etObservationsMLD.value = etObservationsMLD.value!!.replace(oldValue, newValue)
        } else {
            etObservationsMLD.value = etObservationsMLD.value + newValue
        }
        updateObservationsInObsForThePresentSectionAllData(
            etObservationsMLD.value.toString(),
            currentPageIndex
        )
    }

    fun updateObsCheckboxTickedValueInPresentSectionAllData(
        obsChoice: Boolean,
        currentPageIndex: Int,
        obsFrameworkIndex: Int,
        obsDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[obsFrameworkIndex].checkboxDataItemML[obsDataIndex].checkboxTickedValue =
            obsChoice
    }

    fun updateObsCheckboxValueInObservations(
        obsCheckboxLabel: String,
        isChecked: Boolean,
        currentPageIndex: Int
    ) {
        val obsCheckboxValue = "\n" + obsCheckboxLabel + "\n"
        if (etObservationsMLD.value?.contains(obsCheckboxValue) == true) {
            if (!isChecked) {
                etObservationsMLD.value = etObservationsMLD.value!!.replace(obsCheckboxValue, "")
            }
        } else {
            if (isChecked) {
                etObservationsMLD.value = etObservationsMLD.value + obsCheckboxValue
            }

        }
        updateObservationsInObsForThePresentSectionAllData(
            etObservationsMLD.value.toString(),
            currentPageIndex
        )
    }

    fun updateRecoCheckboxTickedValueInPresentSectionAllData(
        recoChoice: Boolean,
        currentPageIndex: Int,
        recoFrameworkIndex: Int,
        recoDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML[recoDataIndex].checkboxTickedValue =
            recoChoice
    }

    fun updateRecoPriorityValueInPresentSectionAllData(
        recoPriorityValue: String,
        currentPageIndex: Int,
        recoFrameworkIndex: Int,
        recoDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML[recoDataIndex].priorityValues =
            recoPriorityValue
    }

    fun updateRecoCheckboxValueWithPriorityInRecommendations(
        recoCheckboxLabelWithPriorityValue: String, isChecked: Boolean, currentPageIndex: Int
    ) {
        val recoCheckboxValue = "\n" + recoCheckboxLabelWithPriorityValue + "\n"
        if (etRecommendationsMLD.value?.contains(recoCheckboxValue) == true) {
            if (!isChecked) {
                etRecommendationsMLD.value =
                    etRecommendationsMLD.value!!.replace(recoCheckboxValue, "")
            }
        } else {
            if (isChecked) {
                etRecommendationsMLD.value = etRecommendationsMLD.value + recoCheckboxValue
            }
        }

        updateRecommendationsInObsForThePresentSectionAllData(
            etRecommendationsMLD.value.toString(),
            currentPageIndex
        )
    }

    fun updateRecoPriorityValueInRecommendations(
        oldRecoCheckboxLabelWithPriorityValue: String,
        newRecoCheckboxLabelWithPriorityValue: String,
        currentPageIndex: Int
    ) {
        val oldCBPriorityValue = "\n" + oldRecoCheckboxLabelWithPriorityValue + "\n"
        val newCBPriorityValue = "\n" + newRecoCheckboxLabelWithPriorityValue + "\n"
        if (etRecommendationsMLD.value?.contains(oldCBPriorityValue) == true) {
            if (newCBPriorityValue != oldCBPriorityValue) {
                etRecommendationsMLD.value =
                    etRecommendationsMLD.value!!.replace(oldCBPriorityValue, newCBPriorityValue)
                updateRecommendationsInObsForThePresentSectionAllData(
                    etRecommendationsMLD.value.toString(),
                    currentPageIndex
                )
            }
        }
    }

    fun updateStdsCheckboxTickedValueInPresentSectionAllData(
        stdsChoice: Boolean,
        currentPageIndex: Int,
        stdsFrameworkIndex: Int,
        stdsDataIndex: Int
    ) {
        presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[stdsFrameworkIndex].checkboxDataItemML[stdsDataIndex].checkboxTickedValue =
            stdsChoice
    }

    fun updateStdsCheckboxValueInStandards(
        stdsCheckboxLabel: String,
        isChecked: Boolean,
        currentPageIndex: Int
    ) {
        val stdsCheckboxValue = ";$stdsCheckboxLabel;"
        if (tvStandardsMLD.value?.contains(stdsCheckboxValue) == true) {
            if (!isChecked) {
                tvStandardsMLD.value = tvStandardsMLD.value!!.replace(stdsCheckboxValue, "")
            } else if (isChecked) {
                tvStandardsMLD.value = tvStandardsMLD.value + stdsCheckboxValue
            }
        } else {
            tvStandardsMLD.value = tvStandardsMLD.value + stdsCheckboxValue
        }
        updateStandardsInObsForThePresentSectionAllData(
            tvStandardsMLD.value.toString(),
            currentPageIndex
        )
    }

    //This flag checks if the section data has been loaded from the db
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
        result.add(sectionPageData.photoPaths)
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
                        sectionPageData.photoPaths = ""
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
                        sectionPageData.photoPaths = ""
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
                        sectionPageData.photoPaths = ""
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
                        sectionPageData.photoPaths = ""
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                        sectionPageData.photoPaths = stringList[3]
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
                sectionPageData.photoPaths = ""
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
            sectionPageData.photoPaths = ""
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

    //Company Report Related Data Variables and Functions
    private var companyReport = CompanyReportDC()

    fun setTheCompanyReport(input: CompanyReportDC) {
        companyReport = input
    }

    fun getTheCompanyReport(): CompanyReportDC {
        return companyReport
    }

    fun uploadTheCompanyReportIntoViewModel(companyReportString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            var companyReport = CompanyReportDC()
            if (companyReportString != "") {
                companyReport = stringToCompanyReport(companyReportString)
            } else {
                companyReport.companyCode = getPresentCompanyCode()
                companyReport.companyName = getPresentCompanyName()
                companyReport.companyAuditDate = ""
            }

            withContext(Dispatchers.Main) {
                setTheCompanyReport(companyReport)
                setTheCompanyReportUploadedFlag(true)
            }
        }
    }

    fun updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(
        companyCode: String,
        companyName: String = "",
        companyAuditDate: String = "",
        companyIntroduction: String = ""
    ) {
        if (companyReport.companyCode == companyCode) {
            if (companyName != "") {
                companyReport.companyName = companyName
            }
            if (companyAuditDate != "") {
                companyReport.companyAuditDate = companyAuditDate
            }
            if (companyIntroduction != "") {
                companyReport.companyIntroduction = companyIntroduction
            }
        }
        //Save the companyReport to DB
        val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
        val companyReportString = companyReportToString(companyReport)
        val aInfo5 = AInfo5(companyReportID, companyReportString)
        insertAInfo5(aInfo5)
        setTheCompanyReportUploadedFlag(false)
    }

    //The below function ensures that the reports are ordered in the Company Report
    fun reorderSectionReportsInCompanyReportAndSave() {
        viewModelScope.launch(Dispatchers.Default) {
            val reorderedSectionReportList = mutableListOf<SectionReportDC>()
            val companySectionCNAndD = getTheCompanySectionCodeAndDisplayNameML()
            if (companySectionCNAndD.isNotEmpty()) {
                if (companyReport.sectionReportList.isNotEmpty()) {
                    for (sectionIndex in 0 until companySectionCNAndD.size) {
                        for (reportIndex in 0 until companyReport.sectionReportList.size) {
                            if (companyReport.sectionReportList[reportIndex].sectionCode == companySectionCNAndD[sectionIndex].uniqueCodeName) {
                                reorderedSectionReportList.add(companyReport.sectionReportList[reportIndex])
                                break
                            }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                companyReport.sectionReportList = reorderedSectionReportList
                //Save the companyReport to DB
                saveTheCompanyReportToDB(getTheCompanyReport())
            }
        }
    }

    fun updateSectionReportInCompanyReportAndSave(sectionReport: SectionReportDC) {
        viewModelScope.launch(Dispatchers.Default) {
            var sectionReportPresentFlag = false
            var sectionReportIndex = 0
            if (companyReport.sectionReportList.isNotEmpty()) {
                for (index in 0 until companyReport.sectionReportList.size) {
                    if (companyReport.sectionReportList[index].sectionCode == sectionReport.sectionCode) {
                        sectionReportPresentFlag = true
                        sectionReportIndex = index
                        break
                    }
                }
            } else {
                companyReport.sectionReportList.add(sectionReport)
            }
            withContext(Dispatchers.Main) {
                if (sectionReportPresentFlag == true) {
                    companyReport.sectionReportList[sectionReportIndex] = sectionReport
                }
                reorderSectionReportsInCompanyReportAndSave()
            }

        }
    }

    fun updateSectionDetailsInCompanyReportAndSave(
        sectionCode: String,
        sectionName: String = "",
        sectionAllData: SectionAllDataDC
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val sectionAllData = getThePresentSectionAllData()
            var sectionReportPresentFlag = false
            var sectionReportIndex = 0
            val sectionReportNew = SectionReportDC()
            sectionReportNew.sectionCode = sectionCode
            sectionReportNew.sectionName = sectionName
            sectionReportNew.sectionIntroduction = sectionAllData.introduction

            sectionReportNew.threeColumnTableWord =
                generateSectionThreeColumnWordTable(sectionAllData.sectionAllPagesData.sectionPageDataList)
            sectionReportNew.threeColumnTableExcel =
                generateSectionThreeColumnExcelTable(sectionAllData.sectionAllPagesData.sectionPageDataList)
            sectionReportNew.sixColumnTableWord =
                generateSectionSixColumnWordTable(sectionAllData.sectionAllPagesData.sectionPageDataList)
            sectionReportNew.sixColumnTableExcel =
                generateSectionSixColumnExcelTable(sectionAllData.sectionAllPagesData.sectionPageDataList)
            sectionReportNew.checkListTableWord =
                generateSectionChecklistWordTable(sectionAllData.sectionAllPagesData.sectionPageDataList)
            sectionReportNew.checkListTableExcel =
                generateSectionChecklistExcelTable(sectionAllData.sectionAllPagesData.sectionPageDataList)

            if (companyReport.sectionReportList.isNotEmpty()) {
                for (index in 0 until companyReport.sectionReportList.size) {
                    if (companyReport.sectionReportList[index].sectionCode == sectionCode) {
                        sectionReportPresentFlag = true
                        sectionReportIndex = index
                        break
                    }
                }
            } else {
                sectionReportPresentFlag = false
            }
            withContext(Dispatchers.Main) {
                if (sectionReportPresentFlag) {
                    companyReport.sectionReportList[sectionReportIndex] = sectionReportNew
                } else {
                    companyReport.sectionReportList.add(sectionReportNew)
                }
                //Save the companyReport to DB
                saveTheCompanyReportToDB(getTheCompanyReport())
            }
        }
    }

    fun deleteSectionReportInCompanyReportAndSave(
        sectionCode: String
    ){
        if (companyReport.sectionReportList.isNotEmpty()){
            for (sectionIndex in 0 until companyReport.sectionReportList.size){
                val sectionReport = companyReport.sectionReportList[sectionIndex]
                if (sectionReport.sectionCode == sectionCode){
                    companyReport.sectionReportList.removeAt(sectionIndex)
                }
            }
        }
        //Save the companyReport to DB
        saveTheCompanyReportToDB(getTheCompanyReport())
    }

    fun saveTheCompanyReportToDB(companyReport: CompanyReportDC) {
        viewModelScope.launch(Dispatchers.IO) {
            val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
            val companyReportString = companyReportToString(companyReport)
            val aInfo5 = AInfo5(companyReportID, companyReportString)
            insertAInfo5(aInfo5)
        }
    }

    fun generateSectionThreeColumnWordTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var threeColumnWordTable = ""
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (pageIndex == 0) {
                    threeColumnWordTable =
                        "Observations" + "$" + "Recommendations" + "$" + "Standards\n" +
                                sectionPageDataList[pageIndex].pageTitle.replace("\n", ";") + ";;" +
                                sectionPageDataList[pageIndex].observations.replace("\n", ";") +
                                sectionPageDataList[pageIndex].photoPaths.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                } else {
                    threeColumnWordTable =
                        threeColumnWordTable + sectionPageDataList[pageIndex].pageTitle.replace(
                            "\n",
                            ";"
                        ) + ";;" +
                                sectionPageDataList[pageIndex].observations.replace("\n", ";") +
                                sectionPageDataList[pageIndex].photoPaths.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                }
            }
            threeColumnWordTable = threeColumnWordTable + "}"
        }
        return threeColumnWordTable
    }

    fun generateSectionThreeColumnExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var threeColumnExcelTable = ""
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (pageIndex == 0) {
                    threeColumnExcelTable =
                        "Observations" + "$" + "Recommendations" + "$" + "Standards\n" +
                                sectionPageDataList[pageIndex].pageTitle.replace("\n", ";") + ";;" +
                                sectionPageDataList[pageIndex].observations.replace("\n", ";") +
                                sectionPageDataList[pageIndex].photoPaths.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                } else {
                    threeColumnExcelTable =
                        threeColumnExcelTable + sectionPageDataList[pageIndex].pageTitle.replace(
                            "\n",
                            ";"
                        ) + ";;" +
                                sectionPageDataList[pageIndex].observations.replace("\n", ";") +
                                sectionPageDataList[pageIndex].photoPaths.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                }
            }
            threeColumnExcelTable = threeColumnExcelTable + "}"
        }
        return threeColumnExcelTable
    }

    fun generateSectionSixColumnWordTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var sixColumnWordTable = ""
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (pageIndex == 0) {
                    sixColumnWordTable =
                        "Space" + "$" + "Observations" + "$" + "Images" + "$" + "Compliance" + "$" + "Recommendations" + "$" + "Standards\n" +
                                sectionPageDataList[pageIndex].pageTitle.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].observations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].photoPaths.replace(
                                    "\n",
                                    ";"
                                ) + "$$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                } else {
                    sixColumnWordTable =
                        sixColumnWordTable + sectionPageDataList[pageIndex].pageTitle.replace(
                            "\n",
                            ";"
                        ) + "$" +
                                sectionPageDataList[pageIndex].observations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].photoPaths.replace(
                                    "\n",
                                    ";"
                                ) + "$$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                }
            }
            sixColumnWordTable = sixColumnWordTable + "}"
        }
        return sixColumnWordTable
    }

    fun generateSectionSixColumnExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var sixColumnExcelTable = ""
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (pageIndex == 0) {
                    sixColumnExcelTable =
                        "Space" + "$" + "Observations" + "$" + "Images" + "$" + "Compliance" + "$" + "Recommendations" + "$" + "Standards\n" +
                                sectionPageDataList[pageIndex].pageTitle.replace("\n", ";") + "$" +
                                sectionPageDataList[pageIndex].observations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].photoPaths.replace(
                                    "\n",
                                    ";"
                                ) + "$$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                } else {
                    sixColumnExcelTable =
                        sixColumnExcelTable + sectionPageDataList[pageIndex].pageTitle.replace(
                            "\n",
                            ";"
                        ) + "$" +
                                sectionPageDataList[pageIndex].observations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].photoPaths.replace(
                                    "\n",
                                    ";"
                                ) + "$$" +
                                sectionPageDataList[pageIndex].recommendations.replace(
                                    "\n",
                                    ";"
                                ) + "$" +
                                sectionPageDataList[pageIndex].standards.replace("\n", ";") + "\n"
                }
            }
            sixColumnExcelTable = sixColumnExcelTable + "}"
        }
        return sixColumnExcelTable
    }

    fun generateSectionChecklistWordTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var checklistWordTable = ""
        checklistWordTable =
            "Checklist" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionsFrameworkList =
                        sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (frameworkIndex in 0 until questionsFrameworkList.size) {
                        val pageCode = questionsFrameworkList[frameworkIndex].pageCode
                        if (pageCode == "PC_General_Entry_01_PC") {
                            checklistWordTable =
                                checklistWordTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                        }
                        else {
                            if (isItemPresentInPageTemplateList(pageCode) == true) {
                                val itemQuestionsList =
                                    getItemFromPageTemplateList(pageCode).questionsList
                                val itemDataList =
                                    questionsFrameworkList[frameworkIndex].questionDataItemList
                                checklistWordTable =
                                    checklistWordTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                                if (itemQuestionsList.isNotEmpty()) {
                                    for (questionsListIndex in 0 until itemQuestionsList.size) {
                                        checklistWordTable =
                                            checklistWordTable + itemQuestionsList[questionsListIndex].question + "$"
                                        if (questionsListIndex <  itemDataList.size) {
                                            checklistWordTable =
                                                checklistWordTable + itemDataList[questionsListIndex].data1Value + "$" + itemDataList[questionsListIndex].data2Value + "$" + itemDataList[questionsListIndex].data3Value + "$" + itemDataList[questionsListIndex].buttonOptionChosen + "\n"
                                        }
                                        else {
                                            checklistWordTable =
                                                checklistWordTable + "$" + "$" + "$" + "\n"
                                        }
                                    }
                                }
                            }
                            else {
                                checklistWordTable =
                                    checklistWordTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                            }
                        }
                    }
                }
            }
            checklistWordTable = checklistWordTable + "}"
        }

        return checklistWordTable
    }

    fun generateSectionChecklistExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
        var checklistExcelTable = ""
        checklistExcelTable =
            "Checklist" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionsFrameworkList =
                        sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (frameworkIndex in 0 until questionsFrameworkList.size) {
                        val pageCode = questionsFrameworkList[frameworkIndex].pageCode
                        if (pageCode == "PC_General_Entry_01_PC") {
                            checklistExcelTable =
                                checklistExcelTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                        } else {
                            if (isItemPresentInPageTemplateList(pageCode) == true) {
                                val itemQuestionsList =
                                    getItemFromPageTemplateList(pageCode).questionsList
                                val itemDataList =
                                    questionsFrameworkList[frameworkIndex].questionDataItemList
                                checklistExcelTable =
                                    checklistExcelTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                                if (itemQuestionsList.isNotEmpty()) {
                                    for (questionsListIndex in 0 until itemQuestionsList.size) {
                                        checklistExcelTable =
                                            checklistExcelTable + itemQuestionsList[questionsListIndex].question + "$"
                                        if (questionsListIndex  < itemDataList.size) {
                                            checklistExcelTable =
                                                checklistExcelTable + itemDataList[questionsListIndex].data1Value + "$" + itemDataList[questionsListIndex].data2Value + "$" + itemDataList[questionsListIndex].data3Value + "$" + itemDataList[questionsListIndex].buttonOptionChosen + "\n"
                                        } else {
                                            checklistExcelTable =
                                                checklistExcelTable + "$" + "$" + "$" + "\n"
                                        }
                                    }
                                }
                            } else {
                                checklistExcelTable =
                                    checklistExcelTable + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + "$" + "$" + "$" + "$" + "\n"
                            }
                        }
                    }
                }
            }
            checklistExcelTable = checklistExcelTable + "}"
        }

        return checklistExcelTable
    }


    fun updateSectionNameAndIntroInCompanyReportAndSave(
        sectionCode: String,
        sectionName: String = "",
        sectionIntroduction: String = ""
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            var sectionReportPresentFlag = false
            var sectionReportIndex = 0
            val sectionReportNew = SectionReportDC()
            if (companyReport.sectionReportList.isNotEmpty()) {
                for (index in 0 until companyReport.sectionReportList.size) {
                    if (companyReport.sectionReportList[index].sectionCode == sectionCode) {
                        sectionReportPresentFlag = true
                        sectionReportIndex = index
                        break
                    }
                }
            } else {
                sectionReportPresentFlag = false
                sectionReportNew.sectionCode = sectionCode
                sectionReportNew.sectionName = sectionName
                sectionReportNew.sectionIntroduction = sectionIntroduction
            }
            withContext(Dispatchers.Main) {
                if (sectionReportPresentFlag) {
                    if (sectionName != "") {
                        companyReport.sectionReportList[sectionReportIndex].sectionName =
                            sectionName
                    }
                    if (sectionIntroduction != "") {
                        companyReport.sectionReportList[sectionReportIndex].sectionIntroduction =
                            sectionIntroduction
                    }
                } else {
                    companyReport.sectionReportList.add(sectionReportNew)
                }
                //Save the companyReport to DB
                val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
                val companyReportString = companyReportToString(companyReport)
                val aInfo5 = AInfo5(companyReportID, companyReportString)
                insertAInfo5(aInfo5)
            }
        }
    }

    fun sectionReportToML(input: SectionReportDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(input.sectionCode)
        result.add(input.sectionName)
        result.add(input.sectionIntroduction)
        result.add(input.threeColumnTableWord)
        result.add(input.threeColumnTableExcel)
        result.add(input.sixColumnTableWord)
        result.add(input.sixColumnTableExcel)
        result.add(input.checkListTableWord)
        result.add(input.checkListTableExcel)
        result.add(input.executiveSummaryTwoColumn)
        result.add(input.executiveSummaryFourColumn)
        return result
    }

    fun sectionReportToString(sectionReport: SectionReportDC): String {
        var result = ""
        val resultML = sectionReportToML(sectionReport)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToSectionReport(input: String): SectionReportDC {
        val sectionReport = SectionReportDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        sectionReport.sectionCode = ""
                        sectionReport.sectionName = ""
                        sectionReport.sectionIntroduction = ""
                        sectionReport.threeColumnTableWord = ""
                        sectionReport.threeColumnTableExcel = ""
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    1 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = ""
                        sectionReport.sectionIntroduction = ""
                        sectionReport.threeColumnTableWord = ""
                        sectionReport.threeColumnTableExcel = ""
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    2 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = ""
                        sectionReport.threeColumnTableWord = ""
                        sectionReport.threeColumnTableExcel = ""
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    3 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = ""
                        sectionReport.threeColumnTableExcel = ""
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    4 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = ""
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    5 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = ""
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    6 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = ""
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    7 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = ""
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    8 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = stringList[7]
                        sectionReport.checkListTableExcel = ""
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    9 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = stringList[7]
                        sectionReport.checkListTableExcel = stringList[8]
                        sectionReport.executiveSummaryTwoColumn = ""
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    10 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = stringList[7]
                        sectionReport.checkListTableExcel = stringList[8]
                        sectionReport.executiveSummaryTwoColumn = stringList[9]
                        sectionReport.executiveSummaryFourColumn = ""
                    }
                    11 -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = stringList[7]
                        sectionReport.checkListTableExcel = stringList[8]
                        sectionReport.executiveSummaryTwoColumn = stringList[9]
                        sectionReport.executiveSummaryFourColumn = stringList[10]
                    }
                    else -> {
                        sectionReport.sectionCode = stringList[0]
                        sectionReport.sectionName = stringList[1]
                        sectionReport.sectionIntroduction = stringList[2]
                        sectionReport.threeColumnTableWord = stringList[3]
                        sectionReport.threeColumnTableExcel = stringList[4]
                        sectionReport.sixColumnTableWord = stringList[5]
                        sectionReport.sixColumnTableExcel = stringList[6]
                        sectionReport.checkListTableWord = stringList[7]
                        sectionReport.checkListTableExcel = stringList[8]
                        sectionReport.executiveSummaryTwoColumn = stringList[9]
                        sectionReport.executiveSummaryFourColumn = stringList[10]
                    }
                }
            } else {
                sectionReport.sectionCode = input
                sectionReport.sectionName = ""
                sectionReport.sectionIntroduction = ""
                sectionReport.threeColumnTableWord = ""
                sectionReport.threeColumnTableExcel = ""
                sectionReport.sixColumnTableWord = ""
                sectionReport.sixColumnTableExcel = ""
                sectionReport.checkListTableWord = ""
                sectionReport.checkListTableExcel = ""
                sectionReport.executiveSummaryTwoColumn = ""
                sectionReport.executiveSummaryFourColumn = ""
            }
        } else {
            sectionReport.sectionCode = ""
            sectionReport.sectionName = ""
            sectionReport.sectionIntroduction = ""
            sectionReport.threeColumnTableWord = ""
            sectionReport.threeColumnTableExcel = ""
            sectionReport.sixColumnTableWord = ""
            sectionReport.sixColumnTableExcel = ""
            sectionReport.checkListTableWord = ""
            sectionReport.checkListTableExcel = ""
            sectionReport.executiveSummaryTwoColumn = ""
            sectionReport.executiveSummaryFourColumn = ""
        }
        return sectionReport
    }

    fun sectionReportListToString(sectionReportList: MutableList<SectionReportDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (sectionReportList.isNotEmpty()) {
            for (index in 0 until sectionReportList.size) {
                val sectionReportString = sectionReportToString(sectionReportList[index])
                resultML.add(sectionReportString)
            }
            result = mlToStringUsingDelimiter2(resultML)
        }
        return result
    }

    fun stringToSectionReportList(input: String): MutableList<SectionReportDC> {
        val sectionReportList = mutableListOf<SectionReportDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val sectionReport = stringToSectionReport(inputML[index])
                sectionReportList.add(sectionReport)
            }
        }

        return sectionReportList
    }

    fun companyReportToML(input: CompanyReportDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(input.companyCode)
        result.add(input.companyName)
        result.add(input.companyIntroduction)
        result.add(sectionReportListToString(input.sectionReportList))
        return result
    }

    fun companyReportToString(companyReport: CompanyReportDC): String {
        var result = ""
        val resultML = companyReportToML(companyReport)
        result = mlToStringUsingDelimiter3(resultML)
        return result
    }

    fun stringToCompanyReport(input: String): CompanyReportDC {
        val companyReport = CompanyReportDC()
        if (input != "") {
            if (input.contains(delimiterLevel3)) {
                val stringList = input.split(delimiterLevel3)
                when (stringList.size) {
                    0 -> {
                        companyReport.companyCode = ""
                        companyReport.companyName = ""
                        companyReport.companyIntroduction = ""
                        companyReport.sectionReportList = mutableListOf()
                    }
                    1 -> {
                        companyReport.companyCode = stringList[0]
                        companyReport.companyName = ""
                        companyReport.companyIntroduction = ""
                        companyReport.sectionReportList = mutableListOf()
                    }
                    2 -> {
                        companyReport.companyCode = stringList[0]
                        companyReport.companyName = stringList[1]
                        companyReport.companyIntroduction = ""
                        companyReport.sectionReportList = mutableListOf()

                    }
                    3 -> {
                        companyReport.companyCode = stringList[0]
                        companyReport.companyName = stringList[1]
                        companyReport.companyIntroduction = stringList[2]
                        companyReport.sectionReportList = mutableListOf()
                    }
                    4 -> {
                        companyReport.companyCode = stringList[0]
                        companyReport.companyName = stringList[1]
                        companyReport.companyIntroduction = stringList[2]
                        companyReport.sectionReportList = stringToSectionReportList(stringList[3])
                    }
                    else -> {
                        companyReport.companyCode = stringList[0]
                        companyReport.companyName = stringList[1]
                        companyReport.companyIntroduction = stringList[2]
                        companyReport.sectionReportList = stringToSectionReportList(stringList[3])
                    }
                }
            } else {
                companyReport.companyCode = input
                companyReport.companyName = ""
                companyReport.companyIntroduction = ""
                companyReport.sectionReportList = mutableListOf()
            }
        } else {
            companyReport.companyCode = ""
            companyReport.companyName = ""
            companyReport.companyIntroduction = ""
            companyReport.sectionReportList = mutableListOf()
        }
        return companyReport
    }

    //This flag indicates if the companyReport has been uploaded or not
    var companyReportUploadedFlag = false
    fun setTheCompanyReportUploadedFlag(input: Boolean) {
        companyReportUploadedFlag = input
    }

    fun getTheCompanyReportUploadedFlag(): Boolean {
        return companyReportUploadedFlag
    }
    //var companyReportUploadedFlag = MutableLiveData<Boolean>()

    private var reportsToBeGeneratedList = mutableListOf<String>()
    fun getTheReportsToBeGeneratedList(): MutableList<String> {
        return reportsToBeGeneratedList
    }

    fun setTheReportsToBeGeneratedList(input: MutableList<String>) {
        reportsToBeGeneratedList = input
    }

    private var allReportsList = mutableListOf<String>()
    fun getTheAllReportsList(): MutableList<String> {
        return allReportsList
    }

    fun setTheAllReportsList(input: MutableList<String>) {
        allReportsList = input
    }

    fun generateReports(inputList: MutableList<String>) {
        val reportsList = getTheAllReportsList()
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
        var threeColumnReport = ""
        val companyReport = getTheCompanyReport()
        threeColumnReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                threeColumnReport =
                    threeColumnReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.threeColumnTableWord};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Word"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, threeColumnReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, threeColumnReport)
            }
        }

    }

    fun generateThreeColumnExcelReport() {
        var threeColumnReport = ""
        val companyReport = getTheCompanyReport()
        threeColumnReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                threeColumnReport =
                    threeColumnReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.threeColumnTableExcel};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Excel"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.TXT_FILE_3COLUMN_EXCEL_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, threeColumnReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.TXT_FILE_3COLUMN_EXCEL_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, threeColumnReport)
            }
        }
    }

    private fun generateSixColumnWordReport() {
        var sixColumnReport = ""
        val companyReport = getTheCompanyReport()
        sixColumnReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                sixColumnReport =
                    sixColumnReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.sixColumnTableWord};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Word"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.DOC_FILE_6COLUMN_WORD_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, sixColumnReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.DOC_FILE_6COLUMN_WORD_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, sixColumnReport)
            }
        }
    }

    fun generateSixColumnExcelReport() {
        var sixColumnReport = ""
        val companyReport = getTheCompanyReport()
        sixColumnReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                sixColumnReport =
                    sixColumnReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.sixColumnTableExcel};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Excel"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.TXT_FILE_6COLUMN_EXCEL_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, sixColumnReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.TXT_FILE_6COLUMN_EXCEL_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, sixColumnReport)
            }
        }
    }

    fun generateChecklistWordReport() {
        var checkListWordReport = ""
        val companyReport = getTheCompanyReport()
        checkListWordReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                checkListWordReport =
                    checkListWordReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.checkListTableWord};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Word"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.DOC_FILE_CHECKLIST_WORD_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, checkListWordReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.DOC_FILE_CHECKLIST_WORD_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, checkListWordReport)
            }
        }
    }

    fun generateChecklistExcelReport() {
        var checkListExcelReport = ""
        val companyReport = getTheCompanyReport()
        checkListExcelReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    };;^12;[H2] Observations and Recommendations;;"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                checkListExcelReport =
                    checkListExcelReport + "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.checkListTableExcel};;;;"
            }
        }
        val fileNameWithoutExtension = getPresentCompanyName() + "_Excel"
        val fileNameWithExtension =
            fileNameWithoutExtension + MainActivity.TXT_FILE_CHECKLIST_EXCEL_EXTENSION
        val dirUriString = getTheCompanyDirectoryURIString()
        if (dirUriString != "") {
            val dirUri = dirUriString.toUri()
            val fileExistsFlag = fileExists(fileNameWithExtension, dirUri)
            if (fileExistsFlag == true) {
                writeToTextFile(dirUri, fileNameWithExtension, checkListExcelReport)
            } else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.TXT_FILE_CHECKLIST_EXCEL_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, checkListExcelReport)
            }
        }
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


    private var presentPhotoName: String = ""
    fun setPresentPhotoName(input: String) {
        presentPhotoName = input
    }

    fun getPresentPhotoName(): String {
        return presentPhotoName
    }

    fun makePresentPhotoName(location: String, count: Int): String {
        var presentPhotoName = ""
        if (location.contains(getPresentCompanyCode()) == true) {
            presentPhotoName = MainActivity.COMPANY_INTRODUCTION + "_" + count.toString()
        } else if (location.contains(getPresentSectionCode()) == true) {
            val list = location.split("_")
            var sectionName = ""
            if (list.size > 1) {
                sectionName = list[1]
            } else {
                sectionName = location
            }
            if (location.contains(MainActivity.INTRODUCTIONS_PAGE)) {
                presentPhotoName =
                    sectionName + "_" + MainActivity.INTRODUCTIONS_PAGE + "_" + count.toString()
            } else if (location.contains(MainActivity.OBSERVATIONS_PAGE)) {
                presentPhotoName =
                    sectionName + "_" + MainActivity.OBSERVATIONS_PAGE + "_" + count.toString()
            }
        }
        return presentPhotoName
    }

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
                result = getPresentCompanyCode() + "_" + MainActivity.INTRODUCTIONS_PAGE
            }
            MainActivity.SECTION_INTRODUCTION -> {
                result = getPresentSectionCode() + "_" + MainActivity.INTRODUCTIONS_PAGE
            }
            MainActivity.SECTION_OBSERVATIONS -> {
                result = getPresentSectionCode() + "_" + MainActivity.OBSERVATIONS_PAGE
            }
        }
        return result
    }

    //This variable stores all the details of the company photos
    private var companyPhotosList: MutableList<PhotoDetailsDC> = mutableListOf()
    fun setPhotosListInCompany(input: MutableList<PhotoDetailsDC>) {
        companyPhotosList = input
    }

    fun getTheCompanyPhotosList(): MutableList<PhotoDetailsDC> {
        return companyPhotosList
    }

    fun addPhotoToCompanyPhotosList(input: PhotoDetailsDC) {
        val companyPhotosList = getTheCompanyPhotosList()
        var isPhotoPresentInPhotosList = false
        for (photo in companyPhotosList) {
            if (photo.fullPhotoName == input.fullPhotoName) {
                photo.location = input.location
                photo.photoUriString = input.photoUriString
                photo.photoCaption = input.photoCaption
                isPhotoPresentInPhotosList = true
                break
            }
        }
        if (isPhotoPresentInPhotosList == false) {
            getTheCompanyPhotosList().add(input)
        }
    }

    fun addAndUpdateInPhotosList() {
        val selectedPhoto = getSelectedPhotoItemDC()
        val modifiedPhoto = getModifiedPhotoItemDC()
        val photosList = getTheCompanyPhotosList()
        var isModifiedPhotoPresent = false
        var selectedPhotoIndex = -1
        for (photo in photosList) {
            if (photo.fullPhotoName == modifiedPhoto.fullPhotoName) {
                photo.location = modifiedPhoto.location
                photo.photoUriString = modifiedPhoto.photoUriString
                photo.photoCaption = modifiedPhoto.photoCaption
                isModifiedPhotoPresent = true
                break
            }
        }
        if (isModifiedPhotoPresent == false) {
            for (photo in photosList) {
                if (photo.fullPhotoName == selectedPhoto.fullPhotoName) {
                    selectedPhotoIndex = photosList.indexOf(photo)
                    break
                }
            }
            if (selectedPhotoIndex > -1) {
                photosList.add(selectedPhotoIndex.plus(1), modifiedPhoto)
            } else {
                photosList.add(modifiedPhoto)
            }
        }
    }

    fun photoDetailsToML(photoDetails: PhotoDetailsDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(photoDetails.location)
        result.add(photoDetails.fullPhotoName)
        result.add(photoDetails.photoUriString)
        result.add(photoDetails.photoCaption)
        return result
    }

    //DelimiterLevel1 is used here
    fun photoDetailsToString(photoDetails: PhotoDetailsDC): String {
        var result = ""
        val resultML = photoDetailsToML(photoDetails)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToPhotoDetails(input: String): PhotoDetailsDC {
        val photoDetails = PhotoDetailsDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        photoDetails.location = ""
                        photoDetails.fullPhotoName = ""
                        photoDetails.photoUriString = ""
                        photoDetails.photoCaption = ""
                    }
                    1 -> {
                        photoDetails.location = stringList[0]
                        photoDetails.fullPhotoName = ""
                        photoDetails.photoUriString = ""
                        photoDetails.photoCaption = ""
                    }
                    2 -> {
                        photoDetails.location = stringList[0]
                        photoDetails.fullPhotoName = stringList[1]
                        photoDetails.photoUriString = ""
                        photoDetails.photoCaption = ""

                    }
                    3 -> {
                        photoDetails.location = stringList[0]
                        photoDetails.fullPhotoName = stringList[1]
                        photoDetails.photoUriString = stringList[2]
                        photoDetails.photoCaption = ""
                    }
                    4 -> {
                        photoDetails.location = stringList[0]
                        photoDetails.fullPhotoName = stringList[1]
                        photoDetails.photoUriString = stringList[2]
                        photoDetails.photoCaption = stringList[3]
                    }
                    else -> {
                        photoDetails.location = stringList[0]
                        photoDetails.fullPhotoName = stringList[1]
                        photoDetails.photoUriString = stringList[2]
                        photoDetails.photoCaption = stringList[3]
                    }
                }
            } else {
                photoDetails.location = input
                photoDetails.fullPhotoName = ""
                photoDetails.photoUriString = ""
                photoDetails.photoCaption = ""
            }
        } else {
            photoDetails.location = ""
            photoDetails.fullPhotoName = ""
            photoDetails.photoUriString = ""
            photoDetails.photoCaption = ""
        }

        return photoDetails
    }

    //DelimiterLevel2 is used here
    fun photoDetailsListToString(photoDetailsList: MutableList<PhotoDetailsDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (photoDetailsList.isNotEmpty()) {
            for (index in 0 until photoDetailsList.size) {
                val photoDetailsString = photoDetailsToString(photoDetailsList[index])
                resultML.add(photoDetailsString)
            }
            result = mlToStringUsingDelimiter2(resultML)
        }
        return result
    }

    fun stringToPhotoDetailsList(input: String): MutableList<PhotoDetailsDC> {
        val photoDetailsList = mutableListOf<PhotoDetailsDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val photoDetails = stringToPhotoDetails(inputML[index])
                photoDetailsList.add(photoDetails)
            }
        }
        return photoDetailsList
    }

    fun saveCompanyPhotoDetailsListToDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val photoDetailsListID = getPresentCompanyCode() + MainActivity.PHOTOS_LIST_ID
            val photoDetailsListString = photoDetailsListToString(getTheCompanyPhotosList())
            val aInfo5 = AInfo5(photoDetailsListID, photoDetailsListString)
            insertAInfo5(aInfo5)
        }

    }

    private var photoCount: Int = 1
    fun setThePhotoCount(input: Int) {
        photoCount = input
    }

    fun getThePhotoCount(): Int {
        return photoCount
    }

    fun listPhotosByLocation(location: String): MutableList<PhotoDetailsDC> {
        val photosList = mutableListOf<PhotoDetailsDC>()
        val companyPhotosList = getTheCompanyPhotosList()
        if (location == "All") {
            for (index in companyPhotosList.indices) {
                if (companyPhotosList[index].location != "") {
                    val photoItem = companyPhotosList[index]
                    photosList.add(photoItem)
                }
            }
        } else {
            for (index in companyPhotosList.indices) {
                if (companyPhotosList[index].location == location) {
                    val photoItem = companyPhotosList[index]
                    photosList.add(photoItem)

                }
            }
        }
        return photosList
    }

    fun getPhotoCountByLocation(location: String): Int {
        val photosList = listPhotosByLocation(location)
        val newPhotosList = mutableListOf<PhotoDetailsDC>()
        if (photosList.isNotEmpty()) {
            for (index in 0 until photosList.size) {
                if (!photosList[index].fullPhotoName.contains("_M.jpg")) {
                    newPhotosList.add(photosList[index])
                }
            }
        }
        return newPhotosList.size
    }

    private var photoItemDCSelectedFromRV = PhotoDetailsDC()
    fun setSelectedPhotoItemDC(input: PhotoDetailsDC) {
        photoItemDCSelectedFromRV = input
    }

    fun getSelectedPhotoItemDC(): PhotoDetailsDC {
        return photoItemDCSelectedFromRV
    }

    private var photoItemDCModified = PhotoDetailsDC()
    fun setModifiedPhotoItemDC(input: PhotoDetailsDC) {
        photoItemDCModified = input
    }

    fun getModifiedPhotoItemDC(): PhotoDetailsDC {
        return photoItemDCModified
    }

    private var oldModifiedPhotoItemCaption = ""
    fun setOldModifiedPhotoCaption(input: String) {
        oldModifiedPhotoItemCaption = input
    }

    fun getOldModifiedPhotoCaption(): String {
        return oldModifiedPhotoItemCaption
    }

    fun updateModifiedPhotoNames(selectedPhotoDC: PhotoDetailsDC): PhotoDetailsDC {
        var originalPhotoDC = PhotoDetailsDC()
        var tempPhotoName = ""
        var originalFullPhotoName = ""
        var modifiedPhotosDCItem = PhotoDetailsDC()
        var modifiedFullPhotoName = ""
        val companyPhotosList = getTheCompanyPhotosList()
        if (selectedPhotoDC.fullPhotoName.contains("_M.jpg")) {
            tempPhotoName = selectedPhotoDC.fullPhotoName.replace("_M.jpg", ".jpg")
            for (photoItem in companyPhotosList) {
                if (photoItem.fullPhotoName == tempPhotoName) {
                    originalFullPhotoName = photoItem.fullPhotoName
                    originalPhotoDC = photoItem
                    break
                }
            }
            modifiedFullPhotoName = selectedPhotoDC.fullPhotoName
            modifiedPhotosDCItem = selectedPhotoDC
            if (originalPhotoDC.fullPhotoName.trim() == "") {
                originalPhotoDC.fullPhotoName = tempPhotoName
                originalPhotoDC.location = selectedPhotoDC.location
                originalPhotoDC.photoCaption = ""
            }
        } else {
            originalPhotoDC = selectedPhotoDC
            originalFullPhotoName = originalPhotoDC.fullPhotoName
            modifiedFullPhotoName = modifyPhotoName(selectedPhotoDC.fullPhotoName)
            for (photoItem in companyPhotosList) {
                if (photoItem.fullPhotoName == modifiedFullPhotoName) {
                    modifiedPhotosDCItem = photoItem
                    break
                }
            }
            if (modifiedPhotosDCItem.fullPhotoName.trim() == "") {
                modifiedPhotosDCItem.fullPhotoName = modifiedFullPhotoName
                modifiedPhotosDCItem.location = selectedPhotoDC.location
                modifiedPhotosDCItem.photoCaption = selectedPhotoDC.photoCaption
            }
        }

        if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
            if (originalFullPhotoName != "") {
                if (originalPhotoDC.fullPhotoName != "") {
                    val originalPhotoComputerPath = "\\Picture\\" + originalPhotoDC.fullPhotoName
                    val modifiedPhotoComputerPath = "\\Picture\\" + modifiedFullPhotoName
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            originalPhotoComputerPath
                        ) == false
                    ) {
                        setModifiedPhotoNamePresentFlag(false)
                        if (modifiedPhotosDCItem.photoCaption != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                            setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                        }
                    } else {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                            ) == false
                        ) {
                            setModifiedPhotoNamePresentFlag(false)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                    tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    )
                                setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                            } else {
                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                    tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                                    )
                            }
                        } else {
                            setModifiedPhotoNamePresentFlag(true)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                        modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    ) == false
                                ) {
                                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                            modifiedPhotoComputerPath,
                                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                        )
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                } else {
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                }
                            }
                        }
                    }
                }
            }

            updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
            updateThePhotoPathsInCompanyIntroData(tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
            saveTheCompanyIntroDataIntoDB()
            //setCompanyIntroductionsFlag(false)
        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
            if (originalFullPhotoName != "") {
                if (originalPhotoDC.fullPhotoName != "") {
                    val originalPhotoComputerPath = "\\Picture\\" + originalPhotoDC.fullPhotoName
                    val modifiedPhotoComputerPath = "\\Picture\\" + modifiedFullPhotoName
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            originalPhotoComputerPath
                        ) == false
                    ) {
                        setModifiedPhotoNamePresentFlag(false)
                        if (modifiedPhotosDCItem.photoCaption != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                            setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                        }
                    } else {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                            ) == false
                        ) {
                            setModifiedPhotoNamePresentFlag(false)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                    tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    )
                                setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                            } else {
                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                    tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                                    )
                            }
                        } else {
                            setModifiedPhotoNamePresentFlag(true)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                        modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    ) == false
                                ) {
                                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                            modifiedPhotoComputerPath,
                                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                        )
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                } else {
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                }
                            }
                        }
                    }
                }
            }

            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            updateIntroInThePresentSectionAllData(etIntroductionsMLD.value.toString())
            updatePicturePathsInIntroForThePresentSectionAllData(
                tvPhotoPathsInIntroductionsFragmentMLD.value.toString()
            )
            saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                getThePresentSectionAllPagesFramework(),
                getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )
            //setSectionIntroductionsFlag(false)

        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_OBSERVATIONS) {
            if (originalFullPhotoName != "") {
                if (originalPhotoDC.fullPhotoName != "") {
                    val originalPhotoComputerPath = "\\Picture\\" + originalPhotoDC.fullPhotoName
                    val modifiedPhotoComputerPath = "\\Picture\\" + modifiedFullPhotoName
                    var otherPageIndex = 0
                    var otherPageContainsOriginalComputerPath = false
                    if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                            originalPhotoComputerPath
                        ) == true
                    ) {

                        if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                                "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
                            ) == false
                        ) {
                            setModifiedPhotoNamePresentFlag(false)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                tvPhotoPathsInObservationsFragmentMLD.value =
                                    tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    )
                                setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                            } else {
                                tvPhotoPathsInObservationsFragmentMLD.value =
                                    tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                        originalPhotoComputerPath,
                                        originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                                    )
                            }
                        } else {
                            setModifiedPhotoNamePresentFlag(true)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                                        modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    ) == false
                                ) {
                                    tvPhotoPathsInObservationsFragmentMLD.value =
                                        tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                            modifiedPhotoComputerPath,
                                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                        )
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                } else {
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                }
                            }
                        }
                    } else {
                        val sectionPageDataList =
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList
                        for (index in 0 until sectionPageDataList.size) {
                            if (sectionPageDataList[index].photoPaths.contains(
                                    originalPhotoComputerPath
                                ) == true
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath == true) {
                            var tvPhotoPathsInObs = sectionPageDataList[otherPageIndex].photoPaths
                            if (!tvPhotoPathsInObs.contains("$originalPhotoComputerPath;$modifiedPhotoComputerPath")) {
                                setModifiedPhotoNamePresentFlag(false)
                                if (modifiedPhotosDCItem.photoCaption != "") {
                                    tvPhotoPathsInObs =
                                        tvPhotoPathsInObs.replace(
                                            originalPhotoComputerPath,
                                            originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                        )
                                    setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                    updatePicturePathsInObsForThePresentSectionAllData(
                                        tvPhotoPathsInObs,
                                        otherPageIndex
                                    )
                                } else {
                                    tvPhotoPathsInObs =
                                        tvPhotoPathsInObs.replace(
                                            originalPhotoComputerPath,
                                            originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                                        )
                                    updatePicturePathsInObsForThePresentSectionAllData(
                                        tvPhotoPathsInObs,
                                        otherPageIndex
                                    )
                                }
                            } else {
                                setModifiedPhotoNamePresentFlag(true)
                                if (modifiedPhotosDCItem.photoCaption != "") {
                                    if (!tvPhotoPathsInObs.contains(
                                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                        )
                                    ) {
                                        tvPhotoPathsInObs =
                                            tvPhotoPathsInObs.replace(
                                                modifiedPhotoComputerPath,
                                                modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                            )
                                        setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                        updatePicturePathsInObsForThePresentSectionAllData(
                                            tvPhotoPathsInObs,
                                            otherPageIndex
                                        )
                                    } else {
                                        setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                                        updatePicturePathsInObsForThePresentSectionAllData(
                                            tvPhotoPathsInObs,
                                            otherPageIndex
                                        )
                                    }
                                }
                            }
                        } else {
                            setModifiedPhotoNamePresentFlag(false)
                            if (modifiedPhotosDCItem.photoCaption != "") {
                                tvPhotoPathsInObservationsFragmentMLD.value =
                                    tvPhotoPathsInObservationsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                setOldModifiedPhotoCaption(modifiedPhotosDCItem.photoCaption)
                            } else {
                                tvPhotoPathsInObservationsFragmentMLD.value =
                                    tvPhotoPathsInObservationsFragmentMLD.value + ";" + originalPhotoComputerPath + ";" + modifiedPhotoComputerPath
                            }
                        }

                    }
                }
            }

            updatePicturePathsInObsForThePresentSectionAllData(
                tvPhotoPathsInObservationsFragmentMLD.value.toString(),
                getThePresentSectionAllPagesFrameworkIndex()
            )
//            pageCountMLD.value?.let {
//                updatePicturePathsInObsForThePresentSectionAllData(
//                    tvPhotoPathsInObservationsFragmentMLD.value.toString(),
//                    it
//                )
//            }
            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID

            saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                getThePresentSectionAllPagesFramework(),
                getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )

        }
        return modifiedPhotosDCItem
    }

    fun saveCaption(caption: String, modifiedPhoto: PhotoDetailsDC): PhotoDetailsDC {
        val modifiedPhotoItem = modifiedPhoto
        if (caption != "") {
            if (caption.contains("[CC]") == false) {
                modifiedPhotoItem.photoCaption = "[CC]" + caption
            } else {
                modifiedPhotoItem.photoCaption = caption
            }
        } else {
            modifiedPhotoItem.photoCaption = caption
        }
        return modifiedPhotoItem
    }

    fun insertNewCaption(modifiedPhotoDC: PhotoDetailsDC) {
        val modifiedPhotosDCItem = modifiedPhotoDC
        val oldModifiedPhotoCaption = getOldModifiedPhotoCaption()
        if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotosDCItem.fullPhotoName
            if (modifiedPhotosDCItem.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                        ) == true
                    ) {
                        if (modifiedPhotosDCItem.photoCaption.trim() != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                modifiedPhotoComputerPath,
                                modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                            )
                    }
                } else {
                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                            modifiedPhotoComputerPath,
                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                        )
                }
                updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
                updateThePhotoPathsInCompanyIntroData(tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
                saveTheCompanyIntroDataIntoDB()
            }

        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotosDCItem.fullPhotoName
            if (modifiedPhotosDCItem.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                        ) == true
                    ) {
                        if (modifiedPhotosDCItem.photoCaption.trim() != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                modifiedPhotoComputerPath,
                                modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                            )
                    }
                } else {
                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                            modifiedPhotoComputerPath,
                            modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                        )
                }
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                updateIntroInThePresentSectionAllData(etIntroductionsMLD.value.toString())
                updatePicturePathsInIntroForThePresentSectionAllData(
                    tvPhotoPathsInIntroductionsFragmentMLD.value.toString()
                )
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )
            }

        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_OBSERVATIONS) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotosDCItem.fullPhotoName
            var otherPageIndex = 0
            var otherPageContainsOriginalComputerPath = false
            if (modifiedPhotosDCItem.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                            modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                        ) == true
                    ) {
                        if (modifiedPhotosDCItem.photoCaption.trim() != "") {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                )
                        } else {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        val sectionPageDataList =
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList
                        for (index in 0 until sectionPageDataList.size) {
                            if (sectionPageDataList[index].photoPaths.contains(
                                    modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                                ) == true
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath == true) {
                            var tvPhotoPathsInObs = sectionPageDataList[otherPageIndex].photoPaths
                            if (modifiedPhotosDCItem.photoCaption.trim() != "") {
                                tvPhotoPathsInObs =
                                    tvPhotoPathsInObs.replace(
                                        modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                        modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                    )
                                updatePicturePathsInObsForThePresentSectionAllData(
                                    tvPhotoPathsInObs,
                                    otherPageIndex
                                )
                            } else {
                                tvPhotoPathsInObs =
                                    tvPhotoPathsInObs.replace(
                                        modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                        modifiedPhotoComputerPath
                                    )
                                updatePicturePathsInObsForThePresentSectionAllData(
                                    tvPhotoPathsInObs,
                                    otherPageIndex
                                )
                            }
                        } else {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    modifiedPhotoComputerPath,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                )
                        }
                    }
                } else {
                    if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                            modifiedPhotoComputerPath
                        ) == true
                    ) {
                        tvPhotoPathsInObservationsFragmentMLD.value =
                            tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                modifiedPhotoComputerPath,
                                modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                            )
                    } else {
                        val sectionPageDataList =
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList
                        for (index in 0 until sectionPageDataList.size) {
                            if (sectionPageDataList[index].photoPaths.contains(
                                    modifiedPhotoComputerPath
                                ) == true
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath == true) {
                            var tvPhotoPathsInObs = sectionPageDataList[otherPageIndex].photoPaths
                            tvPhotoPathsInObs =
                                tvPhotoPathsInObs.replace(
                                    modifiedPhotoComputerPath,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotosDCItem.photoCaption
                                )
                            updatePicturePathsInObsForThePresentSectionAllData(
                                tvPhotoPathsInObs,
                                otherPageIndex
                            )
                        }
                    }
                }
                updatePicturePathsInObsForThePresentSectionAllData(
                    tvPhotoPathsInObservationsFragmentMLD.value.toString(),
                    getThePresentSectionAllPagesFrameworkIndex()
                )
//                pageCountMLD.value?.let {
//                    updatePicturePathsInObsForThePresentSectionAllData(
//                        tvPhotoPathsInObservationsFragmentMLD.value.toString(),
//                        it
//                    )
//                }
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )
            }
        }

    }

    //Flag is meant to check if the company photos have been uploaded
    //False means no. True means yes.
    private var companyPhotosUploadedFlag: Boolean = false
    fun setTheCompanyPhotosUploadedFlag(input: Boolean) {
        companyPhotosUploadedFlag = input
    }

    fun getTheCompanyPhotosUploadedFlag(): Boolean {
        return companyPhotosUploadedFlag
    }

    //Flag is meant to see if the modified photos have been uploaded
    private var modifiedPhotoUploadedFlag: Boolean = false
    fun setModifiedPhotoUploadedFlag(input: Boolean) {
        modifiedPhotoUploadedFlag = input
    }

    fun getModifiedPhotoUploadedFlag(): Boolean {
        return modifiedPhotoUploadedFlag
    }

    private var modifiedPhotoNamePresentFlag: Boolean = false
    fun setModifiedPhotoNamePresentFlag(input: Boolean) {
        modifiedPhotoNamePresentFlag = input
    }

    fun getModifiedPhotoNamePresentFlag(): Boolean {
        return modifiedPhotoNamePresentFlag
    }

    fun undoUpdatedModifiedPhotoNames(
        selectedPhotoDC: PhotoDetailsDC,
        modifiedPhotoDC: PhotoDetailsDC
    ) {
        val modifiedPhotoName = modifiedPhotoDC.fullPhotoName
        val modifiedPhotoDCItem = getModifiedPhotoItemDC()
        val modifiedPhotoCaption = modifiedPhotoDC.photoCaption
        val oldModifiedPhotoCaption = getOldModifiedPhotoCaption()
        modifiedPhotoDCItem.photoCaption = oldModifiedPhotoCaption
        setModifiedPhotoItemDC(modifiedPhotoDCItem)
        if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoName
            if (getModifiedPhotoNamePresentFlag() == false) {
                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                            ) == true
                        ) {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    ""
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath,
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                ";" + modifiedPhotoComputerPath,
                                ""
                            )
                    }
                }
                updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
                updateThePhotoPathsInCompanyIntroData(tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
                saveTheCompanyIntroDataIntoDB()
            }
        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoName
            if (getModifiedPhotoNamePresentFlag() == false) {
                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                            ) == true
                        ) {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    ""
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath,
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                ";" + modifiedPhotoComputerPath,
                                ""
                            )
                    }
                }
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                updateIntroInThePresentSectionAllData(etIntroductionsMLD.value.toString())
                updatePicturePathsInIntroForThePresentSectionAllData(
                    tvPhotoPathsInIntroductionsFragmentMLD.value.toString()
                )
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )
            }
        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_OBSERVATIONS) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoName
            if (getModifiedPhotoNamePresentFlag() == false) {
                if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                                modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption
                            ) == true
                        ) {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath + ";" + oldModifiedPhotoCaption,
                                    ""
                                )
                        } else {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    ";" + modifiedPhotoComputerPath,
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInObservationsFragmentMLD.value =
                            tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                ";" + modifiedPhotoComputerPath,
                                ""
                            )
                    }
                }
                updatePicturePathsInObsForThePresentSectionAllData(
                    tvPhotoPathsInObservationsFragmentMLD.value.toString(),
                    getThePresentSectionAllPagesFrameworkIndex()
                )
//                pageCountMLD.value?.let {
//                    updatePicturePathsInObsForThePresentSectionAllData(
//                        tvPhotoPathsInObservationsFragmentMLD.value.toString(),
//                        it
//                    )
//                }
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )
            }
        }
    }

    fun saveModifiedPhotoToFile(
        mBitmap: Bitmap?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val modifiedPhotoItemDC = getModifiedPhotoItemDC()
                var modifiedPhotoFullName = ""
                if (modifiedPhotoItemDC.fullPhotoName != "") {
                    modifiedPhotoFullName = modifiedPhotoItemDC.fullPhotoName
                } else {
                    val photoName = getSelectedPhotoItemDC().fullPhotoName
                    modifiedPhotoFullName = modifyPhotoName(photoName)
                }
                if (modifiedPhotoItemDC.photoCaption == "") {
                    modifiedPhotoItemDC.photoCaption = etTextCaptionsMLD.value.toString()
                }

                val dirUriString = getTheCompanyDirectoryURIString()
                if (dirUriString != "") {
                    val dirUri = dirUriString.toUri()
                    val fileExistence = fileExists(modifiedPhotoFullName, dirUri)
                    if (fileExistence == false || fileExistence == null) {
                        val file = DocumentFile.fromTreeUri(context, dirUri)
                            ?.createFile("image/*", modifiedPhotoFullName)
                        if (file != null && file.canWrite()) {
                            if (mBitmap != null) {
                                try {
                                    val bytes = ByteArrayOutputStream()
                                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                                    val bitmapdata: ByteArray = bytes.toByteArray()
                                    alterDocument(file.uri, bitmapdata)

                                    //Add or update the modified photo item to the company photos list
                                    modifiedPhotoItemDC.photoUriString = file.uri.toString()
                                    addAndUpdateInPhotosList()

                                    //Update the photos in the database
                                    addPhotoToCompanyPhotosList(modifiedPhotoItemDC)
                                    saveCompanyPhotoDetailsListToDB()

                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        statusMessage.value = Event("Error: File Write Error")
                                    }
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        val file =
                            DocumentFile.fromTreeUri(context!!, dirUri)
                                ?.findFile(modifiedPhotoFullName)
                        if (file != null && file.canWrite()) {
                            if (mBitmap != null) {
                                try {
                                    val bytes = ByteArrayOutputStream()
                                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                                    val bitmapdata: ByteArray = bytes.toByteArray()
                                    alterDocument(file.uri, bitmapdata)

                                    //Add or update modified photo item in the company photo list
                                    modifiedPhotoItemDC.photoUriString = file.uri.toString()
                                    addAndUpdateInPhotosList()

                                    //Update the photos in the Database
                                    addPhotoToCompanyPhotosList(modifiedPhotoItemDC)
                                    saveCompanyPhotoDetailsListToDB()
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        statusMessage.value = Event("Error: Update failed")
                                    }
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun writeToImageFile(photoUri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val contentResolver = context.contentResolver
                val dirUriString = getTheCompanyDirectoryURIString()
                var photoFullName = fileNameFromURI(photoUri)
                if (photoFullName == "") {
                    photoFullName = "Default_Photo.jpg"
                }
                if (dirUriString != "") {
                    val dirUri = dirUriString.toUri()
                    val dir = dirUri.let { DocumentFile.fromTreeUri(context, it) }
                    if (dir != null) {
                        val file = dir.createFile("image/*", photoFullName)
                        //Log.d(MainActivity.TESTING_TAG, "writeToImageFile: 2 ${file.toString()} ")
                        if (file != null && file.canWrite()) {
                            val stream = ByteArrayOutputStream()
                            val bitmap = photoUri?.let { uriToBitmap(it) }
                            if (bitmap != null) {
                                val bitmapRotated = bitmap.let { rotateBitmap(it, 90.00) }
                                bitmapRotated?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                val bitmapdata: ByteArray = stream.toByteArray()
                                alterDocument(file.uri, bitmapdata)

                                //Add picture path into tvPhotoIntroductions and save into database
                                withContext(Dispatchers.Main) {
                                    if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
                                        if (getPresentPhotoName() != "") {
                                            val photoComputerPath = ";;\\Picture\\" + photoFullName
                                            if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                                    photoComputerPath
                                                ) == false
                                            ) {
                                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                                    tvPhotoPathsInIntroductionsFragmentMLD.value + photoComputerPath
                                            }
                                        }
                                        updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
                                        updateThePhotoPathsInCompanyIntroData(
                                            tvPhotoPathsInIntroductionsFragmentMLD.value.toString()
                                        )
                                        saveTheCompanyIntroDataIntoDB()
                                    } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
                                        if (getPresentPhotoName() != "") {
                                            val photoComputerPath = ";;\\Picture\\" + photoFullName
                                            if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                                    photoComputerPath
                                                ) == false
                                            ) {
                                                tvPhotoPathsInIntroductionsFragmentMLD.value =
                                                    tvPhotoPathsInIntroductionsFragmentMLD.value + photoComputerPath
                                            }
                                        }
                                        val sectionPagesFrameworkAndDataID =
                                            getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                                        updateIntroInThePresentSectionAllData(etIntroductionsMLD.value.toString())
                                        updatePicturePathsInIntroForThePresentSectionAllData(
                                            tvPhotoPathsInIntroductionsFragmentMLD.value.toString()
                                        )
                                        saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                                            getThePresentSectionAllPagesFramework(),
                                            getThePresentSectionAllData(),
                                            sectionPagesFrameworkAndDataID
                                        )

                                    } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_OBSERVATIONS) {
                                        if (getPresentPhotoName() != "") {
                                            val photoComputerPath = ";;\\Picture\\" + photoFullName
                                            if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                                                    photoComputerPath
                                                ) == false
                                            ) {
                                                tvPhotoPathsInObservationsFragmentMLD.value =
                                                    tvPhotoPathsInObservationsFragmentMLD.value + photoComputerPath
                                            }
                                        }
                                        updatePicturePathsInObsForThePresentSectionAllData(
                                            tvPhotoPathsInObservationsFragmentMLD.value.toString(),
                                            getThePresentSectionAllPagesFrameworkIndex()
                                        )
//                                        pageCountMLD.value?.let {
//                                            updatePicturePathsInObsForThePresentSectionAllData(
//                                                tvPhotoPathsInObservationsFragmentMLD.value.toString(),
//                                                it
//                                            )
//                                        }
                                        val sectionPagesFrameworkAndDataID =
                                            getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                                        saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                                            getThePresentSectionAllPagesFramework(),
                                            getThePresentSectionAllData(),
                                            sectionPagesFrameworkAndDataID
                                        )

                                    }

                                    //Add the image name to the company photo list and save in DB
                                    val photoItem = PhotoDetailsDC(
                                        getLocationForPhotos(),
                                        photoFullName,
                                        file.uri.toString(),
                                        ""
                                    )
                                    addPhotoToCompanyPhotosList(photoItem)
                                    saveCompanyPhotoDetailsListToDB()
                                    //Set the photos flag as false
                                    setTheCompanyPhotosUploadedFlag(false)
                                    //Update the count number in ViewModel and present PhotoName
                                    setThePhotoCount(getThePhotoCount().plus(1))
                                }


                            }
                        } else {
                            //Error Message
                            withContext(Dispatchers.Main) {
                                statusMessage.value = Event("Error: File Write Failed!")
                            }
                        }
                    }
                }
            }
        }

    }

    fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val context = getApplication<Application>().applicationContext
            val contentResolver = context.contentResolver
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun rotateBitmap(original: Bitmap, degrees: Double): Bitmap? {
        val x = original.width
        val y = original.height
        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
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

    fun pageCodeToAMPCode(pageCode: String): String {
        var result = ""
        if (pageCode != "") {
            if (pageCode.contains("PC_") && pageCode.contains("_PC")) {
                result = pageCode.replace("PC", "APM")
            } else if (pageCode == "Observations" || pageCode == "Recommendations" || pageCode == "Standards") {
                result = "APM_" + pageCode + "_APM"
            } else {
                result = ""
            }
        }
        return result
    }

    fun extractDisplayNameFromPageCodeList(pageCodeList: MutableList<String>): MutableList<String> {
        var result = mutableListOf<String>()
        if (pageCodeList.isNotEmpty()) {
            for (index in 0 until pageCodeList.size) {
                if (pageCodeList[index].contains("_PC") && pageCodeList[index].contains("PC_")) {
                    var replacedString = ""
                    replacedString = pageCodeList[index].replace("_PC", "")
                    replacedString = replacedString.replace("PC_", "")
                    replacedString = replacedString.dropLast(3)
                    replacedString = replacedString.replace("_", " ").trim()
                    result.add(replacedString)
                } else {
                    var replacedString = ""
                    replacedString = pageCodeList[index]
                    result.add(replacedString)
                }
            }

        } else {
            result = mutableListOf()
        }
        return result
    }

    fun makeLinkedHashMapFromML(
        input: MutableList<String>,
        screen: String = ""
    ): LinkedHashMap<String, Boolean> {
        var result = linkedMapOf<String, Boolean>()
        if (input.isNotEmpty()) {
            for (index in 0 until input.size) {
                if (index == 0 || index == 2) {
                    result[input[index]] = screen == MainActivity.SECTION_FRAGMENT
                } else {
                    result[input[index]] = false
                }
            }
        } else {
            result = linkedMapOf()
        }
        return result
    }

    // Code and Display Related functions for Company and Sections
    fun codeAndDisplayNameToML(input: CodeNameAndDisplayNameDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(input.uniqueCodeName)
        result.add(input.displayName)
        result.add(input.pagesPresent.toString())
        return result
    }

    fun codeAndDisplayNameToString(codeAndDisplayName: CodeNameAndDisplayNameDC): String {
        var result = ""
        val resultML = codeAndDisplayNameToML(codeAndDisplayName)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    fun stringToCodeAndDisplayName(input: String): CodeNameAndDisplayNameDC {
        val codeAndDisplayName = CodeNameAndDisplayNameDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        codeAndDisplayName.uniqueCodeName = ""
                        codeAndDisplayName.displayName = ""
                        codeAndDisplayName.pagesPresent = false
                    }
                    1 -> {
                        codeAndDisplayName.uniqueCodeName = stringList[0]
                        codeAndDisplayName.displayName = ""
                        codeAndDisplayName.pagesPresent = false
                    }
                    2 -> {
                        codeAndDisplayName.uniqueCodeName = stringList[0]
                        codeAndDisplayName.displayName = stringList[1]
                        codeAndDisplayName.pagesPresent = false

                    }
                    3 -> {
                        codeAndDisplayName.uniqueCodeName = stringList[0]
                        codeAndDisplayName.displayName = stringList[1]
                        codeAndDisplayName.pagesPresent = stringList[2].toBoolean()
                    }
                    else -> {
                        codeAndDisplayName.uniqueCodeName = stringList[0]
                        codeAndDisplayName.displayName = stringList[1]
                        codeAndDisplayName.pagesPresent = stringList[2].toBoolean()
                    }
                }
            } else {
                codeAndDisplayName.uniqueCodeName = ""
                codeAndDisplayName.displayName = input
                codeAndDisplayName.pagesPresent = false
            }
        } else {
            codeAndDisplayName.uniqueCodeName = ""
            codeAndDisplayName.displayName = ""
            codeAndDisplayName.pagesPresent = false
        }
        return codeAndDisplayName
    }

    fun codeAndDisplayNameListToString(codeAndDisplayNameList: MutableList<CodeNameAndDisplayNameDC>): String {
        var result = ""
        val resultML = mutableListOf<String>()
        if (codeAndDisplayNameList.isNotEmpty()) {
            for (index in 0 until codeAndDisplayNameList.size) {
                val codeAndDisplayNameString =
                    codeAndDisplayNameToString(codeAndDisplayNameList[index])
                resultML.add(codeAndDisplayNameString)
            }
            result = mlToStringUsingDelimiter2(resultML)
        }
        return result
    }

    fun stringToCodeAndDisplayNameList(input: String): MutableList<CodeNameAndDisplayNameDC> {
        val codeAndDisplayNameList = mutableListOf<CodeNameAndDisplayNameDC>()
        if (input != "") {
            val inputML = stringToMLUsingDelimiter2(input)
            for (index in 0 until inputML.size) {
                val codeAndDisplayName =
                    stringToCodeAndDisplayName(inputML[index])
                codeAndDisplayNameList.add(codeAndDisplayName)
            }
        }
        return codeAndDisplayNameList
    }

    //This function generates a unique list of codes also in case
    //the string is a delimiterLevel1 made list or even delimiterlevel2 made list
    fun mlStringToCodeAndDisplayNameListWithUniqueCodes(
        input: String,
        companyOrSectionFlag: String
    ): MutableList<CodeNameAndDisplayNameDC> {
        var codeAndDisplayML = mutableListOf<CodeNameAndDisplayNameDC>()
        if (input == "") {
            codeAndDisplayML = mutableListOf()
        } else {
            if (input.contains(delimiterLevel1) == true) {
                val listLevel1 = input.split(delimiterLevel1)
                for (item in listLevel1) {
                    val codeAndDisplay = generateUniqueCodeFromCDCollection(
                        codeAndDisplayML,
                        item,
                        companyOrSectionFlag
                    )
                    if (codeAndDisplay != CodeNameAndDisplayNameDC("", "", false)) {
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
        return codeAndDisplayML
    }

    fun stringToCodeAndDisplayCollection1(
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
                        val codeAndDisplayName = stringToCodeAndDisplayName(item)
                        //val listLevel1 = item.split(delimiterLevel1)
                        //val codeAndDisplay = CodeNameAndDisplayNameDC(listLevel1[0], listLevel1[1], listLevel1[2].toBoolean())
                        codeAndDisplayML.add(codeAndDisplayName)
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
                            if (codeAndDisplay != CodeNameAndDisplayNameDC("", "", false)) {
                                codeAndDisplayML.add(codeAndDisplay)
                            }
                        }
                    } else {
                        val codeAndDisplay = CodeNameAndDisplayNameDC(
                            listLevel1[0],
                            listLevel1[1],
                            listLevel1[2].toBoolean()
                        )
                        if (codeAndDisplay != CodeNameAndDisplayNameDC("", "", false)) {
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
                        val level1String =
                            item.uniqueCodeName + delimiterLevel1 + item.displayName + delimiterLevel1 + item.pagesPresent.toString()
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
                        val level1String =
                            item.uniqueCodeName + delimiterLevel1 + item.displayName + delimiterLevel1 + item.pagesPresent.toString()
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
            codeNameAndDisplay = CodeNameAndDisplayNameDC("", "", false)
        } else {
            codeNameAndDisplay = CodeNameAndDisplayNameDC(itemCode, displayName, false)
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
                setTheCompanyDirectoryURIString(companyUri.toString())
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
                ?.createFile("*/txt", fullFileName)
            if ((file3Column != null) && file3Column.canWrite()) {
                statusMessage.value = Event("The file was created successfully")
                val aInfo5 = AInfo5(fullFileName, file3Column.uri.toString())
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
                        //statusMessage.value = Event("Error: Cannot write into file")
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
                        //statusMessage.value = Event("Error: No file present or cannot write")
                    }
                }
            }
        }
    }

    fun fileNameFromURI(uri: Uri?): String {
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver
        val returnCursor = uri?.let { contentResolver.query(it, null, null, null, null) }
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (returnCursor != null) {
            returnCursor.moveToFirst()
        }
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        if (returnCursor != null) {
            returnCursor.close()
        }
        return fileName!!
    }

    private suspend fun alterDocument(uri: Uri, data: ByteArray) {
        val context = getApplication<Application>().applicationContext
        try {
            //Log.d(MainActivity.TESTING_TAG, "alterDocument: URI: ${uri.toString()} ")
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
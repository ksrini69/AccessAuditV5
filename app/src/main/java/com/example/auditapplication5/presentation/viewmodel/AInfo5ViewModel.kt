package com.example.auditapplication5.presentation.viewmodel

import android.app.Application
import android.database.SQLException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import com.example.auditapplication5.Event
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private fun deleteAInfo5(aInfo5: AInfo5) = viewModelScope.launch(Dispatchers.IO) {
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

    private val _statusMessageSF = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1
    )

    val statusMessageSF: SharedFlow<String> = _statusMessageSF.asSharedFlow()

    fun setStatusMessageSF(input: String) {
        viewModelScope.launch {
            _statusMessageSF.tryEmit(input) // Non-blocking emit
        }
    }


//Company, Audit Date, Intro and Section related Variables and Functions

    // Getting the Parent Folder URI from the database
    private var parentFolderURIIDML: MutableList<String> =
        mutableListOf(MainActivity.PARENT_FOLDER_URI_ID)
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
    private var companyCodesAndNamesID = MainActivity.COMPANY_CODES_NAMES_ID
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

    private fun modifyDisplayNameOfSpecificCompanyInML(newDisplayName: String, companyCode: String) {
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

    private var companyNameToBeUpdatedFlag: Boolean = false
    fun retrieveTheCompanyNameToBeUpdatedFlag(): Boolean {
        return companyNameToBeUpdatedFlag
    }

    fun setTheCompanyNameToBeUpdatedFlag(input: Boolean) {
        companyNameToBeUpdatedFlag = input
    }

    fun deletePresentCompany(presentCompanyName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext
            val companyDirectoryUriId =
                getPresentCompanyCode() + MainActivity.COMPANY_DIRECTORY_URI_ID
            val companyDirectoryAInfo5 = AInfo5(companyDirectoryUriId)
            deleteAInfo5(companyDirectoryAInfo5)

            val companySectionListID =
                getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            val companySectionListAInfo5 = AInfo5(companySectionListID)
            deleteAInfo5(companySectionListAInfo5)

            val presentCompanyNameID = getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ID
            val presentCompanyAInfo5 = AInfo5(presentCompanyNameID)
            deleteAInfo5(presentCompanyAInfo5)

            val presentSectionNameID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
            val presentSectionNameAInfo5 = AInfo5(presentSectionNameID)
            deleteAInfo5(presentSectionNameAInfo5)

            val dateID = getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
            val companyAuditDateAInfo5 = AInfo5(dateID)
            deleteAInfo5(companyAuditDateAInfo5)

            //Deleting the company photographs
            val photoID = getPresentCompanyCode() + MainActivity.PHOTOS_LIST_ID
            val companyPhotosAInfo5 = AInfo5(photoID)
            deleteAInfo5(companyPhotosAInfo5)

            //Deleting the Company Report
            val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
            val companyReportAInfo5 = AInfo5(companyReportID)
            deleteAInfo5(companyReportAInfo5)

            //Deleting the Present Section Framework and Data
            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            val presentSectionFrameworkAndDataAInfo5 = AInfo5(sectionPagesFrameworkAndDataID)
            deleteAInfo5(presentSectionFrameworkAndDataAInfo5)

            //Deleting all the section framework and data
            val sectionCodeAndDisplayML = getTheCompanySectionCodeAndDisplayNameML()
            for (item in sectionCodeAndDisplayML) {
                val sectionPagesFrameworkDataID =
                    getPresentCompanyCode() + item.uniqueCodeName + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                val sectionFrameworkAndDataAInfo5 = AInfo5(sectionPagesFrameworkDataID)
                deleteAInfo5(sectionFrameworkAndDataAInfo5)
            }

            //Deleting all the present sections
            for (item in sectionCodeAndDisplayML) {
                val presentSectionID =
                    getPresentCompanyCode() + item.uniqueCodeName + MainActivity.PRESENT_SECTION_ID
                val presentSectionAInfo5 = AInfo5(presentSectionID)
                deleteAInfo5(presentSectionAInfo5)
            }


            //Delete All File ID based URIs
            val wordFileNameWithoutExtension = getPresentCompanyName() + "_Word"
            val threeColumnWordFileWithExtension =
                wordFileNameWithoutExtension + MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION
            val threeColumnWordFileAInfo5 = AInfo5(threeColumnWordFileWithExtension)
            deleteAInfo5(threeColumnWordFileAInfo5)

            val sixColumnWordFileWithExtension =
                wordFileNameWithoutExtension + MainActivity.DOC_FILE_6COLUMN_WORD_EXTENSION
            val sixColumnWordFileAInfo5 = AInfo5(sixColumnWordFileWithExtension)
            deleteAInfo5(sixColumnWordFileAInfo5)

            val excelFileNameWithoutExtension = getPresentCompanyName() + "_Excel"

            val threeColumnExcelFileWithExtension =
                excelFileNameWithoutExtension + MainActivity.TXT_FILE_3COLUMN_EXCEL_EXTENSION
            val threeColumnExcelFileAInfo5 = AInfo5(threeColumnExcelFileWithExtension)
            deleteAInfo5(threeColumnExcelFileAInfo5)

            val sixColumnExcelFileWithExtension =
                excelFileNameWithoutExtension + MainActivity.TXT_FILE_6COLUMN_EXCEL_EXTENSION
            val sixColumnExcelFileAInfo5 = AInfo5(sixColumnExcelFileWithExtension)
            deleteAInfo5(sixColumnExcelFileAInfo5)

            val checklistWordFileWithExtension =
                wordFileNameWithoutExtension + MainActivity.DOC_FILE_CHECKLIST_WORD_EXTENSION
            val checklistWordFileAInfo5 = AInfo5(checklistWordFileWithExtension)
            deleteAInfo5(checklistWordFileAInfo5)

            val checklistExcelFileWithExtension =
                excelFileNameWithoutExtension + MainActivity.TXT_FILE_CHECKLIST_EXCEL_EXTENSION
            val checklistExcelFileAInfo5 = AInfo5(checklistExcelFileWithExtension)
            deleteAInfo5(checklistExcelFileAInfo5)

            //Renaming the CompanyDirectory suitably
            val newCompanyName = presentCompanyName + "_DELETED_FROM_DB"
            try {
                val renamedCompanyUri = renameDocument(
                    getTheParentFolderURIString().toUri(),
                    presentCompanyName,
                    newCompanyName
                )
                withContext(Dispatchers.Main) {
                    if (renamedCompanyUri != null) {
                        Toast.makeText(
                            context,
                            "The company has been successfully deleted from the db",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: FileSystemException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "There has been an error while renaming the deleted company folder. Please see $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            withContext(Dispatchers.Main) {
                setTheDeleteCompletedFlagMLD(true)
            }
        }
    }

    private var companyAuditDate = ""
    fun setTheCompanyAuditDate(input: String) {
        companyAuditDate = input
    }

    fun getTheCompanyAuditDate(): String {
        return companyAuditDate
    }


    private var auditDateToBeUpdatedFlag: Boolean = false
    fun getTheAuditDateToBeUpdatedFlag(): Boolean {
        return auditDateToBeUpdatedFlag
    }

    fun setTheAuditDateToBeUpdatedFlag(input: Boolean) {
        auditDateToBeUpdatedFlag = input
    }

    //Introductions and Section OBS,RECO, STDS related variables for company and sections
    private var whichIntroductionsOrObservationsToBeUploaded: String = ""
    fun getTheWhichIntroductionsOrObservationsToBeUploadedVariable(): String {
        return whichIntroductionsOrObservationsToBeUploaded
    }

    fun setTheWhichIntroductionsOrObservationsToBeUploadedVariable(input: String) {
        whichIntroductionsOrObservationsToBeUploaded = input
    }

    //Section Related Variables
    private var templateSectionListID = mutableListOf(MainActivity.TEMPLATE_SECTION_LIST)
    val getDefaultSectionList = getAInfo5TemplatesByIds(templateSectionListID)

    //Present Company Section List
    private var companySectionCodeAndDisplayNameML = mutableListOf<CodeNameAndDisplayNameDC>()
    fun getTheCompanySectionCodeAndDisplayNameML(): MutableList<CodeNameAndDisplayNameDC> {
        return companySectionCodeAndDisplayNameML
    }

    fun setTheCompanySectionCodeAndDisplayNameML(input: MutableList<CodeNameAndDisplayNameDC>) {
        companySectionCodeAndDisplayNameML = input
    }

    fun loadTheDefaultCompanySectionList(defaultSectionListString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val sectionCodesAndNamesML =
                mlStringToCodeAndDisplayNameListWithUniqueCodes(
                    defaultSectionListString,
                    MainActivity.FLAG_VALUE_SECTION
                )
            val sectionCodesAndNamesMLString =
                codeAndDisplayNameListToString(
                    sectionCodesAndNamesML
                )
            val companySectionsCDListID =
                getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            val aInfo5 = AInfo5(
                companySectionsCDListID,
                sectionCodesAndNamesMLString
            )
            insertAInfo5(aInfo5)

            withContext(Dispatchers.Main) {
                setTheCompanySectionCodeAndDisplayNameML(
                    sectionCodesAndNamesML
                )
                setTheCompanySectionListUploadedFlagMLD(true)
            }
        }
    }

    fun loadTheCompanySectionList(companySectionListString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val sectionCodesAndNamesML = stringToCodeAndDisplayNameList(
                companySectionListString
            )
            val companySectionsCDListID =
                getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            val aInfo5 = AInfo5(
                companySectionsCDListID,
                companySectionListString
            )
            insertAInfo5(aInfo5)
            withContext(Dispatchers.Main) {
                setTheCompanySectionCodeAndDisplayNameML(
                    sectionCodesAndNamesML
                )
                companySectionCDMLToBeUpdatedFlagMLD.value = false
                setTheCompanySectionListUploadedFlagMLD(true)
            }

        }
    }

    fun addToTheCompanySectionCodeAndDisplayNameML(input: CodeNameAndDisplayNameDC) {
        companySectionCodeAndDisplayNameML.add(input)
    }

    private fun deleteSectionInTheCompanySectionCodeAndDisplayNameMLAndSave(input: CodeNameAndDisplayNameDC) {
        viewModelScope.launch(Dispatchers.Default) {
            val newCompanySectionCodeAndDisplayML = mutableListOf<CodeNameAndDisplayNameDC>()
            if (companySectionCodeAndDisplayNameML.isNotEmpty()) {
                for (index in 0 until companySectionCodeAndDisplayNameML.size) {
                    if (companySectionCodeAndDisplayNameML[index].uniqueCodeName != input.uniqueCodeName) {
                        newCompanySectionCodeAndDisplayML.add(companySectionCodeAndDisplayNameML[index])
                    }
                }

                withContext(Dispatchers.Main) {
                    setTheCompanySectionCodeAndDisplayNameML(newCompanySectionCodeAndDisplayML)
                    val companySectionListID =
                        getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                    saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionListID)
                }
            }
        }
    }

    private fun modifyDisplayNameOfSpecificSectionInSectionCDML(
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

    fun changePagesPresentToTrueInCompanySectionCodeAndDisplayNameList(sectionCode: String) {
        for (item in companySectionCodeAndDisplayNameML) {
            if (item.uniqueCodeName == sectionCode) {
                item.pagesPresent = true
                break
            }
        }
    }

    fun changePagesPresentToFalseInCompanySectionCodeAndDisplayNameList(sectionCode: String) {
        for (item in companySectionCodeAndDisplayNameML) {
            if (item.uniqueCodeName == sectionCode) {
                item.pagesPresent = false
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

    fun clearTheCompanySectionCodeAndDisplayML() {
        companySectionCodeAndDisplayNameML.clear()
    }

    var companySectionCDMLToBeUpdatedFlagMLD = MutableLiveData<Boolean?>()

    fun deletePresentSection(presentSectionCode: String, sectionName: String = "") {
        viewModelScope.launch(Dispatchers.Default) {

            //Delete the Section Photographs
            deleteSectionPhotosInCompanyPhotosListAndSaveToDb(presentSectionCode)
            saveCompanyPhotoDetailsListToDB()

            val sectionCodeAndDisplayML = getTheCompanySectionCodeAndDisplayNameML()
            for (item in sectionCodeAndDisplayML) {
                if (item.uniqueCodeName == presentSectionCode) {
                    val sectionPagesFrameworkDataID =
                        getPresentCompanyCode() + item.uniqueCodeName + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                    val sectionFrameworkAndDataAInfo5 = AInfo5(sectionPagesFrameworkDataID)
                    deleteAInfo5(sectionFrameworkAndDataAInfo5)
                }
            }


            for (item in sectionCodeAndDisplayML) {
                if (item.uniqueCodeName == presentSectionCode) {
                    val presentSectionID =
                        getPresentCompanyCode() + item.uniqueCodeName + MainActivity.PRESENT_SECTION_ID
                    val presentSectionAInfo5 = AInfo5(presentSectionID)
                    deleteAInfo5(presentSectionAInfo5)
                }
            }

            deleteSectionReportInCompanyReportAndSave(presentSectionCode)
            //Delete the section in the CompanySectionCodeAndDisplayNameML
            deleteSectionInTheCompanySectionCodeAndDisplayNameMLAndSave(
                getThePresentSectionCodeAndDisplayName()
            )
            withContext(Dispatchers.Main) {
                setTheDeleteCompletedFlagMLD(true)
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

    fun changePagesPresentToTrueInPresentSectionCodeAndDisplayName() {
        presentSectionCodeAndDisplayName.pagesPresent = true
    }

    private var sectionNameToBeUpdatedFlag: Boolean = false
    fun retrieveFlagForSectionNameToBeUpdated(): Boolean {
        return sectionNameToBeUpdatedFlag
    }

    fun setFlagForSectionNameToBeUpdated(input: Boolean) {
        sectionNameToBeUpdatedFlag = input
    }

    private var sectionFrameworkAndDataToBeUploadedFlag: Boolean = true
    fun getTheSectionFrameworkAndDataToBeUploadedFlag(): Boolean {
        return sectionFrameworkAndDataToBeUploadedFlag
    }

    fun setTheSectionFrameworkAndDataToBeUploadedFlag(input: Boolean) {
        sectionFrameworkAndDataToBeUploadedFlag = input
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
    private var defaultSectionPageFramework = SectionPageFrameworkDC(
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

    private fun setThePresentSectionAllPagesFramework(input: SectionAllPagesFrameworkDC) {
        presentSectionAllPagesFramework = input
    }

    fun addPageFrameworkToPresentSectionAllPagesFramework(
        sectionPageFramework: SectionPageFrameworkDC,
        indexAt: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > indexAt) {
            presentSectionAllPagesFramework.sectionPageFrameworkList.add(
                indexAt,
                sectionPageFramework
            )
        } else {
            presentSectionAllPagesFramework.sectionPageFrameworkList.add(sectionPageFramework)
        }
    }

    fun deletePageFrameworkInPresentSectionAllPagesFramework(currentPageIndex: Int) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > currentPageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList.removeAt(currentPageIndex)
        }
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
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].pageTitle =
                pageTitle
        }
    }

    fun updatePageFrameworkNumberInPresentSectionAllPagesFramework(
        pageNumber: Int,
        pageIndex: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].pageNumber =
                pageNumber
        }
    }

    fun addPageFrameworkQuestionsInPresentSectionAllPagesFramework(
        input: QuestionsFrameworkItemDC,
        currentPageIndex: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > currentPageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].questionsFrameworkList.add(
                input
            )
        }
    }

    fun deletePageFrameworkQuestionsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        questionsListPosition: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].questionsFrameworkList.removeAt(
                questionsListPosition
            )
        }
    }

    fun addPageFrameworkObservationsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].observationsFrameworkList.add(
                input
            )
        }

    }

    fun deletePageFrameworkObservationsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        observationsListPosition: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].observationsFrameworkList.removeAt(
                observationsListPosition
            )
        }

    }

    fun addPageFrameworkRecommendationsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList.add(
                input
            )
        }
    }

    fun deletePageFrameworkRecommendationsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        recommendationsListPosition: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            if (presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList.size > recommendationsListPosition) {
                presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList.removeAt(
                    recommendationsListPosition
                )
            }
        }
    }

    fun addPageFrameworkStandardsInPresentSectionAllPagesFramework(
        input: CheckboxesFrameworkItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList.add(
                input
            )
        }

    }

    fun deletePageFrameworkStandardsInPresentSectionAllPagesFramework(
        pageIndex: Int,
        standardsListPosition: Int
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > pageIndex) {
            if (presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList.size > standardsListPosition) {
                presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList.removeAt(
                    standardsListPosition
                )
            }
        }
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
            var uniquePageCodesList = mutableListOf<String>()
            if (sectionAllPagesFrameworkString != "") {
                sectionAllPagesFramework =
                    stringToSectionAllPagesFramework(sectionAllPagesFrameworkString)
                uniquePageCodesList =
                    getUniqueListOfPageCodesFromAllPagesFramework(sectionAllPagesFramework)
                sectionAllData = if (sectionAllDataString == "") {
                    createPresentSectionAllDataUsingSectionAllPagesFramework(
                        sectionAllPagesFramework
                    )
                } else {
                    val sectionAllData1 = stringToSectionAllData(sectionAllDataString)
                    if (sectionAllData.sectionAllPagesData.sectionPageDataList.size != sectionAllPagesFramework.sectionPageFrameworkList.size) {
                        equaliseTheFrameworkAndDataStructureSizes(
                            sectionAllData1,
                            sectionAllPagesFramework
                        )
                    } else {
                        sectionAllData1
                    }
                }
            } else {
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
                this@AInfo5ViewModel.setThePresentSectionAllPagesFramework(sectionAllPagesFramework)
                this@AInfo5ViewModel.setThePresentSectionAllData(sectionAllData)

                if (sectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
                    this@AInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(
                        sectionAllPagesFramework.sectionPageFrameworkList.size - 1
                    )
                    this@AInfo5ViewModel.setTheUniqueListOfSectionPageCodes(uniquePageCodesList)

                    this@AInfo5ViewModel.setThePageCountMLD(sectionAllPagesFramework.sectionPageFrameworkList.size)
                    this@AInfo5ViewModel.etPageNameMLD.value =
                        sectionAllPagesFramework.sectionPageFrameworkList[sectionAllPagesFramework.sectionPageFrameworkList.size - 1].pageTitle
                }
                this@AInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(true)
                this@AInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
                //this@AInfo5ViewModel.isTextUpdatedMLD.value = false
            }
        }
    }

    fun loadTheDefaultPresentSectionAllPagesFrameworkAndAllData() {
        if (getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isEmpty()) {
            addPageFrameworkToPresentSectionAllPagesFramework(defaultSectionPageFramework, 0)
            val sectionAllData = createPresentSectionAllDataUsingSectionAllPagesFramework(
                getThePresentSectionAllPagesFramework()
            )
            setThePresentSectionAllData(sectionAllData)

            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                getThePresentSectionAllPagesFramework(),
                getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )
        }
        if (getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isNotEmpty()) {
            this@AInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(
                getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            )

            this@AInfo5ViewModel.setThePageCountMLD(getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size)
            this@AInfo5ViewModel.etPageNameMLD.value =
                getThePresentSectionAllPagesFramework().sectionPageFrameworkList[getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1].pageTitle
            //Put true to pagesPresent in CompanySectionCodeAndDisplayML
            this@AInfo5ViewModel.changePagesPresentToTrueInCompanySectionCodeAndDisplayNameList(
                getPresentSectionCode()
            )
            val companySectionsCDListID =
                getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionsCDListID)
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


    private fun getUniqueListOfPageCodesFromAllPagesFramework(sectionAllPagesFramework: SectionAllPagesFrameworkDC): MutableList<String> {
        val result = mutableListOf<String>()
        if (sectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
            for (pageIndex in 0 until sectionAllPagesFramework.sectionPageFrameworkList.size) {
                val pageInFramework = sectionAllPagesFramework.sectionPageFrameworkList[pageIndex]

                val pagePageCode = pageInFramework.pageCode
                if (!result.contains(pagePageCode)) {
                    result.add(pagePageCode)
                }

                if (pageInFramework.questionsFrameworkList.isNotEmpty()) {
                    val questionsList = pageInFramework.questionsFrameworkList
                    for (questionIndex in 0 until questionsList.size) {
                        val questionsPageCode = questionsList[questionIndex].pageCode
                        if (!result.contains(questionsPageCode)) {
                            result.add(questionsPageCode)
                        }
                    }
                }

                if (pageInFramework.observationsFrameworkList.isNotEmpty()) {
                    val observationsList = pageInFramework.observationsFrameworkList
                    for (observationsIndex in 0 until observationsList.size) {
                        val observationsPageCode = observationsList[observationsIndex].pageCode
                        if (!result.contains(observationsPageCode)) {
                            result.add(observationsPageCode)
                        }
                    }
                }

                if (pageInFramework.recommendationsFrameworkList.isNotEmpty()) {
                    val recommendationsList = pageInFramework.recommendationsFrameworkList
                    for (recommendationsIndex in 0 until recommendationsList.size) {
                        val recommendationsPageCode =
                            recommendationsList[recommendationsIndex].pageCode
                        if (!result.contains(recommendationsPageCode)) {
                            result.add(recommendationsPageCode)
                        }
                    }
                }

                if (pageInFramework.standardsFrameworkList.isNotEmpty()) {
                    val standardsList = pageInFramework.standardsFrameworkList
                    for (standardsIndex in 0 until standardsList.size) {
                        val standardsPageCode = standardsList[standardsIndex].pageCode
                        if (!result.contains(standardsPageCode)) {
                            result.add(standardsPageCode)
                        }
                    }
                }
            }
        }
        return result
    }

    //This live data flag is meant to indicate if the All Pages Framework is uploaded or not
    //False means no. True means yes.
    private var sectionAllPagesFrameworkLoadedFlagMLD = MutableLiveData<Boolean?>()
    val sectionAllPagesFrameworkLoadedFlagLD: LiveData<Boolean?>
        get() = sectionAllPagesFrameworkLoadedFlagMLD

    fun setTheSectionAllPagesFrameworkLoadedFlagMLD(input: Boolean?) {
        sectionAllPagesFrameworkLoadedFlagMLD.value = input
    }

    //The below variable checks if the Framework has been updated or not in the ParentChildRVFragment
    private var frameworkUpdatedInParentChildRVFragmentFlag: Boolean = false
    fun setTheFrameworkUpdatedInParentChildRVFragmentFlag(input: Boolean) {
        frameworkUpdatedInParentChildRVFragmentFlag = input
    }

    fun getTheFrameworkUpdatedInParentChildRVFragmentFlag(): Boolean {
        return frameworkUpdatedInParentChildRVFragmentFlag
    }


    private fun questionsFrameworkItemToML(questionsFrameworkItem: QuestionsFrameworkItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(questionsFrameworkItem.questionsFrameworkTitle)
        result.add(questionsFrameworkItem.pageCode)
        result.add(questionsFrameworkItem.serialStatus)
        result.add(questionsFrameworkItem.isExpandable.toString())
        return result
    }

    //delimiterLevel1 is used here
    private fun questionsFrameworkItemToString(questionsFrameworkItem: QuestionsFrameworkItemDC): String {
        var questionsFrameworkItemString = ""
        val questionsFrameworkItemML = questionsFrameworkItemToML(questionsFrameworkItem)
        questionsFrameworkItemString = mlToStringUsingDelimiter1(questionsFrameworkItemML)
        return questionsFrameworkItemString
    }

    private fun stringToQuestionsFrameworkItem(input: String): QuestionsFrameworkItemDC {
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

    private fun checkboxesFrameworkItemToML(checkboxesFrameworkItem: CheckboxesFrameworkItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxesFrameworkItem.checkboxesFrameworkTitle)
        result.add(checkboxesFrameworkItem.pageCode)
        result.add(checkboxesFrameworkItem.serialStatus)
        result.add(checkboxesFrameworkItem.isExpandable.toString())
        return result
    }

    private fun checkboxesFrameworkItemToString(checkboxesFrameworkItem: CheckboxesFrameworkItemDC): String {
        var checkboxesFrameworkItemString = ""
        val checkboxesFrameworkItemML = checkboxesFrameworkItemToML(checkboxesFrameworkItem)
        checkboxesFrameworkItemString = mlToStringUsingDelimiter1(checkboxesFrameworkItemML)
        return checkboxesFrameworkItemString
    }

    private fun stringToCheckboxesFrameworkItem(input: String): CheckboxesFrameworkItemDC {
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
    private fun questionsFrameworkListToString(input: MutableList<QuestionsFrameworkItemDC>): String {
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

    private fun stringToQuestionsFrameworkList(input: String): MutableList<QuestionsFrameworkItemDC> {
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

    private fun checkboxesFrameworkListToString(input: MutableList<CheckboxesFrameworkItemDC>): String {
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

    private fun stringToCheckboxesFrameworkList(input: String): MutableList<CheckboxesFrameworkItemDC> {
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
    private fun sectionPageFrameworkToML(sectionPageFramework: SectionPageFrameworkDC): MutableList<String> {
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

    private fun sectionPageFrameworkToString(sectionPageFramework: SectionPageFrameworkDC): String {
        var result = ""
        val sectionPageFrameworkMl = sectionPageFrameworkToML(sectionPageFramework)
        result = mlToStringUsingDelimiter3(sectionPageFrameworkMl)
        return result
    }

    private fun stringToSectionPageFramework(input: String): SectionPageFrameworkDC {
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
    private fun sectionAllPagesFrameworkToString(input: SectionAllPagesFrameworkDC): String {
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

    private fun stringToSectionAllPagesFramework(input: String): SectionAllPagesFrameworkDC {
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
    private var templateIDListsID = mutableListOf(MainActivity.TEMPLATE_IDs_LIST_ID)
    val getTemplateIdsListStringFromTemplateDB = getAInfo5TemplatesByIds(templateIDListsID)

    private var templateIDList = mutableListOf<String>()
    private fun setTheTemplateIDList(input: MutableList<String>) {
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
    private fun setThePageGroupIDsList(input: MutableList<String>) {
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
                    templateIDsUploadingCompletedMLD.value = true
                    pageGroupIDsUploadingCompletedMLD.value = true

                    setTheTemplateStringUploadedMAFlagMLD(true)
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
                    setTheParentChildParentListUploadedMAFlagMLD(true)
                }
            }
        }
    }

    private fun collectTermsForPageGroup(input: MutableList<String>): MutableList<String> {
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

    private fun addUniqueItemToTheParentChildParentItemML(input: RVParentChildParentItemDC) {
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
    var parentChildParentItemMLUploadedMLD = MutableLiveData<Boolean?>()

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

    //Templates string from DB converted to Page Template and
    //adding to the Template MLDs
    fun templateStringsToPageTemplatesAndAddingToTemplatesListMLD(
        aInfo5TemplatesML: MutableList<AInfo5Templates>,
        presentSectionAllPagesFramework: SectionAllPagesFrameworkDC = SectionAllPagesFrameworkDC(),
        presentSectionAllData: SectionAllDataDC = SectionAllDataDC()
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val pageTemplateML = mutableListOf<PageTemplateDC>()
            if (aInfo5TemplatesML.isNotEmpty()) {
                for (item in aInfo5TemplatesML) {
                    var pageTemplate = PageTemplateDC()
                    if (item.id == "PC_General_Entry_01_PC") {
                        pageTemplate = getTheDefaultPageTemplate()
                    }
                    else {
                        pageTemplate.pageCode = item.id
                        val pageTemplateString = item.template_string
                        if (pageTemplateString != "" && pageTemplateString != null) {
                            pageTemplate.questionsList =
                                dbStringToQuestionTemplateItemList(pageTemplateString)
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
                            pageTemplate.pageCode = item.id + "-" + MainActivity.TEMPLATE_NOT_IN_DB
                        }
                    }
                    pageTemplateML.add(pageTemplate)
                }
            }
            else {
                val pageTemplate = getTheDefaultPageTemplate()
                pageTemplateML.add(pageTemplate)
            }
            withContext(Dispatchers.Main) {
                addUniquePageMLToPageTemplateMLMLD(pageTemplateML)
                //Check if presentSectionAllData is suitably updated wrt templates
                updateDataItemListInPresentSectionAllData(
                    presentSectionAllPagesFramework,
                    presentSectionAllData
                )
                setTheAllTemplatesUploadedFlagMLD(true)
                setTheSectionAllDataLoadedFlagMLD(true)
            }
        }
    }

    fun templateStringsToPageTemplatesForCheckListReportAndAddingToTemplatesListMLD(
        aInfo5TemplatesML: MutableList<AInfo5Templates>
    ){
        viewModelScope.launch(Dispatchers.Default) {
            val pageTemplateML = mutableListOf<PageTemplateDC>()
            if (aInfo5TemplatesML.isNotEmpty()) {
                for (item in aInfo5TemplatesML) {
                    var pageTemplate = PageTemplateDC()
                    if (item.id == "PC_General_Entry_01_PC") {
                        pageTemplate = getTheDefaultPageTemplate()
                    }
                    else {
                        pageTemplate.pageCode = item.id
                        val pageTemplateString = item.template_string
                        if (pageTemplateString != "" && pageTemplateString != null) {
                            pageTemplate.questionsList =
                                dbStringToQuestionTemplateItemList(pageTemplateString)
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
                            pageTemplate.pageCode = item.id + "-" + MainActivity.TEMPLATE_NOT_IN_DB
                        }
                    }
                    pageTemplateML.add(pageTemplate)
                }
            }
            withContext(Dispatchers.Main) {
                addUniquePageMLToPageTemplateMLMLD(pageTemplateML)
                setTheAllSectionTemplatesUploadedForChecklistFlagMLD(true)
            }
        }
    }

    fun updateDataItemListInPresentSectionAllData(
        presentSectionAllPagesFramework: SectionAllPagesFrameworkDC,
        presentSectionAllData: SectionAllDataDC
    ) {
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
            setThePresentSectionAllPagesFramework(presentSectionAllPagesFramework)
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
                setThePresentSectionAllData(presentSectionAllData)
            }
            for (currentPageIndex in 0 until presentSectionAllPagesFramework.sectionPageFrameworkList.size) {
                //Check the questions list first and update
                val questionsFrameworkList =
                    presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].questionsFrameworkList
                if (questionsFrameworkList.isNotEmpty()) {
                    for (questionsFrameworkIndex in 0 until questionsFrameworkList.size) {
                        val pageCode = questionsFrameworkList[questionsFrameworkIndex].pageCode
                        val questionTemplateItemMLN =
                            getItemFromPageTemplateMLMLD(pageCode)?.questionsList
                        val result = questionTemplateItemMLN?.let {
                            isQuestionDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, questionsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, questionsFrameworkIndex,
                                questionTemplateItemMLN
                            )
                        }
                    }
                }
                //Check the observations list and update
                val observationsFrameworkList =
                    presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].observationsFrameworkList
                if (observationsFrameworkList.isNotEmpty()) {
                    for (observationsFrameworkIndex in 0 until observationsFrameworkList.size) {
                        val pageCode =
                            observationsFrameworkList[observationsFrameworkIndex].pageCode
                        val observationsTemplateItemMLN =
                            getItemFromPageTemplateMLMLD(pageCode)?.observationsList
                        val result = observationsTemplateItemMLN?.let {
                            isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, observationsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, observationsFrameworkIndex,
                                observationsTemplateItemMLN
                            )
                        }
                    }
                }
                //Check the recommendations list and update
                val recommendationsFrameworkList =
                    presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].recommendationsFrameworkList
                if (recommendationsFrameworkList.isNotEmpty()) {
                    for (recommendationsFrameworkIndex in 0 until recommendationsFrameworkList.size) {
                        val pageCode =
                            recommendationsFrameworkList[recommendationsFrameworkIndex].pageCode
                        val recommendationsTemplateItemMLN =
                            getItemFromPageTemplateMLMLD(pageCode)?.recommendationsList
                        val result = recommendationsTemplateItemMLN?.let {
                            isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, recommendationsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, recommendationsFrameworkIndex,
                                recommendationsTemplateItemMLN
                            )
                        }
                    }
                }
                //Check the standards list and update if needed
                val standardsFrameworkList =
                    presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].standardsFrameworkList
                if (standardsFrameworkList.isNotEmpty()) {
                    for (standardsFrameworkIndex in 0 until standardsFrameworkList.size) {
                        val pageCode = standardsFrameworkList[standardsFrameworkIndex].pageCode
                        val standardsTemplateItemMLN =
                            getItemFromPageTemplateMLMLD(pageCode)?.standardsList
                        val result = standardsTemplateItemMLN?.let {
                            isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex,
                                standardsFrameworkIndex,
                                standardsTemplateItemMLN
                            )
                        }
                        if (result == false) {
                            updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex,
                                standardsFrameworkIndex,
                                standardsTemplateItemMLN
                            )
                        }
                    }
                }
            }
            //Save the SectionPagesFramework and Data into db before stopping
            val sectionPagesFrameworkAndDataID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                getThePresentSectionAllPagesFramework(),
                getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )
        }
    }

    fun dbStringToPageTemplateAndAddingToTemplatesList(
        pageCode: String,
        pageTemplateString: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            var pageTemplate = PageTemplateDC()
            if (pageCode == "PC_General_Entry_01_PC") {
                pageTemplate = getTheDefaultPageTemplate()
            } else {
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
                    pageTemplate.pageCode = pageCode + "-" + MainActivity.TEMPLATE_NOT_IN_DB
                }
            }
            withContext(Dispatchers.Main) {
                addUniquePageToPageTemplateList(pageTemplate)
                //setTheQuestionsListMLD(pageTemplate.questionsList)
                //setTheObservationsListMLD(pageTemplate.observationsList)
                //setTheRecommendationsListMLD(pageTemplate.recommendationsList)
                //setTheStandardsListMLD(pageTemplate.standardsList)
                //presentTemplateUploadedFlagMLD.value = true
            }
        }
    }

    fun dbStringToPageTemplateAndAddingToTemplatesListMainScope(
        pageCode: String,
        pageTemplateString: String
    ) {
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
        addUniquePageToPageTemplateList(pageTemplate)
        setTheQuestionsListMLD(pageTemplate.questionsList)
        setTheObservationsListMLD(pageTemplate.observationsList)
        setTheRecommendationsListMLD(pageTemplate.recommendationsList)
        setTheStandardsListMLD(pageTemplate.standardsList)
        //presentTemplateUploadedFlagMLD.value = true
    }

    //Flag to check if the template has been uploaded or not
    //True means uploaded and false not uploaded
    var templatesUploadedFlagMLD = MutableLiveData<Boolean>()

    //Database string converted to QuestionTemplateItem and CheckboxTemplateItem
    private fun dbStringToQuestionTemplateItemList(pageCodeString: String): MutableList<QuestionTemplateItemDC> {
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

    private fun dbStringtoCheckboxTemplateItemList(input: String): MutableList<CheckboxTemplateItemDC> {
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
    private var defaultPageTemplate = PageTemplateDC(
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
        if (!isItemPresentFlag) {
            pageTemplateList.add(input)
        }

    }

    fun getThePageTemplateList(): MutableList<PageTemplateDC> {
        return pageTemplateList
    }

    fun getTheListOfPageCodesInPageTemplateList(): MutableList<String> {
        val result = mutableListOf<String>()
        if (pageTemplateList.isNotEmpty()) {
            for (index in 0 until pageTemplateList.size) {
                val pageCode = pageTemplateList[index].pageCode
                if (!result.contains(pageCode)) {
                    result.add(pageCode)
                }
            }
        }
        return result
    }

    //True represents that the item is present
//    fun isItemPresentInPageTemplateList(pageCode: String): Boolean {
//        var result = false
//        if (pageCode != "") {
//            for (item in pageTemplateList) {
//                if (item.pageCode == pageCode || item.pageCode.contains(MainActivity.TEMPLATE_NOT_IN_DB)) {
//                    result = true
//                    break
//                }
//            }
//        }
//
//        return result
//    }

    fun isItemPresentInPageTemplateList(pageCode: String): Boolean {
        if (pageCode.isEmpty()) return false
        return pageTemplateList.any {
            it.pageCode == pageCode || it.pageCode.contains(MainActivity.TEMPLATE_NOT_IN_DB)
        }
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
    private var pageTemplateMLMLD = MutableLiveData<MutableList<PageTemplateDC>>()
    val pageTemplateML_LD: LiveData<MutableList<PageTemplateDC>>
        get() = pageTemplateMLMLD

    fun isItemPresentInPageTemplateMLMLD(pageCode: String): Boolean {
        var result = false
        if (pageCode != "") {
            val list = pageTemplateMLMLD.value ?: mutableListOf()
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.pageCode == pageCode || item.pageCode.contains(MainActivity.TEMPLATE_NOT_IN_DB)) {
                    result = true
                    break
                }
            }
        }
        return result
    }

    private fun addUniquePageMLToPageTemplateMLMLD(input: MutableList<PageTemplateDC>) {
        val currentList = pageTemplateMLMLD.value ?: mutableListOf()
        for (newItem in input) {
            var exists = false
            val iterator = currentList.iterator()
            while (iterator.hasNext()) {
                val existingItem = iterator.next()
                if (existingItem.pageCode == newItem.pageCode) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                currentList.add(newItem)
            }
        }
        pageTemplateMLMLD.value = currentList
    }

    fun getItemFromPageTemplateMLMLD(pageCode: String): PageTemplateDC? {
        val currentList = pageTemplateMLMLD.value ?: mutableListOf()
        val iterator = currentList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.pageCode == pageCode) {
                return item
            }
        }
        return null
    }


    fun clearThePageTemplateMLMLD() {
        val currentList = pageTemplateMLMLD.value ?: mutableListOf()
        currentList.clear()
        pageTemplateMLMLD.value = currentList
    }


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

    //This Flag indicates whether all templates have been uploaded.
    //True indicates yes and False no.
    var allTemplatesUploadedFlagMLD = MutableLiveData<Boolean?>()
    fun setTheAllTemplatesUploadedFlagMLD(input: Boolean?) {
        allTemplatesUploadedFlagMLD.value = input
    }

    var specificTemplatesUploadedFlagMLD = MutableLiveData<Boolean?>()
    fun setTheSpecificTemplatesUploadedFlagMLD(input: Boolean?) {
        specificTemplatesUploadedFlagMLD.value = input
    }


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

    var isKeyboardVisibleMLD = MutableLiveData<Boolean?>()


    var areAllAuditsDeletedMLD = MutableLiveData<Boolean?>()

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

    //This Flag is meant to indicate whether the RV needs to be reloaded
    //If True, Yes. If False, No.
    private var reloadRVPageFlagMLD = MutableLiveData<Boolean?>()
    val reloadRVPageFlagLD: LiveData<Boolean?>
        get() = reloadRVPageFlagMLD

    fun setTheReloadRVPageFlagMLD(input: Boolean?) {
        reloadRVPageFlagMLD.value = input
    }

    fun sectionNameFormatForDisplay(sectionName: String?): String {
        var result = "Section Name:\n"
        result = "Section Name: $sectionName \n"
        return result
    }

    fun pageCountFormatForDisplay(count: Int): String {
        return "Page $count  "
    }

    //Variables to store Company Intro Data
    private var companyIntroData = CompanyIntroDataDC()
    fun setTheCompanyIntroData(input: CompanyIntroDataDC) {
        companyIntroData = input
    }

    private fun getTheCompanyIntroData(): CompanyIntroDataDC {
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
            }
        }
    }

    //Variables to store section data

    //The variable below stores a unique list of sectionPageCodes
    //that are gotten from the Framework List. PageCodes that have the
    //templates loaded are not included.
    var uniqueListOfSectionPageCodes = mutableListOf<String>()
    private fun setTheUniqueListOfSectionPageCodes(input: MutableList<String>) {
        uniqueListOfSectionPageCodes = input
    }

    fun getTheUniqueListOfSectionPageCodes(): MutableList<String> {
        return uniqueListOfSectionPageCodes
    }

    fun addToTheUniqueListOfSectionPageCodes(input: String) {
        if (!uniqueListOfSectionPageCodes.contains(input)) {
            uniqueListOfSectionPageCodes.add(input)
        }
    }


    private val defaultSectionPageData = SectionPageDataDC("General Entry", 1)
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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > indexAt) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList.add(
                indexAt,
                sectionPageData
            )
        } else {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList.add(sectionPageData)
        }

    }

    fun deleteSectionPageDataInPresentSectionAllData(currentPageIndex: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList.removeAt(currentPageIndex)
        }

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

    private fun equaliseTheFrameworkAndDataStructureSizes(
        presentSectionAllData: SectionAllDataDC,
        presentSectionAllPagesFramework: SectionAllPagesFrameworkDC
    ): SectionAllDataDC {
        val result: SectionAllDataDC = presentSectionAllData
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
            if (presentSectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
                for (index in presentSectionAllData.sectionAllPagesData.sectionPageDataList.size until presentSectionAllPagesFramework.sectionPageFrameworkList.size) {
                    val pageFramework =
                        presentSectionAllPagesFramework.sectionPageFrameworkList[index]
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
                        sectionPageData.questionsFrameworkDataItemList =
                            questionsFrameworkDataItemList
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
                        sectionPageData.standardsFrameworkDataItemList =
                            standardsFrameworkDataItemList
                    }
                    result.sectionAllPagesData.sectionPageDataList.add(sectionPageData)
                }
            }
        } else if (presentSectionAllPagesFramework.sectionPageFrameworkList.size < presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
            for (index in presentSectionAllPagesFramework.sectionPageFrameworkList.size until presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
                result.sectionAllPagesData.sectionPageDataList.removeAt(index)
            }
        }
        return result
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

    fun saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(
        sectionAllPagesFramework: SectionAllPagesFrameworkDC,
        sectionAllData: SectionAllDataDC,
        sectionIDForDB: String
    ) {
        val sectionAllPagesFrameworkString =
            sectionAllPagesFrameworkToString(sectionAllPagesFramework)
        val sectionAllDataString = sectionAllDataToString(sectionAllData)
        val aInfo5 =
            AInfo5(sectionIDForDB, sectionAllPagesFrameworkString, sectionAllDataString)
        insertAInfo5(aInfo5)

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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].pageTitle =
                pageTitle
        }
    }

    fun updatePageNumberInSectionPageDataInPresentSectionAllData(pageNumber: Int, pageIndex: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].pageNumber =
                pageNumber
        }
    }

    fun updateObservationsInObsForThePresentSectionAllData(observations: String, pageIndex: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observations =
                observations
        }

    }

    fun updatePicturePathsInObsForThePresentSectionAllData(picturePaths: String, pageIndex: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].photoPaths =
                picturePaths
        }
    }

    private fun updatePicturePathsInSectionIntroForThePresentSectionAllData(picturePaths: String) {
        presentSectionAllData.picturePathsInIntroductions = picturePaths
    }

    fun updateRecommendationsInObsForThePresentSectionAllData(
        recommendations: String,
        pageIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendations =
                recommendations
        }

    }

    fun updateStandardsInObsForThePresentSectionAllData(standards: String, pageIndex: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standards =
                standards
        }

    }

    fun addQuestionsFrameworkDataItemInThePresentSectionAllData(
        questionsFrameworkDataItem: QuestionsFrameworkDataItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].questionsFrameworkDataItemList.add(
                questionsFrameworkDataItem
            )
        }
    }

    fun deleteQuestionsFrameworkDataItemInThePresentSectionAllData(pageIndex: Int, indexAt: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].questionsFrameworkDataItemList.size > indexAt) {
                presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].questionsFrameworkDataItemList.removeAt(
                    indexAt
                )
            }
        }
    }

    fun addObservationsFrameworkDataItemInThePresentSectionAllData(
        observationsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observationsFrameworkDataItemList.add(
                observationsFrameworkDataItem
            )
        }

    }

    fun deleteObservationsFrameworkDataItemInThePresentSectionAllData(
        pageIndex: Int,
        indexAt: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observationsFrameworkDataItemList.size > indexAt) {
                presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].observationsFrameworkDataItemList.removeAt(
                    indexAt
                )
            }
        }
    }

    fun addRecommendationsFrameworkDataItemInThePresentSectionAllData(
        recommendationsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.add(
                recommendationsFrameworkDataItem
            )
        }

    }

    fun deleteRecommendationsFrameworkDataItemInThePresentSectionAllData(
        pageIndex: Int,
        indexAt: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.size > indexAt) {
                presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.removeAt(
                    indexAt
                )
            }
        }
    }

    fun addStandardsFrameworkDataItemInThePresentSectionAllData(
        standardsFrameworkDataItem: CheckboxesFrameworkDataItemDC,
        pageIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standardsFrameworkDataItemList.add(
                standardsFrameworkDataItem
            )
        }
    }

    fun deleteStandardsFrameworkDataItemInThePresentSectionAllData(pageIndex: Int, indexAt: Int) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > pageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standardsFrameworkDataItemList.size > indexAt) {
                presentSectionAllData.sectionAllPagesData.sectionPageDataList[pageIndex].standardsFrameworkDataItemList.removeAt(
                    indexAt
                )
            }
        }

    }

    fun isTemplateDataItemListUpdatedInPresentSectionAllData(
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

    fun updateTemplateDataItemListUsingTemplatesInPresentSectionAllData(
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
                                if (itemPresentFlag) {
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
                            if (itemPresentFlag) {
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
                                if (itemPresentFlag) {
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
                            if (itemPresentFlag) {
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
                    result = if (obsCheckboxesDataItemList.isEmpty()) {
                        false
                    } else {
                        obsCheckboxesDataItemList.size == obsCheckboxTemplateList.size
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
                    result = if (recoCheckboxesDataItemList.isEmpty()) {
                        false
                    } else {
                        recoCheckboxesDataItemList.size == recoCheckboxTemplateList.size
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
                    result = if (stdsCheckboxesDataItemList.isEmpty()) {
                        false
                    } else {
                        stdsCheckboxesDataItemList.size == stdsCheckboxTemplateList.size
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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size > questionsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size > questionDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data1Value =
                        data1Value
//                    val sectionPagesFrameworkAndDataID =
//                        getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
//                    saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(getThePresentSectionAllPagesFramework(),getThePresentSectionAllData(),sectionPagesFrameworkAndDataID)
                }
            }
        }
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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size > questionsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size > questionDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data2Value =
                        data2Value
//                    val sectionPagesFrameworkAndDataID =
//                        getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
//                    saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(getThePresentSectionAllPagesFramework(),getThePresentSectionAllData(),sectionPagesFrameworkAndDataID)
                }

            }
        }

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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size > questionsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size > questionDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].data3Value =
                        data3Value
//                    val sectionPagesFrameworkAndDataID =
//                        getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
//                    saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(getThePresentSectionAllPagesFramework(),getThePresentSectionAllData(),sectionPagesFrameworkAndDataID)
                }
            }
        }

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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.size > questionsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size > questionDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[questionDataIndex].buttonOptionChosen =
                        buttonChoice
//                    val sectionPagesFrameworkAndDataID =
//                        getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
//                    saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(getThePresentSectionAllPagesFramework(),getThePresentSectionAllData(),sectionPagesFrameworkAndDataID)
                }
            }
        }
    }

    fun updateButtonChoiceTextInObservations(
        oldButtonChoiceTextValue: String,
        buttonChoiceTextValue: String, currentPageIndex: Int
    ) {

        if (etObservationsMLD.value?.contains(oldButtonChoiceTextValue) == true) {
            etObservationsMLD.value = etObservationsMLD.value!!.replace(
                oldButtonChoiceTextValue,
                buttonChoiceTextValue
            )
        } else {
            etObservationsMLD.value = etObservationsMLD.value + buttonChoiceTextValue
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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList.size > obsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[obsFrameworkIndex].checkboxDataItemML.size > obsDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList[obsFrameworkIndex].checkboxDataItemML[obsDataIndex].checkboxTickedValue =
                        obsChoice
                }
            }
        }
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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList.size > recoFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML.size > recoDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML[recoDataIndex].checkboxTickedValue =
                        recoChoice
                }
            }
        }
    }

    fun updateRecoPriorityValueInPresentSectionAllData(
        recoPriorityValue: String,
        currentPageIndex: Int,
        recoFrameworkIndex: Int,
        recoDataIndex: Int
    ) {
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList.size > recoFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML.size > recoDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[recoFrameworkIndex].checkboxDataItemML[recoDataIndex].priorityValues =
                        recoPriorityValue
                }
            }
        }

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
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.size > currentPageIndex) {
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList.size > stdsFrameworkIndex) {
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[stdsFrameworkIndex].checkboxDataItemML.size > stdsDataIndex) {
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList[stdsFrameworkIndex].checkboxDataItemML[stdsDataIndex].checkboxTickedValue =
                        stdsChoice
                }
            }
        }
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
            }
        } else {
            if (isChecked) {
                tvStandardsMLD.value = tvStandardsMLD.value + stdsCheckboxValue
            }
        }
        updateStandardsInObsForThePresentSectionAllData(
            tvStandardsMLD.value.toString(),
            currentPageIndex
        )
    }

    //This flag checks if the section data has been loaded from the db
    private var sectionAllDataLoadedFlagMLD = MutableLiveData<Boolean?>()
    val sectionAllDataLoadedFlagLD: LiveData<Boolean?>
        get() = sectionAllDataLoadedFlagMLD

    fun setTheSectionAllDataLoadedFlagMLD(input: Boolean?) {
        sectionAllDataLoadedFlagMLD.value = input
    }

    private fun questionDataItemToML(questionDataItem: QuestionDataItemDC): MutableList<String> {
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
    private fun questionDataItemToString(questionDataItem: QuestionDataItemDC): String {
        var result = ""
        val resultML = questionDataItemToML(questionDataItem)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    private fun stringToQuestionDataItem(input: String): QuestionDataItemDC {
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
    private fun questionDataItemListToString(questionDataItemList: MutableList<QuestionDataItemDC>): String {
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

    private fun stringToQuestionDataItemList(input: String): MutableList<QuestionDataItemDC> {
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
    private fun checkboxDataItemToML(checkboxDataItem: CheckboxDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxDataItem.checkboxTickedValue.toString())
        result.add(checkboxDataItem.priorityValues)
        return result
    }

    private fun checkboxDataItemToString(checkboxDataItem: CheckboxDataItemDC): String {
        var result = ""
        val resultML = checkboxDataItemToML(checkboxDataItem)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    private fun stringToCheckboxDataItem(input: String): CheckboxDataItemDC {
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
    private fun checkboxDataItemListToString(checkboxDataItemList: MutableList<CheckboxDataItemDC>): String {
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

    private fun stringToCheckboxDataItemList(input: String): MutableList<CheckboxDataItemDC> {
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
    private fun questionsFrameworkDataItemToML(questionsFrameworkDataItem: QuestionsFrameworkDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(questionsFrameworkDataItem.questionsFrameworkTitle)
        result.add(questionsFrameworkDataItem.pageCode)
        result.add(questionDataItemListToString(questionsFrameworkDataItem.questionDataItemList))
        return result
    }

    //DelimiterLevel 3 is used here
    private fun questionsFrameworkDataItemToString(questionsFrameworkDataItem: QuestionsFrameworkDataItemDC): String {
        var result = ""
        val resultML = questionsFrameworkDataItemToML(questionsFrameworkDataItem)
        result = mlToStringUsingDelimiter3(resultML)
        return result
    }

    private fun stringToQuestionsFrameworkDataItem(input: String): QuestionsFrameworkDataItemDC {
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

    private fun checkboxesFrameworkDataItemToML(checkboxesFrameworkDataItem: CheckboxesFrameworkDataItemDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(checkboxesFrameworkDataItem.checkboxesFrameworkTitle)
        result.add(checkboxesFrameworkDataItem.pageCode)
        result.add(checkboxDataItemListToString(checkboxesFrameworkDataItem.checkboxDataItemML))
        return result
    }

    private fun checkboxesFrameworkDataItemToString(checkboxesFrameworkDataItem: CheckboxesFrameworkDataItemDC): String {
        var result = ""
        val resultML = checkboxesFrameworkDataItemToML(checkboxesFrameworkDataItem)
        result = mlToStringUsingDelimiter3(resultML)
        return result
    }

    private fun stringToCheckboxesFrameworkDataItem(input: String): CheckboxesFrameworkDataItemDC {
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
    private fun questionsFrameworkDataItemListToString(questionsFrameworkDataItemList: MutableList<QuestionsFrameworkDataItemDC>): String {
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

    private fun stringToQuestionsFrameworkDataItemList(input: String): MutableList<QuestionsFrameworkDataItemDC> {
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

    private fun stringToCheckboxesFrameworkDataItemList(input: String): MutableList<CheckboxesFrameworkDataItemDC> {
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


    private fun sectionPageDataToML(sectionPageData: SectionPageDataDC): MutableList<String> {
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
    private fun sectionPageDataToString(sectionPageData: SectionPageDataDC): String {
        var result = ""
        val resultML = sectionPageDataToML(sectionPageData)
        result = mlToStringUsingDelimiter5(resultML)
        return result
    }

    private fun stringToSectionPageData(input: String): SectionPageDataDC {
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

    private fun stringToSectionPageDataList(input: String): MutableList<SectionPageDataDC> {
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


    private fun sectionAllDataToML(sectionAllData: SectionAllDataDC): MutableList<String> {
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

    private fun stringToSectionAllData(input: String): SectionAllDataDC {
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
    private fun companyIntroDataToML(companyIntroData: CompanyIntroDataDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(companyIntroData.introduction)
        result.add(companyIntroData.picturePathsInIntroductions)
        return result
    }

    private fun companyIntroDataToString(companyIntroData: CompanyIntroDataDC): String {
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
                    3 -> {
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

    //MLD Flag to ensure that the edit has been completed. True stands for completion
    //False means that it is not yet complete
    private var editCompletedFlagMLD = MutableLiveData(true)
    val editCompletedFlagLD: LiveData<Boolean?>
        get() = editCompletedFlagMLD

    fun setTheEditCompletedFlagMLD(input: Boolean) {
        editCompletedFlagMLD.value = input
    }

    //MLD Flag to ensure that the delete has been completed. True stands for completion
    //False means that it is not yet complete
    private var deleteCompletedFlagMLD = MutableLiveData(true)
    val deleteCompletedFlagLD: LiveData<Boolean?>
        get() = deleteCompletedFlagMLD

    fun setTheDeleteCompletedFlagMLD(input: Boolean) {
        deleteCompletedFlagMLD.value = input
    }

    //MLD Flag to ensure companyReport has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companyReportUploadedFlagMLD = MutableLiveData(true)
    val companyReportUploadedFlagLD: LiveData<Boolean?>
        get() = companyReportUploadedFlagMLD

    fun setTheCompanyReportUploadedFlagMLD(input: Boolean) {
        companyReportUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure companyIntroReport has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companyPhotosUploadedFlagMLD = MutableLiveData(true)
    val companyPhotosUploadedFlagLD: LiveData<Boolean?>
        get() = companyPhotosUploadedFlagMLD

    fun setTheCompanyPhotosUploadedFlagMLD(input: Boolean) {
        companyPhotosUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure companyDirUri has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companyDirectoryURIUploadedFlagMLD = MutableLiveData(true)
    val companyDirectoryURIUploadedFlagLD: LiveData<Boolean?>
        get() = companyDirectoryURIUploadedFlagMLD

    fun setTheCompanyDirectoryURIUploadedFlagMLD(input: Boolean) {
        companyDirectoryURIUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure companySectionList has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companySectionListUploadedFlagMLD = MutableLiveData(true)
    val companySectionListUploadedFlagLD: LiveData<Boolean?>
        get() = companySectionListUploadedFlagMLD

    fun setTheCompanySectionListUploadedFlagMLD(input: Boolean) {
        companySectionListUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure companyName has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companyNameUploadedFlagMLD = MutableLiveData(true)
    val companyNameUploadedFlagLD: LiveData<Boolean?>
        get() = companyNameUploadedFlagMLD

    fun setTheCompanyNameUploadedFlagMLD(input: Boolean) {
        companyNameUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure companyAuditDate has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companyAuditDateUploadedFlagMLD = MutableLiveData(true)
    val companyAuditDateUploadedFlagLD: LiveData<Boolean?>
        get() = companyAuditDateUploadedFlagMLD

    fun setTheCompanyAuditDateUploadedFlagMLD(input: Boolean) {
        companyAuditDateUploadedFlagMLD.value = input
    }

    //MLD Flag to ensure all the sectionTemplates have been uploaded. True stands for completion
    //False means that it is not yet complete
    private var allSectionTemplatesUploadedForChecklistFlagMLD = MutableLiveData(true)
    val allSectionTemplatesUploadedForChecklistFlagLD: LiveData<Boolean?>
        get() = allSectionTemplatesUploadedForChecklistFlagMLD

    fun setTheAllSectionTemplatesUploadedForChecklistFlagMLD(input: Boolean) {
        allSectionTemplatesUploadedForChecklistFlagMLD.value = input
    }


    //All Conditions Met MLD - combines the MLD for ParentChildParent, SectionFramework, SectionData and Templates

    val allConditionsMetLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val parentChildParentUploaded = parentChildParentItemMLUploadedMLD.value ?: false
            val frameworkUploaded = sectionAllPagesFrameworkLoadedFlagLD.value ?: false
            val dataUploaded = sectionAllDataLoadedFlagLD.value ?: false
            val templatesLoaded = allTemplatesUploadedFlagMLD.value ?: false
            val editCompletedFlag = editCompletedFlagLD.value ?: false
            val deleteCompletedFlag = deleteCompletedFlagLD.value ?: false
            val allSectionTemplatesUploadedForChecklistFlag = allSectionTemplatesUploadedForChecklistFlagLD.value?: false

            // Set true only if all are true
            this.value = parentChildParentUploaded && frameworkUploaded
                    && dataUploaded && templatesLoaded && editCompletedFlag
                    && deleteCompletedFlag && allSectionTemplatesUploadedForChecklistFlag
        }
        addSource(parentChildParentItemMLUploadedMLD, observer)
        addSource(sectionAllPagesFrameworkLoadedFlagLD, observer)
        addSource(sectionAllDataLoadedFlagLD, observer)
        addSource(allTemplatesUploadedFlagMLD, observer)
        addSource(editCompletedFlagLD, observer)
        addSource(deleteCompletedFlagLD, observer)
        addSource(allSectionTemplatesUploadedForChecklistFlagLD, observer)
    }

    //MLD Flag to ensure companyAuditDate has been updated in the companyReport.
    // True stands for completion. False means that it is not yet complete
    private var auditDateUpdatedInReportSIFFlagMLD = MutableLiveData(true)
    val auditDateUpdatedInReportSIFFlagLD: LiveData<Boolean?>
        get() = auditDateUpdatedInReportSIFFlagMLD
    fun setTheAuditDateUpdatedInReportSIFFlagMLD(input: Boolean) {
        auditDateUpdatedInReportSIFFlagMLD.value = input
    }

    //MLD Flag to ensure Company Intros has been updated in the companyReport.
    // True stands for completion. False means that it is not yet complete
    private var companyIntroUpdatedInReportSIFFlagMLD = MutableLiveData(true)
    private val companyIntroUpdatedInReportSIFFlagLD: LiveData<Boolean?>
        get() = companyIntroUpdatedInReportSIFFlagMLD
    fun setTheCompanyIntroUpdatedInReportSIFFlagMLD(input: Boolean) {
        companyIntroUpdatedInReportSIFFlagMLD.value = input
    }

    //MLD Flag to ensure Section Intros has been updated in the companyReport.
    // True stands for completion. False means that it is not yet complete
    private var sectionIntroUpdatedInReportSIFFlagMLD = MutableLiveData(true)
    private val sectionIntroUpdatedInReportSIFFlagLD: LiveData<Boolean?>
        get() = sectionIntroUpdatedInReportSIFFlagMLD
    fun setTheSectionIntroUpdatedInReportSIFFlagMLD(input: Boolean) {
        sectionIntroUpdatedInReportSIFFlagMLD.value = input
    }

    //MLD Flag to ensure Section Pages has been updated in the companyReport.
    // True stands for completion. False means that it is not yet complete
    private var sectionPagesUpdatedInReportSIFFlagMLD = MutableLiveData(true)
    val sectionPagesUpdatedInReportSIFFlagLD: LiveData<Boolean?>
        get() = sectionPagesUpdatedInReportSIFFlagMLD
    fun setTheSectionPagesUpdatedInReportSIFFlagMLD(input: Boolean) {
        sectionPagesUpdatedInReportSIFFlagMLD.value = input
    }



    val allConditionsMetSectionAndIntrosLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val editCompletedFlag = editCompletedFlagLD.value ?: false
            val deleteCompletedFlag = deleteCompletedFlagLD.value ?: false
            val companyIntroReportUploadedFlag = companyReportUploadedFlagLD.value ?: false
            val companyPhotosUploadedFlag = companyPhotosUploadedFlagLD.value ?: false
            val companyDirectoryURIUploadedFlag = companyDirectoryURIUploadedFlagLD.value ?: false
            val companySectionListUploadedFlag = companySectionListUploadedFlagLD.value ?: false
            val companyNameUploadedFlag = companyNameUploadedFlagLD.value ?: false
            val companyAuditDateUploadedFlag = companyAuditDateUploadedFlagLD.value?: false
            val companyIntroUpdatedInReportFlag = companyIntroUpdatedInReportSIFFlagLD.value?: false
            val sectionIntroUpdatedInReportFlag = sectionIntroUpdatedInReportSIFFlagLD.value?: false
            val sectionPagesUpdatedInReportFlag = sectionPagesUpdatedInReportSIFFlagLD.value?:false

            // Set true only if all are true
            this.value =
                editCompletedFlag && deleteCompletedFlag && companyIntroReportUploadedFlag
                        && companyPhotosUploadedFlag
                        && companyDirectoryURIUploadedFlag
                        && companySectionListUploadedFlag
                        && companyNameUploadedFlag
                        && companyAuditDateUploadedFlag
                        && companyIntroUpdatedInReportFlag
                        && sectionIntroUpdatedInReportFlag
                        && sectionPagesUpdatedInReportFlag

        }
        addSource(editCompletedFlagLD, observer)
        addSource(deleteCompletedFlagLD, observer)
        addSource(companyReportUploadedFlagLD, observer)
        addSource(companyPhotosUploadedFlagLD, observer)
        addSource(companyDirectoryURIUploadedFlagLD, observer)
        addSource(companySectionListUploadedFlagLD, observer)
        addSource(companyNameUploadedFlagLD, observer)
        addSource(companyAuditDateUploadedFlagLD, observer)
        addSource(companyIntroUpdatedInReportSIFFlagLD, observer)
        addSource(sectionIntroUpdatedInReportSIFFlagLD, observer)
        addSource(sectionPagesUpdatedInReportSIFFlagLD, observer)
    }


    val updateCompanyReportLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val companyReportUploadedFlag = companyReportUploadedFlagLD.value?: false
            if (companyReportUploadedFlag) {
                if (companyAuditDateUploadedFlagLD.value == true){
                    if (!companyReport.companyAuditDate.contains(getTheCompanyAuditDate())){
                        updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(getPresentCompanyCode(),"",getTheCompanyAuditDate(), "")
                    }
                }
            }
        }
        addSource(companyReportUploadedFlagLD, observer)
        addSource(companyAuditDateUploadedFlagLD, observer)
    }


    //MLD Flag to ensure parent folder has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var parentFolderUploadedMAFlagMLD = MutableLiveData(false)
    val parentFolderUploadedMAFlagLD: LiveData<Boolean?>
        get() = parentFolderUploadedMAFlagMLD
    fun setTheParentFolderUploadedMAFlagMLD(input: Boolean) {
        parentFolderUploadedMAFlagMLD.value = input
    }

    //MLD Flag to ensure templates have been uploaded.
    // True stands for completion False means that it is not yet complete
    private var templatesUploadedMAFlagMLD = MutableLiveData(false)
    val templatesUploadedMAFlagLD: LiveData<Boolean?>
        get() = templatesUploadedMAFlagMLD
    fun setTheTemplatesUploadedMAFlagMLD(input: Boolean) {
        templatesUploadedMAFlagMLD.value = input
    }

    //MLD Flag to ensure templateString has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var templateStringUploadedMAFlagMLD = MutableLiveData(false)
    val templateStringUploadedMAFlagLD: LiveData<Boolean?>
        get() = templateStringUploadedMAFlagMLD
    fun setTheTemplateStringUploadedMAFlagMLD(input: Boolean) {
        templateStringUploadedMAFlagMLD.value = input
    }

    //MLD Flag to ensure ParentChildParentItemList has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var parentChildParentListUploadedMAFlagMLD = MutableLiveData(false)
    val parentChildParentListUploadedMAFlagLD: LiveData<Boolean?>
        get() = parentChildParentListUploadedMAFlagMLD
    fun setTheParentChildParentListUploadedMAFlagMLD(input: Boolean) {
        parentChildParentListUploadedMAFlagMLD.value = input
    }

    val allConditionsMetMainActivityLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val parentFolderUploadedMAFlag = parentFolderUploadedMAFlagLD.value?: false
            val templatesUploadedFlag = templatesUploadedMAFlagLD.value?: false
            val templateStringUploadedFlag = templateStringUploadedMAFlagLD.value?: false
            val parentChildParentListUploadedFlag = parentChildParentListUploadedMAFlagLD.value?: false

            // Set true only if all are true
            this.value = parentFolderUploadedMAFlag && templatesUploadedFlag
                    && templateStringUploadedFlag && parentChildParentListUploadedFlag

        }
        addSource(parentFolderUploadedMAFlagLD, observer)
        addSource(templatesUploadedMAFlagLD, observer)
        addSource(templateStringUploadedMAFlagLD, observer)
        addSource(parentChildParentListUploadedMAFlagLD, observer)

    }

    //MLD Flag to ensure CompanyNameList has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var companyNameListUploadedENFFlagMLD = MutableLiveData(false)
    val companyNameListUploadedENFFlagLD: LiveData<Boolean?>
        get() = companyNameListUploadedENFFlagMLD
    fun setTheCompanyNameListUploadedENFFlagMLD(input: Boolean) {
        companyNameListUploadedENFFlagMLD.value = input
    }

    //MLD Flag to ensure CompanyNameList has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var parentFolderURIUploadedENFFlagMLD = MutableLiveData(false)
    val parentFolderURIUploadedENFFlagLD: LiveData<Boolean?>
        get() = parentFolderURIUploadedENFFlagMLD
    fun setTheparentFolderURIUploadedENFFlagMLD(input: Boolean) {
        parentFolderURIUploadedENFFlagMLD.value = input
    }

    val allConditionsMetEnterNameLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val companyNameListUploadedFlag = companyNameListUploadedENFFlagLD.value?: false
            val parentFolderURIUploadedFlag = parentFolderURIUploadedENFFlagLD.value?: false

            // Set true only if all are true
            this.value = companyNameListUploadedFlag && parentFolderURIUploadedFlag
        }
        addSource(companyNameListUploadedENFFlagLD, observer)
        addSource(parentFolderUploadedMAFlagLD, observer)

    }

    //MLD Flag to ensure PresentCompanyName has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var companyNameUploadedIFFlagMLD = MutableLiveData(false)
    val companyNameUploadedIFFlagLD: LiveData<Boolean?>
        get() = companyNameUploadedIFFlagMLD
    fun setTheCompanyNameUploadedIFFlagMLD(input: Boolean) {
        companyNameUploadedIFFlagMLD.value = input
    }

    //MLD Flag to ensure CompanyIntroduction has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var companyIntroductionUploadedIFFlagMLD = MutableLiveData(false)
    val companyIntroductionUploadedIFFlagLD: LiveData<Boolean?>
        get() = companyIntroductionUploadedIFFlagMLD
    fun setThecompanyIntroductionUploadedIFFlagMLD(input: Boolean) {
        companyIntroductionUploadedIFFlagMLD.value = input
    }

    //MLD Flag to ensure PresentSectionName has been uploaded.
    // True stands for completion False means that it is not yet complete
    private var sectionNameUploadedIFFlagMLD = MutableLiveData(false)
    val sectionNameUploadedIFFlagLD: LiveData<Boolean?>
        get() = sectionNameUploadedIFFlagMLD
    fun setTheSectionNameUploadedIFFlagMLD(input: Boolean) {
        sectionNameUploadedIFFlagMLD.value = input
    }


    val allConditionsMetIntroductionsFLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val companyNameUploadedFlag = companyNameUploadedIFFlagLD.value?: false
            val companyIntroUploadedFlag = companyIntroductionUploadedIFFlagLD.value?:false
            val sectionNameUploadedFlag = sectionNameUploadedIFFlagLD.value?: false
            val sectionIntroUploadedFlag = sectionAllDataLoadedFlagLD.value?: false

            // Set true only if all are true
            this.value = companyNameUploadedFlag
                        && companyIntroUploadedFlag
                        && sectionNameUploadedFlag
                        && sectionIntroUploadedFlag
        }
        addSource(companyNameUploadedIFFlagLD, observer)
        addSource(companyIntroductionUploadedIFFlagLD, observer)
        addSource(sectionNameUploadedIFFlagLD, observer)
        addSource(sectionAllDataLoadedFlagLD, observer)
    }


    //MLD Flag to ensure parent folder has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var parentFolderUploadedSLRVFlagMLD = MutableLiveData(false)
    val parentFolderUploadedSLRVFlagLD: LiveData<Boolean?>
        get() = parentFolderUploadedSLRVFlagMLD
    fun setTheParentFolderUploadedSLRVFlagMLD(input: Boolean) {
        parentFolderUploadedSLRVFlagMLD.value = input
    }

    //MLD Flag to ensure companySectionList has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var companySectionCDListUploadedSLRVFlagMLD = MutableLiveData(true)
    val companySectionCDListUploadedSLRVFlagLD: LiveData<Boolean?>
        get() = companySectionCDListUploadedSLRVFlagMLD

    fun setTheCompanySectionCDListUploadedSLRVFlagMLD(input: Boolean) {
        companySectionCDListUploadedSLRVFlagMLD.value = input
    }

    val allConditionsMetSLRVFLD = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val parentFolderUploadedFlag = parentFolderUploadedSLRVFlagLD.value?: false
            val companySectionCDListUPloadedFlag = companySectionCDListUploadedSLRVFlagLD.value?: false

            // Set true only if all are true
            this.value = parentFolderUploadedFlag && companySectionCDListUPloadedFlag

        }
        addSource(parentFolderUploadedSLRVFlagLD, observer)
        addSource(companySectionCDListUploadedSLRVFlagLD, observer)
    }

    //MLD Flag to ensure picture has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var pictureUploadedCXFFlagMLD = MutableLiveData(true)
    val pictureUploadedCXFFlagLD: LiveData<Boolean?>
        get() = pictureUploadedCXFFlagMLD

    fun setThePictureUploadedCXFFlagMLD(input: Boolean) {
        pictureUploadedCXFFlagMLD.value = input
    }

    //MLD Flag to ensure video has been uploaded. True stands for completion
    //False means that it is not yet complete
    private var videoUploadedCXFFlagMLD = MutableLiveData(true)
    val videoUploadedCXFFlagLD: LiveData<Boolean?>
        get() = videoUploadedCXFFlagMLD

    fun setTheVideoUploadedCXFFlagMLD(input: Boolean) {
        videoUploadedCXFFlagMLD.value = input
    }



    val allConditionsMetCameraXFLD= MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean?> {
            val pictureUploadedFlag = pictureUploadedCXFFlagLD.value?: false
            val videoUploadedFlag = videoUploadedCXFFlagLD.value?: false

            // Set true only if all are true
            this.value = pictureUploadedFlag && videoUploadedFlag
        }

        addSource(pictureUploadedCXFFlagLD, observer)
        addSource(videoUploadedCXFFlagLD, observer)
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

    fun setTheSectionReportListInCompanyReport(sectionReportList: MutableList<SectionReportDC>){
        companyReport.sectionReportList = sectionReportList
    }

    fun uploadTheCompanyReportIntoViewModel(companyReportString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            var companyReport = CompanyReportDC()
            if (companyReportString != "") {
                companyReport = stringToCompanyReport(companyReportString)
                if (companyReport.companyCode == ""){
                    companyReport.companyCode = getPresentCompanyCode()
                    companyReport.companyName = getPresentCompanyName()
                    saveTheCompanyReportToDB(companyReport)
                }
            }
            else {
                companyReport.companyCode = getPresentCompanyCode()
                companyReport.companyName = getPresentCompanyName()
                saveTheCompanyReportToDB(companyReport)
            }
            withContext(Dispatchers.Main) {
                setTheCompanyReport(companyReport)
                setTheCompanyReportUploadedFlagMLD(true)
            }
        }
    }

    fun updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(
        companyCode: String,
        companyName: String,
        companyAuditDate: String,
        companyIntroduction: String
    ) {
        if (companyReport.companyCode == companyCode) {
            if (companyName != ""){
                companyReport.companyName = companyName
            }
            if (companyAuditDate != ""){
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
        setTheCompanyIntroUpdatedInReportSIFFlagMLD(true)
    }

    //The below function ensures that the reports are ordered in the Company Report

    private fun reorderSectionReportsInCompanyReport(){
        val reorderedSectionReportList = mutableListOf<SectionReportDC>()
        val companySectionCNAndD = getTheCompanySectionCodeAndDisplayNameML()
        if (companySectionCNAndD.isNotEmpty()) {
            for (sectionIndex in 0 until companySectionCNAndD.size) {
                val matchingReport = companyReport.sectionReportList.find{it.sectionCode == companySectionCNAndD[sectionIndex].uniqueCodeName}
                if (matchingReport != null){
                    reorderedSectionReportList.add(matchingReport)
                } else {
                    break
                }
            }
        }
        if (reorderedSectionReportList.isNotEmpty()){
            setTheSectionReportListInCompanyReport(reorderedSectionReportList)
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
                if (sectionReportPresentFlag) {
                    companyReport.sectionReportList[sectionReportIndex] = sectionReport
                }
                reorderSectionReportsInCompanyReport()
            }

        }
    }

    fun updateSectionDetailsInCompanyReportAndSave(
        sectionCode: String,
        sectionName: String = "",
        sectionAllData: SectionAllDataDC,
        sectionChildPageCodesList: MutableList<String> = mutableListOf()
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            //val sectionAllData = getThePresentSectionAllData()
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
                generateSectionChecklistWordTable(sectionAllData.sectionAllPagesData.sectionPageDataList, sectionChildPageCodesList)
            sectionReportNew.checkListTableExcel =
                generateSectionChecklistExcelTable(sectionAllData.sectionAllPagesData.sectionPageDataList, sectionChildPageCodesList)

            if (companyReport.sectionReportList.isNotEmpty()) {
                for (index in 0 until companyReport.sectionReportList.size) {
                    if (companyReport.sectionReportList[index].sectionCode == sectionCode) {
                        sectionReportPresentFlag = true
                        sectionReportIndex = index
                        break
                    }
                }
            }
            else {
                sectionReportPresentFlag = false
            }
            withContext(Dispatchers.Main) {
                if (sectionReportPresentFlag) {
                    companyReport.sectionReportList[sectionReportIndex] = sectionReportNew
                }
                else {
                    companyReport.sectionReportList.add(sectionReportNew)
                }
                reorderSectionReportsInCompanyReport()
                //Save the companyReport to DB
                saveTheCompanyReportToDB(getTheCompanyReport())
                setTheSectionPagesUpdatedInReportSIFFlagMLD(true)
            }
        }
    }

    fun setSectionReportInCompanyReportToNilAndSave(
        sectionCode: String
    ) {
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (sectionIndex in 0 until companyReport.sectionReportList.size) {
                val sectionReport = companyReport.sectionReportList[sectionIndex]
                if (sectionReport.sectionCode == sectionCode) {
                    companyReport.sectionReportList[sectionIndex].threeColumnTableWord = ""
                    companyReport.sectionReportList[sectionIndex].threeColumnTableExcel = ""
                    companyReport.sectionReportList[sectionIndex].sixColumnTableWord = ""
                    companyReport.sectionReportList[sectionIndex].sixColumnTableExcel = ""
                    companyReport.sectionReportList[sectionIndex].checkListTableWord = ""
                    companyReport.sectionReportList[sectionIndex].checkListTableExcel = ""
                    companyReport.sectionReportList[sectionIndex].executiveSummaryTwoColumn = ""
                    companyReport.sectionReportList[sectionIndex].executiveSummaryFourColumn = ""
                }
            }
        }
        //Save the companyReport to DB
        saveTheCompanyReportToDB(getTheCompanyReport())
    }

    private fun deleteSectionReportInCompanyReportAndSave(sectionCode: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val newCompanyReport = CompanyReportDC()
            if (companyReport.sectionReportList.isNotEmpty()) {
                newCompanyReport.companyCode = companyReport.companyCode
                newCompanyReport.companyName = companyReport.companyName
                newCompanyReport.companyAuditDate = companyReport.companyAuditDate
                newCompanyReport.companyIntroduction = companyReport.companyIntroduction
                for (index in 0 until companyReport.sectionReportList.size) {
                    if (companyReport.sectionReportList[index].sectionCode != sectionCode) {
                        newCompanyReport.sectionReportList.add(companyReport.sectionReportList[index])
                    }
                }
                withContext(Dispatchers.Main) {
                    setTheCompanyReport(newCompanyReport)
                    saveTheCompanyReportToDB(newCompanyReport)
                }
            }
        }
    }

    fun saveTheCompanyReportToDB(companyReport: CompanyReportDC) {
        viewModelScope.launch(Dispatchers.IO) {
            val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
            val companyReportString = companyReportToString(companyReport)
            val aInfo5 = AInfo5(companyReportID, companyReportString)
            insertAInfo5(aInfo5)
        }
    }

    private fun generateSectionThreeColumnWordTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
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
                }
                else {
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
            threeColumnWordTable += "}"
        }
        return threeColumnWordTable
    }

    private fun generateSectionThreeColumnExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
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
            threeColumnExcelTable += "}"
        }
        return threeColumnExcelTable
    }

    private fun generateSectionSixColumnWordTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
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
            sixColumnWordTable += "}"
        }
        return sixColumnWordTable
    }

    private fun generateSectionSixColumnExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>): String {
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
                }
                else {
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
            sixColumnExcelTable += "}"
        }
        return sixColumnExcelTable
    }

    private fun generateSectionChecklistWordTable(sectionPageDataList: MutableList<SectionPageDataDC>, sectionChildPageCodesList: MutableList<String> = mutableListOf()): String {
        val dataPageCodes = buildSet {
            sectionPageDataList.forEach { section ->
                section.questionsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.observationsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.recommendationsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.standardsFrameworkDataItemList.mapTo(this) { it.pageCode }
            }
        }

        val missingPageCodesInSectionPageDataList = sectionChildPageCodesList.minus(dataPageCodes)

        var checklistWordTableListString = ""

        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionsFrameworkList = sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (frameworkIndex in 0 until questionsFrameworkList.size) {
                        val pageCode = questionsFrameworkList[frameworkIndex].pageCode
                        if (pageCode != "PC_General_Entry_01_PC") {
                            if (isItemPresentInPageTemplateMLMLD(pageCode)) {
                                val itemQuestionsList = getItemFromPageTemplateMLMLD(pageCode)?.questionsList
                                val itemDataList = questionsFrameworkList[frameworkIndex].questionDataItemList
                                var checklistWordTable = "\n" + "[H4] " + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + " Checklist\n\n"
                                checklistWordTable += "Checklist Questions" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
                                if (itemQuestionsList != null) {
                                    if (itemQuestionsList.isNotEmpty()) {
                                        for (questionsListIndex in 0 until itemQuestionsList.size) {
                                            checklistWordTable = checklistWordTable + itemQuestionsList[questionsListIndex].question + "$"
                                            checklistWordTable = if (questionsListIndex < itemDataList.size) {
                                                checklistWordTable + itemDataList[questionsListIndex].data1Value + "$" + itemDataList[questionsListIndex].data2Value + "$" + itemDataList[questionsListIndex].data3Value + "$" + itemDataList[questionsListIndex].buttonOptionChosen + "\n"
                                            } else {
                                                checklistWordTable + "$" + "$" + "$" + "\n"
                                            }
                                        }
                                    }
                                }
                                checklistWordTable += "}\n\n"
                                checklistWordTableListString += checklistWordTable
                            }
                        }
                    }
                }
            }
        }

        if (missingPageCodesInSectionPageDataList.isNotEmpty()){
            for (pageCode in missingPageCodesInSectionPageDataList){
                if (isItemPresentInPageTemplateMLMLD(pageCode)) {
                    val itemQuestionsList = getItemFromPageTemplateMLMLD(pageCode)?.questionsList
                    var checklistWordTable = "\n" + "[H4] " + extractDisplayNameFromPageCode(pageCode) + " Checklist\n"
                    checklistWordTable += "Checklist Questions" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
                    if (itemQuestionsList != null) {
                        if (itemQuestionsList.isNotEmpty()) {
                            for (questionsListIndex in 0 until itemQuestionsList.size) {
                                checklistWordTable = checklistWordTable + itemQuestionsList[questionsListIndex].question + "$" + "$" + "$" + "$ \n"
                            }
                        }
                    }
                    checklistWordTable += "}\n\n"
                    checklistWordTableListString += checklistWordTable
                }
            }
        }

        return checklistWordTableListString
    }

    private fun generateSectionChecklistExcelTable(sectionPageDataList: MutableList<SectionPageDataDC>, sectionChildPageCodesList: MutableList<String> = mutableListOf()): String {
        val dataPageCodes = buildSet {
            sectionPageDataList.forEach { section ->
                section.questionsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.observationsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.recommendationsFrameworkDataItemList.mapTo(this) { it.pageCode }
                section.standardsFrameworkDataItemList.mapTo(this) { it.pageCode }
            }
        }

        val missingPageCodesInSectionPageDataList = sectionChildPageCodesList.minus(dataPageCodes)

        var checklistExcelTableListString = ""

        if (sectionPageDataList.isNotEmpty()) {
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionsFrameworkList = sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (frameworkIndex in 0 until questionsFrameworkList.size) {
                        val pageCode = questionsFrameworkList[frameworkIndex].pageCode
                        if (pageCode != "PC_General_Entry_01_PC") {
                            if (isItemPresentInPageTemplateMLMLD(pageCode)) {
                                val itemQuestionsList = getItemFromPageTemplateMLMLD(pageCode)?.questionsList
                                val itemDataList = questionsFrameworkList[frameworkIndex].questionDataItemList
                                var checklistExcelTable = "\n" + "[H4] " + questionsFrameworkList[frameworkIndex].questionsFrameworkTitle + " Excel Checklist\n\n"
                                checklistExcelTable += "Checklist Questions" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
                                if (itemQuestionsList != null) {
                                    if (itemQuestionsList.isNotEmpty()) {
                                        for (questionsListIndex in 0 until itemQuestionsList.size) {
                                            checklistExcelTable = checklistExcelTable + itemQuestionsList[questionsListIndex].question + "$"
                                            checklistExcelTable = if (questionsListIndex < itemDataList.size) {
                                                checklistExcelTable + itemDataList[questionsListIndex].data1Value + "$" + itemDataList[questionsListIndex].data2Value + "$" + itemDataList[questionsListIndex].data3Value + "$" + itemDataList[questionsListIndex].buttonOptionChosen + "\n"
                                            } else {
                                                checklistExcelTable + "$" + "$" + "$" + "\n"
                                            }
                                        }
                                    }
                                }
                                checklistExcelTable += "}\n\n"
                                checklistExcelTableListString += checklistExcelTable
                            }
                        }
                    }
                }
            }
        }

        if (missingPageCodesInSectionPageDataList.isNotEmpty()){
            for (pageCode in missingPageCodesInSectionPageDataList){
                if (isItemPresentInPageTemplateMLMLD(pageCode)) {
                    val itemQuestionsList = getItemFromPageTemplateMLMLD(pageCode)?.questionsList
                    var checklistExcelTable = "\n" + "[H4] " + extractDisplayNameFromPageCode(pageCode) + " Excel Checklist\n"
                    checklistExcelTable += "Checklist Questions" + "$" + "DataField1" + "$" + "DataField2" + "$" + "DataField3" + "$" + "Button Choice\n"
                    if (itemQuestionsList != null) {
                        if (itemQuestionsList.isNotEmpty()) {
                            for (questionsListIndex in 0 until itemQuestionsList.size) {
                                checklistExcelTable = checklistExcelTable + itemQuestionsList[questionsListIndex].question + "$" + "$" + "$" + "$ \n"
                            }
                        }
                    }
                    checklistExcelTable += "}\n\n"
                    checklistExcelTableListString += checklistExcelTable
                }
            }
        }

        return checklistExcelTableListString
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
                sectionReportIndex = companyReport.sectionReportList.indexOfFirst { it.sectionCode == sectionCode }
                sectionReportPresentFlag = sectionReportIndex >= 0
//                for (index in 0 until companyReport.sectionReportList.size) {
//                    if (companyReport.sectionReportList[index].sectionCode == sectionCode) {
//                        sectionReportPresentFlag = true
//                        sectionReportIndex = index
//                        break
//                    }
//                }
                if (!sectionReportPresentFlag){
                    sectionReportNew.sectionCode = sectionCode
                    sectionReportNew.sectionName = sectionName
                    sectionReportNew.sectionIntroduction = sectionIntroduction
                }
            }
            else {
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
                }
                else {
                    companyReport.sectionReportList.add(sectionReportNew)
                }
                reorderSectionReportsInCompanyReport()
                //Save the companyReport to DB
                val companyReportID = getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
                val companyReportString = companyReportToString(companyReport)
                val aInfo5 = AInfo5(companyReportID, companyReportString)
                insertAInfo5(aInfo5)
                setTheSectionIntroUpdatedInReportSIFFlagMLD(true)
            }
        }
    }

    private fun sectionReportToML(input: SectionReportDC): MutableList<String> {
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

    private fun sectionReportToString(sectionReport: SectionReportDC): String {
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

    private fun sectionReportListToString(sectionReportList: MutableList<SectionReportDC>): String {
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

    private fun stringToSectionReportList(input: String): MutableList<SectionReportDC> {
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

    private fun companyIntroReportToML(input: CompanyIntroReportDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(input.companyCode)
        result.add(input.companyName)
        result.add(input.companyAuditDate)
        result.add(input.companyIntroduction)
        return result
    }

    fun companyIntroReportToString(input: CompanyIntroReportDC): String {
        var result = ""
        val resultML = companyIntroReportToML(input)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    private fun stringToCompanyIntroReport(input: String): CompanyIntroReportDC {
        val companyIntroReport = CompanyIntroReportDC()
        if (input != "") {
            if (input.contains(delimiterLevel1)) {
                val stringList = input.split(delimiterLevel1)
                when (stringList.size) {
                    0 -> {
                        companyIntroReport.companyCode = ""
                        companyIntroReport.companyName = ""
                        companyIntroReport.companyAuditDate = ""
                        companyIntroReport.companyIntroduction = ""
                    }
                    1 -> {
                        companyIntroReport.companyCode = stringList[0]
                        companyIntroReport.companyName = ""
                        companyIntroReport.companyAuditDate = ""
                        companyIntroReport.companyIntroduction = ""
                    }
                    2 -> {
                        companyIntroReport.companyCode = stringList[0]
                        companyIntroReport.companyName = stringList[1]
                        companyIntroReport.companyAuditDate = ""
                        companyIntroReport.companyIntroduction = ""
                    }
                    3 -> {
                        companyIntroReport.companyCode = stringList[0]
                        companyIntroReport.companyName = stringList[1]
                        companyIntroReport.companyAuditDate = stringList[2]
                        companyIntroReport.companyIntroduction = ""
                    }
                    4 -> {
                        companyIntroReport.companyCode = stringList[0]
                        companyIntroReport.companyName = stringList[1]
                        companyIntroReport.companyAuditDate = stringList[2]
                        companyIntroReport.companyIntroduction = stringList[3]
                    }
                    else -> {
                        companyIntroReport.companyCode = stringList[0]
                        companyIntroReport.companyName = stringList[1]
                        companyIntroReport.companyAuditDate = stringList[2]
                        companyIntroReport.companyIntroduction = stringList[3]
                    }
                }
            } else {
                companyIntroReport.companyCode = input
                companyIntroReport.companyName = ""
                companyIntroReport.companyAuditDate = ""
                companyIntroReport.companyIntroduction = ""
            }
        } else {
            companyIntroReport.companyCode = ""
            companyIntroReport.companyName = ""
            companyIntroReport.companyAuditDate = ""
            companyIntroReport.companyIntroduction = ""
        }
        return companyIntroReport
    }

    private fun companyReportToML(input: CompanyReportDC): MutableList<String> {
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
    private var companyReportUploadedFlag = false
    fun setTheCompanyReportUploadedFlag(input: Boolean) {
        companyReportUploadedFlag = input
    }

    fun getTheCompanyReportUploadedFlag(): Boolean {
        return companyReportUploadedFlag
    }

    private var companyIntroReportUploadedFlag = false
    private fun setTheCompanyIntroReportUploadedFlag(input: Boolean) {
        companyIntroReportUploadedFlag = input
    }

    fun getTheCompanyIntroReportUploadedFlag(): Boolean {
        return companyIntroReportUploadedFlag
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
                        }
                        else if (reportsList.indexOf(inputList[index]) == 1) {
                            generateThreeColumnExcelReport()
                        }
                        else if (reportsList.indexOf(inputList[index]) == 2) {
                            generateSixColumnWordReport()
                        }
                        else if (reportsList.indexOf(inputList[index]) == 3) {
                            generateSixColumnExcelReport()
                        }
                        else if (reportsList.indexOf(inputList[index]) == 4) {
                            generateChecklistWordReport()
                        }
                        else if (reportsList.indexOf(inputList[index]) == 5) {
                            generateChecklistExcelReport()
                        }
                    }
                }
            }
        }
    }


    private fun generateThreeColumnWordReport() {
        var threeColumnReport = ""
        val companyReport = getTheCompanyReport()
        threeColumnReport =
            "[H1] Acess Audit Report for ${companyReport.companyName} ;Date of Audit: " +
                    "${companyReport.companyAuditDate};; ^12;[H2] Table of Contents;^12;[H2] Introduction;;${
                        companyReport.companyIntroduction.replace(
                            "\n",
                            ";"
                        )
                    }\n\n ;;^12;[H2] Observations and Recommendations;;\n\n"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                threeColumnReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};\n \n ${sectionPage.threeColumnTableWord};;;;\n\n"
                if (sectionPage.sectionName != ""){

                }
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
            }
            else {
                createTextFileWithExtension(
                    fileNameWithoutExtension,
                    dirUri,
                    MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION
                )
                writeToTextFile(dirUri, fileNameWithExtension, threeColumnReport)
            }
        }

    }

    private fun generateThreeColumnExcelReport() {
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
                if (sectionPage.sectionName != ""){
                    threeColumnReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.threeColumnTableExcel};;;;"
                }

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
                    }\n\n;;^12;[H2] Observations and Recommendations;;\n\n"
        if (companyReport.sectionReportList.isNotEmpty()) {
            for (page in 0 until companyReport.sectionReportList.size) {
                val sectionPage = companyReport.sectionReportList[page]
                sixColumnReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};\n\n ${sectionPage.sixColumnTableWord};;;;\n\n"
                if (sectionPage.sectionName != ""){

                }
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

    private fun generateSixColumnExcelReport() {
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
                if (sectionPage.sectionName != ""){
                    sixColumnReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.sixColumnTableExcel};;;;"
                }
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

    private fun generateChecklistWordReport() {
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
                if (sectionPage.sectionName != ""){
                    checkListWordReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.checkListTableWord};;;;"
                }
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

    private fun generateChecklistExcelReport() {
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
                if (sectionPage.sectionName != ""){
                    checkListExcelReport += "[H3] ${sectionPage.sectionName};${sectionPage.sectionIntroduction};${sectionPage.checkListTableExcel};;;;"
                }
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

    fun makePresentPhotoName(location: String, count: Int, sectionName1: String = ""): String {
        var presentPhotoName = ""
        if (location.contains(getPresentCompanyCode())) {
            presentPhotoName = MainActivity.COMPANY_INTRODUCTION + "_" + count.toString()
        } else if (location.contains(getPresentSectionCode())) {

            var sectionName = sectionName1
            if (sectionName == "") {
                val list = location.split("_")
                sectionName = if (list.size > 1) {
                    list[1]
                } else {
                    location
                }
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
    private fun modifyPhotoName(selectedPhotoName: String): String {
        var result = ""
        result = if (selectedPhotoName != "") {
            if (selectedPhotoName.contains(".jpg")) {
                if (selectedPhotoName.contains("_M.jpg")) {
                    selectedPhotoName
                } else {
                    selectedPhotoName.replace(".jpg", "_M.jpg")
                }

            } else {
                selectedPhotoName + "_M.jpg"
            }
        } else {
            "Default.jpg"
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
    private fun setTheCompanyPhotosList(input: MutableList<PhotoDetailsDC>) {
        companyPhotosList = input
    }

    private fun getTheCompanyPhotosList(): MutableList<PhotoDetailsDC> {
        return companyPhotosList
    }

    private fun addPhotoToCompanyPhotosList(input: PhotoDetailsDC) {
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
        if (!isPhotoPresentInPhotosList) {
            getTheCompanyPhotosList().add(input)
        }
    }

    private fun addAndUpdateInPhotosList() {
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
        if (!isModifiedPhotoPresent) {
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

    private fun deleteSectionPhotosInCompanyPhotosListAndSaveToDb(sectionCode: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val newCompanyPhotosList = mutableListOf<PhotoDetailsDC>()
            for (item in companyPhotosList) {
                if (!item.location.contains(sectionCode)) {
                    newCompanyPhotosList.add(item)
                }
            }
            setTheCompanyPhotosList(newCompanyPhotosList)
        }
    }

    private fun photoDetailsToML(photoDetails: PhotoDetailsDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(photoDetails.location)
        result.add(photoDetails.fullPhotoName)
        result.add(photoDetails.photoUriString)
        result.add(photoDetails.photoCaption)
        return result
    }

    //DelimiterLevel1 is used here
    private fun photoDetailsToString(photoDetails: PhotoDetailsDC): String {
        var result = ""
        val resultML = photoDetailsToML(photoDetails)
        result = mlToStringUsingDelimiter1(resultML)
        return result
    }

    private fun stringToPhotoDetails(input: String): PhotoDetailsDC {
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
    private fun photoDetailsListToString(photoDetailsList: MutableList<PhotoDetailsDC>): String {
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

    fun stringToPhotoDetailsListAndSetInViewModel(input: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val photoDetailsList = mutableListOf<PhotoDetailsDC>()
            if (input != "") {
                val inputML = stringToMLUsingDelimiter2(input)
                for (index in 0 until inputML.size) {
                    val photoDetails = stringToPhotoDetails(inputML[index])
                    photoDetailsList.add(photoDetails)
                }
            }
            withContext(Dispatchers.Main) {
                setTheCompanyPhotosList(photoDetailsList)
                setTheCompanyPhotosUploadedFlagMLD(true)

            }
        }
    }

    private fun saveCompanyPhotoDetailsListToDB() {
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
    private fun setOldModifiedPhotoCaption(input: String) {
        oldModifiedPhotoItemCaption = input
    }

    private fun getOldModifiedPhotoCaption(): String {
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
                    val modifiedPhotoComputerPath = "\\Picture\\$modifiedFullPhotoName"
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
                                "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
                                        "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
                    val modifiedPhotoComputerPath = "\\Picture\\$modifiedFullPhotoName"
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
                                "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
                                        "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
                    val modifiedPhotoComputerPath = "\\Picture\\$modifiedFullPhotoName"
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
                                        "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
                                )
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath) {
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
                                            "$originalPhotoComputerPath;$modifiedPhotoComputerPath"
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
        if (caption != "") {
            if (!caption.contains("[CC]")) {
                modifiedPhoto.photoCaption = "[CC]$caption"
            } else {
                modifiedPhoto.photoCaption = caption
            }
        } else {
            modifiedPhoto.photoCaption = caption
        }
        return modifiedPhoto
    }

    fun insertNewCaption(modifiedPhotoDC: PhotoDetailsDC) {
        val oldModifiedPhotoCaption = getOldModifiedPhotoCaption()
        if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoDC.fullPhotoName
            if (modifiedPhotoDC.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                        ) == true
                    ) {
                        if (modifiedPhotoDC.photoCaption.trim() != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                modifiedPhotoComputerPath,
                                modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                            )
                    }
                } else {
                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                            modifiedPhotoComputerPath,
                            modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                        )
                }
                updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
                updateThePhotoPathsInCompanyIntroData(tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
                saveTheCompanyIntroDataIntoDB()
            }

        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoDC.fullPhotoName
            if (modifiedPhotoDC.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                            "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                        ) == true
                    ) {
                        if (modifiedPhotoDC.photoCaption.trim() != "") {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                modifiedPhotoComputerPath,
                                modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                            )
                    }
                } else {
                    tvPhotoPathsInIntroductionsFragmentMLD.value =
                        tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                            modifiedPhotoComputerPath,
                            modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
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
            val modifiedPhotoComputerPath = "\\Picture\\" + modifiedPhotoDC.fullPhotoName
            var otherPageIndex = 0
            var otherPageContainsOriginalComputerPath = false
            if (modifiedPhotoDC.photoCaption.trim() != oldModifiedPhotoCaption.trim()) {
                if (oldModifiedPhotoCaption.trim() != "") {
                    if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                            "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                        ) == true
                    ) {
                        if (modifiedPhotoDC.photoCaption.trim() != "") {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                                )
                        } else {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    modifiedPhotoComputerPath
                                )
                        }
                    } else {
                        val sectionPageDataList =
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList
                        for (index in 0 until sectionPageDataList.size) {
                            if (sectionPageDataList[index].photoPaths.contains(
                                    "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                                )
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath) {
                            var tvPhotoPathsInObs = sectionPageDataList[otherPageIndex].photoPaths
                            if (modifiedPhotoDC.photoCaption.trim() != "") {
                                tvPhotoPathsInObs =
                                    tvPhotoPathsInObs.replace(
                                        "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                        modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                                    )
                                updatePicturePathsInObsForThePresentSectionAllData(
                                    tvPhotoPathsInObs,
                                    otherPageIndex
                                )
                            } else {
                                tvPhotoPathsInObs =
                                    tvPhotoPathsInObs.replace(
                                        "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
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
                                    modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
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
                                modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
                            )
                    } else {
                        val sectionPageDataList =
                            presentSectionAllData.sectionAllPagesData.sectionPageDataList
                        for (index in 0 until sectionPageDataList.size) {
                            if (sectionPageDataList[index].photoPaths.contains(
                                    modifiedPhotoComputerPath
                                )
                            ) {
                                otherPageContainsOriginalComputerPath = true
                                otherPageIndex = index
                                break
                            }
                        }
                        if (otherPageContainsOriginalComputerPath) {
                            var tvPhotoPathsInObs = sectionPageDataList[otherPageIndex].photoPaths
                            tvPhotoPathsInObs =
                                tvPhotoPathsInObs.replace(
                                    modifiedPhotoComputerPath,
                                    modifiedPhotoComputerPath + ";" + modifiedPhotoDC.photoCaption
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
    private fun setTheCompanyPhotosUploadedFlag(input: Boolean) {
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
    private fun setModifiedPhotoNamePresentFlag(input: Boolean) {
        modifiedPhotoNamePresentFlag = input
    }

    private fun retrieveTheModifiedPhotoNamePresentFlag(): Boolean {
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
            val modifiedPhotoComputerPath = "\\Picture\\$modifiedPhotoName"
            if (!retrieveTheModifiedPhotoNamePresentFlag()) {
                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                            ) == true
                        ) {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    ""
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath",
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                ";$modifiedPhotoComputerPath",
                                ""
                            )
                    }
                }
                updateTheIntroInTheCompanyIntroData(etIntroductionsMLD.value.toString())
                updateThePhotoPathsInCompanyIntroData(tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
                saveTheCompanyIntroDataIntoDB()
            }
        } else if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION) {
            val modifiedPhotoComputerPath = "\\Picture\\$modifiedPhotoName"
            if (!retrieveTheModifiedPhotoNamePresentFlag()) {
                if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInIntroductionsFragmentMLD.value?.contains(
                                "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                            ) == true
                        ) {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    ""
                                )
                        } else {
                            tvPhotoPathsInIntroductionsFragmentMLD.value =
                                tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath",
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInIntroductionsFragmentMLD.value =
                            tvPhotoPathsInIntroductionsFragmentMLD.value!!.replace(
                                ";$modifiedPhotoComputerPath",
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
            val modifiedPhotoComputerPath = "\\Picture\\$modifiedPhotoName"
            if (!retrieveTheModifiedPhotoNamePresentFlag()) {
                if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(modifiedPhotoComputerPath) == true) {
                    if (oldModifiedPhotoCaption.trim() != "") {
                        if (tvPhotoPathsInObservationsFragmentMLD.value?.contains(
                                "$modifiedPhotoComputerPath;$oldModifiedPhotoCaption"
                            ) == true
                        ) {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath;$oldModifiedPhotoCaption",
                                    ""
                                )
                        } else {
                            tvPhotoPathsInObservationsFragmentMLD.value =
                                tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                    ";$modifiedPhotoComputerPath",
                                    ""
                                )
                        }
                    } else {
                        tvPhotoPathsInObservationsFragmentMLD.value =
                            tvPhotoPathsInObservationsFragmentMLD.value!!.replace(
                                ";$modifiedPhotoComputerPath",
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
                modifiedPhotoFullName = if (modifiedPhotoItemDC.fullPhotoName != "") {
                    modifiedPhotoItemDC.fullPhotoName
                } else {
                    val photoName = getSelectedPhotoItemDC().fullPhotoName
                    modifyPhotoName(photoName)
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

                        if (file != null && file.canWrite()) {
                            val stream = ByteArrayOutputStream()
                            val bitmap = photoUri?.let { uriToBitmap(it) }
                            if (bitmap != null) {
                                val bitmapRotated = rotateBitmap(bitmap, 00.00)
                                bitmapRotated?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                val bitmapdata: ByteArray = stream.toByteArray()
                                alterDocument(file.uri, bitmapdata)

                                //Delete the photo in the Pictures Folder
                                //deleteAFileOrADirectory()
                                try {
                                    photoUri.let { uri ->
                                        val rowsDeleted = context.contentResolver.delete(uri, null, null)
                                        if (rowsDeleted == 0) {
                                            Log.w("PhotoDelete", "No rows deleted, file might not exist or no permission")
                                            // Optional: Implement a fallback or user notification if needed
                                        } else {
                                            Log.i("PhotoDelete", "Photo deleted successfully")
                                        }
                                    }
                                } catch (securityException: SecurityException) {
                                    Log.e("PhotoDelete", "Permission denied to delete photo: ${securityException.message}")
                                    // On Android 10+ you may need to handle RecoverableSecurityException by requesting user confirmation
                                    // For example, prompt the user to grant delete permission via IntentSender if applicable
                                } catch (illegalArgumentException: IllegalArgumentException) {
                                    Log.e("PhotoDelete", "Invalid Uri for deletion: ${illegalArgumentException.message}")
                                } catch (e: Exception) {
                                    Log.e("PhotoDelete", "Failed to delete photo: ${e.message}")
                                }

                                //Add picture path into tvPhotoIntroductions and save into database
                                withContext(Dispatchers.Main) {
                                    if (getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION) {
                                        if (getPresentPhotoName() != "") {
                                            val photoComputerPath = ";;\\Picture\\$photoFullName"
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
                                            val photoComputerPath = ";;\\Picture\\$photoFullName"
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
                                            val photoComputerPath = ";;\\Picture\\$photoFullName"
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
                                    //Update the Picture Uploaded Flag to Continue Taking Photos
                                    setThePictureUploadedCXFFlagMLD(true)
                                }
                            }
                        } else {
                            //Error Message
                            withContext(Dispatchers.Main) {
                                //statusMessage.value = Event("Error: File Write Failed!")
                                setThePictureUploadedCXFFlagMLD(true)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
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

    private var delimiterLevel1 = "{%DASPL#1}"

    private var delimiterLevel2 = "{%DASPL#2}"

    private var delimiterLevel3 = "{%DASPL#3}"

    private var delimiterLevel4 = "{%DASPL#4}"

    private var delimiterLevel5 = "{%DASPL#5}"

    private var delimiterLevel6 = "{%DASPL#6}"

    private var delimiterLevel7 = "{%DASPL#7}"

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
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter2(inputList: MutableList<String>): String {
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

    private fun stringToMLUsingDelimiter2(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel2)) {
                val inputList = input.split(delimiterLevel2)
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter3(inputList: MutableList<String>): String {
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
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter4(inputList: MutableList<String>): String {
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

    private fun stringToMLUsingDelimiter4(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel4)) {
                val inputList = input.split(delimiterLevel4)
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter5(inputList: MutableList<String>): String {
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
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter6(inputList: MutableList<String>): String {
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

    private fun stringToMLUsingDelimiter6(input: String): MutableList<String> {
        var resultList = mutableListOf<String>()
        if (input == "") {
            resultList = mutableListOf()
        } else {
            if (input.contains(delimiterLevel6)) {
                val inputList = input.split(delimiterLevel6)
                for (element in inputList) {
                    resultList.add(element)
                }
            } else {
                resultList.add(input)
            }
        }
        return resultList
    }

    private fun mlToStringUsingDelimiter7(inputList: MutableList<String>): String {
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
                for (element in inputList) {
                    resultList.add(element)
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
            result = if (pageCode.contains("PC_") && pageCode.contains("_PC")) {
                pageCode.replace("PC", "APM")
            } else if (pageCode == "Observations" || pageCode == "Recommendations" || pageCode == "Standards") {
                "APM_" + pageCode + "_APM"
            } else {
                ""
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
        screen: String = "",
        reportsToBeGenerated: MutableList<String> = mutableListOf()
    ): LinkedHashMap<String, Boolean> {
        var result = linkedMapOf<String, Boolean>()
        if (input.isNotEmpty()) {
            for (index in 0 until input.size) {
                if (screen == MainActivity.SECTION_FRAGMENT){
                    result[input[index]] = reportsToBeGenerated.contains(input[index])
                }
                else {
                    result[input[index]] = false
                }
            }
        }
        else {
            result = linkedMapOf()
        }
        return result
    }

    // Code and Display Related functions for Company and Sections
    private fun codeAndDisplayNameToML(input: CodeNameAndDisplayNameDC): MutableList<String> {
        val result = mutableListOf<String>()
        result.add(input.uniqueCodeName)
        result.add(input.displayName)
        result.add(input.pagesPresent.toString())
        return result
    }

    private fun codeAndDisplayNameToString(codeAndDisplayName: CodeNameAndDisplayNameDC): String {
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
            if (input.contains(delimiterLevel1)) {
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
            if (input.contains(delimiterLevel2)) {
                val listLevel2 = input.split(delimiterLevel2)
                for (item in listLevel2) {
                    if (item.contains(delimiterLevel1)) {
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
                if (input.contains(delimiterLevel1)) {
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
                    codeAndDisplayString = if (item.uniqueCodeName != "" && (item.uniqueCodeName.contains("Comp_") && item.uniqueCodeName.contains(
                            "_Comp"
                        ) || item.uniqueCodeName.contains("Section_") && item.uniqueCodeName.contains(
                            "_Section"
                        )) && item.displayName != ""
                    ) {
                        val level1String =
                            item.uniqueCodeName + delimiterLevel1 + item.displayName + delimiterLevel1 + item.pagesPresent.toString()
                        level1String
                    } else {
                        ""
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

        codeNameAndDisplay = if (!codeNameIsUniqueFlag) {
            CodeNameAndDisplayNameDC("", "", false)
        } else {
            CodeNameAndDisplayNameDC(itemCode, displayName, false)
        }
        return codeNameAndDisplay
    }

    private fun extractMaxNumberFromCDCollection(codeAndDisplayML: MutableList<CodeNameAndDisplayNameDC>): Int {
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

    private fun generateCodeNumberStringFromInt(maxNumber: Int): String {
        var codeNumber = ""
        var maxNumberPositive = 0
        maxNumberPositive = if (maxNumber < 0) {
            -maxNumber
        } else {
            maxNumber
        }
        val codeNumberString = (maxNumberPositive + 1).toString()
        when (codeNumberString.length) {
            1 -> codeNumber = "000$codeNumberString"
            2 -> codeNumber = "00$codeNumberString"
            3 -> codeNumber = "0$codeNumberString"
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

//    //MLD Flag to ensure that the edit has been completed. True stands for completion
//    //False means that it is not yet complete
//    private var editCompletedFlagMLD = MutableLiveData<Boolean?>()
//    val editCompletedFlagLD: LiveData<Boolean?>
//        get() = editCompletedFlagMLD
//    fun setTheEditCompletedFlagMLD(input: Boolean){
//        editCompletedFlagMLD.value = input
//    }


    fun editCompanyNameFunction(newCompanyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var oldCompanyName = ""
            val presentCompanyCodeAndDisplay =
                getThePresentCompanyCodeAndDisplayName()
            oldCompanyName = presentCompanyCodeAndDisplay.displayName
            presentCompanyCodeAndDisplay.displayName = newCompanyName
            setThePresentCompanyCodeAndDisplayName(
                presentCompanyCodeAndDisplay
            )

            val presentCompanyID =
                presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
            val aInfo5NewCompanyName = AInfo5(presentCompanyID, newCompanyName)
            insertAInfo5(aInfo5NewCompanyName)

            setTheCompanyNameToBeUpdatedFlag(true)
            //update the Company related ML
            modifyDisplayNameOfSpecificCompanyInML(
                newCompanyName,
                presentCompanyCodeAndDisplay.uniqueCodeName
            )
            //Save the above in the DB
            val companyCodeAndDisplayNameMLString =
                codeAndDisplayNameListToString(getTheCompanyCodeAndDisplayNameML())
            val aInfo5 = AInfo5(
                MainActivity.COMPANY_CODES_NAMES_ID,
                companyCodeAndDisplayNameMLString
            )
            insertAInfo5(aInfo5)

            val oldCompanyDirectoryUri =
                getTheCompanyDirectoryURIString().toUri()

            //Renaming the document files inside the old Company Directory
            //Three column File
            val old3ColumnWordFileNameWithoutExtension = oldCompanyName + "_Word"
            val old3ColumnWordFileNameWithExtension =
                old3ColumnWordFileNameWithoutExtension + MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION

            val new3ColumnWordFileNameWithoutExtension = newCompanyName + "_Word"
            val new3ColumnWordFileNameWithExtension =
                new3ColumnWordFileNameWithoutExtension + MainActivity.DOC_FILE_3COLUMN_WORD_EXTENSION

            renameDocument(
                oldCompanyDirectoryUri,
                old3ColumnWordFileNameWithExtension,
                new3ColumnWordFileNameWithExtension
            )

            //Six Column File
            val old6ColumnWordFileNameWithoutExtension = oldCompanyName + "_Word"
            val old6ColumnWordFileNameWithExtension =
                old6ColumnWordFileNameWithoutExtension + MainActivity.DOC_FILE_6COLUMN_WORD_EXTENSION

            val new6ColumnWordFileNameWithoutExtension = newCompanyName + "_Word"
            val new6ColumnWordFileNameWithExtension =
                new6ColumnWordFileNameWithoutExtension + MainActivity.DOC_FILE_6COLUMN_WORD_EXTENSION

            renameDocument(
                oldCompanyDirectoryUri,
                old6ColumnWordFileNameWithExtension,
                new6ColumnWordFileNameWithExtension
            )

            //Renaming the old Company Directory suitably and saving the new company directory uri into the db
            val newCompanyDirUri =
                renameDocument(parentFolderURIString.toUri(), oldCompanyName, newCompanyName)

            val companyDirectoryUriId =
                getPresentCompanyCode() + MainActivity.COMPANY_DIRECTORY_URI_ID
            val aInfo5Dir =
                AInfo5(companyDirectoryUriId, newCompanyDirUri.toString())
            insertAInfo5(aInfo5Dir)
            val companyIntroAndPhotoPathsData =
                "${etIntroductionsMLD.value.toString()} \n\n ${tvPhotoPathsInIntroductionsFragmentMLD.value.toString()}"


            withContext(Dispatchers.Main) {
                setTheCompanyDirectoryURIString(newCompanyDirUri.toString())
                //Updating the company name in the company report
                updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(
                    getPresentCompanyCode(),
                    newCompanyName,
                    "",
                    ""
                )
                setTheEditCompletedFlagMLD(true)
            }
        }
    }

    fun editSectionNameFunction(newSectionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val presentSectionCodeAndDisplay = getThePresentSectionCodeAndDisplayName()
            val oldSectionName = presentSectionCodeAndDisplay.displayName

            presentSectionCodeAndDisplay.displayName = newSectionName
            setThePresentSectionCodeAndDisplayName(presentSectionCodeAndDisplay)

            //Update the present section display Name
            val presentSectionNameID =
                getPresentCompanyCode() + getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
            val aInfo5SectionName = AInfo5(presentSectionNameID, newSectionName)
            insertAInfo5(aInfo5SectionName)

            setFlagForSectionNameToBeUpdated(true)

            //update the Section related ML
            modifyDisplayNameOfSpecificSectionInSectionCDML(
                newSectionName,
                presentSectionCodeAndDisplay.uniqueCodeName
            )
            //Save the above in the DB
            val sectionCodeAndDisplayNameMLString =
                codeAndDisplayNameListToString(getTheCompanySectionCodeAndDisplayNameML())
            val companySectionsCDListID =
                getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            val aInfo5 = AInfo5(
                companySectionsCDListID,
                sectionCodeAndDisplayNameMLString
            )
            insertAInfo5(aInfo5)

            //Renaming the photos to reflect the new section name
            val companyDirectoryUri = getTheCompanyDirectoryURIString().toUri()
            val photosList = getTheCompanyPhotosList()
            val newPhotosList = mutableListOf<PhotoDetailsDC>()
            for (photoIndex in 0 until photosList.size) {
                val list = photosList[photoIndex].fullPhotoName.split("_")
                var photoDetailsItem = PhotoDetailsDC()
                photoDetailsItem.location = photosList[photoIndex].location
                if (list[0] == oldSectionName) {
                    val newFullPhotoName = newSectionName + "_" + list[1] + "_" + list[2]
                    photoDetailsItem.location = photosList[photoIndex].location
                    photoDetailsItem.photoCaption = photosList[photoIndex].photoCaption
                    photoDetailsItem.fullPhotoName = newFullPhotoName
                    val newPhotoUri = renameDocument(
                        companyDirectoryUri,
                        photosList[photoIndex].fullPhotoName,
                        newFullPhotoName
                    )
                    photoDetailsItem.photoUriString = newPhotoUri.toString()
                    newPhotosList.add(photoDetailsItem)
                } else {
                    photoDetailsItem = photosList[photoIndex]
                    newPhotosList.add(photoDetailsItem)
                    continue
                }
            }

            setTheCompanyPhotosList(newPhotosList)
            val newPhotosListString = photoDetailsListToString(newPhotosList)
            val photoID = getPresentCompanyCode() + MainActivity.PHOTOS_LIST_ID
            val aInfo5Photos = AInfo5(photoID, newPhotosListString)
            insertAInfo5(aInfo5Photos)

            //Update the section data suitably
            val presentSectionAllData = getThePresentSectionAllData()
            val presentSectionAllPagesData = presentSectionAllData.sectionAllPagesData

            if (presentSectionAllPagesData.sectionPageDataList.isNotEmpty()) {
                for (pageIndex in 0 until presentSectionAllPagesData.sectionPageDataList.size) {
                    val oldPhotoPaths =
                        presentSectionAllPagesData.sectionPageDataList[pageIndex].photoPaths
                    var newPhotoPaths = ""
                    if (oldPhotoPaths.contains(oldSectionName)) {
                        newPhotoPaths = oldPhotoPaths.replace(oldSectionName, newSectionName)
                        updatePicturePathsInObsForThePresentSectionAllData(newPhotoPaths, pageIndex)
                    } else {
                        continue
                    }
                }
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )

            }

            if (presentSectionAllData.picturePathsInIntroductions != "") {
                val oldPhotoPathsInSectionIntroduction =
                    presentSectionAllData.picturePathsInIntroductions
                var newPhotoPathsInSectionIntroduction = ""
                if (oldPhotoPathsInSectionIntroduction.contains(oldSectionName)) {
                    newPhotoPathsInSectionIntroduction =
                        oldPhotoPathsInSectionIntroduction.replace(oldSectionName, newSectionName)
                    updatePicturePathsInSectionIntroForThePresentSectionAllData(
                        newPhotoPathsInSectionIntroduction
                    )
                }
//                //Save the updated sectionPages into the DB
                val sectionPagesFrameworkAndDataID =
                    getPresentCompanyCode() + getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                    getThePresentSectionAllPagesFramework(),
                    getThePresentSectionAllData(),
                    sectionPagesFrameworkAndDataID
                )
            }

            //Update the Company Report and save
            updateSectionDetailsInCompanyReportAndSave(
                getPresentSectionCode(),
                newSectionName,
                getThePresentSectionAllData()
            )

            withContext(Dispatchers.Main) {
                //Ensure that the new company section list string is uploaded
                companySectionCDMLToBeUpdatedFlagMLD.value = true
                setTheEditCompletedFlagMLD(true)
            }
        }
    }

    fun makeAChildDirectory(
        companyName: String,
        companyCode: String = "",
        parentFolderDirUri: Uri,
        oldCompanyUriString: String = ""
    ) {
        var companyDirectoryUri: Uri?
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val companyDirectory =
                    DocumentFile.fromTreeUri(context, parentFolderDirUri)
                        ?.createDirectory(companyName)
                companyDirectoryUri = companyDirectory?.uri
                val companyDirectoryUriId = companyCode + MainActivity.COMPANY_DIRECTORY_URI_ID
                val aInfo5 =
                    AInfo5(companyDirectoryUriId, companyDirectoryUri.toString())
                insertAInfo5(aInfo5)
                setTheCompanyDirectoryURIString(companyDirectoryUri.toString())
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

    fun gettingCompanyDirectoryUriAndSavingIntoDB(companyName: String, parentDirUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            var companyDirectoryUri: Uri? = null
            companyDirectoryUri =
                context?.let { it1 ->
                    DocumentFile.fromTreeUri(
                        it1,
                        parentDirUri!!
                    )?.findFile(companyName)?.uri
                }
            if (companyDirectoryUri != null) {
                val companyDirectoryUriId =
                    getPresentCompanyCode() + MainActivity.COMPANY_DIRECTORY_URI_ID
                val companyDirectoryAInfo5 =
                    AInfo5(companyDirectoryUriId, companyDirectoryUri.toString())
                insertAInfo5(companyDirectoryAInfo5)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "This $companyName company directory already exists.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun renamingFilesAfterSectionNameChange(newSectionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            TODO()
        }
    }

    fun movingFilesToNewDirectory(oldCompanyDirUri: Uri, newCompanyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext


            //Move the old files to the new directory

        }
    }

    private fun fileExists(name: String, dirUri: Uri?): Boolean? {
        val context = getApplication<Application>().applicationContext
        return DocumentFile.fromTreeUri(context, dirUri!!)?.findFile(name)?.exists()
    }

    private fun createTextFileWithExtension(name: String, dirUri: Uri?, extension: String) {
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

    private fun writeToTextFile(dirUri: Uri?, fullFileName: String, content: String) {
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
                    }
                } else {
                    val file = dir?.createFile("*/txt", fullFileName)
                    if (file != null && file.canWrite()) {
                        alterDocument(
                            file.uri, content
                                .toByteArray()
                        )

                    } else {
                        Log.d("LOGTAG", "no file or cannot write")
                    }
                }
            }
        }
    }

    private fun fileNameFromURI(uri: Uri?): String {
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver
        val returnCursor = uri?.let { contentResolver.query(it, null, null, null, null) }
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        return fileName!!
    }

    fun directoryNameFromURI(uri: Uri?): String {
        val directoryURIString = ""
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver
        val returnCursor = uri?.let { contentResolver.query(it, null, null, null, null) }
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
//        val fileName = nameIndex?.let { returnCursor.getString(it) }
//        if (returnCursor != null) {
//            returnCursor.close()
//        }
//        return fileName!!
        return directoryURIString
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
                            Toast.makeText(context, "File Write is Successful!", Toast.LENGTH_SHORT)
                                .show()
                            //statusMessage.value = Event("File Write is Successful!")
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: File Write Failed!", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }

    fun deleteAFileOrADirectory(dirUri: Uri?, fullFileName: String) {
        val context = getApplication<Application>().applicationContext
        val dir = DocumentFile.fromTreeUri(context, dirUri!!)
        val fileExists = dir?.findFile(fullFileName)
        if (fileExists != null) {
            try {
                fileExists.delete()
            } catch (e: FileNotFoundException) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File does not exist in this directory", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun renameDocument(dirUri: Uri?, fullOldFileName: String, fullNewFileName: String): Uri? {
        var renamedFileUri: Uri? = null
        val context = getApplication<Application>().applicationContext
        val dir = DocumentFile.fromTreeUri(context, dirUri!!)
        val oldFileExists = dir?.findFile(fullOldFileName)
        if (oldFileExists != null) {
            oldFileExists.renameTo(fullNewFileName)
            renamedFileUri = dir.findFile(fullNewFileName)!!.uri
        }
        return renamedFileUri
    }

}
package com.example.auditapplication5

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.CodeNameAndDisplayNameDC
import com.example.auditapplication5.databinding.FragmentSimpleListRecyclerViewBinding
import com.example.auditapplication5.presentation.adapter.SimpleListRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import java.util.*


class SimpleListRecyclerViewFragment : Fragment() {
    private lateinit var binding: FragmentSimpleListRecyclerViewBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_simple_list_recycler_view,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel


        aInfo5ViewModel.setTheScreenVariable(MainActivity.SIMPLE_LIST_RV_FRAGMENT)

        //Observe and Display Status Message
//        aInfo5ViewModel.message.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled().let {
//                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
//            }
//        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_openingScreenFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT){
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_1){
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_2){
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable()== MainActivity.SECTION_FRAGMENT_DELETE_1){
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_DELETE_2){
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                    }
                }

            })


        //Getting the Parent Folder URI for saving Audits from DB
        if (aInfo5ViewModel.getTheParentFolderURIString() == "") {
            aInfo5ViewModel.getParentFolderURIStringLD.observe(viewLifecycleOwner) { list ->
                if (list.isEmpty()) {
                    aInfo5ViewModel.setTheParentFolderURIString("")
                } else {
                    var parentFolderURIString = ""
                    for (item in list) {
                        parentFolderURIString += item.framework.toString()
                    }
                    aInfo5ViewModel.setTheParentFolderURIString(parentFolderURIString)
                    val result =
                        (activity as MainActivity).areUriPermissionsGranted(parentFolderURIString)
                    if (!result) {
                        (activity as MainActivity).takePersistableURIPermissions(
                            parentFolderURIString.toUri()
                        )
                    }
                }
            }
        }

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Depending on the value of the previous screen, get the list
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
            if (aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML().isEmpty()) {
                aInfo5ViewModel.getMLOfCompanyCodesAndNamesLD.observe(viewLifecycleOwner) { list ->
                    var companyCodesAndNamesListString = ""
                    if (list.isEmpty()) {
                        companyCodesAndNamesListString = ""
                    } else {
                        companyCodesAndNamesListString = ""
                        for (item in list) {
                            companyCodesAndNamesListString += item.framework
                        }
                        aInfo5ViewModel.setTheCompanyCodeAndDisplayNameML(
                            aInfo5ViewModel.stringToCodeAndDisplayNameList(
                                companyCodesAndNamesListString
                            )
                        )
                        loadRecyclerView(
                            aInfo5ViewModel.getThePreviousScreenVariable(),
                            mutableListOf(),
                            aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML()
                        )
                    }
                }
            } else {
                loadRecyclerView(
                    aInfo5ViewModel.getThePreviousScreenVariable(),
                    mutableListOf(),
                    aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML()
                )
            }
        } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
            if (aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML().isEmpty()) {
                val companySectionListIDML =
                    mutableListOf<String>(aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID)
                aInfo5ViewModel.getAInfo5ByIds(companySectionListIDML)
                    .observe(viewLifecycleOwner) { list ->
                        var sectionCodesAndNamesListString = ""
                        if (list.isEmpty()) {
                            var defaultSectionListString = ""
                            val defaultSectionList = aInfo5ViewModel.getDefaultSectionList
                            defaultSectionList.observe(viewLifecycleOwner) { templateSectionList ->
                                if (templateSectionList.isEmpty()) {
                                    val defaultSectionListML =
                                        resources.getStringArray(R.array.Default_Section_Names)
                                            .toMutableList()
                                    defaultSectionListString =
                                        aInfo5ViewModel.mlToStringUsingDelimiter1(defaultSectionListML)
                                } else {
                                    defaultSectionListString = ""
                                    for (item in templateSectionList) {
                                        defaultSectionListString += item.template_string
                                    }
                                }
                                val sectionCodesAndNamesML =
                                    aInfo5ViewModel.mlStringToCodeAndDisplayNameListWithUniqueCodes(
                                        defaultSectionListString,
                                        MainActivity.FLAG_VALUE_SECTION
                                    )
                                val sectionCodesAndNamesMLString =
                                    aInfo5ViewModel.codeAndDisplayNameListToString(
                                        sectionCodesAndNamesML
                                    )
                                val companySectionsCDListID =
                                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                                val aInfo5 = AInfo5(
                                    companySectionsCDListID,
                                    sectionCodesAndNamesMLString
                                )
                                aInfo5ViewModel.insertAInfo5(aInfo5)
                                aInfo5ViewModel.setTheCompanySectionCodeAndDisplayNameML(
                                    sectionCodesAndNamesML
                                )
                                loadRecyclerView(
                                    aInfo5ViewModel.getThePreviousScreenVariable(),
                                    mutableListOf(),
                                    aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML()
                                )
                            }
                        } else {
                            sectionCodesAndNamesListString = ""
                            for (item in list) {
                                sectionCodesAndNamesListString += item.framework
                            }
                            aInfo5ViewModel.setTheCompanySectionCodeAndDisplayNameML(
                                aInfo5ViewModel.stringToCodeAndDisplayNameList(
                                    sectionCodesAndNamesListString
                                )
                            )
                            loadRecyclerView(
                                aInfo5ViewModel.getThePreviousScreenVariable(),
                                mutableListOf(),
                                aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML()
                            )
                        }
                    }
            }
            else {
                loadRecyclerView(
                    aInfo5ViewModel.getThePreviousScreenVariable(),
                    mutableListOf(),
                    aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML()
                )
            }
        } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_1 || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_2) {
            val editList = aInfo5ViewModel.getTheEditML()
            loadRecyclerView(aInfo5ViewModel.getThePreviousScreenVariable(), editList, mutableListOf())
        } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_DELETE_1 || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_DELETE_2) {
            val deleteML = aInfo5ViewModel.getTheDeleteML()
            loadRecyclerView(aInfo5ViewModel.getThePreviousScreenVariable(), deleteML, mutableListOf())
        }
    }


    //Functions below

    private fun loadRecyclerView(
        specificScreen: String = "",
        namesList: MutableList<String> = mutableListOf(),
        codesAndNamesML: MutableList<CodeNameAndDisplayNameDC> = mutableListOf()
    ) {
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
            binding.rvSimpleList.layoutManager = LinearLayoutManager(this.requireContext())
            binding.rvSimpleList.adapter = SimpleListRVAdapter(
                namesList,
                codesAndNamesML,
                true
            ) { selectedItemName: String, selectedItemCode: String ->
                companyListItemClicked(
                    selectedItemName,
                    selectedItemCode
                )
            }
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
            binding.rvSimpleList.setBackgroundColor(0x3FBB86FC.toInt())
            //binding.rvSimpleList.background = ResourcesCompat.getDrawable(resources, R.drawable.border_2dp, null)
            binding.rvSimpleList.layoutManager = LinearLayoutManager(this.requireContext())
            binding.rvSimpleList.adapter = SimpleListRVAdapter(
                namesList,
                codesAndNamesML, true
            ) { selectedItemName: String, selectedItemCode: String ->
                sectionListItemClicked(
                    selectedItemName,
                    selectedItemCode
                )
            }
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_1 || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_2) {
            binding.rvSimpleList.setBackgroundColor(0xFFCC99.toInt())
            binding.rvSimpleList.layoutManager = LinearLayoutManager(this.requireContext())
            binding.rvSimpleList.adapter = SimpleListRVAdapter(
                namesList,
                codesAndNamesML, false
            ) { selectedItemName: String, selectedItemCode: String ->
                editItemClicked(
                    selectedItemName,
                    selectedItemCode
                )
            }
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_DELETE_1 || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_DELETE_2) {
            binding.rvSimpleList.layoutManager = LinearLayoutManager(this.requireContext())
            binding.rvSimpleList.adapter = SimpleListRVAdapter(
                namesList,
                codesAndNamesML, false
            ) { selectedItemName: String, selectedItemCode: String ->
                deleteItemClicked(
                    selectedItemName,
                    selectedItemCode
                )
            }
        }
    }

    private fun companyListItemClicked(companyName: String, companyCode: String) {
        val presentCompanyCodeAndDisplay = CodeNameAndDisplayNameDC(companyCode, companyName)

        //Putting only the present company CodeAndDisplay into the db
        val presentCompanyID = presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
        val aInfo5PresentCompany = AInfo5(presentCompanyID, companyName)
        aInfo5ViewModel.insertAInfo5(aInfo5PresentCompany)
        aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)
        aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(true)

        aInfo5ViewModel.setThePresentCompanyCodeAndDisplayName(presentCompanyCodeAndDisplay)
        if (aInfo5ViewModel.getTheParentFolderURIString() == "") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            (activity as MainActivity).openDocumentTree(intent)
        } else {
            val dirExists =
                aInfo5ViewModel.directoryExists(
                    companyName,
                    aInfo5ViewModel.getTheParentFolderURIString().toUri()
                )
            if (dirExists == false || dirExists == null) {
                try {
                    aInfo5ViewModel.makeAChildDirectory(
                        companyName,
                        aInfo5ViewModel.getPresentCompanyCode(),
                        aInfo5ViewModel.getTheParentFolderURIString().toUri()
                    )
                } catch (e: FileSystemException) {
                    aInfo5ViewModel.statusMessage.value =
                        Event("Directory Creation Failed. Please note $e")
                }
            }
        }

        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
    }

    private fun sectionListItemClicked(sectionName: String, sectionCode: String) {
        val sectionCodeAndDisplayName = CodeNameAndDisplayNameDC(sectionCode, sectionName, true)
        aInfo5ViewModel.setSectionNameMLD(sectionName.replace("- pages present", "").trim())
        aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(sectionCodeAndDisplayName)
        val presentSectionNameID =
            aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
        val aInfo5SectionName = AInfo5(presentSectionNameID, sectionName.replace("- pages present", "").trim())
        aInfo5ViewModel.insertAInfo5(aInfo5SectionName)
        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentSectionNameID)
        aInfo5ViewModel.clearThePresentSectionAllPagesFramework()
        aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(false)
        aInfo5ViewModel.sectionAllDataLoadedFlagMLD.value = false
        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
    }

    //In this function, the code has no role to play
    private fun editItemClicked(editOptionChosen: String = "", code: String = "") {
        val editingOptionsML = resources.getStringArray(R.array.Edit_Choices_2).toMutableList()
        if (editOptionChosen == editingOptionsML[0]){
            aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)
            editCompanyName()
        } else if (editOptionChosen == editingOptionsML[1]){
            aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(true)
            editAuditDate()
        } else if (editOptionChosen == editingOptionsML[2]){
            aInfo5ViewModel.setFlagForSectionNameToBeUpdated(true)
            editSectionName()
        }
    }

    private fun deleteItemClicked(deleteOptionChosen: String = "", code: String = "") {
        val deletingOptionsML = resources.getStringArray(R.array.Delete_Choices_2).toMutableList()
        if (deleteOptionChosen == deletingOptionsML[0]){
            deleteCompany()
        } else if (deleteOptionChosen == deletingOptionsML[1]){
            deleteSection()
        }

    }

    fun editCompanyName(){
        //Move to the Enter Name Fragment to edit the company name
        val companyDirectoryUriId = aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_DIRECTORY_URI_ID
        aInfo5ViewModel.getAInfo5ByIds(mutableListOf(companyDirectoryUriId)).observe(viewLifecycleOwner){list ->
            var companyDirectoryURIString = ""
            if (list.isNotEmpty()){
                for (item in list){
                    companyDirectoryURIString += item.framework
                }
                aInfo5ViewModel.setTheCompanyDirectoryURIString(companyDirectoryURIString)
            }
        }
        aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SIMPLE_LIST_RV_FRAGMENT)
        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_enterNameFragment)
    }

    fun editAuditDate(){
        datePickerDialog()
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_1){
            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
        } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_EDIT_2){
            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
        }
        aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(true)
        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
    }

    fun editSectionName(){
        //Move to the Enter Name Fragment to edit the section name
        aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SIMPLE_LIST_RV_FRAGMENT)
        findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_enterNameFragment)
    }

    fun deleteCompany(){
        showDialogForCompanyDeletion()
    }

    fun deleteSection(){
        showDialogForSectionDeletion()

    }

    private fun showDialogForSectionDeletion() {
        val title = "Do you want to delete this section?"
        val message =
            "Press \'Yes\' to delete. Press \'No\' to just move to the Section Screen."
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                val presentSectionCode = aInfo5ViewModel.getPresentSectionCode()
                val presentCompanyAllIDS = aInfo5ViewModel.getThePresentCompanyAllIds()
                //Remove section name from Present Company Section List and store it in the DB
                aInfo5ViewModel.deleteSectionInTheCompanySectionCodeAndDisplayNameML(aInfo5ViewModel.getThePresentSectionCodeAndDisplayName())
                val companySectionListID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                aInfo5ViewModel.saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionListID)
                //Delete the section data using present company AllIDs from the Database directly
                for (item in presentCompanyAllIDS){
                    if (item.contains(presentSectionCode)){
                        //Log.d(MainActivity.TESTING_TAG, "deleteSection: Cool it ${aInfo5ViewModel.getThePreviousScreenVariable()}")
                        val aInfo5 = AInfo5(item)
                        aInfo5ViewModel.deleteAInfo5(aInfo5)
                    }
                }
                aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                dialog.dismiss()
            }
            .setNeutralButton("No") { dialog, _ ->
                aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun showDialogForCompanyDeletion() {
        val title = "Do you want to delete this company?"
        val message =
            "Press \'Yes\' to delete and move to the Opening Screen. Press \'No\' to just move to the Section Screen."
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                val presentCompanyCode = aInfo5ViewModel.getPresentCompanyCode()
                val presentCompanAllIDS = aInfo5ViewModel.getThePresentCompanyAllIds()
                //Remove Company in the List of Companies and store the remaining list
                aInfo5ViewModel.deleteCompanyInCompanyCodeAndDisplayNameML(aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName())
                aInfo5ViewModel.saveCompanyCodeAndDisplayIntoDatabase()
                //Remove items in the database
                for (item in presentCompanAllIDS){
                    if (item.contains(presentCompanyCode)){
                        Log.d(MainActivity.TESTING_TAG, "deleteCompany: $item ")
                        val aInfo5 = AInfo5(item)
                        aInfo5ViewModel.deleteAInfo5(aInfo5)
                    }
                }
                findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_openingScreenFragment)
                dialog.dismiss()
            }
            .setNeutralButton("No") { dialog, _ ->
                if (aInfo5ViewModel.getThePreviousScreenVariable()== MainActivity.SECTION_FRAGMENT_DELETE_1){
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                    aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                } else if (aInfo5ViewModel.getThePreviousScreenVariable()== MainActivity.SECTION_FRAGMENT_DELETE_2){
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                    aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                }

                findNavController().navigate(R.id.action_simpleListRecyclerViewFragment_to_sectionAndIntrosFragment)
                dialog.dismiss()
            }

        builder.create().show()
    }


    private fun datePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val myear = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datepickerdialog = DatePickerDialog(
            this@SimpleListRecyclerViewFragment.requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                var monthOfYearString = ""
                when (monthOfYear + 1) {
                    1 -> {
                        monthOfYearString = "January"
                    }
                    2 -> {
                        monthOfYearString = "February"
                    }
                    3 -> {
                        monthOfYearString = "March"
                    }
                    4 -> {
                        monthOfYearString = "April"
                    }
                    5 -> {
                        monthOfYearString = "May"
                    }
                    6 -> {
                        monthOfYearString = "June"
                    }
                    7 -> {
                        monthOfYearString = "July"
                    }
                    8 -> {
                        monthOfYearString = "August"
                    }
                    9 -> {
                        monthOfYearString = "September"
                    }
                    10 -> {
                        monthOfYearString = "October"
                    }
                    11 -> {
                        monthOfYearString = "November"
                    }
                    12 -> {
                        monthOfYearString = "December"
                    }
                }
                val currentDate = "$dayOfMonth $monthOfYearString $year"
                val dateID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                val aInfo5 = AInfo5(dateID, currentDate)
                aInfo5ViewModel.insertAInfo5(aInfo5)
                aInfo5ViewModel.setTheCompanyAuditDate(currentDate)
                aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(dateID)
            },
            myear,
            month,
            day
        )
        datepickerdialog.show()
    }


}
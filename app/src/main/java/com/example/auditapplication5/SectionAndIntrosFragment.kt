package com.example.auditapplication5

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.CompanyReportDC
import com.example.auditapplication5.databinding.FragmentSectionAndIntrosBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import java.util.*
import kotlin.collections.LinkedHashMap


class SectionAndIntrosFragment : Fragment() {
    private lateinit var binding: FragmentSectionAndIntrosBinding
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
            R.layout.fragment_section_and_intros,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        binding.aInfo5ViewModel = aInfo5ViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                        //Save the list of Present Company All Ids into the database
                        val presentCompanyAllIdsId =
                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ALL_IDs_ID
                        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyAllIdsId)
                        aInfo5ViewModel.savePresentCompanyAllIdsIntoDB(aInfo5ViewModel.getPresentCompanyCode())
                        if (aInfo5ViewModel.getTheReportsToBeGeneratedList().size > 0) {
                            showDialogToGenerateReports()
                        } else {
                            findNavController().navigate(R.id.action_sectionAndIntrosFragment_to_openingScreenFragment)
                        }
                    } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        binding.buttonChooseSection.text =
                            resources.getString(R.string.string_choose_a_section)
                        binding.buttonNewSection.visibility = View.VISIBLE
                        binding.llSectionIntroAndObservationsScreen.visibility = View.GONE
                    }
                }

            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.show()


        //Checking stuff here
//        Log.d(
//            MainActivity.TESTING_TAG,
//            "onViewCreated:Report ${aInfo5ViewModel.getTheCompanyReport().sectionReportList} "
//        )
        //Log.d(MainActivity.TESTING_TAG, "onViewCreated: ${aInfo5ViewModel.isItemPresentInPageTemplateList("PC_General_Entry_01_PC")} ")

        //Getting the URI for the company directory and loading it in the ViewModel
        aInfo5ViewModel.setTheCompanyDirectoryUriId(aInfo5ViewModel.getPresentCompanyCode())
        val companyDirectoryURIIdML =
            mutableListOf<String>(aInfo5ViewModel.getTheCompanyDirectoryUriId())
        aInfo5ViewModel.getAInfo5ByIds(companyDirectoryURIIdML)
            .observe(viewLifecycleOwner) { list ->
                var companyDirectoryURIString = ""
                if (list.isEmpty()) {
                    companyDirectoryURIString = ""
                } else {
                    for (item in list) {
                        companyDirectoryURIString += item.framework
                    }
                }
                aInfo5ViewModel.setTheCompanyDirectoryURIString(companyDirectoryURIString)
            }

        //Getting the present company All Ids List and Load it into the ViewModel
        if (aInfo5ViewModel.getThePresentCompanyAllIds().isEmpty()) {
            val presentCompanyAllIdsId =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ALL_IDs_ID
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentCompanyAllIdsId))
                .observe(viewLifecycleOwner) { allIdsList ->
                    var allIdsListString = ""
                    if (allIdsList.isNotEmpty()) {
                        for (item in allIdsList) {
                            allIdsListString += item.framework
                        }
                    } else {
                        allIdsListString = ""
                    }
                    aInfo5ViewModel.setThePresentCompanyAllIdsUsingString(allIdsListString)
                }
        }


        // Getting the company name and updating action bar.
        if (aInfo5ViewModel.getTheCompanyNameToBeUpdatedFlag() == true) {
            aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(false)
            val presentCompanyNameID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ID
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyNameID)
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentCompanyNameID))
                .observe(viewLifecycleOwner) { companyList ->
                    var presentCompanyName = ""
                    if (companyList.isNotEmpty()) {
                        for (item in companyList) {
                            presentCompanyName += item.framework
                        }
                        if (actionBar?.title?.contains(presentCompanyName) == false) {
                            actionBar.title = "Company Name: " + presentCompanyName
                        }
                    }
                }
        }
        else {
            val presentCompanyName =
                aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName().displayName
            if (actionBar?.title?.contains(presentCompanyName) == false) {
                actionBar.title = "Company Name: " + presentCompanyName
            }
        }

        // Getting the audit date and updating action bar.
        if (aInfo5ViewModel.getTheAuditDateToBeUpdatedFlag()) {
            aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(false)
            val dateID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(dateID)
            val dateValue = aInfo5ViewModel.getAInfo5ByIds(mutableListOf(dateID))
            dateValue.observe(viewLifecycleOwner) { list ->
                if (list.isEmpty()) {
                    datePickerDialog()
                } else {
                    var date = ""
                    for (item in list) {
                        date += item.framework.toString()
                    }
                    actionBar?.subtitle = "Audit Date: " + date
                    aInfo5ViewModel.setTheCompanyAuditDate(date)
                }
            }
        }
        else {
            val date = aInfo5ViewModel.getTheCompanyAuditDate()
            actionBar?.subtitle = "Audit Date: " + date
        }

        //Getting the company section List from db and if not available get it from the templates db
        //and if that is not available load in the default section list from strings.
        if (aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML().isEmpty()) {
            val companySectionListID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companySectionListID)
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(companySectionListID))
                .observe(viewLifecycleOwner) { list ->
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
                        }
                    } else {
                        var companySectionListString = ""
                        for (item in list) {
                            companySectionListString += item.framework
                        }
                        val sectionCodesAndNamesML = aInfo5ViewModel.stringToCodeAndDisplayNameList(
                            companySectionListString
                        )
                        val companySectionsCDListID =
                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                        val aInfo5 = AInfo5(
                            companySectionsCDListID,
                            companySectionListString
                        )
                        aInfo5ViewModel.insertAInfo5(aInfo5)
                        aInfo5ViewModel.setTheCompanySectionCodeAndDisplayNameML(
                            sectionCodesAndNamesML
                        )
                    }
                }
        }

        //Upload the company Photographs
        if (!aInfo5ViewModel.getTheCompanyPhotosUploadedFlag()) {
            aInfo5ViewModel.setTheCompanyPhotosUploadedFlag(true)
            val photoID = aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PHOTOS_LIST_ID
            val photoIDMutableList = mutableListOf(photoID)
            aInfo5ViewModel.getAInfo5ByIds(photoIDMutableList).observe(viewLifecycleOwner) { list ->
                if (list.isEmpty()) {
                    aInfo5ViewModel.setPhotosListInCompany(
                        aInfo5ViewModel.stringToPhotoDetailsList(
                            ""
                        )
                    )

                } else {
                    var photosListString = ""
                    for (item in list) {
                        photosListString += item.framework
                    }
                    aInfo5ViewModel.setPhotosListInCompany(
                        aInfo5ViewModel.stringToPhotoDetailsList(
                            photosListString
                        )
                    )
                }
            }

        }

        //Get The Company Report and Upload it into ViewModel
        if (aInfo5ViewModel.getTheCompanyReportUploadedFlag() == false) {
            val companyReportID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companyReportID)
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(companyReportID))
                .observe(viewLifecycleOwner) { list ->
                    var companyReportString = ""
                    if (list.isEmpty()) {
                        companyReportString = ""
                    } else {
                        companyReportString = ""
                        for (item in list) {
                            companyReportString += item.framework
                        }
                    }
                    aInfo5ViewModel.uploadTheCompanyReportIntoViewModel(companyReportString)
                }
        }

        //Set the view for screen being SectionFragmentSectionChoice
        if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
            binding.buttonChooseSection.text = aInfo5ViewModel.getPresentSectionName()
            binding.buttonNewSection.visibility = View.GONE
            binding.llSectionIntroAndObservationsScreen.visibility = View.VISIBLE
            //Put true to pagesPresent in CompanySectionCodeAndDisplayML
            aInfo5ViewModel.changePagesPresentToTrueInPresentSectionCodeAndDisplayNameList(
                aInfo5ViewModel.getPresentSectionCode()
            )
            val companySectionsCDListID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
            aInfo5ViewModel.saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionsCDListID)
            //Updating the section name in the Choose Section button
            if (aInfo5ViewModel.getFlagForSectionNameToBeUpdated() == true) {
                aInfo5ViewModel.setFlagForSectionNameToBeUpdated(false)
                val presentSectionNameID =
                    aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
                aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentSectionNameID)
                aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentSectionNameID))
                    .observe(viewLifecycleOwner) { sectionList ->
                        var presentSectionName = ""
                        if (sectionList.isNotEmpty()) {
                            for (item in sectionList) {
                                presentSectionName += item.framework
                            }
                            binding.buttonChooseSection.setText(presentSectionName)
                            aInfo5ViewModel.sectionNameMLD.value = presentSectionName
                            aInfo5ViewModel.setSectionNameMLD(presentSectionName)
                        }
                    }
            }
            //Getting the section framework and section data and loading it
            aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.observe(viewLifecycleOwner) { flagValue ->
                if (flagValue == false) {
                    val sectionPagesFrameworkAndDataID =
                        aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                    aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(
                        sectionPagesFrameworkAndDataID
                    )
                    aInfo5ViewModel.getAInfo5ByIds(mutableListOf(sectionPagesFrameworkAndDataID))
                        .observe(viewLifecycleOwner) { sectionList ->
                            var sectionAllPagesFrameworkString = ""
                            var sectionAllDataString = ""
                            if (sectionList.isEmpty()) {
                                sectionAllPagesFrameworkString = ""
                                sectionAllDataString = ""
                            } else {
                                sectionAllPagesFrameworkString = ""
                                sectionAllDataString = ""
                                for (item in sectionList) {
                                    sectionAllPagesFrameworkString += item.framework
                                    sectionAllDataString += item.data
                                }
                            }
                            aInfo5ViewModel.loadThePresentSectionAllPagesFrameworkAndAllDataUsingStrings(
                                sectionAllPagesFrameworkString,
                                sectionAllDataString
                            )
                        }
                }
                aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.removeObservers(
                    viewLifecycleOwner
                )
            }
        }
        else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
            binding.buttonChooseSection.text = resources.getString(R.string.string_choose_a_section)
            binding.buttonNewSection.visibility = View.VISIBLE
            binding.llSectionIntroAndObservationsScreen.visibility = View.GONE
        }


        //On Click Listeners Below
        binding.buttonChooseSection.setOnClickListener {
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT)
            }
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_simpleListRecyclerViewFragment)
        }

        binding.buttonEditInSectionAndIntrosFragment.setOnClickListener {
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                aInfo5ViewModel.setTheEditML(
                    resources.getStringArray(R.array.Edit_Choices_1).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_EDIT_1)
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setTheEditML(
                    resources.getStringArray(R.array.Edit_Choices_2).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_EDIT_2)
            }
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_simpleListRecyclerViewFragment)
        }

        binding.ibDelete.setOnClickListener {

            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                aInfo5ViewModel.setTheDeleteML(
                    resources.getStringArray(R.array.Delete_Choices_1).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_DELETE_1)
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setTheDeleteML(
                    resources.getStringArray(R.array.Delete_Choices_2).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_DELETE_2)
            }
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_simpleListRecyclerViewFragment)
        }

        binding.buttonCompanyIntro.setOnClickListener {
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT)
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            }
            aInfo5ViewModel.setTheWhichIntroductionsOrObservationsToBeUploadedVariable(MainActivity.COMPANY_INTRODUCTION)
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_introductionsScrollingFragment)
        }

        binding.buttonSectionIntro.setOnClickListener {
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            aInfo5ViewModel.setTheWhichIntroductionsOrObservationsToBeUploadedVariable(MainActivity.SECTION_INTRODUCTION)
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_introductionsScrollingFragment)
        }

        binding.buttonObsRecoStds.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            aInfo5ViewModel.setTheWhichIntroductionsOrObservationsToBeUploadedVariable(MainActivity.SECTION_OBSERVATIONS)
            findNavController().navigate(R.id.action_sectionAndIntrosFragment_to_observationsFragment)
        }

        binding.fabChoosingReports.setOnClickListener {
            val reportChoicesML = resources.getStringArray(R.array.Report_Choices).toMutableList()
            val linkedHashMap = aInfo5ViewModel.makeLinkedHashMapFromML(
                reportChoicesML,
                MainActivity.SECTION_FRAGMENT
            )
            chooseReportsDialog(linkedHashMap)
        }

    }


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Functions below

    private fun datePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val myear = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datepickerdialog = DatePickerDialog(
            this@SectionAndIntrosFragment.requireContext(),
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
                //aInfo5ViewModel.setCurrentDate(currentDate)
                val actionBar = (activity as MainActivity).supportActionBar
                if (actionBar != null) {
                    actionBar.subtitle = "Audit Date: " + currentDate
                }
                val dateID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                val aInfo5 = AInfo5(dateID, currentDate)
                aInfo5ViewModel.insertAInfo5(aInfo5)
                aInfo5ViewModel.setTheCompanyAuditDate(currentDate)

            },
            myear,
            month,
            day
        )
        datepickerdialog.show()
    }

    private fun chooseReportsDialog(
        multiChoiceList: LinkedHashMap<String, Boolean>
    ) {
        val nameList = mutableListOf<String>()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Choose the Report to Generate")
        builder.setMultiChoiceItems(
            multiChoiceList.keys.toTypedArray(),
            multiChoiceList.values.toBooleanArray()
        ) { dialogInterface, which, isChecked ->
            multiChoiceList.set(multiChoiceList.keys.toTypedArray().get(which), isChecked)
        }
        builder.setPositiveButton("Ok") { dialog, id ->
            for (selection in multiChoiceList) {
                if (selection.value == true) {
                    nameList.add(selection.key)
                }
            }
            aInfo5ViewModel.setTheReportsToBeGeneratedList(nameList)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDialogToGenerateReports() {
        val title = "Do you want to generate the reports?"
        val message =
            "Press \'Yes\' to generate reports and goto Main Screen. Press \'No\' to just move to Main Screen. Press \'Cancel\' to remain."
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                //Company Directory URI ID
                val dirUriString = aInfo5ViewModel.getTheCompanyDirectoryURIString()
                if (dirUriString != "") {
                    aInfo5ViewModel.generateReports(aInfo5ViewModel.getTheReportsToBeGeneratedList())
                } else {
                    //Make sure that a new folder is created here and then generate reports
                    val dirExists = aInfo5ViewModel.directoryExists(
                        aInfo5ViewModel.getPresentCompanyName(),
                        aInfo5ViewModel.getTheParentFolderURIString().toUri()
                    )
                    if (dirExists == false || dirExists == null) {
                        try {
                            aInfo5ViewModel.makeAChildDirectory(
                                aInfo5ViewModel.getPresentCompanyName(),
                                aInfo5ViewModel.getPresentCompanyCode(),
                                aInfo5ViewModel.getTheParentFolderURIString().toUri()
                            )
                        } catch (e: FileSystemException) {
                            Toast.makeText(
                                this.requireContext(),
                                "Directory Creation Failed. Please note $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    aInfo5ViewModel.generateReports(aInfo5ViewModel.getTheReportsToBeGeneratedList())
                }

                findNavController().navigate(R.id.action_sectionAndIntrosFragment_to_openingScreenFragment)
                dialog.dismiss()
            }
            .setNeutralButton("No") { dialog, _ ->
                findNavController().navigate(R.id.action_sectionAndIntrosFragment_to_openingScreenFragment)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
        builder.create().show()
    }


}
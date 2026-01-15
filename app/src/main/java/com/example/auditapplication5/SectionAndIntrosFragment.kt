package com.example.auditapplication5

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.databinding.FragmentSectionAndIntrosBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class SectionAndIntrosFragment : Fragment() {
    private lateinit var binding: FragmentSectionAndIntrosBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel
    private var autoMinimizeJob: Job? = null
    private val TEN_SECONDS = 10_000L // 10 s in ms

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val TAG = MainActivity.TESTING_TAG

        //Status Message using Shared Flow
        observeStatusMessage()

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                        if (aInfo5ViewModel.getTheReportsToBeGeneratedList().size > 0) {
                            showDialogToGenerateReports()
                        }
                        else {
                            findNavController().navigate(R.id.action_sectionAndIntrosFragment_to_openingScreenFragment)
                        }
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        binding.buttonChooseSection.text = resources.getString(R.string.string_choose_a_section)
                        binding.buttonNewSection.visibility = View.VISIBLE
                        binding.llSectionIntroAndObservationsScreen.visibility = View.GONE
                        if (aInfo5ViewModel.editCompletedFlagLD.value == false){
                            aInfo5ViewModel.setTheEditCompletedFlagMLD(true)
                        }
                        if (aInfo5ViewModel.deleteCompletedFlagLD.value == false){
                            aInfo5ViewModel.setTheDeleteCompletedFlagMLD(true)
                        }
                    }
                }
            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.show()

        //Checking stuff here

        //Check to see if the edit and delete MLDs are both true
        // and auditDate, Photos and CompanyIntroReport have been uploaded
        aInfo5ViewModel.allConditionsMetSectionAndIntrosLD.observe(viewLifecycleOwner) { allMet ->
            if (allMet == true) {
//                val companyIntroAndPhotoPathsData =
//                    "${aInfo5ViewModel.etIntroductionsMLD.value.toString()} \n\n ${aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value.toString()}"
//                aInfo5ViewModel.updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(
//                    aInfo5ViewModel.getPresentCompanyCode(),
//                    aInfo5ViewModel.getPresentCompanyName(),
//                    aInfo5ViewModel.getTheCompanyAuditDate(),
//                    companyIntroAndPhotoPathsData
//                )
                binding.pbEditDeleteMldRelated.visibility = View.GONE
                binding.tvPbMessagesSectionAndIntrosFragment.visibility = View.GONE
                binding.llSectionIntros.isEnabled = true

            }
            else {
                binding.pbEditDeleteMldRelated.visibility = View.VISIBLE
                binding.tvPbMessagesSectionAndIntrosFragment.visibility = View.VISIBLE
                if (aInfo5ViewModel.editCompletedFlagLD.value == false){
                    binding.tvPbMessagesSectionAndIntrosFragment.text = getString(R.string.string_message_edit_being_completed)
                } else if (aInfo5ViewModel.deleteCompletedFlagLD.value == false){
                    binding.tvPbMessagesSectionAndIntrosFragment.text = getString(R.string.string_message_deletion_being_completed)
                } else {
                    binding.tvPbMessagesSectionAndIntrosFragment.text = getString(R.string.string_message_records_retrieved_updated_in_database)
                }

                binding.llSectionIntros.isEnabled = false

            }
        }

        //Set the frameworkUpdatedInParentChildRVFragment variable below
        aInfo5ViewModel.setTheFrameworkUpdatedInParentChildRVFragmentFlag(false)

        // Getting the company name and updating action bar.
        aInfo5ViewModel.companyNameUploadedFlagLD.observe(viewLifecycleOwner){companyNameFlag ->
            if (companyNameFlag == false){
                if (aInfo5ViewModel.retrieveTheCompanyNameToBeUpdatedFlag()) {
                    aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(false)
                    val presentCompanyNameID =
                        aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ID
                    aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentCompanyNameID))
                        .observe(viewLifecycleOwner) { companyList ->
                            var presentCompanyName = ""
                            if (companyList.isNotEmpty()) {
                                for (item in companyList) {
                                    presentCompanyName += item.framework
                                }
                                if (actionBar?.title?.contains(presentCompanyName) == false) {
                                    actionBar.title = "Company Name: $presentCompanyName"
                                }
                            }
                            aInfo5ViewModel.setTheCompanyNameUploadedFlagMLD(true)
                        }
                }
                else {
                    val presentCompanyName =
                        aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName().displayName
                    if (actionBar?.title?.contains(presentCompanyName) == false) {
                        actionBar.title = "Company Name: $presentCompanyName"
                    }
                    aInfo5ViewModel.setTheCompanyNameUploadedFlagMLD(true)
                }
            }
        }

        // Getting the audit date and updating action bar.
        aInfo5ViewModel.companyAuditDateUploadedFlagLD.observe(viewLifecycleOwner){companyAuditNameFlag ->
            if (companyAuditNameFlag == false){
                if (aInfo5ViewModel.getTheAuditDateToBeUpdatedFlag()) {
                    aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(false)
                    val dateID =
                        aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                    val dateValue = aInfo5ViewModel.getAInfo5ByIds(mutableListOf(dateID))
                    dateValue.observe(viewLifecycleOwner) { list ->
                        if (list.isEmpty()) {
                            datePickerDialog()
                        }
                        else {
                            var date = ""
                            for (item in list) {
                                date += item.framework.toString()
                            }
                            actionBar?.subtitle = "Audit Date: $date"
                            aInfo5ViewModel.setTheCompanyAuditDate(date)
                            aInfo5ViewModel.setTheCompanyAuditDateUploadedFlagMLD(true)
                        }
                    }
                }
                else {
                    val date = aInfo5ViewModel.getTheCompanyAuditDate()
                    actionBar?.subtitle = "Audit Date: $date"
                    aInfo5ViewModel.setTheCompanyAuditDateUploadedFlagMLD(true)
                }
            }
        }

        //Get The Company Report and Upload it into ViewModel
        aInfo5ViewModel.companyReportUploadedFlagLD.observe(viewLifecycleOwner){ companyReportFlag ->
            if (companyReportFlag == false){
                val companyReportID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_REPORT_ID

                aInfo5ViewModel.getAInfo5ByIds(mutableListOf(companyReportID))
                    .observe(viewLifecycleOwner) { list ->
                        var companyReportString = ""
                        if (list.isEmpty()) {
                            companyReportString = ""
                        }
                        else {
                            companyReportString = ""
                            for (item in list) {
                                companyReportString += item.framework
                            }
                        }
                        aInfo5ViewModel.uploadTheCompanyReportIntoViewModel(
                            companyReportString
                        )
                    }
            }
        }

        //Updating in the Company Report Mediator Live Data
        aInfo5ViewModel.updateCompanyReportLD.observe(viewLifecycleOwner){
        }


        //Upload the company Photographs
        aInfo5ViewModel.companyPhotosUploadedFlagLD.observe(viewLifecycleOwner){ companyPhotoFlag ->
            if (companyPhotoFlag == false){
                val photoID = aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PHOTOS_LIST_ID
                val photoIDMutableList = mutableListOf(photoID)
                aInfo5ViewModel.getAInfo5ByIds(photoIDMutableList).observe(viewLifecycleOwner) { list ->
                    if (list.isEmpty()) {
                        aInfo5ViewModel.stringToPhotoDetailsListAndSetInViewModel("")
                    } else {
                        var photosListString = ""
                        for (item in list) {
                            photosListString += item.framework
                        }
                        aInfo5ViewModel.stringToPhotoDetailsListAndSetInViewModel(photosListString)
                    }
                }
            }

        }


        //Getting the URI for the company directory and loading it in the ViewModel
        aInfo5ViewModel.companyDirectoryURIUploadedFlagLD.observe(viewLifecycleOwner){companyURIFlag ->
            if (companyURIFlag == false){
                aInfo5ViewModel.setTheCompanyDirectoryUriId(aInfo5ViewModel.getPresentCompanyCode())
                val companyDirectoryURIIdML =
                    mutableListOf(aInfo5ViewModel.getTheCompanyDirectoryUriId())
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
                        aInfo5ViewModel.setTheCompanyDirectoryURIUploadedFlagMLD(true)
                    }
            }
        }

        //Getting the company section List from db and if not available get it from the templates db
        //and if that is not available load in the default section list from strings.
        aInfo5ViewModel.companySectionListUploadedFlagLD.observe(viewLifecycleOwner){companySectionListFlag ->
            if (companySectionListFlag == false){
                val companySectionListID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
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
                                aInfo5ViewModel.loadTheDefaultCompanySectionList(defaultSectionListString)

//                        val sectionCodesAndNamesML =
//                            aInfo5ViewModel.mlStringToCodeAndDisplayNameListWithUniqueCodes(
//                                defaultSectionListString,
//                                MainActivity.FLAG_VALUE_SECTION
//                            )
//                        val sectionCodesAndNamesMLString =
//                            aInfo5ViewModel.codeAndDisplayNameListToString(
//                                sectionCodesAndNamesML
//                            )
//                        val companySectionsCDListID =
//                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
//                        val aInfo5 = AInfo5(
//                            companySectionsCDListID,
//                            sectionCodesAndNamesMLString
//                        )
//                        aInfo5ViewModel.insertAInfo5(aInfo5)
//                        aInfo5ViewModel.setTheCompanySectionCodeAndDisplayNameML(
//                            sectionCodesAndNamesML
//                        )
//                        aInfo5ViewModel.setTheCompanySectionListUploadedFlagMLD(true)
                            }
                        } else {
                            if (aInfo5ViewModel.companySectionCDMLToBeUpdatedFlagMLD.value == true) {
                                var companySectionListString = ""
                                for (item in list) {
                                    companySectionListString += item.framework
                                }

                                aInfo5ViewModel.loadTheCompanySectionList(companySectionListString)

//                        val sectionCodesAndNamesML = aInfo5ViewModel.stringToCodeAndDisplayNameList(
//                            companySectionListString
//                        )
//                        val companySectionsCDListID =
//                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
//                        val aInfo5 = AInfo5(
//                            companySectionsCDListID,
//                            companySectionListString
//                        )
//                        aInfo5ViewModel.insertAInfo5(aInfo5)
//                        aInfo5ViewModel.setTheCompanySectionCodeAndDisplayNameML(
//                            sectionCodesAndNamesML
//                        )
//                        aInfo5ViewModel.companySectionCDMLToBeUpdatedFlagMLD.value = false
//                        aInfo5ViewModel.setTheCompanySectionListUploadedFlagMLD(true)
                            }
                        }

                    }
            }

        }

        initialiseMLDsForIntroductionsFragment()

        //Set the view for screen being SectionFragmentSectionChoice
        if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
            binding.buttonChooseSection.text = aInfo5ViewModel.getPresentSectionName()
            binding.buttonNewSection.visibility = View.GONE
            binding.llSectionIntroAndObservationsScreen.visibility = View.VISIBLE

            val presentCompanyName =
                aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName().displayName
            if (actionBar?.title?.contains(presentCompanyName) == false) {
                actionBar.title = "Company Name: $presentCompanyName"
            }
            val date = aInfo5ViewModel.getTheCompanyAuditDate()
            if (actionBar?.subtitle?.contains(date) == false){
                actionBar.subtitle = "Audit Date: $date"
            }

            //Updating the section name in the Choose Section button
            if (aInfo5ViewModel.retrieveFlagForSectionNameToBeUpdated()) {
                aInfo5ViewModel.setFlagForSectionNameToBeUpdated(false)
                val presentSectionNameID =
                    aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
                aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentSectionNameID))
                    .observe(viewLifecycleOwner) { sectionList ->
                        var presentSectionName = ""
                        if (sectionList.isNotEmpty()) {
                            for (item in sectionList) {
                                presentSectionName += item.framework
                            }
                            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                                binding.buttonChooseSection.text = presentSectionName
                            } else {
                                binding.buttonChooseSection.text = resources.getString(R.string.string_choose_a_section)
                            }
                            aInfo5ViewModel.sectionNameMLD.value = presentSectionName
                            aInfo5ViewModel.setSectionNameMLD(presentSectionName)
                        }
                    }
            }

            //Set the flag for getting Section Templates for Checklist report
            aInfo5ViewModel.setTheAllSectionTemplatesUploadedForChecklistFlagMLD(false)

            //Getting the section framework and section data and loading it
            aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.observe(viewLifecycleOwner) { frameworkFlagValue ->
                if (frameworkFlagValue == false) {
                    val sectionPagesFrameworkAndDataID =
                        aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID

                    aInfo5ViewModel.getAInfo5ByIds(mutableListOf(sectionPagesFrameworkAndDataID))
                        .observe(viewLifecycleOwner) { sectionList ->
                            var sectionAllPagesFrameworkString = ""
                            var sectionAllDataString = ""
                            if (sectionList.isEmpty()) {
                                sectionAllPagesFrameworkString = ""
                                sectionAllDataString = ""
                            }
                            else {
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
                else {
                    if (aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isNotEmpty()) {
                        aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1)
                    }
                }
                aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.removeObservers(
                    viewLifecycleOwner
                )
            }

        }
        else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
            binding.buttonChooseSection.text = resources.getString(R.string.string_choose_a_section)
            val presentCompanyName =
                aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName().displayName
            if (actionBar?.title?.contains(presentCompanyName) == false) {
                actionBar.title = "Company Name: $presentCompanyName"
            }
            val date = aInfo5ViewModel.getTheCompanyAuditDate()
            if (actionBar?.subtitle?.contains(date) == false){
                actionBar.subtitle = "Audit Date: $date"
            }
            binding.buttonNewSection.visibility = View.VISIBLE
            binding.llSectionIntroAndObservationsScreen.visibility = View.GONE
        }

        //On Click Listeners Below
        binding.buttonChooseSection.setOnClickListener {
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
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
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setTheEditML(
                    resources.getStringArray(R.array.Edit_Choices_2).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_EDIT_2)
            }
            aInfo5ViewModel.setTheEditCompletedFlagMLD(false)
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_simpleListRecyclerViewFragment)
        }

        binding.ibDelete.setOnClickListener {
            aInfo5ViewModel.setTheDeleteCompletedFlagMLD(false)
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                aInfo5ViewModel.setTheDeleteML(
                    resources.getStringArray(R.array.Delete_Choices_1).toMutableList()
                )
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_DELETE_1)
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
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
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
            }
            aInfo5ViewModel.setTheWhichIntroductionsOrObservationsToBeUploadedVariable(MainActivity.COMPANY_INTRODUCTION)
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_introductionsScrollingFragment)
        }


        var isSectionButtonsViewExpanded = false

        binding.ibToCollapseAndExpandLlSectionButtons.setOnClickListener {
            isSectionButtonsViewExpanded = !isSectionButtonsViewExpanded
            if (isSectionButtonsViewExpanded){
                expandScrollView()
                binding.hsvSectionButtons.visibility = View.VISIBLE
                binding.ibToCollapseAndExpandLlSectionButtons.setImageResource(R.drawable.ic_back_50_white)
            }
            else {
                binding.hsvSectionButtons.visibility = View.GONE
                minimizeScrollView()
                binding.ibToCollapseAndExpandLlSectionButtons.setImageResource(R.drawable.ic_forward_50_white)
            }
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

        binding.buttonReports.setOnClickListener {

            val reportChoicesML = resources.getStringArray(R.array.Report_Choices).toMutableList()
            val linkedHashMap = aInfo5ViewModel.makeLinkedHashMapFromML(
                reportChoicesML,
                MainActivity.SECTION_FRAGMENT,
                aInfo5ViewModel.getTheReportsToBeGeneratedList()
            )
            chooseReportsDialog(linkedHashMap)
        }

        binding.buttonNewSection.setOnClickListener {
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.SECTION_FRAGMENT)
            it.findNavController()
                .navigate(R.id.action_sectionAndIntrosFragment_to_enterNameFragment)
        }

    }


    override fun onStop() {
        super.onStop()

    }

    //Functions below
    private fun observeStatusMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aInfo5ViewModel.statusMessageFlow.collect { message ->
                    // Show Toast, Snackbar, or dialog - executes exactly once
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // When expanding the ScrollView (in your existing click handler)
    private fun expandScrollView() {
        binding.hsvSectionButtons.visibility = View.VISIBLE
        binding.ibToCollapseAndExpandLlSectionButtons.setImageResource(R.drawable.ic_back_50_white)
        // Cancel any existing timer
        autoMinimizeJob?.cancel()
        // Start new 2-minute timer
        autoMinimizeJob = lifecycleScope.launch {
            delay(TEN_SECONDS)
            if (binding.hsvSectionButtons.visibility == View.VISIBLE) {
                minimizeScrollView() // Your existing minimize function
            }
        }
    }

    // When manually minimizing (in your existing click handler)
    private fun minimizeScrollView() {
        binding.hsvSectionButtons.visibility = View.GONE
        binding.ibToCollapseAndExpandLlSectionButtons.setImageResource(R.drawable.ic_forward_50_white)
        autoMinimizeJob?.cancel() // Stop timer
    }

    private fun initialiseMLDsForIntroductionsFragment(){
        aInfo5ViewModel.setTheCompanyNameUploadedIFFlagMLD(false)
        aInfo5ViewModel.setThecompanyIntroductionUploadedIFFlagMLD(false)
        aInfo5ViewModel.setTheSectionNameUploadedIFFlagMLD(false)
        //aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(false)
    }

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
                    actionBar.subtitle = "Audit Date: $currentDate"
                }
                val dateID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                val aInfo5 = AInfo5(dateID, currentDate)
                aInfo5ViewModel.insertAInfo5(aInfo5)
                aInfo5ViewModel.setTheCompanyAuditDate(currentDate)
                aInfo5ViewModel.setTheCompanyAuditDateUploadedFlagMLD(true)
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
            multiChoiceList[multiChoiceList.keys.toTypedArray().get(which)] = isChecked
        }
        builder.setPositiveButton("Ok") { dialog, id ->
            for (selection in multiChoiceList) {
                if (selection.value) {
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
                    if (aInfo5ViewModel.getTheCompanyReport().companyAuditDate == "") {
                        aInfo5ViewModel.updateTheCompanyNameAuditDateAndIntroInCompanyReportAndSave(
                            aInfo5ViewModel.getPresentCompanyCode(),
                            aInfo5ViewModel.getPresentCompanyName(),
                            "",
                            aInfo5ViewModel.getTheCompanyReport().companyIntroduction
                        )
                    }

                    aInfo5ViewModel.generateReports(aInfo5ViewModel.getTheReportsToBeGeneratedList())
                }
                else {
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
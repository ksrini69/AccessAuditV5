package com.example.auditapplication5

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentEnterNameBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class EnterNameFragment : Fragment() {
    private lateinit var binding: FragmentEnterNameBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel
    private lateinit var parentFolderURIString: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_enter_name, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        val TAG = MainActivity.TESTING_TAG

        //Status Message using Shared Flow
        observeStatusMessage()

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
                        findNavController().navigate(R.id.action_enterNameFragment_to_openingScreenFragment)
                    }
                    else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    }
                    else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT) {
                        if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_EDIT_1) {
                            if (aInfo5ViewModel.editCompletedFlagLD.value == false){
                                aInfo5ViewModel.setTheEditCompletedFlagMLD(true)
                            }
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        }
                        else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_EDIT_2) {
                            if (aInfo5ViewModel.editCompletedFlagLD.value == false){
                                aInfo5ViewModel.setTheEditCompletedFlagMLD(true)
                            }
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        }
                        else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_DELETE_1) {
                            if (aInfo5ViewModel.deleteCompletedFlagLD.value == false){
                                aInfo5ViewModel.setTheDeleteCompletedFlagMLD(true)
                            }
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        }
                        else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_DELETE_2) {
                            if (aInfo5ViewModel.deleteCompletedFlagLD.value == false){
                                aInfo5ViewModel.setTheDeleteCompletedFlagMLD(true)
                            }
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        }
                        findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    }
                }
            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        aInfo5ViewModel.allConditionsMetEnterNameLD.observe(viewLifecycleOwner){allMet ->
            if (allMet == true){
                binding.pbEnterNameFragment.visibility = View.GONE
                binding.tvPbMessagesOpeningScreenFragment.visibility = View.GONE
                binding.llLayoutContainerEnterNameFragment.isEnabled = true
            }
            else {
                binding.pbEnterNameFragment.visibility = View.VISIBLE
                binding.tvPbMessagesOpeningScreenFragment.visibility = View.VISIBLE
                binding.tvPbMessagesOpeningScreenFragment.text = getString(R.string.string_message_loading_from_db)
                binding.llLayoutContainerEnterNameFragment.isEnabled = false
            }
        }

        //Getting the list of companies from the db and storing in ViewModel
        if (aInfo5ViewModel.companyNameListUploadedENFFlagLD.value == false){
            aInfo5ViewModel.getMLOfCompanyCodesAndNamesLD.observe(viewLifecycleOwner) { list ->
                var companyCodesAndNamesListString = ""
                if (list.isEmpty()) {
                    aInfo5ViewModel.setTheCompanyCodeAndDisplayNameML(mutableListOf())
                } else {
                    companyCodesAndNamesListString = ""
                    for (item in list) {
                        companyCodesAndNamesListString += item.framework
                    }
                    val companyCodesAndNamesML = aInfo5ViewModel.stringToCodeAndDisplayNameList(
                        companyCodesAndNamesListString
                    )
                    aInfo5ViewModel.setTheCompanyCodeAndDisplayNameML(companyCodesAndNamesML)
                }
                aInfo5ViewModel.setTheCompanyNameListUploadedENFFlagMLD(true)
            }
        }


        //Getting the Parent Folder for saving Audits from DB
        var parentFolderURIString = aInfo5ViewModel.getTheParentFolderURIString()
        if (parentFolderURIString == ""){
            if (aInfo5ViewModel.parentFolderURIUploadedENFFlagLD.value == false){
                aInfo5ViewModel.getParentFolderURIStringLD.observe(viewLifecycleOwner) { list ->
                    if (list.isEmpty()) {
                        aInfo5ViewModel.setTheParentFolderURIString("")
                        aInfo5ViewModel.setTheparentFolderURIUploadedENFFlagMLD(true)
                    }
                    else {
                        parentFolderURIString = ""
                        for (item in list) {
                            parentFolderURIString += item.framework.toString()
                        }
                        aInfo5ViewModel.setTheParentFolderURIString(parentFolderURIString)
                        val result =
                            (activity as MainActivity).haveAllUriPermissionsBeenGranted(parentFolderURIString)
                        if (!result) {
                            (activity as MainActivity).takePersistableURIPermissions(
                                parentFolderURIString.toUri()
                            )
                        }
                        aInfo5ViewModel.setTheparentFolderURIUploadedENFFlagMLD(true)
                    }
                }
            }
        }
        else {
            aInfo5ViewModel.setTheparentFolderURIUploadedENFFlagMLD(true)
        }


        //Putting the name of Company or Section in the Edit Text
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT) {
            if (aInfo5ViewModel.retrieveTheCompanyNameToBeUpdatedFlag()) {
                val presentCompanyNameID =
                    aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ID
                aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentCompanyNameID))
                    .observe(viewLifecycleOwner) { companyList ->
                        var presentCompanyName = ""
                        if (companyList.isNotEmpty()) {
                            for (item in companyList) {
                                presentCompanyName += item.framework
                            }
                            binding.etEnterName.setText(presentCompanyName)
                        }
                    }

            }
            else if (aInfo5ViewModel.retrieveFlagForSectionNameToBeUpdated()) {
                val presentSectionNameID =
                    aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
                aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentSectionNameID))
                    .observe(viewLifecycleOwner) { sectionList ->
                        var presentSectionName = ""
                        if (sectionList.isNotEmpty()) {
                            for (item in sectionList) {
                                presentSectionName += item.framework
                            }
                            binding.etEnterName.setText(presentSectionName)
                        }
                    }
            }
        }


        //On Click Listeners
        binding.buttonContinue.setOnClickListener {
            if (binding.etEnterName.text.isNotEmpty()) {
                if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
                    val companyName = binding.etEnterName.text.toString().trim()
                    if (aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML().isEmpty()) {
                        val presentCompanyCodeAndDisplay =
                            aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML(),
                                companyName,
                                MainActivity.FLAG_VALUE_COMPANY
                            )
                        aInfo5ViewModel.setThePresentCompanyCodeAndDisplayName(
                            presentCompanyCodeAndDisplay
                        )

                        //Putting only the present company display name into the db
                        val presentCompanyNameID =
                            presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
                        val aInfo5PresentCompany = AInfo5(presentCompanyNameID, companyName)
                        aInfo5ViewModel.insertAInfo5(aInfo5PresentCompany)
                        aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)

                        //Putting the ML of CompanyCodeAndDisplay into the db
                        aInfo5ViewModel.addToCompanyCodeAndDisplayNameML(
                            presentCompanyCodeAndDisplay
                        )
                        val companyCodeAndDisplayNameMLString =
                            aInfo5ViewModel.codeAndDisplayNameListToString(aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML())
                        val aInfo5 = AInfo5(
                            MainActivity.COMPANY_CODES_NAMES_ID,
                            companyCodeAndDisplayNameMLString
                        )
                        aInfo5ViewModel.insertAInfo5(aInfo5)

                        val time = Calendar.getInstance().time
                        val formatter = SimpleDateFormat("dd-MMMM-yyyy", Locale.UK)
                        val currentDate = formatter.format(time)
                        val dateID =
                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                        val aInfo5Date = AInfo5(dateID, currentDate.toString())
                        aInfo5ViewModel.insertAInfo5(aInfo5Date)
                        aInfo5ViewModel.setTheCompanyAuditDate(currentDate.toString())
                        aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(true)

                        val dirExists = aInfo5ViewModel.directoryExists(
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
                                aInfo5ViewModel.setStatusMessageFlow("Directory Creation Failed. Please note $e")
//                                Toast.makeText(
//                                    this.requireContext(),
//                                    "Directory Creation Failed. Please note $e",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        }
                        else {
                            try {
                                aInfo5ViewModel.gettingCompanyDirectoryUriAndSavingIntoDB(companyName,aInfo5ViewModel.getTheParentFolderURIString().toUri() )
                            } catch (e: FileSystemException){
                                aInfo5ViewModel.setStatusMessageFlow("Getting this $companyName directory Uri has failed. Please note $e")
                                //Toast.makeText(context, "Getting this $companyName directory Uri has failed. Please note $e", Toast.LENGTH_SHORT).show()
                            }

                        }
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)

                        it.findNavController()
                            .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    }
                    else {
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML(),
                                companyName
                            )
                        ) {
                            val presentCompanyCodeAndDisplay =
                                aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                    aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML(),
                                    companyName,
                                    MainActivity.FLAG_VALUE_COMPANY
                                )
                            aInfo5ViewModel.setThePresentCompanyCodeAndDisplayName(
                                presentCompanyCodeAndDisplay
                            )

                            //Putting the present company display name into the db
                            val presentCompanyID =
                                presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
                            val aInfo5PresentCompany = AInfo5(presentCompanyID, companyName)
                            aInfo5ViewModel.insertAInfo5(aInfo5PresentCompany)
                            aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)

                            aInfo5ViewModel.addToCompanyCodeAndDisplayNameML(
                                presentCompanyCodeAndDisplay
                            )
                            val companyCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayNameListToString(aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML())
                            val aInfo5 = AInfo5(
                                MainActivity.COMPANY_CODES_NAMES_ID,
                                companyCodeAndDisplayNameMLString
                            )
                            aInfo5ViewModel.insertAInfo5(aInfo5)
                            val time = Calendar.getInstance().time
                            val formatter = SimpleDateFormat("dd-MMMM-yyyy", Locale.UK)
                            val currentDate = formatter.format(time)
                            val dateID =
                                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_AUDIT_DATE_ID
                            val aInfo5Date = AInfo5(dateID, currentDate.toString())
                            aInfo5ViewModel.insertAInfo5(aInfo5Date)
                            aInfo5ViewModel.setTheAuditDateToBeUpdatedFlag(true)

                            val dirExists = aInfo5ViewModel.directoryExists(
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
                                    aInfo5ViewModel.setStatusMessageFlow("Directory Creation Has Failed. Please note $e")
//                                    Toast.makeText(
//                                        this.requireContext(),
//                                        "Directory Creation Has Failed. Please note $e",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                }
                            } else {
                                try {
                                    aInfo5ViewModel.gettingCompanyDirectoryUriAndSavingIntoDB(companyName,aInfo5ViewModel.getTheParentFolderURIString().toUri() )
                                } catch (e: FileSystemException){
                                    aInfo5ViewModel.setStatusMessageFlow("Getting this $companyName directory Uri has failed. Please note $e")
                                    //Toast.makeText(context, "Getting this $companyName directory Uri has failed. Please note $e", Toast.LENGTH_SHORT).show()
                                }
                            }
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)

                            it.findNavController()
                                .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        } else {
                            aInfo5ViewModel.setStatusMessageFlow("This company name exists. Please enter a unique name.")
//                            Toast.makeText(
//                                this.requireContext(),
//                                "This company name exists. Please enter a unique name.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                    val sectionName = binding.etEnterName.text.toString().trim()
                    if (aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML().isEmpty()) {
                        val presentSectionCodeAndDisplay =
                            aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML(),
                                sectionName,
                                MainActivity.FLAG_VALUE_SECTION
                            )
                        aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(
                            presentSectionCodeAndDisplay
                        )
                        aInfo5ViewModel.addToTheCompanySectionCodeAndDisplayNameML(
                            presentSectionCodeAndDisplay
                        )
                        val sectionCodeAndDisplayNameMLString =
                            aInfo5ViewModel.codeAndDisplayNameListToString(aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML())
                        val companySectionListID =
                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                        val aInfo5 = AInfo5(companySectionListID, sectionCodeAndDisplayNameMLString)
                        aInfo5ViewModel.insertAInfo5(aInfo5)

                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        it.findNavController()
                            .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    }
                    else {
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML(),
                                sectionName
                            )
                        ) {
                            val presentSectionCodeAndDisplay =
                                aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                    aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML(),
                                    sectionName,
                                    MainActivity.FLAG_VALUE_SECTION
                                )
                            aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(
                                presentSectionCodeAndDisplay
                            )
                            aInfo5ViewModel.addToTheCompanySectionCodeAndDisplayNameML(
                                presentSectionCodeAndDisplay
                            )
                            val sectionCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayNameListToString(aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML())
                            val companySectionListID =
                                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                            val aInfo5 =
                                AInfo5(companySectionListID, sectionCodeAndDisplayNameMLString)
                            aInfo5ViewModel.insertAInfo5(aInfo5)

                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            it.findNavController()
                                .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        }
                        else {
                            aInfo5ViewModel.setStatusMessageFlow("This section exists")
                            Toast.makeText(
                                this.requireContext(),
                                "This section exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT) {
                    if (aInfo5ViewModel.retrieveTheCompanyNameToBeUpdatedFlag()) {
                        val newCompanyName = binding.etEnterName.text.toString().trim()
                        //Check to see if this name is unique
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML(),
                                newCompanyName
                            )
                        ) {
                            aInfo5ViewModel.editCompanyNameFunction(newCompanyName)

                            //Move to the SectionAndIntrosFragment
                            if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_EDIT_1) {
                                aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                                aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                            } else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_EDIT_2) {
                                aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                                aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                            }
                            findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        }
                        else {
                            aInfo5ViewModel.setStatusMessageFlow("This company name exists. Please enter a unique name.")
//                            Toast.makeText(
//                                this.requireContext(),
//                                "This company name exists. Please enter a unique name.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }
                    else if (aInfo5ViewModel.retrieveFlagForSectionNameToBeUpdated()) {
                        val newSectionName = binding.etEnterName.text.toString().trim()

                        //Checking to see if this section name is unique
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheCompanySectionCodeAndDisplayNameML(),
                                newSectionName
                            )
                        ) {
                            aInfo5ViewModel.editSectionNameFunction(newSectionName)
                            //Move to the Section Fragment
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                            findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        } else {
                            aInfo5ViewModel.setStatusMessageFlow("This section exists. Please enter a unique name.")
//                            Toast.makeText(
//                                this.requireContext(),
//                                "This section exists. Please enter a unique name.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }

                    }
                }
            }
            else {
                showDialogForEmptyEditText()
            }
        }
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

    private fun showDialogForEmptyEditText() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("The field cannot be blank")
            .setMessage("Please enter a value or press the back button to go back")
            .setNeutralButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

}
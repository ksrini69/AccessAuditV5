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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.CodeNameAndDisplayNameDC
import com.example.auditapplication5.databinding.FragmentEnterNameBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import java.text.SimpleDateFormat
import java.util.*


class EnterNameFragment : Fragment() {
    private lateinit var binding: FragmentEnterNameBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel
    private lateinit var parentFolderURIString: String
    var companyCodesAndNamesML = mutableListOf<CodeNameAndDisplayNameDC>()
    private lateinit var companyCodesAndNamesListString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_enter_name, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT) {
                        findNavController().navigate(R.id.action_enterNameFragment_to_openingScreenFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    } else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT){
                        if (aInfo5ViewModel.getThePreviousScreen2Variable() ==  MainActivity.SECTION_FRAGMENT_EDIT_1){
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        } else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_EDIT_2){
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        } else if (aInfo5ViewModel.getThePreviousScreen2Variable()== MainActivity.SECTION_FRAGMENT_DELETE_1){
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                        } else if (aInfo5ViewModel.getThePreviousScreen2Variable() == MainActivity.SECTION_FRAGMENT_DELETE_2){
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

        //Observe and Display Status Message
        aInfo5ViewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled().let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        //Getting the list of companies from the db and storing in ViewModel
        aInfo5ViewModel.getMLOfCompanyCodesAndNamesLD.observe(viewLifecycleOwner) { list ->
            var companyCodesAndNamesListString = ""
            if (list.isEmpty()) {
                aInfo5ViewModel.setTheCompanyCodeAndDisplayNameML(mutableListOf())
            } else {
                companyCodesAndNamesListString = ""
                for (item in list) {
                    companyCodesAndNamesListString += item.framework
                }
                val companyCodesAndNamesML = aInfo5ViewModel.stringToCodeAndDisplayCollection(
                    companyCodesAndNamesListString,
                    MainActivity.FLAG_VALUE_COMPANY
                )
                aInfo5ViewModel.setTheCompanyCodeAndDisplayNameML(companyCodesAndNamesML)
            }
        }

        //Getting the Parent Folder for saving Audits from DB
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


        //Setting the edit functionality for Company and Section
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT) {
            if (aInfo5ViewModel.getTheCompanyNameToBeUpdatedFlag() == true) {
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
                aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyNameID)
            } else if (aInfo5ViewModel.getFlagForSectionNameToBeUpdated() == true) {
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
                aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentSectionNameID)
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

                        //Setting up the Present Company All Ids first term in the ViewModel
                        val presentCompanyAllIdsId = aInfo5ViewModel.getPresentCompanyCode() + MainActivity.PRESENT_COMPANY_ALL_IDs_ID
                        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyAllIdsId)


                        //Putting only the present company display name into the db
                        val presentCompanyID =
                            presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
                        val aInfo5PresentCompany = AInfo5(presentCompanyID, companyName)
                        aInfo5ViewModel.insertAInfo5(aInfo5PresentCompany)
                        aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)
                        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyID)

                        //Putting the ML of CompanyCodeAndDisplay into the db
                        aInfo5ViewModel.addToCompanyCodeAndDisplayNameML(
                            presentCompanyCodeAndDisplay
                        )
                        val companyCodeAndDisplayNameMLString =
                            aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML())
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
                        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(dateID)

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
                                Toast.makeText(
                                    this.requireContext(),
                                    "Directory Creation Failed. Please note $e",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        aInfo5ViewModel.savePresentCompanyAllIdsIntoDB(aInfo5ViewModel.getPresentCompanyCode())
                        it.findNavController()
                            .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    } else {
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
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyID)

                            aInfo5ViewModel.addToCompanyCodeAndDisplayNameML(
                                presentCompanyCodeAndDisplay
                            )
                            val companyCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML())
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
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(dateID)

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
                                    Toast.makeText(
                                        this.requireContext(),
                                        "Directory Creation Failed. Please note $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            aInfo5ViewModel.savePresentCompanyAllIdsIntoDB(aInfo5ViewModel.getPresentCompanyCode())
                            it.findNavController()
                                .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        } else {
                            Toast.makeText(this.requireContext(), "This company name exists. Please enter a unique name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT) {
                    val sectionName = binding.etEnterName.text.toString().trim()
                    if (aInfo5ViewModel.getTheSectionCodeAndDisplayNameML().isEmpty()) {
                        val presentSectionCodeAndDisplay =
                            aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                aInfo5ViewModel.getTheSectionCodeAndDisplayNameML(),
                                sectionName,
                                MainActivity.FLAG_VALUE_SECTION
                            )
                        aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(
                            presentSectionCodeAndDisplay
                        )
                        aInfo5ViewModel.addToSectionCodeAndDisplayNameML(
                            presentSectionCodeAndDisplay
                        )
                        val sectionCodeAndDisplayNameMLString =
                            aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheSectionCodeAndDisplayNameML())
                        val companySectionListID =
                            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                        val aInfo5 = AInfo5(companySectionListID, sectionCodeAndDisplayNameMLString)
                        aInfo5ViewModel.insertAInfo5(aInfo5)
                        aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companySectionListID)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                        it.findNavController()
                            .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                    } else {
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheSectionCodeAndDisplayNameML(),
                                sectionName
                            )
                        ) {
                            val presentSectionCodeAndDisplay =
                                aInfo5ViewModel.generateUniqueCodeFromCDCollection(
                                    aInfo5ViewModel.getTheSectionCodeAndDisplayNameML(),
                                    sectionName,
                                    MainActivity.FLAG_VALUE_SECTION
                                )
                            aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(
                                presentSectionCodeAndDisplay
                            )
                            aInfo5ViewModel.addToSectionCodeAndDisplayNameML(
                                presentSectionCodeAndDisplay
                            )
                            val sectionCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheSectionCodeAndDisplayNameML())
                            val companySectionListID =
                                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                            val aInfo5 =
                                AInfo5(companySectionListID, sectionCodeAndDisplayNameMLString)
                            aInfo5ViewModel.insertAInfo5(aInfo5)
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companySectionListID)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                            it.findNavController()
                                .navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        } else {
                            Toast.makeText(this.requireContext(), "This section exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SIMPLE_LIST_RV_FRAGMENT) {
                    if (aInfo5ViewModel.getTheCompanyNameToBeUpdatedFlag() == true) {
                        val newCompanyName = binding.etEnterName.text.toString().trim()
                        //Check to see if this name is unique
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML(),
                                newCompanyName
                            )
                        ) {
                            val presentCompanyCodeAndDisplay =
                                aInfo5ViewModel.getThePresentCompanyCodeAndDisplayName()
                            presentCompanyCodeAndDisplay.displayName = newCompanyName
                            aInfo5ViewModel.setThePresentCompanyCodeAndDisplayName(
                                presentCompanyCodeAndDisplay
                            )
                            //Putting only the present company display name into the db
                            val presentCompanyID =
                                presentCompanyCodeAndDisplay.uniqueCodeName + MainActivity.PRESENT_COMPANY_ID
                            val aInfo5NewCompanyName = AInfo5(presentCompanyID, newCompanyName)
                            aInfo5ViewModel.insertAInfo5(aInfo5NewCompanyName)
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyID)
                            aInfo5ViewModel.setTheCompanyNameToBeUpdatedFlag(true)
                            //update the Company related ML
                            aInfo5ViewModel.modifyDisplayNameOfSpecificCompanyInML(
                                newCompanyName,
                                presentCompanyCodeAndDisplay.uniqueCodeName
                            )
                            //Save the above in the DB
                            val companyCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheCompanyCodeAndDisplayNameML())
                            val aInfo5 = AInfo5(
                                MainActivity.COMPANY_CODES_NAMES_ID,
                                companyCodeAndDisplayNameMLString
                            )
                            aInfo5ViewModel.insertAInfo5(aInfo5)
                            //Reconstructing the company directory and moving files
//                            aInfo5ViewModel.remakeCompanyDirectoryAndMovingFiles(
//                                aInfo5ViewModel.getCompanyDirectoryURIString_A().toUri(),
//                                newCompanyName
//                            )
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
                        } else {
                            Toast.makeText(this.requireContext(), "This company name exists. Please enter a unique name.", Toast.LENGTH_SHORT).show()
                        }
                    } else if (aInfo5ViewModel.getFlagForSectionNameToBeUpdated() == true) {
                        val newSectionName = binding.etEnterName.text.toString().trim()
                        //Checking to see if this section name is unique
                        if (aInfo5ViewModel.uniquenessCheckInCodesAndNames(
                                aInfo5ViewModel.getTheSectionCodeAndDisplayNameML(),
                                newSectionName
                            )
                        ) {
                            //Update the present section codeAndDisplay in ViewModel
                            val presentSectionCodeAndDisplay =
                                aInfo5ViewModel.getThePresentSectionCodeAndDisplayName()
                            presentSectionCodeAndDisplay.displayName = newSectionName
                            aInfo5ViewModel.setThePresentSectionCodeAndDisplayName(
                                presentSectionCodeAndDisplay
                            )
                            //Saving only the section display Name into the db
                            val presentSectionNameID =
                                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
                            val aInfo5SectionName = AInfo5(presentSectionNameID, newSectionName)
                            aInfo5ViewModel.insertAInfo5(aInfo5SectionName)
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentSectionNameID)
                            aInfo5ViewModel.setFlagForSectionNameToBeUpdated(true)
                            //update the Section related ML
                            aInfo5ViewModel.modifyDisplayNameOfSpecificSectionInML(
                                newSectionName,
                                presentSectionCodeAndDisplay.uniqueCodeName
                            )
                            //Save the above in the DB
                            val sectionCodeAndDisplayNameMLString =
                                aInfo5ViewModel.codeAndDisplayCollectionToString(aInfo5ViewModel.getTheSectionCodeAndDisplayNameML())
                            val companySectionsCDListID =
                                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                            val aInfo5 = AInfo5(
                                companySectionsCDListID,
                                sectionCodeAndDisplayNameMLString
                            )
                            aInfo5ViewModel.insertAInfo5(aInfo5)
                            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companySectionsCDListID)
                            //Reconstructing the company directory and moving files
//                            aInfo5ViewModel.renamingFilesAfterSectionNameChange(
//                                newSectionName
//                            )
                            //Move to the Section Fragment
                            aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                            findNavController().navigate(R.id.action_enterNameFragment_to_sectionAndIntrosFragment)
                        } else {
                            Toast.makeText(this.requireContext(), "This section exists. Please enter a unique name.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            } else {
                showDialogForEmptyEditText()
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Functions below

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
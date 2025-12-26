package com.example.auditapplication5

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentOpeningScreenBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class OpeningScreenFragment : Fragment() {
    private lateinit var binding: FragmentOpeningScreenBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_opening_screen,
            container,
            false
        )
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
                    if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT){
                        this@OpeningScreenFragment.onDestroy()
                        exitProcess(0)
                    } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OPENING_SCREEN_FRAGMENT_DB_DELETION){
                        binding.llPasswordDatabaseDeletion.visibility = View.GONE
                        binding.llOpeningScreen.visibility = View.VISIBLE
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
                    }

                }

            })

        //Make the password dialog for deleting the db disappear
        binding.llPasswordDatabaseDeletion.visibility = View.GONE

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = resources.getString(R.string.string_application_name_on_action_bar)


        //set Screen Values
        aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
        aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)

        //Initialise some variables from the ViewModel
        initialiseSomeViewModelVariablesHere()


//    //Ensuring that the list of templates loaded contains the Default template
//        if (!aInfo5ViewModel.isItemPresentInPageTemplateList(aInfo5ViewModel.getTheDefaultPageTemplate().pageCode)){
//            aInfo5ViewModel.addUniquePageToPageTemplateList(aInfo5ViewModel.getTheDefaultPageTemplate())
//        }


        //On Click Listeners Below

        binding.buttonNewAudit.setOnClickListener {
            aInfo5ViewModel.setTheFileFlag(MainActivity.DIRECTORY)
            if (aInfo5ViewModel.getTheParentFolderURIString() == "") {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                (activity as MainActivity).openDocumentTree(intent)
            }
            else {
                val title = resources.getString(R.string.string_new_parent_folder)
                val message = resources.getString(R.string.string_message_for_new_parent_folder)
                showDialog(title, message)
            }
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            it.findNavController().navigate(R.id.action_openingScreenFragment_to_enterNameFragment)
        }

        binding.buttonEditDeleteAudits.setOnClickListener {
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            it.findNavController()
                .navigate(R.id.action_openingScreenFragment_to_simpleListRecyclerViewFragment)
        }

        binding.fabAddingTemplateFiles.setOnClickListener {
            val templatesLoadedFlag = aInfo5ViewModel.getTheTemplatesHaveBeenLoadedIntoDBFlag()
            if (templatesLoadedFlag) {
                showDialogForNewTemplateUpload()
            } else {
                aInfo5ViewModel.setTheFileFlag(MainActivity.TEMPLATE_DOCUMENT_LOAD)
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
                (activity as MainActivity).openDocumentTree(intent)
            }
        }

        binding.fabDeletingDatabaseL.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT_DB_DELETION)
            binding.etPasswordDatabaseDeletion.setText("")
            binding.llPasswordDatabaseDeletion.visibility = View.VISIBLE
            binding.llOpeningScreen.visibility = View.GONE
            binding.buttonSubmitDatabaseDeletion.setOnClickListener {
                if (binding.etPasswordDatabaseDeletion.text.isNotEmpty()) {
                    val passwd = binding.etPasswordDatabaseDeletion.text.toString()
                    if (passwd == resources.getString(R.string.string_deletion_password)) {
                        aInfo5ViewModel.setTheFileFlag(MainActivity.AUDIT_DATABASE_DELETE)
                        if (aInfo5ViewModel.getTheFileFlag() == MainActivity.AUDIT_DATABASE_DELETE) {
                            val auditDeleteTitle =
                                resources.getString(R.string.string_deleting_audit_records_db)
                            val auditDeleteMessage =
                                resources.getString(R.string.string_message_for_deleting_audit_records_db)
                            showDialog(auditDeleteTitle, auditDeleteMessage)
                        }
                    } else {
                        binding.llPasswordDatabaseDeletion.visibility = View.GONE
                        binding.llOpeningScreen.visibility = View.VISIBLE
                        aInfo5ViewModel.setStatusMessageFlow("The password entered is incorrect")
                        //Toast.makeText(requireContext(),"The password entered is incorrect", Toast.LENGTH_SHORT).show()
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
                    }
                } else {
                    binding.llPasswordDatabaseDeletion.visibility = View.GONE
                    binding.llOpeningScreen.visibility = View.VISIBLE
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
                }
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

    private fun initialiseSomeViewModelVariablesHere(){

        aInfo5ViewModel.clearTheCompanySectionCodeAndDisplayML()
        aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(false)
        aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(false)
        aInfo5ViewModel.setTheAllSectionTemplatesUploadedForChecklistFlagMLD(false)

        aInfo5ViewModel.setTheCompanyReportUploadedFlagMLD(false)
        aInfo5ViewModel.setTheCompanyPhotosUploadedFlagMLD(false)
        aInfo5ViewModel.setTheCompanyDirectoryURIUploadedFlagMLD(false)
        aInfo5ViewModel.setTheCompanySectionListUploadedFlagMLD(false)
        aInfo5ViewModel.setTheCompanyNameUploadedFlagMLD(false)
        aInfo5ViewModel.setTheCompanyAuditDateUploadedFlagMLD(false)

        aInfo5ViewModel.setTheCompanyNameListUploadedENFFlagMLD(false)
        aInfo5ViewModel.setTheparentFolderURIUploadedENFFlagMLD(false)

        aInfo5ViewModel.setTheCompanyNameUploadedIFFlagMLD(false)
        aInfo5ViewModel.setThecompanyIntroductionUploadedIFFlagMLD(false)
        aInfo5ViewModel.setTheSectionNameUploadedIFFlagMLD(false)

        aInfo5ViewModel.setTheCompanyIntroUpdatedInReportSIFFlagMLD(true)
        aInfo5ViewModel.setTheSectionIntroUpdatedInReportSIFFlagMLD(true)
        aInfo5ViewModel.setTheSectionPagesUpdatedInReportSIFFlagMLD(true)

        aInfo5ViewModel.setThePictureUploadedCXFFlagMLD(true)
        aInfo5ViewModel.setTheVideoUploadedCXFFlagMLD(true)

        //Generate and load the Default Reports List
        val reportsList = resources.getStringArray(R.array.Report_Choices).toMutableList()
        aInfo5ViewModel.setTheAllReportsList(reportsList)
        val defaultReportsList = mutableListOf<String>()
        if (reportsList.size >= 2){
            defaultReportsList.add(reportsList[0])
            defaultReportsList.add(reportsList[2])
        }
        aInfo5ViewModel.setTheReportsToBeGeneratedList(defaultReportsList)

    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        val fileFlag = aInfo5ViewModel.getTheFileFlag()
        when (fileFlag) {
            MainActivity.DIRECTORY -> {
                builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes") { dialog, _ ->
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        (activity as MainActivity).openDocumentTree(intent)
                        dialog.dismiss()
                    }
                    .setNeutralButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
            }
            MainActivity.AUDIT_DATABASE_DELETE -> {
                val templateDialogTitle = resources.getString(R.string.string_deleting_templates_db)
                val templateDialogMessage =
                    resources.getString(R.string.string_message_for_deleting_templates_db)
                builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes") { dialog, _ ->
                        aInfo5ViewModel.deleteAllAInfo5()
                        aInfo5ViewModel.areAllAuditsDeletedMLD.value = true
                        aInfo5ViewModel.setStatusMessageFlow("Deletion of Audit Records is Complete")
                        //Toast.makeText(this.requireContext(), "Deletion of Audit Records is Complete", Toast.LENGTH_LONG).show()
                        aInfo5ViewModel.setTheFileFlag(MainActivity.TEMPLATE_DATABASE_DELETE)
                        dialog.dismiss()
                        showDialog(templateDialogTitle, templateDialogMessage)
                    }
                    .setNeutralButton("No") { dialog, _ ->
                        aInfo5ViewModel.setTheFileFlag(MainActivity.TEMPLATE_DATABASE_DELETE)
                        dialog.dismiss()
                        showDialog(templateDialogTitle, templateDialogMessage)
                    }
            }
            MainActivity.TEMPLATE_DATABASE_DELETE -> {
                builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes") { dialog, _ ->
                        aInfo5ViewModel.deleteAllAInfo5Templates()
                        //Clear the Template related Variables in ViewModel
                        aInfo5ViewModel.clearThePageTemplateList()
                        aInfo5ViewModel.clearTheTemplateIDList()
                        aInfo5ViewModel.clearThePageGroupIDsList()
                        aInfo5ViewModel.clearTheParentChildParentItemML()

                        aInfo5ViewModel.setStatusMessageFlow("Deletion of Templates is Complete")
                        //Toast.makeText(this.requireContext(), "Deletion of Templates is Complete", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        //Load the templateLoadedIntoDBFlag into Template DB
                        val templateLoadedIntoDBFlagID = MainActivity.TEMPLATES_LOADED_INTO_DB_ID
                        val aInfo5Template = AInfo5Templates(templateLoadedIntoDBFlagID, false.toString())
                        aInfo5ViewModel.insertAInfo5Templates(aInfo5Template)
                        //Clear the ViewModel variables that depend on the template
                        aInfo5ViewModel.setTheTemplatesHaveBeenLoadedIntoDBFlag(false)
                        aInfo5ViewModel.clearTheParentChildParentItemML()

                        //aInfo5ViewModel.setTheParentChildParentItemML(mutableListOf(aInfo5ViewModel.defaultRVParentChildParentItem))
                        aInfo5ViewModel.clearThePageTemplateList()
                        aInfo5ViewModel.clearThePageGroupIDsList()

                        aInfo5ViewModel.clearTheTemplateIDList()

                        aInfo5ViewModel.templateIDsUploadingCompletedMLD.value = false
                        aInfo5ViewModel.pageGroupIDsUploadingCompletedMLD.value = false

                        binding.llPasswordDatabaseDeletion.visibility = View.GONE
                        binding.llOpeningScreen.visibility = View.VISIBLE
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
                    }
                    .setNeutralButton("No") { dialog, _ ->
                        dialog.dismiss()
                        binding.llPasswordDatabaseDeletion.visibility = View.GONE
                        binding.llOpeningScreen.visibility = View.VISIBLE
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OPENING_SCREEN_FRAGMENT)
                    }
            }
        }
        builder.create().show()
    }

    private fun showDialogForNewTemplateUpload() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("The templates have already been uploaded")
            .setMessage("Do you want to upload a new template file? The Old Templates will be deleted")
            .setPositiveButton("Yes") { dialog, _ ->
                //Delete the old template file and then proceed
                aInfo5ViewModel.deleteAllAInfo5Templates()
                aInfo5ViewModel.clearTheParentChildParentItemML()
//                aInfo5ViewModel.clearThePageTemplateList()
//                aInfo5ViewModel.clearThePageGroupIDsList()
//
//                aInfo5ViewModel.clearTheTemplateIDList()
//
//                aInfo5ViewModel.templateIDsUploadingCompletedMLD.value = false
//                aInfo5ViewModel.pageGroupIDsUploadingCompletedMLD.value = false

                aInfo5ViewModel.setTheFileFlag(MainActivity.TEMPLATE_DOCUMENT_LOAD)
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
                (activity as MainActivity).openDocumentTree(intent)
                dialog.dismiss()
            }
            .setNeutralButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }


}
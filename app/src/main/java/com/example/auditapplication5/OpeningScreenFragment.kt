package com.example.auditapplication5

import android.app.AlertDialog
import android.content.Intent
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
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentOpeningScreenBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlin.system.exitProcess


class OpeningScreenFragment : Fragment() {
    private lateinit var binding: FragmentOpeningScreenBinding
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
            R.layout.fragment_opening_screen,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel


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

        //For Checking
        //Log.d(MainActivity.TESTING_TAG, "onViewCreated: ${aInfo5ViewModel.getTheCompanyReport().sectionReportList} ")
        //Getting the Parent Folder for saving Audits from DB
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
                    (activity as MainActivity).takePersistableURIPermissions(parentFolderURIString.toUri())
                }
            }
        }

        //Getting the TemplateIDs List and PageGroupIDs List from the db
        if (aInfo5ViewModel.getTheTemplateIDList().isEmpty()){
            aInfo5ViewModel.getTemplateIdsListStringFromTemplateDB.observe(viewLifecycleOwner) { list ->
                var templateIDsListString = ""
                if (list.isEmpty()) {
                    templateIDsListString = ""
                }
                else {
                    templateIDsListString = ""
                    for (item in list) {
                        templateIDsListString += item.template_string
                    }
                }
                aInfo5ViewModel.tasksToDoWithTemplateIDsListString(templateIDsListString)
                if (!aInfo5ViewModel.getTheObserveAndActOnceForTemplatesIDsListFlag()){

                    aInfo5ViewModel.setTheObserveAndActOnceForTemplatesIDsListFlag(true)
                }
            }
        }

        //Getting the template elements for Add A Page in Observations Fragment
        if (aInfo5ViewModel.getTheParentChildParentItemML().isEmpty()){
            aInfo5ViewModel.pageGroupIDsUploadingCompletedMLD.observe(viewLifecycleOwner){value ->
                if (value == true){
                    val iDsListForPageGroup = aInfo5ViewModel.getThePageGroupIDsList()
                    if (iDsListForPageGroup.isNotEmpty()){
                        for (index in 0 until iDsListForPageGroup.size){
                            val itemIDNeededML = mutableListOf<String>(iDsListForPageGroup[index])
                            aInfo5ViewModel.getAInfo5TemplatesByIds(itemIDNeededML).observe(viewLifecycleOwner){list ->
                                var pageGroupItemString = ""
                                if (list.isEmpty()){
                                    pageGroupItemString = ""
                                } else {
                                    for (item in list){
                                        pageGroupItemString += item.template_string
                                    }
                                }
                                aInfo5ViewModel.tasksToDoWithPageGroupIDs(pageGroupItemString, iDsListForPageGroup, index)
                            }
                        }
                    } else {
                        aInfo5ViewModel.setTheParentChildParentItemML(mutableListOf(aInfo5ViewModel.defaultRVParentChildParentItem))
                    }
                }

            }
        }




        //On Click Listeners Below

        binding.buttonNewAudit.setOnClickListener {
            aInfo5ViewModel.setTheFileFlag(MainActivity.DIRECTORY)
            if (aInfo5ViewModel.getTheParentFolderURIString() == "") {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                (activity as MainActivity).openDocumentTree(intent)
            } else {
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
            if (templatesLoadedFlag == true) {
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
                        Toast.makeText(requireContext(),"The password entered is incorrect", Toast.LENGTH_SHORT).show()
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

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Functions below

    fun initialiseSomeViewModelVariablesHere(){
        aInfo5ViewModel.clearPresentCompanyAllIds()
        aInfo5ViewModel.sectionAllDataLoadedFlagMLD.value = false
        aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(false)

        //Generate and load the Default Reports List
        val reportsList = resources.getStringArray(R.array.Report_Choices).toMutableList()
        aInfo5ViewModel.setTheAllReportsList(reportsList)
        val defaultReportsList = mutableListOf<String>()
        if (reportsList.size >= 2){
            defaultReportsList.add(reportsList[0])
            defaultReportsList.add(reportsList[2])
        }
        aInfo5ViewModel.setTheReportsToBeGeneratedList(defaultReportsList)

        aInfo5ViewModel.setTheCompanyPhotosUploadedFlag(false)
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        val fileFlag = aInfo5ViewModel.getTheFileFlag()
        if (fileFlag == MainActivity.DIRECTORY) {
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
        else if (fileFlag == MainActivity.AUDIT_DATABASE_DELETE) {
            val templateDialogTitle = resources.getString(R.string.string_deleting_templates_db)
            val templateDialogMessage =
                resources.getString(R.string.string_message_for_deleting_templates_db)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes") { dialog, _ ->
                    aInfo5ViewModel.deleteAllAInfo5()
                    Toast.makeText(this.requireContext(), "Deletion of Audit Records is Complete", Toast.LENGTH_LONG).show()
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
        else if (fileFlag == MainActivity.TEMPLATE_DATABASE_DELETE) {
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes") { dialog, _ ->
                    aInfo5ViewModel.deleteAllAInfo5Templates()
                    Toast.makeText(this.requireContext(), "Deletion of Templates is Complete", Toast.LENGTH_LONG).show()
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
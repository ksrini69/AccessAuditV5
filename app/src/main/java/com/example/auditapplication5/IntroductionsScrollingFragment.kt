package com.example.auditapplication5

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.CompanyIntroDataDC
import com.example.auditapplication5.databinding.FragmentIntroductionsScrollingBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class IntroductionsScrollingFragment : Fragment() {
    private lateinit var binding: FragmentIntroductionsScrollingBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_introductions_scrolling, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        binding.aInfo5ViewModel = aInfo5ViewModel
        binding.lifecycleOwner=viewLifecycleOwner


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.COMPANY_INTRODUCTION){
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT)
                    saveIntroductionsToDB(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
                    aInfo5ViewModel.etIntroductionsMLD.value = ""
                    aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value  = ""
                } else if (aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION){
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                    saveIntroductionsToDB(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
                    aInfo5ViewModel.etIntroductionsMLD.value = ""
                    aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value  = ""
                }

                findNavController().navigate(R.id.action_introductionsScrollingFragment_to_sectionAndIntrosFragment)
            }

        })

        //Set Screen for Introductions?
        aInfo5ViewModel.setTheScreenVariable(MainActivity.INTROS_FRAGMENT)

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.show()

        //Scrolling for Textviews
        binding.tvPhotoPathsInIntroductionsPage.movementMethod = ScrollingMovementMethod()

        //Get the Introductions Info from DB to load
        if (aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable()== MainActivity.COMPANY_INTRODUCTION){
            //Get the action bar title
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
                            actionBar.title = "Company Name: " + presentCompanyName
                        }
                        actionBar?.subtitle = "Introduction Screen"
                    }
                }
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentCompanyNameID)
            //Get the Company Introductions to load
            val companyIntroID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_INTRO_ID
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companyIntroID)
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(companyIntroID)).observe(viewLifecycleOwner){list ->
                var companyIntroAndPhotoPaths = ""
                if (list.isEmpty()){
                    companyIntroAndPhotoPaths = ""
                    aInfo5ViewModel.etIntroductionsMLD.value = ""
                    aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value  = ""
                }else {
                    companyIntroAndPhotoPaths = ""
                    for (item in list){
                        companyIntroAndPhotoPaths += item.framework
                    }
                }
                val companyIntroData = aInfo5ViewModel.stringToCompanyIntroData(companyIntroAndPhotoPaths)
                aInfo5ViewModel.setTheCompanyIntroData(companyIntroData)
                aInfo5ViewModel.etIntroductionsMLD.value = companyIntroData.introduction
                aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value = companyIntroData.picturePathsInIntroductions
            }

        }
        else if (aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable() == MainActivity.SECTION_INTRODUCTION){
            //Get the action bar title
            val presentSectionNameID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.PRESENT_SECTION_ID
            aInfo5ViewModel.getAInfo5ByIds(mutableListOf(presentSectionNameID)).observe(viewLifecycleOwner){sectionList ->
                var presentSectionName = ""
                if (sectionList.isNotEmpty()){
                    for (item in sectionList){
                        presentSectionName += item.framework
                    }
                    if (actionBar?.title?.contains(presentSectionName) == false) {
                        actionBar.title = "Section Name: " + presentSectionName
                    }
                    actionBar?.subtitle = "Introduction Screen"
                }
            }
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(presentSectionNameID)
            //Get the section introduction to load
            aInfo5ViewModel.sectionAllDataLoadedFlagMLD.observe(viewLifecycleOwner){ dataFlag ->
                if (dataFlag == true){
                    binding.pbUploadingFormDbInIntroductions.visibility = View.GONE
                    binding.llIntroductionsPage.isEnabled = true
                    aInfo5ViewModel.etIntroductionsMLD.value = aInfo5ViewModel.getThePresentSectionAllData().introduction
                    aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value = aInfo5ViewModel.getThePresentSectionAllData().picturePathsInIntroductions
                } else {
                    binding.pbUploadingFormDbInIntroductions.visibility = View.VISIBLE
                    binding.llIntroductionsPage.isEnabled = false
                }
            }

        }


        //Onclick Listeners below

        binding.ibCameraXInIntroductionPage.setOnClickListener {


            //Setting the appropriate previous screens
            aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            it.findNavController().navigate(R.id.action_introductionsScrollingFragment_to_cameraXFragment)

        }

        binding.ibEditPhotoInIntroductionPage.setOnClickListener {
            showDialogForPhotoModification()
        }

        binding.ibUndoInIntroductionsPage.setOnClickListener {
            var newValue = ""
            var newValuePhotos = ""
            if (aInfo5ViewModel.getTheHoldingVariableInIntroductions() != ""){
                if (binding.etIntroductionRemarks.text.toString() != ""){
                    newValue = aInfo5ViewModel.getTheHoldingVariableInIntroductions() + "\n" + binding.etIntroductionRemarks.text.toString()
                } else {
                    newValue = aInfo5ViewModel.getTheHoldingVariableInIntroductions()
                }
                binding.etIntroductionRemarks.setText(newValue)
                aInfo5ViewModel.setTheHoldingVariableInIntroductions("")
            }
            if (aInfo5ViewModel.getTheHoldingVariableForPhotoPathsInIntroductions() != ""){
                if (binding.tvPhotoPathsInIntroductionsPage.text.toString() != ""){
                    newValuePhotos = aInfo5ViewModel.getTheHoldingVariableForPhotoPathsInIntroductions() + "\n" + binding.tvPhotoPathsInIntroductionsPage.text.toString()
                } else {
                    newValuePhotos = aInfo5ViewModel.getTheHoldingVariableForPhotoPathsInIntroductions()
                }
                binding.tvPhotoPathsInIntroductionsPage.setText(newValuePhotos)
                aInfo5ViewModel.setTheHoldingVariableForPhotoPathsInIntroductions("")
            }
        }

        binding.ibDeleteInIntroductionPage.setOnClickListener {
            showDialogForDeletion()
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        saveIntroductionsToDB(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
    }


    //Functions below

    private fun showDialogForPhotoModification(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Choose Photos from Present Location/All ")
            .setMessage("Press \" All\" to choose from All Photos")
            .setPositiveButton("Location") { dialog, _ ->
                val location = aInfo5ViewModel.makeLocationForPhotos(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
                aInfo5ViewModel.setLocationForPhotos(location)
                aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
                aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
                findNavController().navigate(R.id.action_introductionsScrollingFragment_to_photoDisplayRecyclerviewFragment)
                dialog.dismiss()
            }
            .setNeutralButton("All") { dialog, _ ->
                aInfo5ViewModel.setLocationForPhotos("All")
                aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
                aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
                findNavController().navigate(R.id.action_introductionsScrollingFragment_to_photoDisplayRecyclerviewFragment)
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showDialogForDeletion(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Delete the Introduction")
            .setMessage("Press \" Yes\" to Delete ")
            .setPositiveButton("Yes") { dialog, _ ->
                if (aInfo5ViewModel.getTheHoldingVariableInIntroductions() != ""){
                    aInfo5ViewModel.setTheHoldingVariableInIntroductions(aInfo5ViewModel.getTheHoldingVariableInIntroductions() + "\n" + binding.etIntroductionRemarks.text.toString())
                } else {
                    aInfo5ViewModel.setTheHoldingVariableInIntroductions(binding.etIntroductionRemarks.text.toString())
                }
                if (aInfo5ViewModel.getTheHoldingVariableForPhotoPathsInIntroductions() != ""){
                    aInfo5ViewModel.setTheHoldingVariableForPhotoPathsInIntroductions(aInfo5ViewModel.getTheHoldingVariableForPhotoPathsInIntroductions() + "\n" + binding.tvPhotoPathsInIntroductionsPage.text.toString())
                } else {
                    aInfo5ViewModel.setTheHoldingVariableForPhotoPathsInIntroductions(binding.tvPhotoPathsInIntroductionsPage.text.toString())
                }
                binding.etIntroductionRemarks.setText("")
                binding.tvPhotoPathsInIntroductionsPage.text = ""
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }


    private fun saveIntroductionsToDB(companyOrSection: String = ""){
        if (companyOrSection == MainActivity.COMPANY_INTRODUCTION){
            val companyIntroID =
                aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_INTRO_ID
            val companyIntroData = CompanyIntroDataDC()

            companyIntroData.introduction = aInfo5ViewModel.etIntroductionsMLD.value.toString()
            companyIntroData.picturePathsInIntroductions = aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value.toString()

            aInfo5ViewModel.setTheCompanyIntroData(companyIntroData)
            val companyIntroDataString = aInfo5ViewModel.companyIntroDataToString(companyIntroData)
            val aInfo5Intro = AInfo5(companyIntroID, companyIntroDataString)
            aInfo5ViewModel.insertAInfo5(aInfo5Intro)
            aInfo5ViewModel.addUniqueItemToPresentCompanyAllIds(companyIntroID)
        } else if (companyOrSection == MainActivity.SECTION_INTRODUCTION){
            val sectionPagesFrameworkAndDataID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            aInfo5ViewModel.updateIntroForThePresentSectionAllData(aInfo5ViewModel.etIntroductionsMLD.value.toString())
            aInfo5ViewModel.updatePicturePathsForThePresentSectionAllData(aInfo5ViewModel.tvPhotoPathsInIntroductionsFragmentMLD.value.toString())
            aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(aInfo5ViewModel.getThePresentSectionAllPagesFramework(),aInfo5ViewModel.getThePresentSectionAllData(),sectionPagesFrameworkAndDataID)
        }
    }
}
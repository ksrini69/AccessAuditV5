package com.example.auditapplication5

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentObservationsBinding
import com.example.auditapplication5.presentation.adapter.CheckboxesFrameworkRVAdapter
import com.example.auditapplication5.presentation.adapter.QuestionsFrameworkRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class ObservationsFragment : Fragment() {
    private lateinit var binding: FragmentObservationsBinding
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
            R.layout.fragment_observations,
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
                    if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT){
                        //Save the page title into the page framework and data
                        val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                        aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(aInfo5ViewModel.etPageNameMLD.value.toString(), currentPageIndex)
                        //Save the observations etc info into Section Page Data
                        aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(aInfo5ViewModel.etObservationsMLD.value.toString(),currentPageIndex)
                        aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(aInfo5ViewModel.tvPhotoPathsInObservationsFragmentMLD.value.toString(), currentPageIndex)
                        aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(aInfo5ViewModel.etRecommendationsMLD.value.toString(), currentPageIndex)
                        aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(aInfo5ViewModel.tvStandardsMLD.value.toString(), currentPageIndex)
                        //Save the SectionPagesFramework and Data before exiting
                        val sectionPagesFrameworkAndDataID =
                            aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                        aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                            aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                            aInfo5ViewModel.getThePresentSectionAllData(),
                            sectionPagesFrameworkAndDataID
                        )
                        //Save the information into the CompanyReport suitably
                        aInfo5ViewModel.updateSectionDetailsInCompanyReportAndSave(aInfo5ViewModel.getPresentSectionCode(),aInfo5ViewModel.getPresentSectionName(),aInfo5ViewModel.getThePresentSectionAllData())

                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS){
                        var currentPageIndex = 0
                        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
                        } else {
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Questions_Below))
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS){
                        var currentPageIndex = 0
                        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
                        } else {
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Questions_Below))
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS){
                        var currentPageIndex = 0
                        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
                        } else {
                            currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Questions_Below))
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }

                }
            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Ensure that the templates and data blocks are being loaded after Framework is uploaded and/or updated
        aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.observe(viewLifecycleOwner){frameworkFlag ->
            if (frameworkFlag == true){
                //upload templates
                if (aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isNotEmpty()){
                    val frameworkListSize = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size
                    for (index in (frameworkListSize - 1) downTo 0){
                        val pageCode = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[index].pageCode
                        if (aInfo5ViewModel.isItemPresentInPageTemplateList(pageCode) == false){
                            getThePageTemplateAndUploadIntoViewModel(pageCode)
                        }
                        val questionsFrameworkList = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[index].questionsFrameworkList
                        if (questionsFrameworkList.size > 0){
                            for (index1 in 0 until questionsFrameworkList.size){
                                val qpageCode = questionsFrameworkList[index1].pageCode
                                if (aInfo5ViewModel.isItemPresentInPageTemplateList(qpageCode)== false){
                                    getThePageTemplateAndUploadIntoViewModel(qpageCode)
                                }

                            }
                        }
                        val observationsFrameworkList = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[index].observationsFrameworkList
                        if (observationsFrameworkList.size > 0){
                            for (index1 in 0 until observationsFrameworkList.size){
                                val opageCode = observationsFrameworkList[index1].pageCode
                                if (aInfo5ViewModel.isItemPresentInPageTemplateList(opageCode)== false){
                                    getThePageTemplateAndUploadIntoViewModel(opageCode)
                                }
                            }
                        }
                        val recommendationsFrameworkList = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[index].recommendationsFrameworkList
                        if (recommendationsFrameworkList.size > 0){
                            for (index1 in 0 until recommendationsFrameworkList.size){
                                val rpageCode = recommendationsFrameworkList[index1].pageCode
                                if (aInfo5ViewModel.isItemPresentInPageTemplateList(rpageCode)== false){
                                    getThePageTemplateAndUploadIntoViewModel(rpageCode)
                                }
                            }
                        }
                        val standardsFrameworkList = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[index].standardsFrameworkList
                        if (standardsFrameworkList.size > 0){
                            for (index1 in 0 until standardsFrameworkList.size){
                                val spageCode = standardsFrameworkList[index1].pageCode
                                if (aInfo5ViewModel.isItemPresentInPageTemplateList(spageCode)== false){
                                    getThePageTemplateAndUploadIntoViewModel(spageCode)
                                }
                            }
                        }
                    }
                }
                binding.pbUploadingFromDatabase.visibility = View.GONE
                binding.llObservationsActionbuttonsAndViews.isEnabled = true
                binding.fabAddANewBlock.isEnabled = true
            }
            else {
                //Ensure that the Progress bar is visible
                binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                binding.llObservationsActionbuttonsAndViews.isEnabled = false
                binding.fabAddANewBlock.isEnabled = false
            }
        }

        //Update the present section all data based on templates which have been loaded
        aInfo5ViewModel.sectionAllDataLoadedFlagMLD.observe(viewLifecycleOwner){dataFlag ->
            if (dataFlag == true){
                val presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
                if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()){
                    val sectionPageDataList = presentSectionAllData.sectionAllPagesData.sectionPageDataList
                    for (pageIndex in 0 until sectionPageDataList.size){
                        if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()){
                            val questionFrameworkDataItemList = sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                            for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size){
                                val frameworkPageCode = questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                                if (aInfo5ViewModel.isItemPresentInPageTemplateList(frameworkPageCode) == false){
                                    getThePageTemplateAndUploadIntoViewModel(frameworkPageCode)
                                } else {
                                    val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                                    val result = questionTemplateItemML?.let {
                                        aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(pageIndex,questionsFrameworkIndex,
                                            it
                                        )
                                    }
                                    if (result == false){
                                        aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(pageIndex,questionsFrameworkIndex,
                                            questionTemplateItemML
                                        )
                                    }
                                }
                            }
                        }
                        if (sectionPageDataList[pageIndex].observationsFrameworkDataItemList.isNotEmpty()){
                            val observationsFrameworkDataItemList = sectionPageDataList[pageIndex].observationsFrameworkDataItemList
                            for (observationsFrameworkIndex in 0 until observationsFrameworkDataItemList.size){
                                val observationsTemplateItemML = aInfo5ViewModel.observationsList_LD.value
                                val result = observationsTemplateItemML?.let {
                                    aInfo5ViewModel.isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, observationsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, observationsFrameworkIndex,
                                        observationsTemplateItemML
                                    )
                                }
                            }
                        }
                        if (sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.isNotEmpty()){
                            val recommendationsFrameworkDataItemList = sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList
                            for (recommendationsFrameworkIndex in 0 until recommendationsFrameworkDataItemList.size){
                                val recommendationsTemplateItemML = aInfo5ViewModel.recommendationsList_LD.value
                                val result = recommendationsTemplateItemML?.let {
                                    aInfo5ViewModel.isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, recommendationsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, recommendationsFrameworkIndex,
                                        recommendationsTemplateItemML
                                    )
                                }
                            }
                        }
                        if (sectionPageDataList[pageIndex].standardsFrameworkDataItemList.isNotEmpty()){
                            val standardsFrameworkDataItemList = sectionPageDataList[pageIndex].standardsFrameworkDataItemList
                            for (standardsFrameworkIndex in 0 until standardsFrameworkDataItemList.size){
                                val standardsTemplateItemML = aInfo5ViewModel.standardsList_LD.value
                                val result = standardsTemplateItemML?.let {
                                    aInfo5ViewModel.isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, standardsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, standardsFrameworkIndex,
                                        standardsTemplateItemML
                                    )
                                }
                            }
                        }
                    }
                }
                binding.pbUploadingFromDatabase.visibility = View.GONE
                binding.llObservationsActionbuttonsAndViews.isEnabled = true
                binding.fabAddANewBlock.isEnabled = true
            } else{
                //Ensure that the Progress bar is visible
                binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                binding.llObservationsActionbuttonsAndViews.isEnabled = false
                binding.fabAddANewBlock.isEnabled = false
            }
        }

        //Check if the present template has been uploaded
        aInfo5ViewModel.presentTemplateUploadedFlagMLD.observe(viewLifecycleOwner){flag ->
            if (flag == true){
                binding.pbUploadingFromDatabase.visibility = View.GONE
                binding.llObservationsActionbuttonsAndViews.isEnabled = true
                binding.fabAddANewBlock.isEnabled = true
            } else if (flag == false){
                //Ensure that the Progress bar is visible
                binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                binding.llObservationsActionbuttonsAndViews.isEnabled = false
                binding.fabAddANewBlock.isEnabled = false
            }
        }

        //Set Up the Questions View when arriving and when a new page is added
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
            binding.rvQuestionsFramework.visibility = View.VISIBLE
            binding.rvCheckboxesFramework.visibility = View.GONE
            var presentSectionAllPagesFrameworkIndex = 0
            presentSectionAllPagesFrameworkIndex =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(presentSectionAllPagesFrameworkIndex)
            aInfo5ViewModel.setThePageCountMLD(presentSectionAllPagesFrameworkIndex)
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            reloadingPage(presentSectionAllPagesFrameworkIndex)
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.RV_PARENT_CHILD_FRAGMENT){
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
            binding.rvQuestionsFramework.visibility = View.VISIBLE
            binding.rvCheckboxesFramework.visibility = View.GONE
            var presentSectionAllPagesFrameworkIndex = 0
            presentSectionAllPagesFrameworkIndex =
                aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            reloadingPage(presentSectionAllPagesFrameworkIndex)
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.GOTO_RECYCLERVIEW_FRAGMENT){
            aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
            aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
            val presentSectionAllPagesFrameworkIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            reloadingPage(presentSectionAllPagesFrameworkIndex)
        }

        //Expand and Collapse Views
        expandAndCollapseViews()

        //Scrolling for TextViews and edit texts
        binding.tvPhotoPathsInObservationsPage.movementMethod = ScrollingMovementMethod()
        binding.tvStandardsInObservationsPage.movementMethod = ScrollingMovementMethod()
        binding.etObservationsOnly.movementMethod = ScrollingMovementMethod()
        binding.etRecommendationsOnly.movementMethod = ScrollingMovementMethod()

        // On Click Listeners for Questions, Observations, Recommendations and Standards

        binding.buttonQuestionsView.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            var currentPageIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            } else {
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            }
            binding.rvQuestionsFramework.visibility = View.VISIBLE
            binding.rvCheckboxesFramework.visibility = View.GONE
            binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Questions_Below))
            loadQuestionsRecyclerView(currentPageIndex)
        }

        binding.buttonObservationsView.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS)
            var currentPageIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            } else {
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            }
            binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Observations_Below))
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadObservationsRecyclerView(currentPageIndex)
        }

        binding.buttonRecommendationsView.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS)
            var currentPageIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            } else {
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            }
            binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Recommendations_Below))
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadRecommendationsRecyclerView(currentPageIndex)
        }

        binding.buttonStandardsView.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS)
            var currentPageIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            } else {
                currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            }
            binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Standards_Below))
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadStandardsRecyclerView(currentPageIndex)
        }

        //On Click Listeners for Camera and Modify Photo
        binding.ibCameraInObservationsPage.setOnClickListener {
            //Create the location, count and photograph name (without extension)
            val location = aInfo5ViewModel.makeLocationForPhotos(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
            aInfo5ViewModel.setLocationForPhotos(location)
            val count = aInfo5ViewModel.getPhotoCountByLocation(location) + 1
            aInfo5ViewModel.setThePhotoCount(count)
            val presentPhotoNameWithoutExtension = aInfo5ViewModel.makePresentPhotoName(location, count)
            aInfo5ViewModel.setPresentPhotoName(presentPhotoNameWithoutExtension)
            //Setting the previous screen
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            findNavController().navigate(R.id.action_observationsFragment_to_cameraXFragment)
        }

        binding.ibEditPhotoInObservationsPage.setOnClickListener {
            showDialogForPhotoModification()
            //Setting the appropriate previous screens
            aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())

        }

        //On Click Listener for Add a Page and Add a Block
        binding.ibAddAPage.setOnClickListener {
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(binding.etObservationsOnly.text.toString(),currentPageIndex)
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(binding.tvPhotoPathsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(binding.etRecommendationsOnly.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(binding.tvStandardsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            findNavController().navigate(R.id.action_observationsFragment_to_parentChildRecyclerviewFragment)
        }

        binding.fabAddANewBlock.setOnClickListener {
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            var pageCodeFromViewModel = ""
            if (aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isNotEmpty()) {
                pageCodeFromViewModel =
                    aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].pageCode
            }
            val ampCode = aInfo5ViewModel.pageCodeToAMPCode(pageCodeFromViewModel)
            ampEntriesFromTemplateDB(ampCode)
        }

        //Click Listeners for move left and move right

        binding.ibBack.setOnClickListener {
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(binding.etObservationsOnly.text.toString(),currentPageIndex)
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(binding.tvPhotoPathsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(binding.etRecommendationsOnly.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(binding.tvStandardsInObservationsPage.text.toString(), currentPageIndex)
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibBack.startAnimation(animation)
            //val pageIndex = aInfo5ViewModel.getValueOfSectionPagesFrameworkIndex()
            val minimumValueOfIndex = 0
            val maximumValueOfIndex =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_OBS) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            }
            if (currentPageIndex > minimumValueOfIndex) {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(pageIndex - 1)
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex - 1)
            } else {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(maximumValueOfIndex)
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(maximumValueOfIndex)
            }
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            reloadingPage(aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex())
        }

        binding.ibForward.setOnClickListener {
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(binding.etObservationsOnly.text.toString(),currentPageIndex)
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(binding.tvPhotoPathsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(binding.etRecommendationsOnly.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(binding.tvStandardsInObservationsPage.text.toString(), currentPageIndex)
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibForward.startAnimation(animation)
            val maximumValueOfIndex =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
            val minimumValueOfIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_OBS) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            }
            if (currentPageIndex < maximumValueOfIndex) {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(pageIndex + 1)
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex + 1)
            } else {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(minimumValueOfIndex)
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(minimumValueOfIndex)
            }
            reloadingPage(aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex())
        }

        //Click Listener for Goto
        binding.buttonGoto.setOnClickListener {
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(binding.etObservationsOnly.text.toString(),currentPageIndex)
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(binding.tvPhotoPathsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(binding.etRecommendationsOnly.text.toString(), currentPageIndex)
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(binding.tvStandardsInObservationsPage.text.toString(), currentPageIndex)
            aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getTheScreenVariable())
            findNavController().navigate(R.id.action_observationsFragment_to_gotoRecyclerviewFragment)
        }

        //Click Listener for Deleting a page
        binding.ibDeleteInObservationsPage.setOnClickListener {
            showDialogToDeleteAPage()
        }


    }


    //Functions below

    private fun showDialogToDeleteAPage() {
        val title = "Delete this page? Note that the page data will be lost"
        val message =
            "Press \'Yes\' to delete the page. Press \'Cancel\' to cancel this operation."
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                aInfo5ViewModel.deletePageFrameworkInPresentSectionAllPagesFramework(currentPageIndex)
                aInfo5ViewModel.deleteSectionPageDataInPresentSectionAllData(currentPageIndex)
                if (currentPageIndex > 0) {
                    aInfo5ViewModel.setThePageCountMLD(currentPageIndex - 1)
                    aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex - 1)
                } else if (currentPageIndex == 0) {
                    val frameworkPagesSize =
                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size
                    if (frameworkPagesSize > 0) {
                        aInfo5ViewModel.setThePageCountMLD(currentPageIndex)
                        aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex)
                    } else {
                        aInfo5ViewModel.setThePageCountMLD(0)
                        aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(0)
                        findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
                    }

                }
                //Save the Section Page Framework and the page data before exiting
                aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllPagesFramework()
                aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllData()
                val sectionPagesFrameworkAndDataID =
                    aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(aInfo5ViewModel.getThePresentSectionAllPagesFramework(), aInfo5ViewModel.getThePresentSectionAllData(), sectionPagesFrameworkAndDataID)

                val newListIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
                reloadingPage(newListIndex)
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
    
    //amp refers to add menu page values where one may add question blocks etc by
    //pressing the + floating action button
    private fun ampEntriesFromTemplateDB(ampCode: String) {
        var ampValueList = mutableListOf<String>()
        var ampValueListString = ""
        if (ampCode != "") {
            if (ampCode.contains("_APM") && ampCode.contains("APM_")) {
                val ampIDML = mutableListOf<String>(ampCode)
                val ampListStringValue = aInfo5ViewModel.getAInfo5TemplatesByIds(ampIDML)
                ampListStringValue.observe(viewLifecycleOwner) { list ->
                    if (list.isEmpty()) {
                        ampValueListString = ""
                    } else {
                        ampValueListString = ""
                        for (item in list) {
                            ampValueListString += item.template_string
                        }
                        ampValueList = aInfo5ViewModel.stringToMLUsingDlimiter1(ampValueListString)
                        //val ampValueListTruncated = ampValueList.drop(1)
                        val mutableMapMade = aInfo5ViewModel.makeLinkedHashMapFromML(
                            aInfo5ViewModel.extractDisplayNameFromPageCodeList(ampValueList.toMutableList())
                        )
                        choosePageToAddDialog(ampValueList, mutableMapMade)
                    }
                }
            }
        }
    }

    private fun choosePageToAddDialog(
        inputList: MutableList<String>,
        multiChoiceList: LinkedHashMap<String, Boolean>
    ) {
        //val nameList = mutableListOf<String>()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Choose all the pages to Add")
        builder.setMultiChoiceItems(
            multiChoiceList.keys.toTypedArray(),
            multiChoiceList.values.toBooleanArray()
        ) { dialogInterface, which, isChecked ->
            multiChoiceList.set(multiChoiceList.keys.toTypedArray().get(which), isChecked)
        }

        builder.setPositiveButton("ok") { dialog, id ->
            for (selection in multiChoiceList) {
                val questionsFrameworkItem = QuestionsFrameworkItemDC()
                val observationsFrameworkItem = CheckboxesFrameworkItemDC()
                val recommendationsFrameworkItem = CheckboxesFrameworkItemDC()
                val standardsFrameworkItem = CheckboxesFrameworkItemDC()

                val questionFrameworkDataItem = QuestionsFrameworkDataItemDC()
                val observationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val recommendationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val standardsFrameworkDataItem = CheckboxesFrameworkDataItemDC()

                if (selection.value == true) {
                    for (item in inputList) {
                        if (aInfo5ViewModel.extractDisplayNameFromPageCode(item) == selection.key && item.contains("PC_")) {
                            var pageTemplateItem = PageTemplateDC()
                            if (aInfo5ViewModel.isItemPresentInPageTemplateList(item) == true){
                                pageTemplateItem = aInfo5ViewModel.getItemFromPageTemplateList(item)
                            }

                            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                                val currentpageIndex =
                                    aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()

                                questionsFrameworkItem.questionsFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromQuestionsParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentpageIndex].questionsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()

                                questionsFrameworkItem.pageCode = item
                                questionsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                questionFrameworkDataItem.questionsFrameworkTitle =
                                    questionsFrameworkItem.questionsFrameworkTitle
                                questionFrameworkDataItem.pageCode = questionsFrameworkItem.pageCode

                                //Add the Questions Block Data Item based on the template
//                                for (questionsItem in pageTemplateItem.questionsList){
//                                    val questionDataItem = QuestionDataItemDC()
//                                    questionDataItem.blockNumber = questionsItem.blockNumber
//                                    questionDataItem.mandatoryValue = questionsItem.mandatory
//                                    questionFrameworkDataItem.questionDataItemList.add(questionDataItem)
//                                }

                                val positionOfQuestionsFrameworkList =
                                    binding.rvQuestionsFramework.adapter?.itemCount
                                aInfo5ViewModel.addPageFrameworkQuestionsInPresentSectionAllPagesFramework(
                                    questionsFrameworkItem,
                                    currentpageIndex
                                )
                                aInfo5ViewModel.addQuestionsFrameworkDataItemInThePresentSectionAllData(
                                    questionFrameworkDataItem,
                                    currentpageIndex
                                )
                                if (positionOfQuestionsFrameworkList != null) {
                                    binding.rvQuestionsFramework.adapter?.notifyItemChanged(
                                        positionOfQuestionsFrameworkList - 1
                                    )
                                }

                                //Add the Observations Block item too
                                observationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentpageIndex].observationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                observationsFrameworkItem.pageCode = item
                                observationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                observationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    observationsFrameworkItem.checkboxesFrameworkTitle
                                observationsFrameworkDataItem.pageCode = observationsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkObservationsInPresentSectionAllPagesFramework(
                                    observationsFrameworkItem,
                                    currentpageIndex
                                )
//                                for (observationsItem in pageTemplateItem.observationsList){
//                                    val observationDataItem = CheckboxDataItemDC()
//                                    observationsFrameworkDataItem.checkboxDataItemML.add(observationDataItem)
//                                }

                                aInfo5ViewModel.addObservationsFrameworkDataItemInThePresentSectionAllData(
                                    observationsFrameworkDataItem,
                                    currentpageIndex
                                )

                                //Add the Recommendations Block Item
                                recommendationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentpageIndex].recommendationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                recommendationsFrameworkItem.pageCode = item
                                recommendationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                recommendationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    recommendationsFrameworkItem.checkboxesFrameworkTitle
                                recommendationsFrameworkDataItem.pageCode = recommendationsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkRecommendationsInPresentSectionAllPagesFramework(
                                    recommendationsFrameworkItem,
                                    currentpageIndex
                                )
//                                for (recommendationsItem in pageTemplateItem.recommendationsList){
//                                    val recommendationDataItem = CheckboxDataItemDC()
//                                    recommendationsFrameworkDataItem.checkboxDataItemML.add(recommendationDataItem)
//                                }

                                aInfo5ViewModel.addRecommendationsFrameworkDataItemInThePresentSectionAllData(
                                    recommendationsFrameworkDataItem,
                                    currentpageIndex
                                )

                                //Add the Standards Block Item
                                standardsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentpageIndex].standardsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                standardsFrameworkItem.pageCode = item
                                standardsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                standardsFrameworkDataItem.checkboxesFrameworkTitle =
                                    standardsFrameworkItem.checkboxesFrameworkTitle
                                standardsFrameworkDataItem.pageCode = standardsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkStandardsInPresentSectionAllPagesFramework(
                                    standardsFrameworkItem,
                                    currentpageIndex
                                )
//                                for (standardsItem in pageTemplateItem.standardsList){
//                                    val standardDataItem = CheckboxDataItemDC()
//                                    standardsFrameworkDataItem.checkboxDataItemML.add(standardDataItem)
//                                }

                                aInfo5ViewModel.addStandardsFrameworkDataItemInThePresentSectionAllData(
                                    standardsFrameworkDataItem,
                                    currentpageIndex
                                )
                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                                val currentPageIndex =
                                    aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()

                                observationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].observationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                observationsFrameworkItem.pageCode = item
                                observationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                observationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    observationsFrameworkItem.checkboxesFrameworkTitle
                                observationsFrameworkDataItem.pageCode = observationsFrameworkItem.pageCode
//                                for (observationsItem in pageTemplateItem.observationsList){
//                                    val observationDataItem = CheckboxDataItemDC()
//                                    observationsFrameworkDataItem.checkboxDataItemML.add(observationDataItem)
//                                }

                                aInfo5ViewModel.addPageFrameworkObservationsInPresentSectionAllPagesFramework(
                                    observationsFrameworkItem,
                                    currentPageIndex
                                )

                                aInfo5ViewModel.addObservationsFrameworkDataItemInThePresentSectionAllData(
                                    observationsFrameworkDataItem,
                                    currentPageIndex
                                )

                                val position = binding.rvCheckboxesFramework.adapter?.itemCount
                                if (position != null) {
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(position - 1)
                                }
                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                                val pageIndex =
                                    aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()

                                recommendationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[pageIndex].recommendationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                recommendationsFrameworkItem.pageCode = item
                                recommendationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                recommendationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    recommendationsFrameworkItem.checkboxesFrameworkTitle
                                recommendationsFrameworkDataItem.pageCode = recommendationsFrameworkItem.pageCode
//                                for (recommendationsItem in pageTemplateItem.recommendationsList){
//                                    val recommendationDataItem = CheckboxDataItemDC()
//                                    recommendationsFrameworkDataItem.checkboxDataItemML.add(recommendationDataItem)
//                                }

                                aInfo5ViewModel.addPageFrameworkRecommendationsInPresentSectionAllPagesFramework(
                                    recommendationsFrameworkItem,
                                    pageIndex
                                )

                                aInfo5ViewModel.addRecommendationsFrameworkDataItemInThePresentSectionAllData(
                                    recommendationsFrameworkDataItem,
                                    pageIndex
                                )

                                val position = binding.rvCheckboxesFramework.adapter?.itemCount
                                if (position != null) {
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(position - 1)
                                }
                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                                val pageIndex =
                                    aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()

                                standardsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[pageIndex].standardsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                standardsFrameworkItem.pageCode = item
                                standardsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                standardsFrameworkDataItem.checkboxesFrameworkTitle =
                                    standardsFrameworkItem.checkboxesFrameworkTitle
                                standardsFrameworkDataItem.pageCode = standardsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkStandardsInPresentSectionAllPagesFramework(
                                    standardsFrameworkItem,
                                    pageIndex
                                )
                                for (standardsItem in pageTemplateItem.standardsList){
                                    val standardDataItem = CheckboxDataItemDC()
                                    standardsFrameworkDataItem.checkboxDataItemML.add(standardDataItem)
                                }

                                aInfo5ViewModel.addStandardsFrameworkDataItemInThePresentSectionAllData(
                                    standardsFrameworkDataItem,
                                    pageIndex
                                )

                                val position = binding.rvCheckboxesFramework.adapter?.itemCount
                                if (position != null) {
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(position - 1)
                                }
                            }
                        }
                    }
                }
            }

            dialog.dismiss()
        }
        builder.setNegativeButton("cancel") { dialog, id ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }

    fun uniqueCodeFromQuestionsParentTemplateItemList(
        questionsFrameworkItemML: MutableList<QuestionsFrameworkItemDC>,
        blockTitle: String
    ): Int {
        var result = 0
        val numberList = mutableListOf<Int>()
        for (item in questionsFrameworkItemML) {
            if (item.questionsFrameworkTitle.contains(blockTitle)) {
                val itemNumber = item.questionsFrameworkTitle.replace(blockTitle, "").trim()
                if (itemNumber != "") {
                    numberList.add(itemNumber.toInt())
                }
            }
        }
        if (numberList.isNotEmpty()) {
            result = numberList.max() + 1
        } else {
            result = 1
        }
        return result
    }

    fun uniqueCodeFromCheckboxParentTemplateItemList(
        inputMutableList: MutableList<CheckboxesFrameworkItemDC>,
        blockTitle: String
    ): Int {
        var result = 0
        val numberList = mutableListOf<Int>()
        for (item in inputMutableList) {
            if (item.checkboxesFrameworkTitle.contains(blockTitle)) {
                val itemNumber = item.checkboxesFrameworkTitle.replace(blockTitle, "").trim()
                if (itemNumber != "") {
                    numberList.add(itemNumber.toInt())
                }
            }
        }
        if (numberList.isNotEmpty()) {
            result = numberList.max() + 1
        } else {
            result = 1
        }

        return result
    }

    private fun loadQuestionsRecyclerView(currentPageIndex: Int) {
        //val sectionPagesFrameworkListIndex = 1
        val questionsFrameworkList =
            aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].questionsFrameworkList
        for (item in questionsFrameworkList) {
            item.isExpandable = false
        }
        binding.rvQuestionsFramework.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvQuestionsFramework.adapter =
            QuestionsFrameworkRVAdapter(
                questionsFrameworkList,
                aInfo5ViewModel,
                { selectedQuestionsFrameworkTitle: String, selectedQuestionsListPosition: Int ->
                    questionsListItemClickedForRemoval(
                        selectedQuestionsFrameworkTitle,
                        selectedQuestionsListPosition
                    )
                },
                { selectedPageCode -> generateQuestionsTemplateList(selectedPageCode) })

    }

    fun generateQuestionsTemplateList(pageCode: String) {
        if (aInfo5ViewModel.isItemPresentInPageTemplateList(pageCode) == false) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(pageCode)
        } else {
            aInfo5ViewModel.setTheQuestionsListMLD(aInfo5ViewModel.getItemFromPageTemplateList(pageCode).questionsList)
        }
    }

    private fun questionsListItemClickedForRemoval(
        selectedQuestionsListTitle: String,
        selectedQuestionsListPosition: Int
    ) {
        questionsListDeletionDialog(
            selectedQuestionsListTitle,
            selectedQuestionsListPosition
        )
    }

    private fun questionsListDeletionDialog(
        questionsListTitle: String,
        selectedQuestionsListPosition: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Do you really want to delete $questionsListTitle?")

        builder.setPositiveButton("Yes") { dialog, id ->
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            aInfo5ViewModel.deletePageFrameworkQuestionsInPresentSectionAllPagesFramework(
                currentPageIndex,
                selectedQuestionsListPosition
            )
            aInfo5ViewModel.deleteQuestionsFrameworkDataItemInThePresentSectionAllData(
                currentPageIndex,
                selectedQuestionsListPosition
            )
            reloadingPage(currentPageIndex)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun loadObservationsRecyclerView(currentPageIndex: Int) {
        val observationsFrameworkList =
            aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].observationsFrameworkList
        for (item in observationsFrameworkList) {
            item.isExpandable = false
        }
        binding.rvCheckboxesFramework.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvCheckboxesFramework.adapter =
            CheckboxesFrameworkRVAdapter(
                observationsFrameworkList,
                aInfo5ViewModel,
                { selectedObservationsListTitle: String, selectedObservationsListPosition: Int ->
                    observationsItemClickedForRemoval(
                        selectedObservationsListTitle,
                        selectedObservationsListPosition
                    )
                },
                { selectedPageCode -> generateObservationsTemplateList(selectedPageCode) })
    }

    fun generateObservationsTemplateList(pageCode: String) {
        if (aInfo5ViewModel.isItemPresentInPageTemplateList(pageCode) == false) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(pageCode)
        } else {
            aInfo5ViewModel.setTheObservationsListMLD(aInfo5ViewModel.getItemFromPageTemplateList(pageCode).observationsList)
        }

    }

    private fun observationsItemClickedForRemoval(
        selectedObservationsListTitle: String,
        selectedObservationsListPosition: Int
    ) {
        observationsListDeletionDialog(
            selectedObservationsListTitle,
            selectedObservationsListPosition
        )

    }

    private fun observationsListDeletionDialog(
        observationsBlockTitle: String,
        selectedObservationsBlockPosition: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Do you really want to delete $observationsBlockTitle?")

        builder.setPositiveButton("Yes") { dialog, id ->
            val sectionPagesFrameworkIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            aInfo5ViewModel.deletePageFrameworkObservationsInPresentSectionAllPagesFramework(
                sectionPagesFrameworkIndex,
                selectedObservationsBlockPosition
            )
            aInfo5ViewModel.deleteObservationsFrameworkDataItemInThePresentSectionAllData(
                sectionPagesFrameworkIndex,
                selectedObservationsBlockPosition
            )
            reloadingPage(sectionPagesFrameworkIndex)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun loadRecommendationsRecyclerView(currentPageIndex: Int) {
        val recommendationsFrameworkList =
            aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].recommendationsFrameworkList
        for (item in recommendationsFrameworkList) {
            item.isExpandable = false
        }
        binding.rvCheckboxesFramework.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvCheckboxesFramework.adapter =
            CheckboxesFrameworkRVAdapter(
                recommendationsFrameworkList,
                aInfo5ViewModel,
                { selectedRecommendationsListTitle: String, selectedRecommendationsListPosition: Int ->
                    recommendationsItemClickedForRemoval(
                        selectedRecommendationsListTitle,
                        selectedRecommendationsListPosition
                    )
                },
                { selectedPageCode -> generateRecommendationsTemplateList(selectedPageCode) })
    }

    fun generateRecommendationsTemplateList(pageCode: String) {
        if (aInfo5ViewModel.isItemPresentInPageTemplateList(pageCode) == false) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(pageCode)
        } else {
            aInfo5ViewModel.setTheRecommendationsListMLD(
                aInfo5ViewModel.getItemFromPageTemplateList(
                    pageCode
                ).recommendationsList
            )
        }
    }

    private fun recommendationsItemClickedForRemoval(
        selectedRecommendationsListTitle: String,
        selectedRecommendationsListPosition: Int
    ) {
        recommendationsListDeletionDialog(
            selectedRecommendationsListTitle,
            selectedRecommendationsListPosition
        )

    }

    private fun recommendationsListDeletionDialog(
        recommendationsBlockTitle: String,
        selectedRecommendationsBlockPosition: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Do you really want to delete $recommendationsBlockTitle?")

        builder.setPositiveButton("Yes") { dialog, id ->
            val sectionPagesFrameworkIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            aInfo5ViewModel.deletePageFrameworkRecommendationsInPresentSectionAllPagesFramework(
                sectionPagesFrameworkIndex,
                selectedRecommendationsBlockPosition
            )
            aInfo5ViewModel.deleteRecommendationsFrameworkDataItemInThePresentSectionAllData(
                sectionPagesFrameworkIndex,
                selectedRecommendationsBlockPosition
            )
            reloadingPage(sectionPagesFrameworkIndex)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun loadStandardsRecyclerView(currentPageIndex: Int) {
        val standardsCheckboxParentTemplateList =
            aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].standardsFrameworkList
        for (item in standardsCheckboxParentTemplateList) {
            item.isExpandable = false
        }
        binding.rvCheckboxesFramework.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvCheckboxesFramework.adapter =
            CheckboxesFrameworkRVAdapter(
                standardsCheckboxParentTemplateList,
                aInfo5ViewModel,
                { selectedStandardsListTitle: String, selectedStandardsListPosition: Int ->
                    standardsItemClickedForRemoval(
                        selectedStandardsListTitle,
                        selectedStandardsListPosition
                    )
                },
                { selectedPageCode -> generateStandardsTemplateList(selectedPageCode) })
    }

    fun generateStandardsTemplateList(pageCode: String) {
        if (aInfo5ViewModel.isItemPresentInPageTemplateList(pageCode) == false) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(pageCode)
        } else {
            aInfo5ViewModel.setTheStandardsListMLD(
                aInfo5ViewModel.getItemFromPageTemplateList(
                    pageCode
                ).standardsList
            )
        }
    }

    private fun standardsItemClickedForRemoval(
        selectedStandardsListTitle: String,
        selectedStandardsListPosition: Int
    ) {
        standardsListDeletionDialog(
            selectedStandardsListTitle,
            selectedStandardsListPosition
        )

    }

    private fun standardsListDeletionDialog(
        standardsListTitle: String,
        selectedStandardsListPosition: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Do you really want to delete $standardsListTitle?")

        builder.setPositiveButton("Yes") { dialog, id ->
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            aInfo5ViewModel.deletePageFrameworkStandardsInPresentSectionAllPagesFramework(
                currentPageIndex,
                selectedStandardsListPosition
            )
            aInfo5ViewModel.deleteStandardsFrameworkDataItemInThePresentSectionAllData(
                currentPageIndex,
                selectedStandardsListPosition
            )
            reloadingPage(currentPageIndex)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun reloadingPage(currentPageIndex: Int) {
        if (currentPageIndex >= 0) {
            aInfo5ViewModel.setThePageCountMLD(currentPageIndex + 1)
            if (aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.isNotEmpty()) {
                aInfo5ViewModel.setTheEtPageNameMLD(aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].pageTitle)
                aInfo5ViewModel.updatePageFrameworkNumberInPresentSectionAllPagesFramework(currentPageIndex + 1, currentPageIndex)
                aInfo5ViewModel.updatePageNumberInSectionPageDataInPresentSectionAllData(currentPageIndex + 1, currentPageIndex)
            }
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                loadQuestionsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Questions_Below))
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                loadObservationsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Observations_Below))
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                loadRecommendationsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Recommendations_Below))
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                loadStandardsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.setText(getString(R.string.string_Standards_Below))
            }
            loadPageData(currentPageIndex)
        }
    }

    fun loadPageData(currentPageIndex: Int) {
        aInfo5ViewModel.etObservationsMLD.value = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex].observations
        aInfo5ViewModel.etRecommendationsMLD.value = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendations
        aInfo5ViewModel.tvStandardsMLD.value = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex].standards
        aInfo5ViewModel.tvPhotoPathsInObservationsFragmentMLD.value = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex].photoPaths

    }

    //Note that this function also loads the template MLDs
    fun getThePageTemplateAndUploadIntoViewModel(pageCode: String){
        val pageCodeML = mutableListOf(pageCode)
        aInfo5ViewModel.getAInfo5TemplatesByIds(pageCodeML).observe(viewLifecycleOwner){list ->
            var pageTemplateString = ""
            if (list.isEmpty()) {
                pageTemplateString = ""
            } else {
                for (item1 in list) {
                    pageTemplateString += item1.template_string
                }
            }
            aInfo5ViewModel.dbStringToPageTemplateAndAddingToTemplatesList(pageCode, pageTemplateString)
        }
    }

    private fun showDialogForPhotoModification(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Choose Photos from Present Location/All ")
            .setMessage("Press \" All\" to choose from All Photos")
            .setPositiveButton("Location") { dialog, _ ->
                val location = aInfo5ViewModel.makeLocationForPhotos(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
                aInfo5ViewModel.setLocationForPhotos(location)
                aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
                aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
                findNavController().navigate(R.id.action_observationsFragment_to_photoDisplayRecyclerviewFragment)
                dialog.dismiss()
            }
            .setNeutralButton("All") { dialog, _ ->
                aInfo5ViewModel.setLocationForPhotos("All")
                aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getThePreviousScreenVariable())
                aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
                aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT)
                findNavController().navigate(R.id.action_observationsFragment_to_photoDisplayRecyclerviewFragment)
                dialog.dismiss()
            }
        builder.create().show()
    }

    fun expandAndCollapseViews(){
        var isActionButtonsExpanded = true

        fun setActionButtonsExpandedValue(input: Boolean) {
            isActionButtonsExpanded = input
        }

        fun getActionButtonsExpandedValue(): Boolean {
            return isActionButtonsExpanded
        }

        binding.tvPageNameAndNumber.setOnClickListener {
            setActionButtonsExpandedValue(!getActionButtonsExpandedValue())
            if (getActionButtonsExpandedValue()) {
                binding.llActionButtons1.visibility = View.VISIBLE
                binding.llActionButtons2.visibility = View.VISIBLE
                binding.etModifyPageNameByUser.visibility = View.VISIBLE
                binding.tvPageNameAndNumber.setBackgroundResource(R.drawable.border1dp_with_up_arrow)
            } else {
                binding.llActionButtons1.visibility = View.GONE
                binding.llActionButtons2.visibility = View.GONE
                binding.etModifyPageNameByUser.visibility = View.GONE
                binding.tvPageNameAndNumber.setBackgroundResource(R.drawable.border1dp_with_down_arrow)
            }
        }

        var isObsRecoExpanded = true
        fun setObsRecoExpandedValue(input: Boolean) {
            isObsRecoExpanded = input
        }

        fun getObsRecoExpandedValue(): Boolean {
            return isObsRecoExpanded
        }
        binding.tvObservationsRecommendationsLabel.setOnClickListener {
            setObsRecoExpandedValue(!getObsRecoExpandedValue())
            if (getObsRecoExpandedValue()) {
                binding.svObservationsRecommendations.visibility = View.VISIBLE
                binding.tvObservationsRecommendationsLabel.setBackgroundResource(R.drawable.border1dp_color_purple700_with_up_arrow)
            } else {
                binding.svObservationsRecommendations.visibility = View.GONE
                binding.tvObservationsRecommendationsLabel.setBackgroundResource(R.drawable.border1dp_color_purple700_with_down_arrow)
            }
        }

        var isObsExpanded = true
        fun setObsExpandedValue(input: Boolean) {
            isObsExpanded = input
        }

        fun getObsExpandedValue(): Boolean {
            return isObsExpanded
        }

        binding.tvObservationsPicturesLabel.setOnClickListener {
            setObsExpandedValue(!getObsExpandedValue())
            if (getObsExpandedValue()) {
                binding.tvPhotoPathsInObservationsPage.visibility = View.VISIBLE
                binding.etObservationsOnly.visibility = View.VISIBLE
                binding.tvObservationsPicturesLabel.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow)
            } else {
                binding.tvPhotoPathsInObservationsPage.visibility = View.GONE
                binding.etObservationsOnly.visibility = View.GONE
                binding.tvObservationsPicturesLabel.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow)
            }
        }

        var isRecoExpanded = true
        fun setRecoExpandedValue(input: Boolean) {
            isRecoExpanded = input
        }

        fun getRecoExpandedValue(): Boolean {
            return isRecoExpanded
        }
        binding.tvRecommendationsStandardsLabel.setOnClickListener {
            setRecoExpandedValue(!getRecoExpandedValue())
            if (getRecoExpandedValue()) {
                binding.tvStandardsInObservationsPage.visibility = View.VISIBLE
                binding.etRecommendationsOnly.visibility = View.VISIBLE
                binding.tvRecommendationsStandardsLabel.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow)
            } else {
                binding.tvStandardsInObservationsPage.visibility = View.GONE
                binding.etRecommendationsOnly.visibility = View.GONE
                binding.tvRecommendationsStandardsLabel.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow)
            }
        }

        var isQsObsRecoStdsLabelExpanded = true
        fun setQsObsRecoStdsLabelExpandedValue(input: Boolean) {
            isQsObsRecoStdsLabelExpanded = input
        }

        fun getQsObsRecoStdsLabelExpandedValue(): Boolean {
            return isQsObsRecoStdsLabelExpanded
        }

        binding.tvQuestionsEtcLabel.setOnClickListener {
            setQsObsRecoStdsLabelExpandedValue(!getQsObsRecoStdsLabelExpandedValue())
            if (getQsObsRecoStdsLabelExpandedValue()) {
                if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                    binding.rvQuestionsFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                }
                binding.tvQuestionsEtcLabel.setBackgroundResource(R.drawable.border1dp_color_purple700_with_up_arrow)
            } else {
                if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                    binding.rvQuestionsFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                }
                binding.tvQuestionsEtcLabel.setBackgroundResource(R.drawable.border1dp_color_purple700_with_down_arrow)
            }
        }



    }



}
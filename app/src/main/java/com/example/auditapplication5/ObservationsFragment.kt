package com.example.auditapplication5

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentObservationsBinding
import com.example.auditapplication5.presentation.adapter.CheckboxesFrameworkRVAdapter
import com.example.auditapplication5.presentation.adapter.QuestionsFrameworkRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlin.properties.Delegates


class ObservationsFragment : Fragment() {
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    private var presentSectionAllPagesFrameworkIndex by Delegates.notNull<Int>()
    private lateinit var presentSectionAllPagesFramework: SectionAllPagesFrameworkDC
    private lateinit var presentSectionAllData: SectionAllDataDC
    private lateinit var pageTemplateList: MutableList<PageTemplateDC>
    private lateinit var uniqueListOfSectionPageCodes: MutableList<String>
    private lateinit var sectionChildPageCodesList: MutableList<String>

    val TAG = MainActivity.TESTING_TAG

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val TAG = MainActivity.TESTING_TAG

        presentSectionAllPagesFrameworkIndex =
            aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
        presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
        presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        pageTemplateList = aInfo5ViewModel.getThePageTemplateList()
        uniqueListOfSectionPageCodes = aInfo5ViewModel.getTheUniqueListOfSectionPageCodes()



        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                        //saveIntoDB()
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                        findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                        var currentPageIndex = 0
                        currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                        } else {
                            presentSectionAllPagesFrameworkIndex
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.text = getString(R.string.string_Questions_Below)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                        var currentPageIndex = 0
                        currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                        } else {
                            presentSectionAllPagesFrameworkIndex
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.text = getString(R.string.string_Questions_Below)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }
                    else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                        var currentPageIndex = 0
                        currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                        } else {
                            presentSectionAllPagesFrameworkIndex
                        }
                        binding.rvQuestionsFramework.visibility = View.VISIBLE
                        binding.rvCheckboxesFramework.visibility = View.GONE
                        binding.tvQuestionsEtcLabel.text = getString(R.string.string_Questions_Below)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        loadQuestionsRecyclerView(currentPageIndex)
                    }
                }
            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Initialise Some LD Flags for CameraX etc
        initialiseSomeFlagsLD()

        //Check things here



        //Set the pages present in the Section Code and Display to be True
        aInfo5ViewModel.changePagesPresentToTrueInCompanySectionCodeAndDisplayNameList(
            aInfo5ViewModel.getPresentSectionCode()
        )
        val companySectionsCDListID =
            aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
        aInfo5ViewModel.saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionsCDListID)

        //Get the Section Templates in Place
        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE){
            aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(false)
            aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(false)
            if (uniqueListOfSectionPageCodes.isNotEmpty()){
                //Ensure that the Progress bar is visible
                binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                binding.llObservationsActionbuttonsAndViews.isEnabled = false
                binding.fabAddANewBlock.isEnabled = false
                getThePageTemplateAndUploadIntoViewModel(uniqueListOfSectionPageCodes, presentSectionAllPagesFramework, presentSectionAllData)
            }
            else {
                aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(true)
            }
        }
        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.RV_PARENT_CHILD_FRAGMENT){
            if (aInfo5ViewModel.getTheFrameworkUpdatedInParentChildRVFragmentFlag()){
                aInfo5ViewModel.setTheFrameworkUpdatedInParentChildRVFragmentFlag(false)
                aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(false)
                aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(false)
                if (uniqueListOfSectionPageCodes.isNotEmpty()){
                    //Ensure that the Progress bar is visible
                    binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                    binding.llObservationsActionbuttonsAndViews.isEnabled = false
                    binding.fabAddANewBlock.isEnabled = false
                    getThePageTemplateAndUploadIntoViewModel(uniqueListOfSectionPageCodes, presentSectionAllPagesFramework, presentSectionAllData)
                }
                else {
                    aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(true)
                }
            }
            else {
                aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(true)
                aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
            }
        }

        //Get All the SectionCode Related Templates for Checklist Report
        val currentSectionCode = aInfo5ViewModel.getThePresentSectionCodeAndDisplayName().uniqueCodeName
        val currentPageGroupCode = processSectionCode(currentSectionCode)
        val parentChildParentItemML = aInfo5ViewModel.getTheParentChildParentItemML()
        sectionChildPageCodesList = parentChildParentItemML
            .find { it.pageGroupCode == currentPageGroupCode }
            ?.childItemList ?: mutableListOf()

        val presentInTemplateList = mutableListOf<Int>()
        if (sectionChildPageCodesList.isNotEmpty()){
            for (item in sectionChildPageCodesList){
                if (aInfo5ViewModel.isItemPresentInPageTemplateList(item)){
                    presentInTemplateList.add(1)
                }
            }
            if (presentInTemplateList.size != sectionChildPageCodesList.size){
                aInfo5ViewModel.allSectionTemplatesUploadedForChecklistFlagLD.observe(viewLifecycleOwner){
                    if (it == false){
                        getThePageTemplatesForCheckListReport(sectionChildPageCodesList)
                    }
                }
            }
            else  {
                aInfo5ViewModel.setTheAllSectionTemplatesUploadedForChecklistFlagMLD(true)
            }
        }
        else {
            aInfo5ViewModel.setTheAllSectionTemplatesUploadedForChecklistFlagMLD(true)
        }


        //Set process of getting the questions view etc
        aInfo5ViewModel.allConditionsMetLD.observe(viewLifecycleOwner) { allMet ->
            if (allMet == true) {
                binding.tvPbMessagesObservationsFragment.visibility = View.GONE
                //Set Up the Questions View Now
                if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                    binding.rvQuestionsFramework.visibility = View.VISIBLE
                    binding.rvCheckboxesFramework.visibility = View.GONE
                    if (presentSectionAllPagesFrameworkIndex < 0) {
                        presentSectionAllPagesFrameworkIndex = 0
                    }
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)

                    if (presentSectionAllPagesFrameworkIndex < presentSectionAllPagesFramework.sectionPageFrameworkList.size) {
                        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size == presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex
                            )
                        }
                        else {
                            val presentSectionAllPagesFrameworkIndex1 =
                                minOf(
                                    presentSectionAllPagesFrameworkIndex,
                                    presentSectionAllData.sectionAllPagesData.sectionPageDataList.size
                                )
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex1
                            )
                            presentSectionAllPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex1
                        }
                    }
                    else {
                        presentSectionAllPagesFrameworkIndex =
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size == presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex
                            )
                        }
                        else {
                            val presentSectionAllPagesFrameworkIndex1 =
                                minOf(
                                    presentSectionAllPagesFrameworkIndex,
                                    presentSectionAllData.sectionAllPagesData.sectionPageDataList.size
                                )
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex1
                            )
                            presentSectionAllPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex1
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.RV_PARENT_CHILD_FRAGMENT) {
                    binding.rvQuestionsFramework.visibility = View.VISIBLE
                    binding.rvCheckboxesFramework.visibility = View.GONE
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)

                    if (presentSectionAllPagesFrameworkIndex < presentSectionAllPagesFramework.sectionPageFrameworkList.size) {
                        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size == presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex
                            )
                        } else {
                            val presentSectionAllPagesFrameworkIndex1 =
                                minOf(
                                    presentSectionAllPagesFrameworkIndex,
                                    presentSectionAllData.sectionAllPagesData.sectionPageDataList.size
                                )
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex1
                            )
                            presentSectionAllPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex1
                        }
                    }
                    else {
                        presentSectionAllPagesFrameworkIndex =
                            aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList.size - 1
                        if (presentSectionAllPagesFramework.sectionPageFrameworkList.size == presentSectionAllData.sectionAllPagesData.sectionPageDataList.size) {
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex
                            )
                        }
                        else {
                            val presentSectionAllPagesFrameworkIndex1 =
                                minOf(
                                    presentSectionAllPagesFrameworkIndex,
                                    presentSectionAllData.sectionAllPagesData.sectionPageDataList.size
                                )
                            reloadingPage(
                                presentSectionAllPagesFrameworkIndex1
                            )
                            presentSectionAllPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex1
                        }
                    }
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.GOTO_RECYCLERVIEW_FRAGMENT) {
                    aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
                    if (presentSectionAllPagesFrameworkIndex < presentSectionAllPagesFramework.sectionPageFrameworkList.size) {
                        reloadingPage(presentSectionAllPagesFrameworkIndex)
                    } else {
                        presentSectionAllPagesFrameworkIndex =
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                        reloadingPage(presentSectionAllPagesFrameworkIndex)
                    }
                }
            }
            else {
//                Log.d(TAG, "onViewCreated:Obs fragment \n" +
//                        "parentChildParentItemMLUploadedMLD: ${aInfo5ViewModel.parentChildParentItemMLUploadedMLD.value.toString()} \n" +
//                        "sectionAllPagesFrameworkLoadedFlagLD: ${aInfo5ViewModel.sectionAllPagesFrameworkLoadedFlagLD.value.toString()}\n" +
//                        "sectionAllDataLoadedFlagLD: ${aInfo5ViewModel.sectionAllDataLoadedFlagLD.value.toString()}\n" +
//                        "allTemplatesUploadedFlagMLD: ${aInfo5ViewModel.allTemplatesUploadedFlagMLD.value.toString()}\n" +
//                        "editCompletedFlagLD: ${aInfo5ViewModel.editCompletedFlagLD.value.toString()}\n" +
//                        "deleteCompletedFlagLD: ${aInfo5ViewModel.deleteCompletedFlagLD.value.toString()}")
                //Ensure that the Progress bar is visible
                binding.pbUploadingFromDatabase.visibility = View.VISIBLE
                binding.tvPbMessagesObservationsFragment.visibility = View.VISIBLE
                binding.tvPbMessagesObservationsFragment.text = getString(R.string.string_message_loading_from_db)
                binding.llObservationsActionbuttonsAndViews.isEnabled = false
                binding.fabAddANewBlock.isEnabled = false
            }
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
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.buttonQuestionsView.startAnimation(animation)
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            var currentPageIndex = 0
            currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                } else {
                    0
                }
            } else {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFrameworkIndex
                } else {
                    0
                }
            }
            //aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
            binding.rvQuestionsFramework.visibility = View.VISIBLE
            binding.rvCheckboxesFramework.visibility = View.GONE
            binding.tvQuestionsEtcLabel.text = getString(R.string.string_Questions_Below)
            binding.tvQuestionsEtcLabel.startAnimation(animation)
            loadQuestionsRecyclerView(currentPageIndex)
        }

        binding.buttonObservationsView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.buttonObservationsView.startAnimation(animation)
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS)
            var currentPageIndex = 0
            currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                } else {
                    0
                }
            } else {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFrameworkIndex
                } else {
                    0
                }
            }
            //aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
            binding.tvQuestionsEtcLabel.text = getString(R.string.string_Observations_Below)
            binding.tvQuestionsEtcLabel.startAnimation(animation)
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadObservationsRecyclerView(currentPageIndex)
        }

        binding.buttonRecommendationsView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.buttonRecommendationsView.startAnimation(animation)
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS)
            var currentPageIndex = 0
            currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                } else {
                    0
                }
            } else {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFrameworkIndex
                } else {
                    0
                }
            }
            binding.tvQuestionsEtcLabel.text = getString(R.string.string_Recommendations_Below)
            binding.tvQuestionsEtcLabel.startAnimation(animation)
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadRecommendationsRecyclerView(currentPageIndex)
        }

        binding.buttonStandardsView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.buttonStandardsView.startAnimation(animation)
            aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS)
            var currentPageIndex = 0
            currentPageIndex = if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_CHOICE) {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
                } else {
                    0
                }
            } else {
                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0) {
                    presentSectionAllPagesFrameworkIndex
                } else {
                    0
                }
            }
            binding.tvQuestionsEtcLabel.text = getString(R.string.string_Standards_Below)
            binding.tvQuestionsEtcLabel.startAnimation(animation)
            binding.rvQuestionsFramework.visibility = View.GONE
            binding.rvCheckboxesFramework.visibility = View.VISIBLE
            loadStandardsRecyclerView(currentPageIndex)
        }

        //On Click Listeners for Camera and Modify Photo
        binding.ibCameraInObservationsPage.setOnClickListener {
            //Create the location, count and photograph name (without extension)
            val location =
                aInfo5ViewModel.makeLocationForPhotos(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
            aInfo5ViewModel.setLocationForPhotos(location)
            val count = aInfo5ViewModel.getPhotoCountByLocation(location) + 1
            aInfo5ViewModel.setThePhotoCount(count)
            val presentPhotoNameWithoutExtension =
                aInfo5ViewModel.makePresentPhotoName(
                    location,
                    count,
                    aInfo5ViewModel.getPresentSectionName()
                )
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

        //On Click Listener for Add a Page
        binding.ibAddAPage.setOnClickListener {
            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
                binding.etObservationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
                binding.tvPhotoPathsInObservationsPage.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
                binding.etRecommendationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
                binding.tvStandardsInObservationsPage.text.toString(),
                currentPageIndex
            )

            presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            //Save the SectionPagesFramework and Data into db
            val sectionPagesFrameworkAndDataID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(
                aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                aInfo5ViewModel.getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )

            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
            findNavController().navigate(R.id.action_observationsFragment_to_parentChildRecyclerviewFragment)
        }

        //On Click Listener for Add a Block
        binding.fabAddANewBlock.setOnClickListener {
            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            var pageCodeFromViewModel = ""
            if (presentSectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()) {
                pageCodeFromViewModel =
                    presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].pageCode
            }
            val ampCode = aInfo5ViewModel.pageCodeToAMPCode(pageCodeFromViewModel)
            ampEntriesFromTemplateDB(ampCode)
        }

        //Click Listeners for move left and move right

        binding.ibBack.setOnClickListener {

            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
                binding.etObservationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
                binding.tvPhotoPathsInObservationsPage.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
                binding.etRecommendationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
                binding.tvStandardsInObservationsPage.text.toString(),
                currentPageIndex
            )

            presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibBack.startAnimation(animation)
            //val pageIndex = aInfo5ViewModel.getValueOfSectionPagesFrameworkIndex()
            val minimumValueOfIndex = 0
            val maximumValueOfIndex =
                presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_OBS) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            }
            if (currentPageIndex > minimumValueOfIndex) {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(pageIndex - 1)
                presentSectionAllPagesFrameworkIndex = currentPageIndex - 1
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex - 1)
            } else {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(maximumValueOfIndex)
                presentSectionAllPagesFrameworkIndex = maximumValueOfIndex
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

            //Save the SectionPagesFramework and Data into db before stopping
            val sectionPagesFrameworkAndDataID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                aInfo5ViewModel.getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )

            //saveIntoDB()

            reloadingPage(presentSectionAllPagesFrameworkIndex)
        }

        binding.ibForward.setOnClickListener {

            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
                binding.etObservationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
                binding.tvPhotoPathsInObservationsPage.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
                binding.etRecommendationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
                binding.tvStandardsInObservationsPage.text.toString(),
                currentPageIndex
            )

            presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            //Save the SectionPagesFramework and Data into db before stopping
            val sectionPagesFrameworkAndDataID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                aInfo5ViewModel.getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )

            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibForward.startAnimation(animation)
            val maximumValueOfIndex =
                presentSectionAllPagesFramework.sectionPageFrameworkList.size - 1
            val minimumValueOfIndex = 0
            if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.SECTION_FRAGMENT_SECTION_OBS) {
                aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
            }
            if (currentPageIndex < maximumValueOfIndex) {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(pageIndex + 1)
                presentSectionAllPagesFrameworkIndex = currentPageIndex + 1
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex + 1)
            } else {
                //aInfo5ViewModel.setSectionPagesFrameworkIndexMLD(minimumValueOfIndex)
                presentSectionAllPagesFrameworkIndex = minimumValueOfIndex
                aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(minimumValueOfIndex)
            }

            reloadingPage(presentSectionAllPagesFrameworkIndex)
        }

        //Click Listener for Goto
        binding.buttonGoto.setOnClickListener {

            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.buttonGoto.startAnimation(animation)
            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            //Save the page title into the page structure
            aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePageTitleInSectionPageDataInPresentSectionAllData(
                aInfo5ViewModel.etPageNameMLD.value.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
                binding.etObservationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
                binding.tvPhotoPathsInObservationsPage.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
                binding.etRecommendationsOnly.text.toString(),
                currentPageIndex
            )
            aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
                binding.tvStandardsInObservationsPage.text.toString(),
                currentPageIndex
            )

            presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            //Save the SectionPagesFramework and Data into db before stopping
            val sectionPagesFrameworkAndDataID =
                aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
            aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                aInfo5ViewModel.getThePresentSectionAllData(),
                sectionPagesFrameworkAndDataID
            )

            aInfo5ViewModel.setThePreviousScreen2Variable(aInfo5ViewModel.getTheScreenVariable())
            findNavController().navigate(R.id.action_observationsFragment_to_gotoRecyclerviewFragment)
        }

        //Click Listener for Deleting a page
        binding.ibDeleteInObservationsPage.setOnClickListener {
            showDialogToDeleteAPage()
        }

        //Click Listener for Score
//        binding.buttonScore.setOnClickListener {
//
//        }


    }

    override fun onStop() {
        super.onStop()
        saveIntoDB()
    }


    //Functions below


    private fun processSectionCode(input: String): String {
        var resultString = ""
        resultString = if (input == "Section_DropoffandBuildingEntry_0004_Section"){
            "PG_Drop_Off_And_Building_Entry_PG"
        }
        else {
            val parts = input.split("_")
            if (parts.size < 2) return ""
            val secondTerm = parts[1]  // "Gates" or "MiscellaneousRooms"
            // Proper camelCase split using regex - keeps capitalization
            val words = secondTerm.split(Regex("(?<=\\p{Ll})(?=\\p{Lu})")).filter { it.isNotEmpty() }
            "PG_${words.joinToString("_")}_PG"
        }

        return resultString
    }

    private fun initialiseSomeFlagsLD(){
        aInfo5ViewModel.setThePictureUploadedCXFFlagMLD(true)
        aInfo5ViewModel.setTheVideoUploadedCXFFlagMLD(true)
    }

    fun createUniqueListOfPageCodesForCurrentPageFromFramework(currentPageIndex: Int){
        uniqueListOfSectionPageCodes = mutableListOf()
        if (presentSectionAllPagesFramework.sectionPageFrameworkList.isNotEmpty()){
            val currentPageSet = presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex]
            if (!uniqueListOfSectionPageCodes.contains(currentPageSet.pageCode)){
                uniqueListOfSectionPageCodes.add(currentPageSet.pageCode)
            }
            val questionList = currentPageSet.questionsFrameworkList
            if (questionList.isNotEmpty()){
                for (qIndex in 0 until questionList.size){
                    if (!uniqueListOfSectionPageCodes.contains(questionList[qIndex].pageCode)){
                        uniqueListOfSectionPageCodes.add(questionList[qIndex].pageCode)
                    }
                }
            }
            val obsList = currentPageSet.observationsFrameworkList
            if (obsList.isNotEmpty()){
                for (oIndex in 0 until obsList.size){
                    if (!uniqueListOfSectionPageCodes.contains(obsList[oIndex].pageCode)){
                        uniqueListOfSectionPageCodes.add(obsList[oIndex].pageCode)
                    }
                }
            }
            val recoList = currentPageSet.recommendationsFrameworkList
            if (recoList.isNotEmpty()){
                for (rIndex in 0 until recoList.size){
                    if (!uniqueListOfSectionPageCodes.contains(recoList[rIndex].pageCode)){
                        uniqueListOfSectionPageCodes.add(recoList[rIndex].pageCode)
                    }
                }
            }
            val stdsList = currentPageSet.standardsFrameworkList
            if (stdsList.isNotEmpty()){
                for (sIndex in 0 until stdsList.size){
                    if (!uniqueListOfSectionPageCodes.contains(stdsList[sIndex].pageCode)){
                        uniqueListOfSectionPageCodes.add(stdsList[sIndex].pageCode)
                    }
                }
            }
        }
    }

    private fun saveIntoDB(){
        //Save the page title into the page framework and data
        val currentPageIndex = presentSectionAllPagesFrameworkIndex
        aInfo5ViewModel.updatePageFrameworkTitleInPresentSectionAllPagesFramework(
            aInfo5ViewModel.etPageNameMLD.value.toString(),
            currentPageIndex
        )
        //Save the observations etc info into Section Page Data
        aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
            aInfo5ViewModel.etObservationsMLD.value.toString(),
            currentPageIndex
        )
        aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
            aInfo5ViewModel.tvPhotoPathsInObservationsFragmentMLD.value.toString(),
            currentPageIndex
        )
        aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
            aInfo5ViewModel.etRecommendationsMLD.value.toString(),
            currentPageIndex
        )
        aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
            aInfo5ViewModel.tvStandardsMLD.value.toString(),
            currentPageIndex
        )

        //Update and save the Company Report into db
        aInfo5ViewModel.setTheSectionPagesUpdatedInReportSIFFlagMLD(false)

            aInfo5ViewModel.updateSectionDetailsInCompanyReportAndSave(
                aInfo5ViewModel.getPresentSectionCode(),
                aInfo5ViewModel.getPresentSectionName(),
                aInfo5ViewModel.getThePresentSectionAllData(),
                sectionChildPageCodesList
            )

        presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
        presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

        //Save the SectionPagesFramework and Data into db before stopping
        val sectionPagesFrameworkAndDataID =
            aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
        aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
            aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
            aInfo5ViewModel.getThePresentSectionAllData(),
            sectionPagesFrameworkAndDataID
        )
    }

    private fun showDialogToDeleteAPage() {
        val title = "Delete this page? Note that the page data will be lost"
        val message =
            "Press \'Delete\' to delete the page. Press \'Cancel\' to cancel this operation."
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { dialog, _ ->
                val currentPageIndex = presentSectionAllPagesFrameworkIndex
                aInfo5ViewModel.deletePageFrameworkInPresentSectionAllPagesFramework(
                    currentPageIndex
                )
                aInfo5ViewModel.deleteSectionPageDataInPresentSectionAllData(currentPageIndex)
                aInfo5ViewModel.setSectionReportInCompanyReportToNilAndSave(aInfo5ViewModel.getPresentSectionCode())

                presentSectionAllPagesFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework()
                presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

                if (presentSectionAllPagesFramework.sectionPageFrameworkList.size > 0){
                    if (currentPageIndex > 0) {
                        aInfo5ViewModel.setThePageCountMLD(currentPageIndex - 1)
                        presentSectionAllPagesFrameworkIndex = currentPageIndex - 1
                        aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex - 1)
                    }
                    else if (currentPageIndex == 0) {
                        val frameworkPagesSize =
                            presentSectionAllPagesFramework.sectionPageFrameworkList.size
                        if (frameworkPagesSize > 0) {
                            aInfo5ViewModel.setThePageCountMLD(currentPageIndex )
                            presentSectionAllPagesFrameworkIndex = currentPageIndex
                            aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex )
                        }
                        else {
                            aInfo5ViewModel.setThePageCountMLD(0)
                            presentSectionAllPagesFrameworkIndex = 0
                            aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(0)
                            //Save the information into the CompanyReport suitably
                            aInfo5ViewModel.updateSectionDetailsInCompanyReportAndSave(
                                aInfo5ViewModel.getPresentSectionCode(),
                                aInfo5ViewModel.getPresentSectionName(),
                                aInfo5ViewModel.getThePresentSectionAllData()
                            )

                            findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
                        }

                    }
                    //Save the Section Page Framework and the page data before exiting
                    aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllPagesFramework()
                    aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllData()

                    val sectionPagesFrameworkAndDataID =
                        aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                    aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                        aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                        aInfo5ViewModel.getThePresentSectionAllData(),
                        sectionPagesFrameworkAndDataID
                    )
                    val newListIndex = presentSectionAllPagesFrameworkIndex
                    reloadingPage(newListIndex)
                }
                else {
                    //Save the Section Page Framework and the page data before exiting
                    val sectionPagesFrameworkAndDataID =
                        aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
                    aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
                        aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
                        aInfo5ViewModel.getThePresentSectionAllData(),
                        sectionPagesFrameworkAndDataID
                    )
                    //Set the pages present in the Section Code and Display to be False
                    aInfo5ViewModel.changePagesPresentToFalseInCompanySectionCodeAndDisplayNameList(
                        aInfo5ViewModel.getPresentSectionCode()
                    )
                    val companySectionsCDListID =
                        aInfo5ViewModel.getPresentCompanyCode() + MainActivity.COMPANY_SECTION_LIST_ID
                    aInfo5ViewModel.saveTheCompanySectionCodeAndDisplayMLIntoDB(companySectionsCDListID)
                    //Navigate to the SectionAndIntrosFragment suitably
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                    aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.NOT_RELEVANT)
                    findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
                }
                dialog.dismiss()
            }
//            .setNegativeButton("Clear"){ dialog, _ ->
//                val currentPageIndex = presentSectionAllPagesFrameworkIndex
//                binding.etObservationsOnly.text.clear()
//                aInfo5ViewModel.updateObservationsInObsForThePresentSectionAllData(
//                    binding.etObservationsOnly.text.toString(),
//                    currentPageIndex
//                )
//                binding.tvPhotoPathsInObservationsPage.text = ""
//                aInfo5ViewModel.updatePicturePathsInObsForThePresentSectionAllData(
//                    binding.tvPhotoPathsInObservationsPage.text.toString(),
//                    currentPageIndex
//                )
//                binding.etRecommendationsOnly.text.clear()
//                aInfo5ViewModel.updateRecommendationsInObsForThePresentSectionAllData(
//                    binding.etRecommendationsOnly.text.toString(),
//                    currentPageIndex
//                )
//                binding.tvStandardsInObservationsPage.text = ""
//                aInfo5ViewModel.updateStandardsInObsForThePresentSectionAllData(
//                    binding.tvStandardsInObservationsPage.text.toString(),
//                    currentPageIndex
//                )
//
//                //Save the Section Page Framework and the page data before exiting
//                val sectionPagesFrameworkAndDataID =
//                    aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
//                aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
//                    aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
//                    aInfo5ViewModel.getThePresentSectionAllData(),
//                    sectionPagesFrameworkAndDataID
//                )
//
//                dialog.dismiss()
//            }
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
                val ampIDML = mutableListOf(ampCode)
                val ampListStringValue = aInfo5ViewModel.getAInfo5TemplatesByIds(ampIDML)
                ampListStringValue.observe(viewLifecycleOwner) { list ->
                    if (list.isEmpty()) {
                        ampValueListString = ""
                    }
                    else {
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
            multiChoiceList[multiChoiceList.keys.toTypedArray().get(which)] = isChecked
        }

        builder.setPositiveButton("ok") { dialog, id ->
            //var pageCodePresentInTemplateResult = 0
            for (selection in multiChoiceList) {
                val questionsFrameworkItem = QuestionsFrameworkItemDC()
                val observationsFrameworkItem = CheckboxesFrameworkItemDC()
                val recommendationsFrameworkItem = CheckboxesFrameworkItemDC()
                val standardsFrameworkItem = CheckboxesFrameworkItemDC()

                val questionsFrameworkDataItem = QuestionsFrameworkDataItemDC()
                val observationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val recommendationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val standardsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                
                if (selection.value) {
                    for (item in inputList) {
                        if (!uniqueListOfSectionPageCodes.contains(item)){
                            aInfo5ViewModel.uniqueListOfSectionPageCodes.add(item)
                            uniqueListOfSectionPageCodes.add(item)
                        }
                        if (aInfo5ViewModel.extractDisplayNameFromPageCode(item) == selection.key && item.contains(
                                "PC_"
                            )
                        ) {

                            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                                val currentpageIndex = presentSectionAllPagesFrameworkIndex

                                questionsFrameworkItem.questionsFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromQuestionsParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[currentpageIndex].questionsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()

                                questionsFrameworkItem.pageCode = item
                                questionsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                questionsFrameworkDataItem.questionsFrameworkTitle =
                                    questionsFrameworkItem.questionsFrameworkTitle
                                questionsFrameworkDataItem.pageCode =
                                    questionsFrameworkItem.pageCode

                                val positionOfQuestionsFrameworkList =
                                    binding.rvQuestionsFramework.adapter?.itemCount
                                aInfo5ViewModel.addPageFrameworkQuestionsInPresentSectionAllPagesFramework(
                                    questionsFrameworkItem,
                                    currentpageIndex
                                )
                                aInfo5ViewModel.addQuestionsFrameworkDataItemInThePresentSectionAllData(
                                    questionsFrameworkDataItem,
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
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[currentpageIndex].observationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                observationsFrameworkItem.pageCode = item
                                observationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                observationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    observationsFrameworkItem.checkboxesFrameworkTitle
                                observationsFrameworkDataItem.pageCode =
                                    observationsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkObservationsInPresentSectionAllPagesFramework(
                                    observationsFrameworkItem,
                                    currentpageIndex
                                )

                                aInfo5ViewModel.addObservationsFrameworkDataItemInThePresentSectionAllData(
                                    observationsFrameworkDataItem,
                                    currentpageIndex
                                )

                                //Add the Recommendations Block Item
                                recommendationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[currentpageIndex].recommendationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                recommendationsFrameworkItem.pageCode = item
                                recommendationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                recommendationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    recommendationsFrameworkItem.checkboxesFrameworkTitle
                                recommendationsFrameworkDataItem.pageCode =
                                    recommendationsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkRecommendationsInPresentSectionAllPagesFramework(
                                    recommendationsFrameworkItem,
                                    currentpageIndex
                                )

                                aInfo5ViewModel.addRecommendationsFrameworkDataItemInThePresentSectionAllData(
                                    recommendationsFrameworkDataItem,
                                    currentpageIndex
                                )

                                //Add the Standards Block Item
                                standardsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[currentpageIndex].standardsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                standardsFrameworkItem.pageCode = item
                                standardsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                standardsFrameworkDataItem.checkboxesFrameworkTitle =
                                    standardsFrameworkItem.checkboxesFrameworkTitle
                                standardsFrameworkDataItem.pageCode =
                                    standardsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkStandardsInPresentSectionAllPagesFramework(
                                    standardsFrameworkItem,
                                    currentpageIndex
                                )

                                aInfo5ViewModel.addStandardsFrameworkDataItemInThePresentSectionAllData(
                                    standardsFrameworkDataItem,
                                    currentpageIndex
                                )

                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                                val currentPageIndex = presentSectionAllPagesFrameworkIndex

                                observationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].observationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                observationsFrameworkItem.pageCode = item
                                observationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                observationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    observationsFrameworkItem.checkboxesFrameworkTitle
                                observationsFrameworkDataItem.pageCode =
                                    observationsFrameworkItem.pageCode

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
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(
                                        position - 1
                                    )
                                }
                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                                val pageIndex = presentSectionAllPagesFrameworkIndex

                                recommendationsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].recommendationsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                recommendationsFrameworkItem.pageCode = item
                                recommendationsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                recommendationsFrameworkDataItem.checkboxesFrameworkTitle =
                                    recommendationsFrameworkItem.checkboxesFrameworkTitle
                                recommendationsFrameworkDataItem.pageCode =
                                    recommendationsFrameworkItem.pageCode

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
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(
                                        position - 1
                                    )
                                }
                            }
                            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                                val pageIndex = presentSectionAllPagesFrameworkIndex

                                standardsFrameworkItem.checkboxesFrameworkTitle =
                                    aInfo5ViewModel.extractDisplayNameFromPageCode(item) + " " + uniqueCodeFromCheckboxParentTemplateItemList(
                                        presentSectionAllPagesFramework.sectionPageFrameworkList[pageIndex].standardsFrameworkList,
                                        aInfo5ViewModel.extractDisplayNameFromPageCode(item)
                                    ).toString()
                                standardsFrameworkItem.pageCode = item
                                standardsFrameworkItem.serialStatus =
                                    MainActivity.OTHER_QUESTION_SET

                                standardsFrameworkDataItem.checkboxesFrameworkTitle =
                                    standardsFrameworkItem.checkboxesFrameworkTitle
                                standardsFrameworkDataItem.pageCode =
                                    standardsFrameworkItem.pageCode

                                aInfo5ViewModel.addPageFrameworkStandardsInPresentSectionAllPagesFramework(
                                    standardsFrameworkItem,
                                    pageIndex
                                )

                                aInfo5ViewModel.addStandardsFrameworkDataItemInThePresentSectionAllData(
                                    standardsFrameworkDataItem,
                                    pageIndex
                                )

                                val position = binding.rvCheckboxesFramework.adapter?.itemCount
                                if (position != null) {
                                    binding.rvCheckboxesFramework.adapter?.notifyItemChanged(
                                        position - 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(false)
            //aInfo5ViewModel.setTheAllTemplatesUploadedFlagMLD(false)
            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData =
                aInfo5ViewModel.getThePresentSectionAllData()
            getThePageTemplateAndUploadIntoViewModel(uniqueListOfSectionPageCodes, presentSectionAllPagesFramework, presentSectionAllData)

            dialog.dismiss()
        }
        builder.setNegativeButton("cancel") { dialog, id ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }

    private fun uniqueCodeFromQuestionsParentTemplateItemList(
        questionsFrameworkItemML: MutableList<QuestionsFrameworkItemDC>,
        blockTitle: String
    ): Int {
        var result = 0
        val numberList = mutableListOf<Int>()
        if (questionsFrameworkItemML.isNotEmpty()) {
            for (item in questionsFrameworkItemML) {
                if (item.questionsFrameworkTitle.contains(blockTitle)) {
                    val itemNumber = item.questionsFrameworkTitle.replace(blockTitle, "").trim()
                    if (itemNumber.toIntOrNull() != null) {
                        numberList.add(itemNumber.toInt())
                    }
                }
            }
        }

        result = if (numberList.isNotEmpty()) {
            numberList.max() + 1
        } else {
            1
        }
        return result
    }

    private fun uniqueCodeFromCheckboxParentTemplateItemList(
        inputMutableList: MutableList<CheckboxesFrameworkItemDC>,
        blockTitle: String
    ): Int {
        var result = 0
        val numberList = mutableListOf<Int>()
        if (inputMutableList.isNotEmpty()) {
            for (item in inputMutableList) {
                if (item.checkboxesFrameworkTitle.contains(blockTitle)) {
                    val itemNumber = item.checkboxesFrameworkTitle.replace(blockTitle, "").trim()
                    if (itemNumber.toIntOrNull() != null) {
                        numberList.add(itemNumber.toInt())
                    }
                }
            }
        }
        result = if (numberList.isNotEmpty()) {
            numberList.max() + 1
        } else {
            1
        }

        return result
    }

    private fun loadQuestionsRecyclerView(currentPageIndex: Int) {
        val questionsFrameworkList =
            presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].questionsFrameworkList
        for (item in questionsFrameworkList) {
            item.isExpandable = false
        }
        if (questionsFrameworkList.size == 1){
            questionsFrameworkList[0].isExpandable = true
        }

        binding.rvQuestionsFramework.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvQuestionsFramework.adapter =
            QuestionsFrameworkRVAdapter(
                questionsFrameworkList,
                aInfo5ViewModel,
                presentSectionAllPagesFrameworkIndex,
                presentSectionAllData,
                { selectedQuestionsFrameworkTitle: String, selectedQuestionsListPosition: Int ->
                    questionsListItemClickedForRemoval(
                        selectedQuestionsFrameworkTitle,
                        selectedQuestionsListPosition
                    )
                },
                { selectedPageCode -> generateQuestionsTemplateList(selectedPageCode) })
    }

    private fun generateQuestionsTemplateList(pageCode: String) {
        if (!aInfo5ViewModel.isItemPresentInPageTemplateMLMLD(pageCode)) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(mutableListOf(pageCode) )
            aInfo5ViewModel.getItemFromPageTemplateMLMLD(
                pageCode
            )?.let {
                aInfo5ViewModel.setTheQuestionsListMLD(
                    it.questionsList
                )
            }
        }
        else {
            aInfo5ViewModel.getItemFromPageTemplateMLMLD(
                pageCode
            )?.let {
                aInfo5ViewModel.setTheQuestionsListMLD(
                    it.questionsList
                )
            }
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
            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            aInfo5ViewModel.deletePageFrameworkQuestionsInPresentSectionAllPagesFramework(
                currentPageIndex,
                selectedQuestionsListPosition
            )
            aInfo5ViewModel.deleteQuestionsFrameworkDataItemInThePresentSectionAllData(
                currentPageIndex,
                selectedQuestionsListPosition
            )
            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

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
            presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].observationsFrameworkList
        for (item in observationsFrameworkList) {
            item.isExpandable = false
        }
        if (observationsFrameworkList.size == 1){
            observationsFrameworkList[0].isExpandable = true
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

    private fun generateObservationsTemplateList(pageCode: String) {
        if (!aInfo5ViewModel.isItemPresentInPageTemplateMLMLD(pageCode)) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(mutableListOf(pageCode) )
        } else {
            aInfo5ViewModel.getItemFromPageTemplateMLMLD(
                pageCode
            )?.let {
                aInfo5ViewModel.setTheObservationsListMLD(
                    it.observationsList
                )
            }
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
            val sectionPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex
            aInfo5ViewModel.deletePageFrameworkObservationsInPresentSectionAllPagesFramework(
                sectionPagesFrameworkIndex,
                selectedObservationsBlockPosition
            )
            aInfo5ViewModel.deleteObservationsFrameworkDataItemInThePresentSectionAllData(
                sectionPagesFrameworkIndex,
                selectedObservationsBlockPosition
            )
            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

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
            presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].recommendationsFrameworkList
        for (item in recommendationsFrameworkList) {
            item.isExpandable = false
        }
        if (recommendationsFrameworkList.size == 1){
            recommendationsFrameworkList[0].isExpandable = true
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

    private fun generateRecommendationsTemplateList(pageCode: String) {
        if (!aInfo5ViewModel.isItemPresentInPageTemplateMLMLD(pageCode)) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(mutableListOf(pageCode))
        } else {
            aInfo5ViewModel.getItemFromPageTemplateMLMLD(
                pageCode
            )?.let {
                aInfo5ViewModel.setTheRecommendationsListMLD(
                    it.recommendationsList
                )
            }
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
            val sectionPagesFrameworkIndex = presentSectionAllPagesFrameworkIndex
            aInfo5ViewModel.deletePageFrameworkRecommendationsInPresentSectionAllPagesFramework(
                sectionPagesFrameworkIndex,
                selectedRecommendationsBlockPosition
            )
            aInfo5ViewModel.deleteRecommendationsFrameworkDataItemInThePresentSectionAllData(
                sectionPagesFrameworkIndex,
                selectedRecommendationsBlockPosition
            )

            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

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
            presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].standardsFrameworkList
        for (item in standardsCheckboxParentTemplateList) {
            item.isExpandable = false
        }
        if (standardsCheckboxParentTemplateList.size == 1){
            standardsCheckboxParentTemplateList[0].isExpandable = true
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
        if (!aInfo5ViewModel.isItemPresentInPageTemplateMLMLD(pageCode)) {
            aInfo5ViewModel.presentTemplateUploadedFlagMLD.value = false
            getThePageTemplateAndUploadIntoViewModel(mutableListOf(pageCode))
        } else {
            aInfo5ViewModel.getItemFromPageTemplateMLMLD(
                pageCode
            )?.let {
                aInfo5ViewModel.setTheStandardsListMLD(
                    it.standardsList
                )
            }
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
            val currentPageIndex = presentSectionAllPagesFrameworkIndex
            aInfo5ViewModel.deletePageFrameworkStandardsInPresentSectionAllPagesFramework(
                currentPageIndex,
                selectedStandardsListPosition
            )
            aInfo5ViewModel.deleteStandardsFrameworkDataItemInThePresentSectionAllData(
                currentPageIndex,
                selectedStandardsListPosition
            )

            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            reloadingPage(currentPageIndex)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun reloadingPage(currentPageIndex: Int) {
        binding.pbUploadingFromDatabase.visibility = View.GONE
        binding.llObservationsActionbuttonsAndViews.isEnabled = true
        binding.fabAddANewBlock.isEnabled = true
        if (currentPageIndex >= 0) {
            aInfo5ViewModel.setThePageCountMLD(currentPageIndex + 1)
            aInfo5ViewModel.setTheEtPageNameMLD(presentSectionAllPagesFramework.sectionPageFrameworkList[currentPageIndex].pageTitle)
            aInfo5ViewModel.updatePageFrameworkNumberInPresentSectionAllPagesFramework(
                currentPageIndex + 1,
                currentPageIndex
            )
            aInfo5ViewModel.updatePageNumberInSectionPageDataInPresentSectionAllData(
                currentPageIndex + 1,
                currentPageIndex
            )
            presentSectionAllPagesFramework =
                aInfo5ViewModel.getThePresentSectionAllPagesFramework()
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()

            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                loadQuestionsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.text = getString(R.string.string_Questions_Below)
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                loadObservationsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.text = getString(R.string.string_Observations_Below)
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                loadRecommendationsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.text = getString(R.string.string_Recommendations_Below)
            }
            else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                loadStandardsRecyclerView(currentPageIndex)
                binding.tvQuestionsEtcLabel.text = getString(R.string.string_Standards_Below)
            }
            loadPageData(currentPageIndex)

        }
    }

    private fun loadPageData(currentPageIndex: Int) {
        aInfo5ViewModel.etObservationsMLD.value =
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].observations
        aInfo5ViewModel.etRecommendationsMLD.value =
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendations
        aInfo5ViewModel.tvStandardsMLD.value =
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].standards
        aInfo5ViewModel.tvPhotoPathsInObservationsFragmentMLD.value =
            presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].photoPaths
        aInfo5ViewModel.setThePreviousScreenVariable(
            MainActivity.NOT_RELEVANT
        )
        binding.pbUploadingFromDatabase.visibility = View.GONE
        binding.llObservationsActionbuttonsAndViews.isEnabled = true
        binding.fabAddANewBlock.isEnabled = true
    }

    //Note that this function also loads the template MLDs
    fun getThePageTemplateAndUploadIntoViewModel(pageCodeML: MutableList<String>, presentSectionAllPagesFramework: SectionAllPagesFrameworkDC = SectionAllPagesFrameworkDC(), presentSectionAllData: SectionAllDataDC = SectionAllDataDC()) {
        aInfo5ViewModel.getAInfo5TemplatesByIds(pageCodeML).observe(viewLifecycleOwner) { list ->
            aInfo5ViewModel.templateStringsToPageTemplatesAndAddingToTemplatesListMLD(list, presentSectionAllPagesFramework, presentSectionAllData)
        }
    }

    //Get PageTemplates For Checklist Report and Upload into TemplateList
    fun getThePageTemplatesForCheckListReport(pageCodeML: MutableList<String>){
        aInfo5ViewModel.getAInfo5TemplatesByIds(pageCodeML).observe(viewLifecycleOwner){list ->
            aInfo5ViewModel.templateStringsToPageTemplatesForCheckListReportAndAddingToTemplatesListMLD(list)
        }
    }

    private fun showDialogForPhotoModification() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Choose Photos from Present Location/All ")
            .setMessage("Press \" All\" to choose from All Photos")
            .setPositiveButton("Location") { dialog, _ ->
                val location =
                    aInfo5ViewModel.makeLocationForPhotos(aInfo5ViewModel.getTheWhichIntroductionsOrObservationsToBeUploadedVariable())
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

    private fun expandAndCollapseViews() {
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
                binding.tvObservationsPicturesLabel.setBackgroundResource(R.drawable.border1dp_color_black_with_up_arrow)
            } else {
                binding.tvPhotoPathsInObservationsPage.visibility = View.GONE
                binding.etObservationsOnly.visibility = View.GONE
                binding.tvObservationsPicturesLabel.setBackgroundResource(R.drawable.border1dp_color_black_with_down_arrow)
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
                binding.tvRecommendationsStandardsLabel.setBackgroundResource(R.drawable.border1dp_color_black_with_up_arrow)
            } else {
                binding.tvStandardsInObservationsPage.visibility = View.GONE
                binding.etRecommendationsOnly.visibility = View.GONE
                binding.tvRecommendationsStandardsLabel.setBackgroundResource(R.drawable.border1dp_color_black_with_down_arrow)
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

    fun checkIfPageSpecificDataStructureIsProper(pageIndex: Int): Int{
        var result = 0
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
            val sectionPageDataList = presentSectionAllData.sectionAllPagesData.sectionPageDataList
            if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                val questionFrameworkDataItemList =
                    sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                    val frameworkPageCode =
                        questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                    if (!aInfo5ViewModel.isItemPresentInPageTemplateList(
                            frameworkPageCode
                        )
                    ) {
                        getThePageTemplateAndUploadIntoViewModel(mutableListOf(frameworkPageCode))
                        val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                        val ifUpdatedOrNo = questionTemplateItemML?.let {
                            aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                pageIndex, questionsFrameworkIndex,
                                it
                            )
                        }
                        if (ifUpdatedOrNo == false) {
                            result += 1
                        }
                    }
                    else {
                        val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                        val ifUpdatedOrNo = questionTemplateItemML?.let {
                            aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                pageIndex, questionsFrameworkIndex,
                                it
                            )
                        }
                        if (ifUpdatedOrNo == false) {
                            result += 1
                        }
                    }
                    if (result > 1){
                        break
                    }
                }
            }
        }
        return  result
    }

    fun checkIfNonEmptyDataStructureIsProper(): Int{
        var result = 0
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
            val sectionPageDataList = presentSectionAllData.sectionAllPagesData.sectionPageDataList
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionFrameworkDataItemList =
                        sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                        val frameworkPageCode =
                            questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                        if (!aInfo5ViewModel.isItemPresentInPageTemplateList(
                                frameworkPageCode
                            )
                        ) {
                            getThePageTemplateAndUploadIntoViewModel(mutableListOf(frameworkPageCode))
                        }
                        else {
                            val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                            val ifUpdatedOrNo = questionTemplateItemML?.let {
                                aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                    pageIndex, questionsFrameworkIndex,
                                    it
                                )
                            }
                            if (ifUpdatedOrNo == false) {
                                result += 1
                            }
                        }
                        if (result > 0){
                            break
                        }
                    }
                }
            }
        }

        return result
    }

    fun updatePresentSectionAllDataStructureBasedOnTemplates() {
        //var presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
            val sectionPageDataList =
                presentSectionAllData.sectionAllPagesData.sectionPageDataList
            for (pageIndex in 0 until sectionPageDataList.size) {
                if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                    val questionFrameworkDataItemList =
                        sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                    for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                        val frameworkPageCode =
                            questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                        if (!aInfo5ViewModel.isItemPresentInPageTemplateList(
                                frameworkPageCode
                            )
                        ) {
                            getThePageTemplateAndUploadIntoViewModel(mutableListOf(frameworkPageCode))
                            val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                            val result = questionTemplateItemML?.let {
                                aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                    pageIndex, questionsFrameworkIndex,
                                    it
                                )
                            }
                            if (result == false) {
                                aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                    pageIndex, questionsFrameworkIndex,
                                    questionTemplateItemML
                                )
                            }
                        }
                        else {
                            val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                            val result = questionTemplateItemML?.let {
                                aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                    pageIndex, questionsFrameworkIndex,
                                    it
                                )
                            }
                            if (result == false) {
                                aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                    pageIndex, questionsFrameworkIndex,
                                    questionTemplateItemML
                                )
                            }
                        }
                    }
                }
                if (sectionPageDataList[pageIndex].observationsFrameworkDataItemList.isNotEmpty()) {
                    val observationsFrameworkDataItemList =
                        sectionPageDataList[pageIndex].observationsFrameworkDataItemList
                    for (observationsFrameworkIndex in 0 until observationsFrameworkDataItemList.size) {
                        val observationsTemplateItemML =
                            aInfo5ViewModel.observationsList_LD.value
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
                if (sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.isNotEmpty()) {
                    val recommendationsFrameworkDataItemList =
                        sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList
                    for (recommendationsFrameworkIndex in 0 until recommendationsFrameworkDataItemList.size) {
                        val recommendationsTemplateItemML =
                            aInfo5ViewModel.recommendationsList_LD.value
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
                if (sectionPageDataList[pageIndex].standardsFrameworkDataItemList.isNotEmpty()) {
                    val standardsFrameworkDataItemList =
                        sectionPageDataList[pageIndex].standardsFrameworkDataItemList
                    for (standardsFrameworkIndex in 0 until standardsFrameworkDataItemList.size) {
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
                if (pageIndex == sectionPageDataList.size -1){
                    aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
                }
            }
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        }
        else {
            aInfo5ViewModel.setThePresentSectionAllData(
                aInfo5ViewModel.createPresentSectionAllDataUsingSectionAllPagesFramework(
                    presentSectionAllPagesFramework
                )
            )
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
                val sectionPageDataList =
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList
                for (pageIndex in 0 until sectionPageDataList.size) {
                    if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                        val questionFrameworkDataItemList =
                            sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                        for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                            val frameworkPageCode =
                                questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                            if (!aInfo5ViewModel.isItemPresentInPageTemplateList(
                                    frameworkPageCode
                                )
                            ) {
                                getThePageTemplateAndUploadIntoViewModel(mutableListOf(frameworkPageCode))
                            } else {
                                val questionTemplateItemML =
                                    aInfo5ViewModel.questionsList_LD.value
                                val result = questionTemplateItemML?.let {
                                    aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        questionTemplateItemML
                                    )
                                }
                            }
                        }
                    }
                    if (sectionPageDataList[pageIndex].observationsFrameworkDataItemList.isNotEmpty()) {
                        val observationsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].observationsFrameworkDataItemList
                        for (observationsFrameworkIndex in 0 until observationsFrameworkDataItemList.size) {
                            val observationsTemplateItemML =
                                aInfo5ViewModel.observationsList_LD.value
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
                    if (sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.isNotEmpty()) {
                        val recommendationsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList
                        for (recommendationsFrameworkIndex in 0 until recommendationsFrameworkDataItemList.size) {
                            val recommendationsTemplateItemML =
                                aInfo5ViewModel.recommendationsList_LD.value
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
                    if (sectionPageDataList[pageIndex].standardsFrameworkDataItemList.isNotEmpty()) {
                        val standardsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].standardsFrameworkDataItemList
                        for (standardsFrameworkIndex in 0 until standardsFrameworkDataItemList.size) {
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
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
            aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
        }

    }

    fun updateSpecificPageDataStructureBasedOnTemplates(currentPageIndex: Int){
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
            val sectionPageDataList = presentSectionAllData.sectionAllPagesData.sectionPageDataList
            if (sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                val questionFrameworkDataItemList = sectionPageDataList[currentPageIndex].questionsFrameworkDataItemList
                for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                    val frameworkPageCode = questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                    if (aInfo5ViewModel.isItemPresentInPageTemplateList(frameworkPageCode)) {
                        val questionTemplateItemML = aInfo5ViewModel.questionsList_LD.value
                        val result = questionTemplateItemML?.let {
                            aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, questionsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, questionsFrameworkIndex,
                                questionTemplateItemML
                            )
                        }
                    }
                }
            }
            if (sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList.isNotEmpty()) {
                val obsFrameworkDataItemList = sectionPageDataList[currentPageIndex].observationsFrameworkDataItemList
                for (obsFrameworkIndex in 0 until obsFrameworkDataItemList.size) {
                    val obsFrameworkPageCode = obsFrameworkDataItemList[obsFrameworkIndex].pageCode
                    if (aInfo5ViewModel.isItemPresentInPageTemplateList(obsFrameworkPageCode)) {
                        val observationsTemplateItemML = aInfo5ViewModel.observationsList_LD.value
                        val result = observationsTemplateItemML?.let {
                            aInfo5ViewModel.isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, obsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            aInfo5ViewModel.updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, obsFrameworkIndex,
                                observationsTemplateItemML
                            )
                        }
                    }
                }
            }
            if (sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList.isNotEmpty()) {
                val recosFrameworkDataItemList = sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList
                for (recosFrameworkIndex in 0 until recosFrameworkDataItemList.size) {
                    val recosFrameworkPageCode = recosFrameworkDataItemList[recosFrameworkIndex].pageCode
                    if (aInfo5ViewModel.isItemPresentInPageTemplateList(recosFrameworkPageCode)) {
                        val recosTemplateItemML = aInfo5ViewModel.recommendationsList_LD.value
                        val result = recosTemplateItemML?.let {
                            aInfo5ViewModel.isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, recosFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            aInfo5ViewModel.updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, recosFrameworkIndex,
                                recosTemplateItemML
                            )
                        }
                    }
                }
            }
            if (sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList.isNotEmpty()) {
                val stdsFrameworkDataItemList = sectionPageDataList[currentPageIndex].standardsFrameworkDataItemList
                for (stdsFrameworkIndex in 0 until stdsFrameworkDataItemList.size) {
                    val stdsFrameworkPageCode = stdsFrameworkDataItemList[stdsFrameworkIndex].pageCode
                    if (aInfo5ViewModel.isItemPresentInPageTemplateList(stdsFrameworkPageCode)) {
                        val standardsTemplateItemML = aInfo5ViewModel.standardsList_LD.value
                        val result = standardsTemplateItemML?.let {
                            aInfo5ViewModel.isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                                currentPageIndex, stdsFrameworkIndex,
                                it
                            )
                        }
                        if (result == false) {
                            aInfo5ViewModel.updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                                currentPageIndex, stdsFrameworkIndex,
                                standardsTemplateItemML
                            )
                        }
                    }
                }
            }
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        }
    }

    fun createPresentSectionAllDataStructureBasedOnTemplates() {
        //var presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isEmpty()) {
            aInfo5ViewModel.setThePresentSectionAllData(
                aInfo5ViewModel.createPresentSectionAllDataUsingSectionAllPagesFramework(
                    presentSectionAllPagesFramework
                )
            )
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
            if (presentSectionAllData.sectionAllPagesData.sectionPageDataList.isNotEmpty()) {
                val sectionPageDataList =
                    presentSectionAllData.sectionAllPagesData.sectionPageDataList
                for (pageIndex in 0 until sectionPageDataList.size) {
                    if (sectionPageDataList[pageIndex].questionsFrameworkDataItemList.isNotEmpty()) {
                        val questionFrameworkDataItemList =
                            sectionPageDataList[pageIndex].questionsFrameworkDataItemList
                        for (questionsFrameworkIndex in 0 until questionFrameworkDataItemList.size) {
                            val frameworkPageCode =
                                questionFrameworkDataItemList[questionsFrameworkIndex].pageCode
                            if (!aInfo5ViewModel.isItemPresentInPageTemplateList(
                                    frameworkPageCode
                                )
                            ) {
                                getThePageTemplateAndUploadIntoViewModel(mutableListOf(frameworkPageCode))
                                val questionTemplateItemML =
                                    aInfo5ViewModel.questionsList_LD.value
                                val result = questionTemplateItemML?.let {
                                    aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        questionTemplateItemML
                                    )
                                }
                            }
                            else {
                                val questionTemplateItemML =
                                    aInfo5ViewModel.questionsList_LD.value
                                val result = questionTemplateItemML?.let {
                                    aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        it
                                    )
                                }
                                if (result == false) {
                                    aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(
                                        pageIndex, questionsFrameworkIndex,
                                        questionTemplateItemML
                                    )
                                }
                            }
                        }
                    }
                    if (sectionPageDataList[pageIndex].observationsFrameworkDataItemList.isNotEmpty()) {
                        val observationsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].observationsFrameworkDataItemList
                        for (observationsFrameworkIndex in 0 until observationsFrameworkDataItemList.size) {
                            val observationsTemplateItemML =
                                aInfo5ViewModel.observationsList_LD.value
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
                    if (sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList.isNotEmpty()) {
                        val recommendationsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].recommendationsFrameworkDataItemList
                        for (recommendationsFrameworkIndex in 0 until recommendationsFrameworkDataItemList.size) {
                            val recommendationsTemplateItemML =
                                aInfo5ViewModel.recommendationsList_LD.value
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
                    if (sectionPageDataList[pageIndex].standardsFrameworkDataItemList.isNotEmpty()) {
                        val standardsFrameworkDataItemList =
                            sectionPageDataList[pageIndex].standardsFrameworkDataItemList
                        for (standardsFrameworkIndex in 0 until standardsFrameworkDataItemList.size) {
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
            presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
        }
        aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
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

}
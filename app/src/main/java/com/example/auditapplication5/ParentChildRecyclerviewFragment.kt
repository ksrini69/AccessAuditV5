package com.example.auditapplication5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentParentChildRecyclerviewBinding
import com.example.auditapplication5.presentation.adapter.ParentChildParentRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class ParentChildRecyclerviewFragment : Fragment() {
    private lateinit var binding: FragmentParentChildRecyclerviewBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_parent_child_recyclerview,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT
                        || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS
                        || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS
                        || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS
                    ) {
                        aInfo5ViewModel.setTheFrameworkUpdatedInParentChildRVFragmentFlag(false)
                        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
                        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.RV_PARENT_CHILD_FRAGMENT)
                    }
                    findNavController().navigate(R.id.action_parentChildRecyclerviewFragment_to_observationsFragment)
                }
            })

        val TAG = MainActivity.TESTING_TAG

        //Set Screen for Introductions?
        aInfo5ViewModel.setTheScreenVariable(MainActivity.RV_PARENT_CHILD_FRAGMENT)

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        if (aInfo5ViewModel.getTheParentChildParentItemML().isEmpty()) {
            aInfo5ViewModel.setTheParentChildParentItemML(mutableListOf(aInfo5ViewModel.defaultRVParentChildParentItem))
        }

        val currentSectionCode = aInfo5ViewModel.getThePresentSectionCodeAndDisplayName().uniqueCodeName

        val currentPageGroupCode = processSectionCode(currentSectionCode)
        val priorityPgGroupCodes = listOf(currentPageGroupCode)
        val originalList = aInfo5ViewModel.getTheParentChildParentItemML()
        val sortedMutableList: MutableList<RVParentChildParentItemDC> = originalList
            .sortedWith(compareBy<RVParentChildParentItemDC> { item ->
                when (item.pageGroupCode.trim()) {
                    in priorityPgGroupCodes -> 0  // Priority group first
                    else -> 1               // Rest second
                }
            }.thenBy { it.pageGroupCode.trim() })  // Sort within each group by title
            .toMutableList()

        for (index in 0 until sortedMutableList.size){
            sortedMutableList[index].isExpandable = index == 0
        }


//aInfo5ViewModel.getTheParentChildParentItemML().sortedBy { it.title.trim() }.toMutableList()
        loadRecyclerView(sortedMutableList)
    }


    //Functions below
    private fun loadRecyclerView(parentChildParentML: MutableList<RVParentChildParentItemDC>) {
        binding.rvParent.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvParent.adapter =
            ParentChildParentRVAdapter(parentChildParentML) { selectedPageCode: String ->
                childListItemClicked(selectedPageCode)
            }
    }

    private fun childListItemClicked(selectedPageCode: String) {
        //val ampCode = aInfo5ViewModel.pageCodeToAMPCode(aInfo5ViewModel.getCurrentPageCodeRV())
        val presentSectionAllPagesFrameworkIndex =
            aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()

        var sectionPageFramework = SectionPageFrameworkDC()
        var sectionPageData = SectionPageDataDC()

        when (selectedPageCode) {
            "PC_General_Entry_01_PC" -> {
                sectionPageFramework = aInfo5ViewModel.getTheDefaultSectionPageFramework()
                sectionPageData = aInfo5ViewModel.getTheDefaultSectionPageData()
            }
            "No Pages Present", "" -> {
                sectionPageFramework.pageCode = ""
                sectionPageFramework = SectionPageFrameworkDC()
                sectionPageData = SectionPageDataDC()
            }
            else -> {
                sectionPageFramework.pageCode = selectedPageCode
                sectionPageFramework.pageTitle =
                    aInfo5ViewModel.extractDisplayNameFromPageCode(selectedPageCode)
                sectionPageData.pageTitle = sectionPageFramework.pageTitle

                val questionsFrameworkItem = QuestionsFrameworkItemDC()
                val observationsCheckboxesFrameworkItem = CheckboxesFrameworkItemDC()
                val recommendationsCheckboxesFrameworkItem = CheckboxesFrameworkItemDC()
                val standardsCheckboxesFrameworkItem = CheckboxesFrameworkItemDC()

                val questionsFrameworkDataItem = QuestionsFrameworkDataItemDC()
                val observationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val recommendationsFrameworkDataItem = CheckboxesFrameworkDataItemDC()
                val standardsFrameworkDataItem = CheckboxesFrameworkDataItemDC()

                questionsFrameworkItem.questionsFrameworkTitle = sectionPageFramework.pageTitle
                questionsFrameworkItem.pageCode = selectedPageCode
                questionsFrameworkItem.serialStatus = MainActivity.PRIMARY_QUESTION_SET
                sectionPageFramework.questionsFrameworkList.add(questionsFrameworkItem)

                questionsFrameworkDataItem.questionsFrameworkTitle =
                    questionsFrameworkItem.questionsFrameworkTitle
                questionsFrameworkDataItem.pageCode = selectedPageCode
                sectionPageData.questionsFrameworkDataItemList.add(questionsFrameworkDataItem)

                observationsCheckboxesFrameworkItem.checkboxesFrameworkTitle = sectionPageFramework.pageTitle
                observationsCheckboxesFrameworkItem.pageCode = selectedPageCode
                observationsCheckboxesFrameworkItem.serialStatus = MainActivity.PRIMARY_QUESTION_SET
                sectionPageFramework.observationsFrameworkList.add(observationsCheckboxesFrameworkItem)

                observationsFrameworkDataItem.checkboxesFrameworkTitle =
                    observationsCheckboxesFrameworkItem.checkboxesFrameworkTitle
                observationsFrameworkDataItem.pageCode = selectedPageCode
                sectionPageData.observationsFrameworkDataItemList.add(observationsFrameworkDataItem)

                recommendationsCheckboxesFrameworkItem.checkboxesFrameworkTitle = sectionPageFramework.pageTitle
                recommendationsCheckboxesFrameworkItem.pageCode = selectedPageCode
                recommendationsCheckboxesFrameworkItem.serialStatus = MainActivity.PRIMARY_QUESTION_SET
                sectionPageFramework.recommendationsFrameworkList.add(
                    recommendationsCheckboxesFrameworkItem
                )

                recommendationsFrameworkDataItem.checkboxesFrameworkTitle =
                    recommendationsCheckboxesFrameworkItem.checkboxesFrameworkTitle
                recommendationsFrameworkDataItem.pageCode = selectedPageCode
                sectionPageData.recommendationsFrameworkDataItemList.add(
                    recommendationsFrameworkDataItem
                )

                standardsCheckboxesFrameworkItem.checkboxesFrameworkTitle = sectionPageFramework.pageTitle
                standardsCheckboxesFrameworkItem.pageCode = selectedPageCode
                standardsCheckboxesFrameworkItem.serialStatus = MainActivity.PRIMARY_QUESTION_SET
                sectionPageFramework.standardsFrameworkList.add(standardsCheckboxesFrameworkItem)

                standardsFrameworkDataItem.checkboxesFrameworkTitle =
                    standardsCheckboxesFrameworkItem.checkboxesFrameworkTitle
                standardsFrameworkDataItem.pageCode = selectedPageCode
                sectionPageData.standardsFrameworkDataItemList.add(standardsFrameworkDataItem)

            }
        }

        if (sectionPageFramework.pageCode != "") {
            if (!aInfo5ViewModel.uniqueListOfSectionPageCodes.contains(selectedPageCode)){
                aInfo5ViewModel.uniqueListOfSectionPageCodes.add(selectedPageCode)
            }
            sectionPageFramework.pageNumber = presentSectionAllPagesFrameworkIndex + 1
            sectionPageData.pageNumber = presentSectionAllPagesFrameworkIndex + 1
            aInfo5ViewModel.addPageFrameworkToPresentSectionAllPagesFramework(
                sectionPageFramework,
                presentSectionAllPagesFrameworkIndex + 1
            )

            aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(
                presentSectionAllPagesFrameworkIndex + 1
            )

            aInfo5ViewModel.addSectionPageDataToPresentSectionAllData(
                sectionPageData,
                presentSectionAllPagesFrameworkIndex + 1
            )
            aInfo5ViewModel.setTheFrameworkUpdatedInParentChildRVFragmentFlag(true)
            aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(true)
            aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllPagesFramework()
            aInfo5ViewModel.resetThePageNumbersOfPresentSectionAllData()
        } else {
            aInfo5ViewModel.setTheFrameworkUpdatedInParentChildRVFragmentFlag(false)
            aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(true)
            aInfo5ViewModel.setTheSectionAllDataLoadedFlagMLD(true)
        }

        val sectionPagesFrameworkAndDataID =
            aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
        aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDBMainActivity(
            aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
            aInfo5ViewModel.getThePresentSectionAllData(),
            sectionPagesFrameworkAndDataID
        )

        aInfo5ViewModel.setTheScreenVariable(MainActivity.OBSERVATIONS_FRAGMENT)
        aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.RV_PARENT_CHILD_FRAGMENT)
        findNavController().navigate(R.id.action_parentChildRecyclerviewFragment_to_observationsFragment)
    }

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

}
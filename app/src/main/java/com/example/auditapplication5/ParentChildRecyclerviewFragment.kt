package com.example.auditapplication5

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.*
import com.example.auditapplication5.databinding.FragmentParentChildRecyclerviewBinding
import com.example.auditapplication5.presentation.adapter.ParentChildParentRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class ParentChildRecyclerviewFragment : Fragment() {
    private lateinit var binding: FragmentParentChildRecyclerviewBinding
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

        //Set Screen for Introductions?
        aInfo5ViewModel.setTheScreenVariable(MainActivity.RV_PARENT_CHILD_FRAGMENT)

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        if (aInfo5ViewModel.getTheParentChildParentItemML().isEmpty()) {
            aInfo5ViewModel.setTheParentChildParentItemML(mutableListOf(aInfo5ViewModel.defaultRVParentChildParentItem))
        }
        loadRecyclerView(aInfo5ViewModel.getTheParentChildParentItemML())
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

        if (selectedPageCode == "PC_General_Entry_01_PC") {
            sectionPageFramework = aInfo5ViewModel.getTheDefaultSectionPageFramework()
            sectionPageData = aInfo5ViewModel.getTheDefaultSectionPageData()
        }
        else if (selectedPageCode == "No Pages Present" || selectedPageCode == "") {
            sectionPageFramework.pageCode = ""
            sectionPageFramework = SectionPageFrameworkDC()
            sectionPageData = SectionPageDataDC()
        }
        else {
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


}
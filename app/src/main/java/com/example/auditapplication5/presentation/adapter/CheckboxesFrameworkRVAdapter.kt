package com.example.auditapplication5.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.R
import com.example.auditapplication5.data.model.CheckboxTemplateItemDC
import com.example.auditapplication5.data.model.CheckboxesFrameworkItemDC
import com.example.auditapplication5.databinding.CheckboxesFrameworkItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class CheckboxesFrameworkRVAdapter(
    private val checkboxesFrameworkItemML: MutableList<CheckboxesFrameworkItemDC> = mutableListOf(),
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val clickListener1: (checkboxBlockTitle: String, checkboxBlockItem: Int) -> Unit,
    private val clickListener2: (checkboxBlockPageId: String) -> Unit
) : RecyclerView.Adapter<CheckboxesFrameworkRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckboxesFrameworkItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val checkboxesFrameworkItem = checkboxesFrameworkItemML[position]
        holder.bind(checkboxesFrameworkItem, position)

    }

    override fun getItemCount(): Int {
        return checkboxesFrameworkItemML.size
    }

    fun isAnyItemExpanded(position: Int) {
        val temp = checkboxesFrameworkItemML.indexOfFirst {
            it.isExpandable
        }
        if (temp >= 0 && temp != position) {
            checkboxesFrameworkItemML[temp].isExpandable = false
            notifyItemChanged(temp)
        }
    }

    inner class ViewHolder(val binding: CheckboxesFrameworkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(checkboxesFrameworkItem: CheckboxesFrameworkItemDC, position: Int) {
            binding.tvCheckboxesFrameworkItemHeading.text =
                checkboxesFrameworkItem.checkboxesFrameworkTitle
            if (checkboxesFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET) {
                binding.ibClearAndDelete2.visibility = View.INVISIBLE
            } else {
                binding.ibClearAndDelete2.visibility = View.VISIBLE
            }

            binding.rvCheckboxesTemplateItems.layoutManager =
                LinearLayoutManager(binding.root.context)

            val screen = aInfo5ViewModel.getTheScreenVariable()
            val pageCode = checkboxesFrameworkItem.pageCode
            val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
            val itemFromPageTemplateMLMLD = aInfo5ViewModel.getItemFromPageTemplateMLMLD(pageCode)
            if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                //val observationsTemplateItemML = aInfo5ViewModel.observationsList_LD.value
                val observationsTemplateItemMLN = itemFromPageTemplateMLMLD?.observationsList
                val result = observationsTemplateItemMLN?.let {
                    aInfo5ViewModel.isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex, position,
                        it
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex, position,
                        observationsTemplateItemMLN
                    )
                }
                binding.rvCheckboxesTemplateItems.adapter = observationsTemplateItemMLN?.let {
                    CheckboxTemplateRVAdapter(
                        it, aInfo5ViewModel, screen, checkboxesFrameworkItem.serialStatus,checkboxesFrameworkItem.checkboxesFrameworkTitle, position
                    )
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                //val recommendationsTemplateItemML = aInfo5ViewModel.recommendationsList_LD.value
                val recommendationsTemplateItemMLN = itemFromPageTemplateMLMLD?.recommendationsList
                val result = recommendationsTemplateItemMLN?.let {
                    aInfo5ViewModel.isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex, position,
                        it
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex, position,
                        recommendationsTemplateItemMLN
                    )
                }
//                val presentSectionAllData = aInfo5ViewModel.getThePresentSectionAllData()
//                val recoData = presentSectionAllData.sectionAllPagesData.sectionPageDataList[currentPageIndex].recommendationsFrameworkDataItemList[position]
//                val recoFramework = aInfo5ViewModel.getThePresentSectionAllPagesFramework().sectionPageFrameworkList[currentPageIndex].recommendationsFrameworkList[position]
//                Log.d(MainActivity.TESTING_TAG, "bind: RecoData ${recoData} and \n RecoFramework ${recoFramework}")

                binding.rvCheckboxesTemplateItems.adapter = recommendationsTemplateItemMLN?.let {
                    CheckboxTemplateRVAdapter(
                        it, aInfo5ViewModel, screen, checkboxesFrameworkItem.serialStatus, checkboxesFrameworkItem.checkboxesFrameworkTitle, position
                    )
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                //val standardsTemplateItemML = aInfo5ViewModel.standardsList_LD.value
                val standardsTemplateItemMLN = itemFromPageTemplateMLMLD?.standardsList
                val result = standardsTemplateItemMLN?.let {
                    aInfo5ViewModel.isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex,
                        position,
                        standardsTemplateItemMLN
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex,
                        position,
                        standardsTemplateItemMLN
                    )
                }
                binding.rvCheckboxesTemplateItems.adapter = standardsTemplateItemMLN?.let {
                    CheckboxTemplateRVAdapter(
                        it, aInfo5ViewModel, screen, checkboxesFrameworkItem.serialStatus, checkboxesFrameworkItem.checkboxesFrameworkTitle,position
                    )
                }
            }

            val isExpandable = checkboxesFrameworkItem.isExpandable
            if (isExpandable){
                binding.rvCheckboxesTemplateItems.visibility = View.VISIBLE
                if (checkboxesFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    binding.clTextviewAndDeleteOption2.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow)
                } else if (checkboxesFrameworkItem.serialStatus == MainActivity.OTHER_QUESTION_SET){
                    binding.clTextviewAndDeleteOption2.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow_centered)
                }
            } else {
                binding.rvCheckboxesTemplateItems.visibility = View.GONE
                if (checkboxesFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    binding.clTextviewAndDeleteOption2.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow)
                } else if (checkboxesFrameworkItem.serialStatus == MainActivity.OTHER_QUESTION_SET){
                    binding.clTextviewAndDeleteOption2.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow_centered)
                }
            }
            binding.rvCheckboxesTemplateItems.visibility =
                if (isExpandable) View.VISIBLE else View.GONE

            binding.llCheckboxesFrameworkItem.setOnClickListener {
                if (itemFromPageTemplateMLMLD == null){
                    clickListener2(checkboxesFrameworkItem.pageCode)
                }
                isAnyItemExpanded(position)
                checkboxesFrameworkItem.isExpandable = !checkboxesFrameworkItem.isExpandable
                notifyItemChanged(position)
            }

            binding.ibClearAndDelete2.setOnClickListener {
                clickListener1(checkboxesFrameworkItem.checkboxesFrameworkTitle, position)
            }
        }
    }
}
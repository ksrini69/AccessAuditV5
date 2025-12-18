package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.R
import com.example.auditapplication5.data.model.QuestionsFrameworkItemDC
import com.example.auditapplication5.data.model.SectionAllDataDC
import com.example.auditapplication5.databinding.QuestionsFrameworkItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class QuestionsFrameworkRVAdapter(
    private val questionsFrameworkML: MutableList<QuestionsFrameworkItemDC>,
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val currentPageIndex: Int,
    private val presentSectionAllData: SectionAllDataDC,
    private val clickListener1: (questionsBlockTitle: String, questionsBlockItem: Int) -> Unit,
    private val clickListener2: (questionsBlockPageId: String) -> Unit
) : RecyclerView.Adapter<QuestionsFrameworkRVAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionsFrameworkItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionFrameworkItem = questionsFrameworkML[position]
        holder.bind(questionFrameworkItem, position)
    }

    override fun getItemCount(): Int {
        return questionsFrameworkML.size
    }

    fun isAnyItemExpanded(position: Int) {
        val temp = questionsFrameworkML.indexOfFirst {
            it.isExpandable
        }
        if (temp >= 0 && temp != position) {
            questionsFrameworkML[temp].isExpandable = false
            notifyItemChanged(temp)
        }
    }

    inner class ViewHolder(val binding: QuestionsFrameworkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(questionsFrameworkItem: QuestionsFrameworkItemDC, position: Int) {
            binding.tvQuestionsFrameworkItemHeading.text =
                questionsFrameworkItem.questionsFrameworkTitle
            if (questionsFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET) {
                binding.ibClearAndDelete.visibility = View.INVISIBLE
            }
            else {
                binding.ibClearAndDelete.visibility = View.VISIBLE
            }

            binding.rvQuestionsTemplateItems.layoutManager =
                LinearLayoutManager(binding.root.context)
            val pageCode = questionsFrameworkItem.pageCode
            val questionTemplateItemMLN = aInfo5ViewModel.getItemFromPageTemplateMLMLD(pageCode)?.questionsList

            if (questionTemplateItemMLN == null){
                clickListener2(questionsFrameworkItem.pageCode)
            }

            val result = questionTemplateItemMLN?.let {
                aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(currentPageIndex,position,
                    it
                )
            }
            if (result == false){
                aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(currentPageIndex,position,
                    questionTemplateItemMLN
                )
            }


            binding.rvQuestionsTemplateItems.adapter = questionTemplateItemMLN?.let {
                QuestionTemplateRVAdapter(
                    it,
                    aInfo5ViewModel,
                    position,
                    questionsFrameworkItem.questionsFrameworkTitle,
                    questionsFrameworkItem.serialStatus,
                    currentPageIndex,
                    presentSectionAllData
                )
            }

            val isExpandable = questionsFrameworkItem.isExpandable
            if (isExpandable){
                binding.rvQuestionsTemplateItems.visibility = View.VISIBLE
                if (questionsFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    binding.clTextviewAndDeleteOption.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow)
                } else if (questionsFrameworkItem.serialStatus == MainActivity.OTHER_QUESTION_SET){
                    binding.clTextviewAndDeleteOption.setBackgroundResource(R.drawable.border1dp_color_purple500_with_up_arrow_centered)
                }
            } else {
                binding.rvQuestionsTemplateItems.visibility = View.GONE
                if (questionsFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    binding.clTextviewAndDeleteOption.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow)
                }
                else if (questionsFrameworkItem.serialStatus == MainActivity.OTHER_QUESTION_SET){
                    binding.clTextviewAndDeleteOption.setBackgroundResource(R.drawable.border1dp_color_purple500_with_down_arrow_centered)
                }

            }

            binding.llQuestionsFrameworkItem.setOnClickListener {

                if (result == false){
                    clickListener2(questionsFrameworkItem.pageCode)
                }
                isAnyItemExpanded(position)
                questionsFrameworkItem.isExpandable = !questionsFrameworkItem.isExpandable
                notifyItemChanged(position)
            }
            //Click Listener to ensure that the block can be deleted
            binding.ibClearAndDelete.setOnClickListener {
                clickListener1(questionsFrameworkItem.questionsFrameworkTitle, position)
            }
        }
    }
}
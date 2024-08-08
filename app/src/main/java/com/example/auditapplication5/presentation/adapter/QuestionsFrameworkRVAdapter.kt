package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.QuestionTemplateItemDC
import com.example.auditapplication5.data.model.QuestionsFrameworkItemDC

import com.example.auditapplication5.databinding.QuestionsFrameworkItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class QuestionsFrameworkRVAdapter(
    private val questionsFrameworkML: MutableList<QuestionsFrameworkItemDC>,
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val clickListener1: (questionsBlockTitle: String, questionsBlockItem: Int) -> Unit,
    private val clickListener2: (questionsBlockPageId: String) -> Unit
) : RecyclerView.Adapter<QuestionsFrameworkRVAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionsFrameworkItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
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

    inner class ViewHolder(val binding: QuestionsFrameworkItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(questionsFrameworkItem: QuestionsFrameworkItemDC, position: Int){
            binding.tvQuestionsFrameworkItemHeading.text =
                questionsFrameworkItem.questionsFrameworkTitle
            if (questionsFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET) {
                binding.ibClearAndDelete.visibility = View.INVISIBLE
            } else {
                binding.ibClearAndDelete.visibility = View.VISIBLE
            }

            binding.rvQuestionsTemplateItems.layoutManager =
                LinearLayoutManager(binding.root.context)
            //Need to get the QuestionTemplateItem MutableList from the ViewModel
            var questionTemplateItemML : MutableList<QuestionTemplateItemDC> = mutableListOf()

            binding.rvQuestionsTemplateItems.adapter = QuestionTemplateRVAdapter(questionTemplateItemML,aInfo5ViewModel,position,questionsFrameworkItem.questionsFrameworkTitle,questionsFrameworkItem.serialStatus)

            val isExpandable = questionsFrameworkItem.isExpandable
            binding.rvQuestionsTemplateItems.visibility =
                if (isExpandable) View.VISIBLE else View.GONE

            binding.llQuestionsFrameworkItem.setOnClickListener {
                clickListener2(questionsFrameworkItem.pageCode)
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
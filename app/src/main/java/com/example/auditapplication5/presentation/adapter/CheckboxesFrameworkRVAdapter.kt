package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
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
            .inflate(LayoutInflater.from(parent.context),parent, false)
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

    inner class ViewHolder(val binding: CheckboxesFrameworkItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(checkboxesFrameworkItem: CheckboxesFrameworkItemDC, position: Int){
            binding.tvCheckboxesFrameworkItemHeading.text = checkboxesFrameworkItem.checkboxesFrameworkTitle
            if (checkboxesFrameworkItem.serialStatus == MainActivity.PRIMARY_QUESTION_SET) {
                binding.ibClearAndDelete2.visibility = View.INVISIBLE
            } else {
                binding.ibClearAndDelete2.visibility = View.VISIBLE
            }
            binding.rvCheckboxesTemplateItems.layoutManager =
                LinearLayoutManager(binding.root.context)

            //Need to define the screen variable and get the checkbox template item from the viewmodel
            //Need to get the CheckboxTemplateItem MutableList from the ViewModel
            var checkboxTemplateItemML : MutableList<CheckboxTemplateItemDC> = mutableListOf()

            //Ensure that this is changed to getting from viewmodel
            val screen = aInfo5ViewModel.getTheScreenVariable()

            if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                binding.rvCheckboxesTemplateItems.adapter = CheckboxTemplateRVAdapter(checkboxTemplateItemML,aInfo5ViewModel,screen,checkboxesFrameworkItem.serialStatus)
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                binding.rvCheckboxesTemplateItems.adapter = CheckboxTemplateRVAdapter(checkboxTemplateItemML,aInfo5ViewModel,screen,checkboxesFrameworkItem.serialStatus)
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                binding.rvCheckboxesTemplateItems.adapter = CheckboxTemplateRVAdapter(checkboxTemplateItemML,aInfo5ViewModel,screen,checkboxesFrameworkItem.serialStatus)
            }

            val isExpandable = checkboxesFrameworkItem.isExpandable
            binding.rvCheckboxesTemplateItems.visibility =
                if (isExpandable) View.VISIBLE else View.GONE


            binding.llCheckboxesFrameworkItem.setOnClickListener {
                clickListener2(checkboxesFrameworkItem.pageCode)
                isAnyItemExpanded(position)
                checkboxesFrameworkItem.isExpandable = !checkboxesFrameworkItem.isExpandable
                notifyItemChanged(position)
            }

            binding.ibClearAndDelete2.setOnClickListener{
                clickListener1(checkboxesFrameworkItem.checkboxesFrameworkTitle, position)
            }


        }

    }
}
package com.example.auditapplication5.presentation.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.R
import com.example.auditapplication5.data.model.CheckboxTemplateItemDC
import com.example.auditapplication5.databinding.CheckboxTemplateItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class CheckboxTemplateRVAdapter(
    private val checkboxTemplateItemML: MutableList<CheckboxTemplateItemDC> = mutableListOf(),
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val screen: String = "",
    private val checkboxesFrameworkSerialStatus: String = ""
): RecyclerView.Adapter<CheckboxTemplateRVAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckboxTemplateItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val checkboxTemplateItem = checkboxTemplateItemML[position]
        holder.bind(checkboxTemplateItem, position)
    }

    override fun getItemCount(): Int {
        return checkboxTemplateItemML.size
    }

    inner class ViewHolder(val binding: CheckboxTemplateItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(checkboxTemplateItem: CheckboxTemplateItemDC, checkboxPosition: Int){
            if (checkboxTemplateItem.checkboxVisibility == false){
                binding.cbCheckboxItem.visibility = View.GONE
            } else {
                binding.cbCheckboxItem.visibility = View.VISIBLE
                binding.cbCheckboxItem.setText(checkboxTemplateItem.checkboxLabel)
            }

            if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS || screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS){
                binding.buttonPriorityChoices.visibility = View.GONE
            } else {
                binding.buttonPriorityChoices.visibility = View.VISIBLE
            }

            val buttonChoicesML = Resources.getSystem().getStringArray(R.array.Priority_Choices).toMutableList()
            //val buttonChoicesML = mutableListOf<String>("HP", "MP","RA", "PC")
            //Button priority choice needs to be set from Data
            var buttonPriorityChoice = ""
            buttonPriorityChoice = buttonChoicesML[0]
            binding.buttonPriorityChoices.text = buttonPriorityChoice

            binding.buttonPriorityChoices.setOnClickListener {
                val index = buttonChoicesML.indexOf(binding.buttonPriorityChoices.text.toString())
                val indexTotal = buttonChoicesML.size
                if (index < indexTotal - 1){
                    binding.buttonPriorityChoices.text = buttonChoicesML[index + 1]
                } else if (index == indexTotal -1){
                    binding.buttonPriorityChoices.text = buttonChoicesML[0]
                }
                //Update ViewModel Here

            }
            binding.cbCheckboxItem.setOnCheckedChangeListener{_, isChecked ->
                if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS){
                    //aInfo5ViewModel.updateCheckboxValuesInRecommendations(binding.checkboxUnit.text.toString().replace("#", ","), isChecked)
                } else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS){
                    //aInfo5ViewModel.updateCheckboxValuesInStandards(binding.checkboxUnit.text.toString().replace("#", ","), isChecked)
                } else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS){
                    //aInfo5ViewModel.updateCheckboxValuesInObservations(binding.checkboxUnit.text.toString().replace("#", ","), isChecked)
                }

            }


        }

    }
}
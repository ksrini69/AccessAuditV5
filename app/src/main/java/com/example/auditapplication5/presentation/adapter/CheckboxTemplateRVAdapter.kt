package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.CheckboxTemplateItemDC
import com.example.auditapplication5.databinding.CheckboxTemplateItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class CheckboxTemplateRVAdapter(
    private val checkboxTemplateItemML: MutableList<CheckboxTemplateItemDC> = mutableListOf(),
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val screen: String = "",
    private val checkboxesFrameworkSerialStatus: String = "",
    private val checkboxesFrameworkTitle: String,
    private val checkboxesFrameworkIndex: Int = 0
) : RecyclerView.Adapter<CheckboxTemplateRVAdapter.ViewHolder>() {

    val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
    val currentPageData = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex]


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckboxTemplateItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val checkboxTemplateItem = checkboxTemplateItemML[position]
        holder.bind(checkboxTemplateItem, position)
    }

    override fun getItemCount(): Int {
        return checkboxTemplateItemML.size
    }

    inner class ViewHolder(val binding: CheckboxTemplateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(checkboxTemplateItem: CheckboxTemplateItemDC, templateCheckboxPosition: Int) {
            if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                binding.buttonPriorityChoices.visibility = View.GONE
                val observationsTemplateItemML = aInfo5ViewModel.observationsList_LD.value
                val result = observationsTemplateItemML?.let {
                    aInfo5ViewModel.isObsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex, checkboxesFrameworkIndex,
                        it
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateObsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex, checkboxesFrameworkIndex,
                        observationsTemplateItemML
                    )
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS){
                binding.buttonPriorityChoices.visibility = View.VISIBLE
                val recommendationsTemplateItemML = aInfo5ViewModel.recommendationsList_LD.value
                val result = recommendationsTemplateItemML?.let {
                    aInfo5ViewModel.isRecoCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex, checkboxesFrameworkIndex,
                        it
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateRecoCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex, checkboxesFrameworkIndex,
                        recommendationsTemplateItemML
                    )
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS){
                binding.buttonPriorityChoices.visibility = View.GONE
                val standardsTemplateItemML = aInfo5ViewModel.standardsList_LD.value
                val result = standardsTemplateItemML?.let {
                    aInfo5ViewModel.isStdsCheckboxesDataItemListUpdatedInPresentSectionAllData(
                        currentPageIndex,
                        checkboxesFrameworkIndex,
                        standardsTemplateItemML
                    )
                }
                if (result == false) {
                    aInfo5ViewModel.updateStdsCheckboxesDataItemListUsingTemplateInPresentSectionAllData(
                        currentPageIndex,
                        checkboxesFrameworkIndex,
                        standardsTemplateItemML
                    )
                }
            }

            if (!checkboxTemplateItem.checkboxVisibility) {
                binding.cbCheckboxItem.visibility = View.GONE
            } else {
                binding.cbCheckboxItem.visibility = View.VISIBLE
                binding.cbCheckboxItem.setText(checkboxTemplateItem.checkboxLabel.replace("#", ","))
            }

//            val buttonChoicesML =
//                Resources.getSystem().getStringArray(R.array.Priority_Choices).toMutableList()
            val buttonChoicesML = mutableListOf<String>("HP", "MP","RA", "PC")
            //Button priority choice and old checkbox label needs to be set from Data
            var buttonPriorityChoice = ""
            var oldCheckboxTickedValue = false
            if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS){
                if (currentPageData.observationsFrameworkDataItemList.size >= checkboxesFrameworkIndex){
                    if (currentPageData.observationsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML.size>=templateCheckboxPosition){
                        oldCheckboxTickedValue = currentPageData.observationsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML[templateCheckboxPosition].checkboxTickedValue
                    }
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS){
                if (currentPageData.recommendationsFrameworkDataItemList.size >= checkboxesFrameworkIndex){
                    if (currentPageData.recommendationsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML.size>=templateCheckboxPosition){
                        buttonPriorityChoice = currentPageData.recommendationsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML[templateCheckboxPosition].priorityValues
                        oldCheckboxTickedValue = currentPageData.recommendationsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML[templateCheckboxPosition].checkboxTickedValue
                        if (buttonPriorityChoice == ""){
                            buttonPriorityChoice = buttonChoicesML[0]
                        } else {
                            if (!buttonChoicesML.contains(buttonPriorityChoice)){
                                buttonPriorityChoice = buttonChoicesML[0]
                            }
                        }
                    }
                }
            }
            else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS){
                if (currentPageData.standardsFrameworkDataItemList.size >= checkboxesFrameworkIndex){
                    if (currentPageData.standardsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML.size>=templateCheckboxPosition){
                        oldCheckboxTickedValue = currentPageData.standardsFrameworkDataItemList[checkboxesFrameworkIndex].checkboxDataItemML[templateCheckboxPosition].checkboxTickedValue
                    }
                }
            }


            binding.buttonPriorityChoices.text = buttonPriorityChoice

            binding.cbCheckboxItem.isChecked = oldCheckboxTickedValue

            binding.buttonPriorityChoices.setOnClickListener {
                var oldCBPriorityValue = ""
                if (checkboxesFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    oldCBPriorityValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                } else if (checkboxesFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                    oldCBPriorityValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                }
                val index = buttonChoicesML.indexOf(binding.buttonPriorityChoices.text.toString())
                val indexTotal = buttonChoicesML.size
                if (index < indexTotal - 1) {
                    binding.buttonPriorityChoices.text = buttonChoicesML[index + 1]
                } else if (index == indexTotal - 1) {
                    binding.buttonPriorityChoices.text = buttonChoicesML[0]
                }
                var newCBPriorityValue = ""
                if (checkboxesFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    newCBPriorityValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                } else if (checkboxesFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                    newCBPriorityValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                }
                if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS){
                    aInfo5ViewModel.updateRecoPriorityValueInPresentSectionAllData(binding.buttonPriorityChoices.text.toString(), currentPageIndex, checkboxesFrameworkIndex, templateCheckboxPosition)
                    aInfo5ViewModel.updateRecoPriorityValueInRecommendations(oldCBPriorityValue,newCBPriorityValue,currentPageIndex)
                }
            }

            binding.cbCheckboxItem.setOnCheckedChangeListener { _, isChecked ->
                if (screen == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                    aInfo5ViewModel.updateRecoCheckboxTickedValueInPresentSectionAllData(isChecked, currentPageIndex, checkboxesFrameworkIndex, templateCheckboxPosition)
                    var recoCheckboxLabelWithPriorityValue = ""
                    if (checkboxesFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                        recoCheckboxLabelWithPriorityValue =  checkboxesFrameworkTitle + ":" +binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                    } else if (checkboxesFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                        recoCheckboxLabelWithPriorityValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",") + "[" + binding.buttonPriorityChoices.text.toString()+ "]"
                    }
                    aInfo5ViewModel.updateRecoCheckboxValueWithPriorityInRecommendations(recoCheckboxLabelWithPriorityValue,isChecked, currentPageIndex)
                } else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                    aInfo5ViewModel.updateStdsCheckboxTickedValueInPresentSectionAllData(isChecked, currentPageIndex, checkboxesFrameworkIndex, templateCheckboxPosition)
                    aInfo5ViewModel.updateStdsCheckboxValueInStandards(binding.cbCheckboxItem.text.toString().replace("#", ","), isChecked, currentPageIndex)
                } else if (screen == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                    aInfo5ViewModel.updateObsCheckboxTickedValueInPresentSectionAllData(isChecked, currentPageIndex, checkboxesFrameworkIndex, templateCheckboxPosition)
                    var obsCheckboxLabelValue = ""
                    if (checkboxesFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                        obsCheckboxLabelValue = binding.cbCheckboxItem.text.toString().replace("#", ",")
                    } else if (checkboxesFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                        obsCheckboxLabelValue = checkboxesFrameworkTitle + ":" + binding.cbCheckboxItem.text.toString().replace("#", ",")
                    }
                    aInfo5ViewModel.updateObsCheckboxValueInObservations(obsCheckboxLabelValue, isChecked, currentPageIndex)
                }
            }
        }
    }
}
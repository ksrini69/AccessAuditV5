package com.example.auditapplication5.presentation.adapter

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.QuestionTemplateItemDC
import com.example.auditapplication5.databinding.QuestionTemplateItemBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel

class QuestionTemplateRVAdapter(
    private val questionTemplateItemML: MutableList<QuestionTemplateItemDC> = mutableListOf(),
    private val aInfo5ViewModel: AInfo5ViewModel,
    private val questionsFrameworkIndex: Int,
    private val questionsFrameworkTitle: String = "",
    private val questionsFrameworkSerialStatus: String = ""
) :RecyclerView.Adapter<QuestionTemplateRVAdapter.ViewHolder>() {

    val currentPageIndex = aInfo5ViewModel.getThePresentSectionAllPagesFrameworkIndex()
    val currentPageData = aInfo5ViewModel.getThePresentSectionAllData().sectionAllPagesData.sectionPageDataList[currentPageIndex]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionTemplateItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionTemplateItem = questionTemplateItemML[position]
        holder.bind(questionTemplateItem, position)
    }

    override fun getItemCount(): Int {
        return questionTemplateItemML.size
    }



    inner class ViewHolder(val binding: QuestionTemplateItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(questionTemplateItem: QuestionTemplateItemDC, templateQuestionPosition: Int){
            val result = questionTemplateItemML?.let {
                aInfo5ViewModel.isQuestionDataItemListUpdatedInPresentSectionAllData(currentPageIndex,questionsFrameworkIndex,
                    it
                )
            }
            if (result == false){
                aInfo5ViewModel.updateQuestionDataItemListUsingTemplateInPresentSectionAllData(currentPageIndex,questionsFrameworkIndex,
                    questionTemplateItemML
                )
            }

            binding.tvBlockNumber.text = questionTemplateItem.blockNumber
            binding.tvQuestion.text = questionTemplateItem.question

            if (questionTemplateItem.message.contains("<>") || questionTemplateItem.message == "") {
                binding.tvMessage.visibility = View.GONE
            }
            else {
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvMessage.text = questionTemplateItem.message
            }

            if (questionTemplateItem.mandatory.contains("<>") || questionTemplateItem.mandatory == "") {
                binding.tvMandatory.visibility = View.GONE
            }
            else {
                binding.tvMandatory.visibility = View.VISIBLE
                if (questionTemplateItem.mandatory.contains("M")){
                    binding.tvMandatory.text = questionTemplateItem.mandatory.replace("M", "Mandatory")
                }
            }

            //Data Field 1 Related
            if (questionTemplateItem.data1Visibility == false) {
                binding.llDataField1.visibility = View.GONE
            } else {
                binding.llDataField1.visibility = View.VISIBLE
                binding.tvDataField1Description.text = questionTemplateItem.data1Label
            }
            if (questionTemplateItem.data1Hint != "<>" && questionTemplateItem.data1Hint != "") {
                binding.etDataField1.hint = questionTemplateItem.data1Hint
            }
            if (questionTemplateItem.data1Type == "N") {
                binding.etDataField1.inputType = InputType.TYPE_CLASS_NUMBER
            } else {
                binding.etDataField1.inputType = InputType.TYPE_CLASS_TEXT
            }
            //Getting the Sentences in place for Data Field 1 from the Template
            var s11 = ""
            if (questionsFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                s11 = questionTemplateItem.data1Sentence1.trim()
            } else if (questionsFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                s11 = questionsFrameworkTitle + ": " + questionTemplateItem.data1Sentence1.trim()
            }
            var s12 = ""
            s12 = questionTemplateItem.data1Sentence2.trim()

            //Getting the field 1 data
            var dataField1Value = ""
            if (currentPageData.questionsFrameworkDataItemList.size >= questionsFrameworkIndex){
                if (currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size >= templateQuestionPosition){
                    dataField1Value = currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[templateQuestionPosition].data1Value
                }
            }
            if (dataField1Value != ""){
                binding.etDataField1.setText(dataField1Value)
            }

            //Setting up the text watcher to capture the data entered
            var dataField1OldValue = ""
            var dataField1PresentValue = ""
            var isDataField1TextChanged = false
            binding.etDataField1.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    isDataField1TextChanged = false
                    dataField1OldValue = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    isDataField1TextChanged = true
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isDataField1TextChanged == true){
                        isDataField1TextChanged = false
                        dataField1PresentValue = s.toString()
                        //Update ViewModel here
                        aInfo5ViewModel.updateData1ValueInPresentSectionAllData(dataField1PresentValue,currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                        aInfo5ViewModel.updateData1ValueInObservations(dataField1OldValue, dataField1PresentValue,s11, s12,currentPageIndex)
                    }
                }

            })

            //Data Field 2 Related
            if (questionTemplateItem.data2Visibility == false) {
                binding.llDataField2.visibility = View.GONE
            }
            else {
                binding.llDataField2.visibility = View.VISIBLE
                binding.tvDataField2Description.text = questionTemplateItem.data2Label
            }
            if (questionTemplateItem.data2Hint != "<>" && questionTemplateItem.data2Hint != "") {
                binding.etDataField2.hint = questionTemplateItem.data2Hint
            }
            if (questionTemplateItem.data2Type == "N") {
                binding.etDataField2.inputType = InputType.TYPE_CLASS_NUMBER
            }
            else {
                binding.etDataField2.inputType = InputType.TYPE_CLASS_TEXT
            }

            //Getting the Sentences in place for Data Field 2
            var s21 = ""
            if (questionsFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                s21 = questionTemplateItem.data2Sentence1.trim()
            }
            else if (questionsFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                s21 = questionsFrameworkTitle + ": " + questionTemplateItem.data2Sentence1.trim()
            }
            var s22 = ""
            s22 = questionTemplateItem.data2Sentence2.trim()

            //Getting the field 2 data
            var dataField2Value = ""
            if (currentPageData.questionsFrameworkDataItemList.size >= questionsFrameworkIndex){
                if (currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size >= templateQuestionPosition){
                    dataField2Value = currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[templateQuestionPosition].data2Value
                }
            }
            if (dataField2Value != ""){
                binding.etDataField2.setText(dataField2Value)
            }

            //Setting up the text watcher to capture the data entered
            var dataField2OldValue = ""
            var dataField2PresentValue = ""
            var isDataField2TextChanged = false
            binding.etDataField2.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    isDataField2TextChanged = false
                    dataField2OldValue = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    isDataField2TextChanged = true
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isDataField2TextChanged == true){
                        isDataField2TextChanged = false
                        dataField2PresentValue = s.toString()
                        //Update ViewModel here
                        aInfo5ViewModel.updateData2ValueInPresentSectionAllData(dataField2PresentValue,currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                        aInfo5ViewModel.updateData2ValueInObservations(dataField2OldValue, dataField2PresentValue,s21, s22,currentPageIndex)

                    }
                }

            })

            //Data Field 3 Related
            if (questionTemplateItem.data3Visibility == false) {
                binding.llDataField3.visibility = View.GONE
            }
            else {
                binding.llDataField3.visibility = View.VISIBLE
                binding.tvDataField3Description.text = questionTemplateItem.data3Label
            }
            if (questionTemplateItem.data3Hint != "<>" && questionTemplateItem.data3Hint != "") {
                binding.etDataField3.hint = questionTemplateItem.data3Hint
            }
            if (questionTemplateItem.data3Type == "N") {
                binding.etDataField3.inputType = InputType.TYPE_CLASS_NUMBER
            }
            else {
                binding.etDataField3.inputType = InputType.TYPE_CLASS_TEXT
            }

            //Getting the Sentences in place for Data Field 3
            var s31 = ""
            if (questionsFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                s31 = questionTemplateItem.data3Sentence1.trim()
            }
            else if (questionsFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                s31 = questionsFrameworkTitle + ": " + questionTemplateItem.data3Sentence1.trim()
            }
            var s32 = ""
            s32 = questionTemplateItem.data3Sentence2.trim()

            //Getting the field 2 data
            var dataField3Value = ""
            if (currentPageData.questionsFrameworkDataItemList.size >= questionsFrameworkIndex){
                if (currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size >= templateQuestionPosition){
                    dataField3Value = currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[templateQuestionPosition].data3Value
                }
            }
            if (dataField3Value != ""){
                binding.etDataField3.setText(dataField2Value)
            }

            //Setting up the text watcher to capture the data entered
            var dataField3OldValue = ""
            var dataField3PresentValue = ""
            var isDataField3TextChanged = false
            binding.etDataField3.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    isDataField3TextChanged = false
                    dataField3OldValue = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    isDataField3TextChanged = true
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isDataField3TextChanged == true){
                        isDataField3TextChanged = false
                        dataField3PresentValue = s.toString()
                        //Update ViewModel here
                        aInfo5ViewModel.updateData3ValueInPresentSectionAllData(dataField3PresentValue,currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                        aInfo5ViewModel.updateData3ValueInObservations(dataField3OldValue, dataField3PresentValue,s31, s32,currentPageIndex)
                    }
                }

            })

            //Button Related
            if (questionTemplateItem.buttonVisibility == false) {
                binding.buttonChoose.visibility = View.GONE
            } else {
                binding.buttonChoose.visibility = View.VISIBLE
            }
            //buttonDataText should be filled by the data
            var buttonDataText = ""
            if (currentPageData.questionsFrameworkDataItemList.size >= questionsFrameworkIndex){
                if (currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList.size >= templateQuestionPosition){
                    buttonDataText = currentPageData.questionsFrameworkDataItemList[questionsFrameworkIndex].questionDataItemList[templateQuestionPosition].buttonOptionChosen
                }
            }

            if (buttonDataText == ""){
                binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[0]
            } else {
                if (questionTemplateItem.buttonOptionsList.contains(buttonDataText)){
                    val buttonDataTextIndex = questionTemplateItem.buttonOptionsList.indexOf(buttonDataText)
                    if (buttonDataTextIndex%2 == 0){
                        binding.buttonChoose.text = buttonDataText
                    } else {
                        if (buttonDataTextIndex>0){
                            binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[buttonDataTextIndex-1]
                        } else {
                            binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[0]
                        }
                    }
                } else {
                    binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[0]
                }
            }

            binding.buttonChoose.setOnClickListener {
                if (questionsFrameworkSerialStatus == MainActivity.PRIMARY_QUESTION_SET){
                    var oldButtonChoiceIndex = questionTemplateItem.buttonOptionsList.indexOf(binding.buttonChoose.text.toString())
                    val oldButtonChoiceTextValue = questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 1]
                    val indexTotal = questionTemplateItem.buttonOptionsList.size
                    if (indexTotal % 2 == 0) {
                        if (oldButtonChoiceIndex + 2 < indexTotal) {
                            if (questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 2].contains("<>")){
                                oldButtonChoiceIndex = 0
                                binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex]
                                aInfo5ViewModel.updateButtonChoiceInThePresentSectionAllData(
                                    binding.buttonChoose.text.toString(),currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                                aInfo5ViewModel.updateButtonChoiceTextInObservations(oldButtonChoiceTextValue + "\n",questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 1].replace("<>", "") ,currentPageIndex)
                            } else {
                                binding.buttonChoose.text =
                                    questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 2]
                                aInfo5ViewModel.updateButtonChoiceInThePresentSectionAllData(
                                    binding.buttonChoose.text.toString(), currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                                aInfo5ViewModel.updateButtonChoiceTextInObservations(oldButtonChoiceTextValue + "\n",questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 3] + "\n" ,currentPageIndex)
                            }

                        } else if (oldButtonChoiceIndex + 2 == indexTotal) {
                            oldButtonChoiceIndex = 0
                            binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex]
                            aInfo5ViewModel.updateButtonChoiceInThePresentSectionAllData(
                                binding.buttonChoose.text.toString(),currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                            aInfo5ViewModel.updateButtonChoiceTextInObservations(oldButtonChoiceTextValue + "\n",questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 1].replace("<>", "") ,currentPageIndex)
                        }
                    }
                }
                else if (questionsFrameworkSerialStatus == MainActivity.OTHER_QUESTION_SET){
                    var oldButtonChoiceIndex =
                        questionTemplateItem.buttonOptionsList.indexOf(binding.buttonChoose.text.toString())
                    val oldButtonChoiceTextValue = questionsFrameworkTitle + ": " +questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 1] + "\n"
                    val indexTotal = questionTemplateItem.buttonOptionsList.size
                    if (indexTotal % 2 == 0) {
                        if (oldButtonChoiceIndex + 2 < indexTotal) {
                            binding.buttonChoose.text =
                                questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 2]
                            aInfo5ViewModel.updateButtonChoiceInThePresentSectionAllData(
                                binding.buttonChoose.text.toString(), currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                            aInfo5ViewModel.updateButtonChoiceTextInObservations(oldButtonChoiceTextValue,questionsFrameworkTitle+ ": " + questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 3] + "\n" ,currentPageIndex)
                        } else if (oldButtonChoiceIndex + 2 == indexTotal) {
                            oldButtonChoiceIndex = 0
                            binding.buttonChoose.text = questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex]
                            aInfo5ViewModel.updateButtonChoiceInThePresentSectionAllData(
                                binding.buttonChoose.text.toString(), currentPageIndex,questionsFrameworkIndex,templateQuestionPosition)
                            aInfo5ViewModel.updateButtonChoiceTextInObservations(oldButtonChoiceTextValue, questionTemplateItem.buttonOptionsList[oldButtonChoiceIndex + 1].replace("<>", ""),currentPageIndex )
                        }
                    }
                }
            }
        }
    }
}
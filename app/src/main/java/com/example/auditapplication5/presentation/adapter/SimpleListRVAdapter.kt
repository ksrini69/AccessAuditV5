package com.example.auditapplication5.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.data.model.CodeNameAndDisplayNameDC
import com.example.auditapplication5.databinding.RvSimpleListItemBinding

class SimpleListRVAdapter(
    private val namesList: MutableList<String>,
    private val codesAndNamesML: MutableList<CodeNameAndDisplayNameDC>,
    private val codeNameAndDisplayFlag : Boolean = true,
    private val clickListener: (name: String, code: String) -> Unit
) : RecyclerView.Adapter<SimpleListRVAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvSimpleListItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var name = ""
        var code = ""
        if (codeNameAndDisplayFlag == true){
            name = codesAndNamesML[position].displayName
            code = codesAndNamesML[position].uniqueCodeName
        } else {
            name = namesList[position]
            code = ""
        }
        holder.bind(name, code, clickListener)
    }

    override fun getItemCount(): Int {
        var size = 0
        if (codeNameAndDisplayFlag == true){
            size = codesAndNamesML.size
        } else {
            size = namesList.size
        }
        return size
    }

    inner class ViewHolder(val binding: RvSimpleListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(name : String, code: String, clickListener: (name: String, code: String) -> Unit){
            binding.tvSimpleListItem.text = name
            binding.root.setOnClickListener {
                clickListener(name, code)
            }
        }
    }
}
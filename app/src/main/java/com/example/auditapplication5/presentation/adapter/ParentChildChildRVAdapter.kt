package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.databinding.RvParentChildChildItemBinding

class ParentChildChildRVAdapter(
    private val pageCodesML: MutableList<String>,
    private val clickListener: (pageCode: String) -> Unit
) : RecyclerView.Adapter<ParentChildChildRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvParentChildChildItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pageCode = pageCodesML[position]
        holder.bind(pageCode, position)
    }

    override fun getItemCount(): Int {
        return pageCodesML.size
    }

    fun extractDisplayNameFromPageCode(pageCode: String) : String{
        var result = ""
        if (pageCode != ""){
            if (pageCode.contains("_PC") && pageCode.contains("PC_") ){
                result = pageCode.replace("_PC", "")
                result = result.replace("PC_", "")
                result = result.dropLast(3)
                result = result.replace("_", " ").trim()
            } else {
                result = pageCode
            }
        }
        return result
    }

    inner class ViewHolder(val binding: RvParentChildChildItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(pageCode: String, position: Int){
            binding.tvParentChildChildItem.text = extractDisplayNameFromPageCode(pageCode)

            binding.tvParentChildChildItem.setOnClickListener {
                clickListener(pageCode)
            }
        }
    }
}
package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.data.model.SectionAllPagesFrameworkDC
import com.example.auditapplication5.databinding.RvSimpleListItemBinding

class GotoRVAdapter(
    private val sectionFramework: SectionAllPagesFrameworkDC,
    private val clickListener: (pageTitle: String, pagePosition: Int) -> Unit
) : RecyclerView.Adapter<GotoRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvSimpleListItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pageTitle = sectionFramework.sectionPageFrameworkList[position].pageTitle
        val pagePosition = position
        holder.bind(pageTitle, pagePosition, clickListener)
    }

    override fun getItemCount(): Int {
        return sectionFramework.sectionPageFrameworkList.size
    }

    inner class ViewHolder(val binding: RvSimpleListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            pageTitle: String,
            pagePosition: Int,
            clickListener: (pageTitle: String, pagePosition: Int) -> Unit
        ){
            binding.tvSimpleListItem.text = pageTitle
            binding.root.setOnClickListener {
                clickListener(pageTitle, pagePosition)
            }
        }
    }
}
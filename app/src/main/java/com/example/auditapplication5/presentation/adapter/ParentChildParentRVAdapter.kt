package com.example.auditapplication5.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.data.model.RVParentChildParentItemDC
import com.example.auditapplication5.databinding.RvParentChildParentItemBinding

class ParentChildParentRVAdapter(
    private val rvParentChildParentItemML: MutableList<RVParentChildParentItemDC>,
    private val clickListener: (pageCode: String) -> Unit
) : RecyclerView.Adapter<ParentChildParentRVAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvParentChildParentItemBinding
            .inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rvParentChildParentItem = rvParentChildParentItemML[position]
        holder.bind(rvParentChildParentItem,position)
    }

    override fun getItemCount(): Int {
        return rvParentChildParentItemML.size
    }

    fun isAnyItemExpanded(position: Int) {
        val temp = rvParentChildParentItemML.indexOfFirst {
            it.isExpandable
        }
        if (temp >= 0 && temp != position) {
            rvParentChildParentItemML[temp].isExpandable = false
            notifyItemChanged(temp)
        }
    }

    inner class ViewHolder(val binding: RvParentChildParentItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(rvParentChildParentItem: RVParentChildParentItemDC,
                 position: Int){
            binding.tvRvParentChildParentItem.text = rvParentChildParentItem.title
            binding.rvParentChildChildRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvParentChildChildRecyclerview.adapter =
                ParentChildChildRVAdapter(rvParentChildParentItem.childItemList.map { it.trim() }.sorted().toMutableList() ) { selectedPageCode: String ->
                    clickListener(selectedPageCode)
                }


            val isExpandable = rvParentChildParentItem.isExpandable
            binding.rvParentChildChildRecyclerview.visibility = if (isExpandable) View.VISIBLE else View.GONE
            binding.tvRvParentChildParentItem.setOnClickListener {
                isAnyItemExpanded(position)
                rvParentChildParentItem.isExpandable =
                    !rvParentChildParentItem.isExpandable
                notifyItemChanged(position)
            }
        }
    }
}
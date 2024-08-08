package com.example.auditapplication5

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.example.auditapplication5.databinding.FragmentParentChildRecyclerviewBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class ParentChildRecyclerviewFragment : Fragment() {
    private lateinit var binding: FragmentParentChildRecyclerviewBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_parent_child_recyclerview, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                TODO("Not yet implemented")
            }

        })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Functions below


}
package com.example.auditapplication5

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.SectionAllPagesFrameworkDC
import com.example.auditapplication5.databinding.FragmentGotoRecyclerviewBinding
import com.example.auditapplication5.presentation.adapter.GotoRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class GotoRecyclerviewFragment : Fragment() {
    private lateinit var binding: FragmentGotoRecyclerviewBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_goto_recyclerview, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreenVariable())
                findNavController().navigate(R.id.action_gotoRecyclerviewFragment_to_observationsFragment)
            }

        })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()
        //Set Screen
        aInfo5ViewModel.setTheScreenVariable(MainActivity.GOTO_RECYCLERVIEW_FRAGMENT)
        //Get the appropriate list to be given to the recycler view
        val presentSectionAllPagesFramework = aInfo5ViewModel.presentSectionAllPagesFramework
        loadRecyclerView(presentSectionAllPagesFramework)
    }


    //Functions below

    private fun loadRecyclerView(presentSectionAllPagesFramework: SectionAllPagesFrameworkDC) {
        binding.rvGoto.setBackgroundColor(Color.LTGRAY)
        binding.rvGoto.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvGoto.adapter =
            GotoRVAdapter(presentSectionAllPagesFramework) { selectedItemName: String, selectedItemPosition: Int ->
                gotoListItemClicked(
                    selectedItemName,
                    selectedItemPosition
                )
            }
    }

    private fun gotoListItemClicked(pageTitle: String, currentPageIndex: Int) {

        aInfo5ViewModel.setThePageCountMLD(currentPageIndex + 1)
        aInfo5ViewModel.setThePresentSectionAllPagesFrameworkIndex(currentPageIndex)
        aInfo5ViewModel.setTheSectionAllPagesFrameworkLoadedFlagMLD(true)
        aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getTheScreenVariable())
        aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
        findNavController().navigate(R.id.action_gotoRecyclerviewFragment_to_observationsFragment)
    }


}
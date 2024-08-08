package com.example.auditapplication5

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.databinding.FragmentObservationsBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class ObservationsFragment : Fragment() {
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_observations, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        binding.aInfo5ViewModel = aInfo5ViewModel
        binding.lifecycleOwner = viewLifecycleOwner


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                aInfo5ViewModel.setTheScreenVariable(MainActivity.SECTION_FRAGMENT_SECTION_CHOICE)
                findNavController().navigate(R.id.action_observationsFragment_to_sectionAndIntrosFragment)
            }
        })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Expand and Collapse Views

        var isActionButtonsExpanded = true
        fun setActionButtonsExpandedValue(input: Boolean) {
            isActionButtonsExpanded = input
        }

        fun getActionButtonsExpandedValue(): Boolean {
            return isActionButtonsExpanded
        }

        binding.tvPageNameAndNumber.setOnClickListener {
            setActionButtonsExpandedValue(!getActionButtonsExpandedValue())
            if (getActionButtonsExpandedValue()) {
                binding.llActionButtons1.visibility = View.VISIBLE
                binding.llActionButtons2.visibility = View.VISIBLE
                binding.etModifyPageNameByUser.visibility = View.VISIBLE

            } else {
                binding.llActionButtons1.visibility = View.GONE
                binding.llActionButtons2.visibility = View.GONE
                binding.etModifyPageNameByUser.visibility = View.GONE
            }
        }

        var isObsRecoExpanded = true
        fun setObsRecoExpandedValue(input: Boolean) {
            isObsRecoExpanded = input
        }

        fun getObsRecoExpandedValue(): Boolean {
            return isObsRecoExpanded
        }
        binding.tvObservationsRecommendationsLabel.setOnClickListener {
            setObsRecoExpandedValue(!getObsRecoExpandedValue())
            if (getObsRecoExpandedValue()) {
                binding.svObservationsRecommendations.visibility = View.VISIBLE
            } else {
                binding.svObservationsRecommendations.visibility = View.GONE
            }
        }

        var isObsExpanded = true
        fun setObsExpandedValue(input: Boolean) {
            isObsExpanded = input
        }

        fun getObsExpandedValue(): Boolean {
            return isObsExpanded
        }

        binding.tvObservationsPicturesLabel.setOnClickListener {
            setObsExpandedValue(!getObsExpandedValue())
            if (getObsExpandedValue()) {
                binding.tvPhotoPathsInObservationsPage.visibility = View.VISIBLE
                binding.etObservationsOnly.visibility = View.VISIBLE
            } else {
                binding.tvPhotoPathsInObservationsPage.visibility = View.GONE
                binding.etObservationsOnly.visibility = View.GONE
            }
        }

        var isRecoExpanded = true
        fun setRecoExpandedValue(input: Boolean) {
            isRecoExpanded = input
        }

        fun getRecoExpandedValue(): Boolean {
            return isRecoExpanded
        }
        binding.tvRecommendationsStandardsLabel.setOnClickListener {
            setRecoExpandedValue(!getRecoExpandedValue())
            if (getRecoExpandedValue()) {
                binding.tvStandardsInObservationsPage.visibility = View.VISIBLE
                binding.etRecommendationsOnly.visibility = View.VISIBLE
            } else {
                binding.tvStandardsInObservationsPage.visibility = View.GONE
                binding.etRecommendationsOnly.visibility = View.GONE
            }
        }

        var isQsObsRecoStdsLabelExpanded = true
        fun setQsObsRecoStdsLabelExpandedValue(input: Boolean) {
            isQsObsRecoStdsLabelExpanded = input
        }

        fun getQsObsRecoStdsLabelExpandedValue(): Boolean {
            return isQsObsRecoStdsLabelExpanded
        }

        binding.tvQuestionsEtcLabel.setOnClickListener {
            setQsObsRecoStdsLabelExpandedValue(!getQsObsRecoStdsLabelExpandedValue())
            if (getQsObsRecoStdsLabelExpandedValue()) {
                if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                    binding.rvQuestionsFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                    binding.rvCheckboxesFramework.visibility = View.VISIBLE
                }

            } else {
                if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT) {
                    binding.rvQuestionsFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS) {
                    binding.rvCheckboxesFramework.visibility = View.GONE
                }

            }
        }

        //Scrolling for TextViews
        binding.tvPhotoPathsInObservationsPage.movementMethod = ScrollingMovementMethod()
        binding.tvStandardsInObservationsPage.movementMethod = ScrollingMovementMethod()

        // On Click Listeners for Observations, Recommendations and Standards




    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()

    }


    //Functions below




}
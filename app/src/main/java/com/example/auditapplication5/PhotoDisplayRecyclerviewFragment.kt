package com.example.auditapplication5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapplication5.data.model.PhotoDetailsDC
import com.example.auditapplication5.databinding.FragmentPhotoDisplayRecyclerviewBinding
import com.example.auditapplication5.presentation.adapter.PhotoDisplayRVAdapter
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel


class PhotoDisplayRecyclerviewFragment : Fragment() {
    private lateinit var binding: FragmentPhotoDisplayRecyclerviewBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_photo_display_recyclerview, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.INTROS_FRAGMENT){
                    aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
                    aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                    findNavController().navigate(R.id.action_photoDisplayRecyclerviewFragment_to_introductionsScrollingFragment)
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS){
                    aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreenVariable())
                    aInfo5ViewModel.setThePreviousScreenVariable(MainActivity.PHOTO_DISPLAY_RV_FRAGMENT)
                    findNavController().navigate(R.id.action_photoDisplayRecyclerviewFragment_to_observationsFragment)
                }
            }

        })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Set the screen variable
        aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_DISPLAY_RV_FRAGMENT)

        loadRecyclerView(aInfo5ViewModel.listPhotosByLocation(aInfo5ViewModel.getLocationForPhotos()))

    }

    //Functions below

    private fun loadRecyclerView(photosList: MutableList<PhotoDetailsDC>){
        binding.rvPhotoDisplay.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rvPhotoDisplay.adapter = PhotoDisplayRVAdapter(photosList, this.requireContext(),aInfo5ViewModel.getTheCompanyDirectoryURIString().toUri()){
                selectedItem: PhotoDetailsDC, imageLoadedflag: Boolean -> photosListItemClicked(selectedItem, imageLoadedflag)
        }

    }


    private fun photosListItemClicked(selectedPhotoItem: PhotoDetailsDC, imageLoadedFlag: Boolean){
        if (!imageLoadedFlag){
            selectedPhotoItem.photoUriString = ""
            aInfo5ViewModel.setSelectedPhotoItemDC(selectedPhotoItem)
        } else {
            aInfo5ViewModel.setSelectedPhotoItemDC(selectedPhotoItem)
        }
        aInfo5ViewModel.setModifiedPhotoItemDC(aInfo5ViewModel.updateModifiedPhotoNames(selectedPhotoItem))
        findNavController().navigate(R.id.action_photoDisplayRecyclerviewFragment_to_photoModificationFragment)

    }

}
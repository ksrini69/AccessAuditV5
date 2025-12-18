package com.example.auditapplication5

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.databinding.FragmentPhotoModificationBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException


class PhotoModificationFragment : Fragment() {
   private lateinit var binding: FragmentPhotoModificationBinding
   private lateinit var aInfo5ViewModel: AInfo5ViewModel

    private var mImageButtonCurrentPaint : ImageButton? = null
    private var mImageButtonCurrentBrush: ImageButton? = null

    private lateinit var imageView: ImageView

    private val openGalleryLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null){
                binding.ivBackground.visibility = View.VISIBLE
                binding.ivBackground.setImageURI(result.data?.data)
            }
        }

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    aInfo5ViewModel.setStatusMessageSF("Permission Granted. Now you can read the storage files")
//                    Toast.makeText(this.requireContext(), "Permission Granted. Now you can read the storage files",
//                        Toast.LENGTH_SHORT).show()
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE ){
                        aInfo5ViewModel.setStatusMessageSF("You have not granted permission")
//                        Toast.makeText(this.requireContext(), "You have not granted permission",
//                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_photo_modification, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel
        //Enabling two way binding for the captions etc
        binding.aInfo5ViewModel = aInfo5ViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.drawingview = binding.drawingView

        //Set The Screen Variable upon entry into this fragment
        aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT)

        //Status Message using Shared Flow
        observeStatusMessage()

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT){
                    if (!aInfo5ViewModel.getModifiedPhotoUploadedFlag()){
                        showDialogForImageSaveBP()
                    } else {
                        if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.INTROS_FRAGMENT) {
                            aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
                            aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                            findNavController().navigate(R.id.action_cameraXFragment_to_introductionsScrollingFragment)
                        }
                        else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT
                            || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS
                            || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS
                            || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS
                        ) {
                            aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreenVariable())
                            findNavController().navigate(R.id.action_cameraXFragment_to_observationsFragment)
                        }
                    }

                }
                else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_CAPTIONS){
                    binding.llActionButtons1InPhotoModification.visibility = View.VISIBLE
                    binding.llActionButtons2InPhotoModification.visibility = View.VISIBLE
                    binding.llPaintColors.visibility = View.VISIBLE
                    binding.drawingView.visibility = View.VISIBLE
                    binding.tetForCaptions.visibility = View.INVISIBLE
                    binding.tetForCaptions.isEnabled = false
                    aInfo5ViewModel.setModifiedPhotoItemDC(aInfo5ViewModel.saveCaption(aInfo5ViewModel.etTextCaptionsMLD.value.toString(),aInfo5ViewModel.getModifiedPhotoItemDC()))
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT)
                }
                else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT){
                    binding.drawingView.textSave()
                    binding.drawingView.textInput.value = ""
                    binding.llActionButtons2InPhotoModification.visibility = View.VISIBLE
                    binding.drawingView.setTextPathDirection(1)
                    binding.drawingView.setRotationInputFlag(1)
                    binding.tietForPictureLabelling.visibility = View.INVISIBLE
                    binding.tietForPictureLabelling.isEnabled = false
                    binding.drawingView.chooseShape = 4
                    brushSelected(binding.ibOvals)
                    aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT)
                }

            }
        })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        //Uploading the image details from selected Item in ViewModel
        imageView = binding.ivBackground
        if (aInfo5ViewModel.getSelectedPhotoItemDC().photoUriString != ""){
            uploadImageFile(aInfo5ViewModel.getSelectedPhotoItemDC().photoUriString.toUri(), imageView)
        } else {
            context?.let { ContextCompat.getColor(it, R.color.white) }
                ?.let { imageView.setBackgroundColor(it) }
        }
        binding.tvForPhotoTitle.text = aInfo5ViewModel.getSelectedPhotoItemDC().fullPhotoName
        binding.tetForCaptions.setText(aInfo5ViewModel.getSelectedPhotoItemDC().photoCaption)
        aInfo5ViewModel.etTextCaptionsMLD.value = aInfo5ViewModel.getSelectedPhotoItemDC().photoCaption
        binding.tetForCaptions.visibility = View.INVISIBLE

        //Initializing Paint Color to Red
        mImageButtonCurrentPaint = binding.ibColorRed
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this.requireContext(), R.drawable.palette_selected)
        )

        //Initializing Brush Size to 4, Text Size to 40 and Shape to 4
        binding.drawingView.setSizeForBrush(6.toFloat())
        binding.drawingView.setSizeForText(40.toFloat())
        mImageButtonCurrentBrush = binding.ibOvals
        mImageButtonCurrentBrush!!.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.purple_experimental))
        binding.drawingView.chooseShape = 4

        //Setting up OnClickListeners for the Brush, Undo, Save and Text Buttons
        var isColorPaletteExpanded = true
        binding.ibToCollapseAndExpandPaintColours.setOnClickListener {
            isColorPaletteExpanded = !isColorPaletteExpanded
            if (isColorPaletteExpanded){
                binding.ibColorBlack.visibility = View.VISIBLE
                binding.ibColorRed.visibility = View.VISIBLE
                binding.ibColorGreen.visibility = View.VISIBLE
                binding.ibColorBlue.visibility = View.VISIBLE
                binding.ibColorYellow.visibility = View.VISIBLE
                binding.ibColorWhite.visibility = View.VISIBLE
                binding.ibToCollapseAndExpandPaintColours.setImageResource(R.drawable.ic_back_50)
            }
            else {
                binding.ibColorBlack.visibility = View.GONE
                binding.ibColorRed.visibility = View.GONE
                binding.ibColorGreen.visibility = View.GONE
                binding.ibColorBlue.visibility = View.GONE
                binding.ibColorYellow.visibility = View.GONE
                binding.ibColorWhite.visibility = View.GONE
                binding.ibToCollapseAndExpandPaintColours.setImageResource(R.drawable.ic_forward_50)
            }
        }

        var isActionButton1PaletteExpanded = true

        binding.ibToCollapseAndExpandLl1.setOnClickListener {
            isActionButton1PaletteExpanded = !isActionButton1PaletteExpanded
            if (isActionButton1PaletteExpanded){
                binding.ibUndoInPhotoModification.visibility = View.VISIBLE
                binding.ibSave.visibility = View.VISIBLE
                binding.ibBrush.visibility = View.VISIBLE
                binding.ibTextFontForLabels.visibility = View.VISIBLE
                binding.ibRotateTextForLabels.visibility = View.VISIBLE
                binding.ibToCollapseAndExpandLl1.setImageResource(R.drawable.ic_back_50)
            }
            else {
                binding.ibUndoInPhotoModification.visibility = View.GONE
                binding.ibSave.visibility = View.GONE
                binding.ibBrush.visibility = View.GONE
                binding.ibTextFontForLabels.visibility = View.GONE
                binding.ibRotateTextForLabels.visibility = View.GONE
                binding.ibToCollapseAndExpandLl1.setImageResource(R.drawable.ic_forward_50)
            }
        }


        var isActionButton2PaletteExpanded = true

        binding.ibToCollapseAndExpandLl2.setOnClickListener {
            isActionButton2PaletteExpanded = !isActionButton2PaletteExpanded
            if (isActionButton2PaletteExpanded){
                binding.ibClosedCaptions.visibility = View.VISIBLE
                binding.ibRectangle.visibility = View.VISIBLE
                binding.ibOvals.visibility = View.VISIBLE
                binding.ibStraightLineWithArrows.visibility = View.VISIBLE
                binding.ibImageLabels.visibility = View.VISIBLE
                binding.ibToCollapseAndExpandLl2.setImageResource(R.drawable.ic_back_50)
            }else {
                binding.ibClosedCaptions.visibility = View.GONE
                binding.ibRectangle.visibility = View.GONE
                binding.ibOvals.visibility = View.GONE
                binding.ibStraightLineWithArrows.visibility = View.GONE
                binding.ibImageLabels.visibility = View.GONE
                binding.ibToCollapseAndExpandLl2.setImageResource(R.drawable.ic_forward_50)
            }
        }

        binding.ibBrush.setOnClickListener {
            chooseBrushSizeListDialog()
        }

        binding.ibTextFontForLabels.setOnClickListener {
            chooseTextSizeListDialog()
        }

        binding.ibUndoInPhotoModification.setOnClickListener {
            binding.ibUndoInPhotoModification.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibUndoInPhotoModification.startAnimation(animation)
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT){
                aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
                binding.drawingView.onClickUndo()
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT){
                aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
                binding.drawingView.onClickTextUndo()
            }

        }


        binding.ibSave.setOnClickListener {
            binding.ibSave.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.fade_out_in)
            binding.ibSave.startAnimation(animation)
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT){
                showDialogForImageSaveBP()
            } else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_CAPTIONS){
                aInfo5ViewModel.setModifiedPhotoItemDC(aInfo5ViewModel.saveCaption(aInfo5ViewModel.etTextCaptionsMLD.value.toString(), aInfo5ViewModel.getModifiedPhotoItemDC()))
            }else if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT){
                binding.drawingView.textSave()
            }
        }

        binding.ibClosedCaptions.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT_CAPTIONS)
            aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
            binding.llActionButtons1InPhotoModification.visibility = View.INVISIBLE
            binding.llActionButtons2InPhotoModification.visibility = View.INVISIBLE
            binding.llPaintColors.visibility = View.INVISIBLE
            //binding.drawingView.visibility = View.INVISIBLE
            binding.tetForCaptions.visibility = View.VISIBLE
            binding.tietForPictureLabelling.visibility = View.GONE
            binding.tetForCaptions.isEnabled = true
        }

        // Setting up onClickListener for running shape clicked functions
        binding.ibImageLabels.setOnClickListener {
            aInfo5ViewModel.setTheScreenVariable(MainActivity.PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT)
            aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
            binding.llActionButtons2InPhotoModification.visibility = View.INVISIBLE
            binding.drawingView.setTextPathDirection(1)
            binding.drawingView.setRotationInputFlag(1)
            binding.tietForPictureLabelling.visibility = View.VISIBLE
            binding.tetForCaptions.visibility = View.GONE
            binding.tietForPictureLabelling.isEnabled = true
            binding.drawingView.chooseShape = 1
        }

        binding.ibRotateTextForLabels.setOnClickListener {
            if (aInfo5ViewModel.getTheScreenVariable() == MainActivity.PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT){
                aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
                when(binding.drawingView.getRotationInputFlag()){
                    1 -> {
                        binding.drawingView.setRotationInputFlag(2)
                        binding.drawingView.setTextPathDirection(binding.drawingView.getRotationInputFlag())
                    }
                    2-> {
                        binding.drawingView.setRotationInputFlag(3)
                        binding.drawingView.setTextPathDirection(binding.drawingView.getRotationInputFlag())
                    }
                    3-> {
                        binding.drawingView.setRotationInputFlag(4)
                        binding.drawingView.setTextPathDirection(binding.drawingView.getRotationInputFlag())
                    }
                    4 -> {
                        binding.drawingView.setRotationInputFlag(1)
                        binding.drawingView.setTextPathDirection(binding.drawingView.getRotationInputFlag())

                    }
                }
                if (binding.drawingView.getFlagIfTextDrawn()){
                    binding.drawingView.textRotate()
                }
            }
        }

        binding.ibStraightLineWithArrows.setOnClickListener {
            aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
            brushSelected(it)
            binding.drawingView.chooseShape = 2
        }

        binding.ibRectangle.setOnClickListener {
            aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
            brushSelected(it)
            binding.drawingView.chooseShape = 3
        }

        binding.ibOvals.setOnClickListener {
            aInfo5ViewModel.setModifiedPhotoUploadedFlag(false)
            brushSelected(it)
            binding.drawingView.chooseShape = 4
        }

        // Setting up ImageButton onClickListener for running paintClicked Function

        binding.ibColorBlack.setOnClickListener {
            paintClicked(it)
        }
        binding.ibColorRed.setOnClickListener {
            paintClicked(it)
        }
        binding.ibColorGreen.setOnClickListener {
            paintClicked(it)
        }
        binding.ibColorBlue.setOnClickListener {
            paintClicked(it)
        }
        binding.ibColorYellow.setOnClickListener {
            paintClicked(it)
        }
        binding.ibColorWhite.setOnClickListener {
            paintClicked(it)
        }



    }

    override fun onStop() {
        super.onStop()


        //Update and save the Company Report into db
        aInfo5ViewModel.updateSectionDetailsInCompanyReportAndSave(
            aInfo5ViewModel.getPresentSectionCode(),
            aInfo5ViewModel.getPresentSectionName(),
            aInfo5ViewModel.getThePresentSectionAllData()
        )

        //Save the SectionPagesFramework and Data into db before stopping
        val sectionPagesFrameworkAndDataID =
            aInfo5ViewModel.getPresentCompanyCode() + aInfo5ViewModel.getPresentSectionCode() + MainActivity.SECTION_PAGES_FRAMEWORK_AND_DATA_ID
        aInfo5ViewModel.saveThePresentSectionAllPagesFrameworkAndAllDataToDB(
            aInfo5ViewModel.getThePresentSectionAllPagesFramework(),
            aInfo5ViewModel.getThePresentSectionAllData(),
            sectionPagesFrameworkAndDataID
        )

    }


    //Functions below

    private fun observeStatusMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aInfo5ViewModel.statusMessageSF.collect { message ->
                    // Show Toast, Snackbar, or dialog - executes exactly once
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun brushSelected(view: View){
        if (view != mImageButtonCurrentBrush){
            val imageButton = view as ImageButton
            imageButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.purple_experimental))
            mImageButtonCurrentBrush?.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.purple_200))
            mImageButtonCurrentBrush = view
        }
    }

    private fun paintClicked(view: View){
        if (view != mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this.requireContext(), R.drawable.palette_selected)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this.requireContext(), R.drawable.palette_normal)
            )
            mImageButtonCurrentPaint = view

        }

    }

    private val arrayOfBrushSizesList = arrayOf<CharSequence>("Brush Size 4", "Brush Size 6", "Brush Size 8", "Brush Size 10", "Brush Size 12", "Brush Size 14", "Brush Size 16", "Brush Size 18", "Brush Size 20", "Brush Size 22")

    private var checkedBrushItemNumber = 0
    private fun setBrushCheckedItem(input: Int){
        checkedBrushItemNumber = input
    }
    private fun getBrushCheckedItem(): Int{
        return checkedBrushItemNumber
    }

    private fun chooseBrushSizeListDialog(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Set the Brush Size")
        builder.setSingleChoiceItems(arrayOfBrushSizesList, getBrushCheckedItem()) { dialog, which ->
            setBrushCheckedItem(which)
            when (which) {
                0 -> {
                    binding.drawingView.setSizeForBrush(4.toFloat())
                }
                1 -> {
                    binding.drawingView.setSizeForBrush(6.toFloat())
                }
                2 -> {
                    binding.drawingView.setSizeForBrush(8.toFloat())
                }
                3 -> {
                    binding.drawingView.setSizeForBrush(10.toFloat())
                }
                4 -> {
                    binding.drawingView.setSizeForBrush(12.toFloat())
                }
                5 -> {
                    binding.drawingView.setSizeForBrush(14.toFloat())
                }
                6 -> {
                    binding.drawingView.setSizeForBrush(16.toFloat())
                }
                7 -> {
                    binding.drawingView.setSizeForBrush(18.toFloat())
                }
                8 -> {
                    binding.drawingView.setSizeForBrush(20.toFloat())
                }
                9 -> {
                    binding.drawingView.setSizeForBrush(22.toFloat())
                }
            }
            dialog.dismiss()
        }
        builder.create().show()
    }

    private val arrayOfTextSizesList = arrayOf<CharSequence>("Text Size 20", "Text Size 25", "Text Size 30", "Text Size 35", "Text Size 40", "Text Size 45", "Text Size 50", "Text Size 55", "Text Size 60", "Text Size 70")

    private var checkedTextItemNumber = 4
    private fun setTextCheckedItem(input: Int){
        checkedTextItemNumber = input
    }
    private fun getTextCheckedItem(): Int{
        return checkedTextItemNumber
    }

    private fun chooseTextSizeListDialog(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Set the Text Size")
        builder.setSingleChoiceItems(arrayOfTextSizesList, getTextCheckedItem()) { dialog, which ->
            setTextCheckedItem(which)
            when (which) {
                0 -> {
                    binding.drawingView.setSizeForText(20.toFloat())
                }
                1 -> {
                    binding.drawingView.setSizeForText(25.toFloat())
                }
                2 -> {
                    binding.drawingView.setSizeForText(30.toFloat())
                }
                3 -> {
                    binding.drawingView.setSizeForText(35.toFloat())
                }
                4 -> {
                    binding.drawingView.setSizeForText(40.toFloat())
                }
                5 -> {
                    binding.drawingView.setSizeForText(45.toFloat())
                }
                6 -> {
                    binding.drawingView.setSizeForText(50.toFloat())
                }
                7 -> {
                    binding.drawingView.setSizeForText(55.toFloat())
                }
                8 -> {
                    binding.drawingView.setSizeForText(60.toFloat())
                }
                9 -> {
                    binding.drawingView.setSizeForText(70.toFloat())
                }
            }
            dialog.dismiss()
        }
        builder.create().show()
    }


    private fun showDialogForImageSaveBP(){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        val title = "Save and Navigate Back"
        val message = "Press Yes to save changes, No to not save changes and Cancel to continue in this screen"
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                aInfo5ViewModel.insertNewCaption(aInfo5ViewModel.getModifiedPhotoItemDC())
                aInfo5ViewModel.saveModifiedPhotoToFile(getBitmapFromView(binding.flDrawingViewContainer))
                if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.INTROS_FRAGMENT) {
                    aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
                    aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                    findNavController().navigate(R.id.action_photoModificationFragment_to_introductionsScrollingFragment)
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS
                ) {
                    aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreenVariable())
                    findNavController().navigate(R.id.action_photoModificationFragment_to_observationsFragment)
                }
                dialog.dismiss()
            }
            .setNeutralButton("No"){ dialog, _ ->
                aInfo5ViewModel.undoUpdatedModifiedPhotoNames(aInfo5ViewModel.getSelectedPhotoItemDC(), aInfo5ViewModel.getModifiedPhotoItemDC())
                if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.INTROS_FRAGMENT) {
                    aInfo5ViewModel.setThePreviousScreenVariable(aInfo5ViewModel.getThePreviousScreen2Variable())
                    aInfo5ViewModel.setThePreviousScreen2Variable(MainActivity.NOT_RELEVANT)
                    findNavController().navigate(R.id.action_photoModificationFragment_to_introductionsScrollingFragment)
                }
                else if (aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_OBSERVATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_RECOMMENDATIONS
                    || aInfo5ViewModel.getThePreviousScreenVariable() == MainActivity.OBSERVATIONS_FRAGMENT_STANDARDS
                ) {
                    aInfo5ViewModel.setTheScreenVariable(aInfo5ViewModel.getThePreviousScreenVariable())
                    findNavController().navigate(R.id.action_photoModificationFragment_to_observationsFragment)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()

    }

    private fun uploadImageFile(uri: Uri, imageView: ImageView) {
        val bitmap = getBitmapFromUri(uri)
        imageView.setImageBitmap(bitmap)
    }

    @Throws(IOException::class)
    fun getBitmapFromUri(uri: Uri): Bitmap {
        val contentResolver = activity?.contentResolver
        val parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver?.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }


    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null){
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return  returnedBitmap

    }


}
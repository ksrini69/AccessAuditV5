package com.example.auditapplication5

import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.auditapplication5.databinding.FragmentCameraXBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraXFragment : Fragment() {
    private lateinit var binding: FragmentCameraXBinding
    private lateinit var aInfo5ViewModel: AInfo5ViewModel

    var savedImageUri: Uri? = null

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    var defaultImageName = "Default_Photo"
    var defaultVideoName = "Default_Video"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_camera_x, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aInfo5ViewModel = (activity as MainActivity).aInfo5ViewModel

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
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

            })

        //Action Bar
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.hide()

        // Set the screen
        aInfo5ViewModel.setTheScreenVariable(MainActivity.CAMERA_FRAGMENT)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        }
        else {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { REQUIRED_PERMISSIONS ->
                REQUIRED_PERMISSIONS.forEach{ actionMap ->
                    when(actionMap.key) {
                        android.Manifest.permission.CAMERA -> {
                            if (actionMap.value) {
                                Log.i("DEBUG", "permission granted")
                                startCamera()
                            } else {
                                Log.i("DEBUG", "permission denied")
                                Toast.makeText(this.requireContext(),
                                    "Permissions not granted by the user.",
                                    Toast.LENGTH_SHORT).show()
                                activity?.finish()
                            }
                        }
                    }
                }
            }.launch(REQUIRED_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        binding.buttonImageCapture.setOnClickListener {
            takePhoto()
            binding.buttonImageCapture.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.buttonImageCapture.isEnabled = true
            }, 600)
        }
        binding.buttonVideoCapture.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()


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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder()
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,videoCapture)

            } catch(exc: Exception) {
                Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case

        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
//        val name = SimpleDateFormat(FILENAME_DEFAULT_FORMAT, Locale.CHINA)
//            .format(System.currentTimeMillis()) + "_" + imageName
        var presentSectionName = ""
        if (aInfo5ViewModel.getLocationForPhotos().contains(aInfo5ViewModel.getPresentSectionCode())){
           presentSectionName = aInfo5ViewModel.getPresentSectionName()
        }

        val photoName = aInfo5ViewModel.makePresentPhotoName(aInfo5ViewModel.getLocationForPhotos(), aInfo5ViewModel.getThePhotoCount(),presentSectionName)
        aInfo5ViewModel.setPresentPhotoName(photoName)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${aInfo5ViewModel.getPresentCompanyCode()}")
        }


        // Create output options object which contains file + metadata
        val outputOptions = activity?.let {
            ImageCapture.OutputFileOptions
                .Builder(
                    it.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
                .build()
        }

        // Set up image capture listener, which is triggered after photo has
        // been taken
        if (outputOptions != null) {
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this.requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults){
                        savedImageUri = output.savedUri
                        aInfo5ViewModel.writeToImageFile(savedImageUri)
                    }
                }
            )
        }
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        binding.buttonVideoCapture.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(MainActivity.FILENAME_DEFAULT_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${aInfo5ViewModel.getPresentCompanyCode()}")
            }
        }

        val mediaStoreOutputOptions = activity?.let {
            MediaStoreOutputOptions
                .Builder(it.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()
        }
        recording = mediaStoreOutputOptions?.let {
            videoCapture.output
                .prepareRecording(this.requireContext(), it)
                .apply {
                    if (PermissionChecker.checkSelfPermission(this@CameraXFragment.requireContext(),
                            android.Manifest.permission.RECORD_AUDIO) ==
                        PermissionChecker.PERMISSION_GRANTED) {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(this.requireContext())) { recordEvent ->
                    when(recordEvent) {
                        is VideoRecordEvent.Start -> {
                            binding.buttonVideoCapture.apply {
                                text = getString(R.string.string_stop_capture)
                                isEnabled = true
                            }
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                val msg = "Video capture succeeded: " +
                                        "${recordEvent.outputResults.outputUri}"
                                Toast.makeText(activity?.baseContext, msg, Toast.LENGTH_SHORT)
                                    .show()
                                Log.d(MainActivity.TAG, msg)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e(MainActivity.TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}")
                            }
                            binding.buttonVideoCapture.apply {
                                text = getString(R.string.string_start_capture)
                                isEnabled = true
                            }
                        }
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        activity?.let { it1 ->
            ContextCompat.checkSelfPermission(
                it1.baseContext, it)
        } == PackageManager.PERMISSION_GRANTED
    }


    companion object{
        private const val TAG = "For Testing"
        private const val FILENAME_DEFAULT_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        @RequiresApi(Build.VERSION_CODES.R)
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}
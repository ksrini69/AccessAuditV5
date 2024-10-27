package com.example.auditapplication5

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.auditapplication5.data.model.AInfo5
import com.example.auditapplication5.data.model.AInfo5Templates
import com.example.auditapplication5.databinding.ActivityMainBinding
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var aInfo5ViewModelFactory: AInfo5ViewModelFactory
    lateinit var aInfo5ViewModel: AInfo5ViewModel

    private val storageAccessLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK){
            if (result.data != null){
                val treeURI : Uri? = result.data!!.data
                if (treeURI != null) {
                    // here we ask the content resolver to persist the permission for us
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    val contentResolver = this.contentResolver
                    contentResolver.takePersistableUriPermission(
                        treeURI,
                        takeFlags
                    )
                    //Store the uri in the database and in the ViewModel
                    val fileFlag = aInfo5ViewModel.getTheFileFlag()
                    if (fileFlag == DIRECTORY){
                        val aInfo5 = AInfo5(PARENT_FOLDER_URI_ID, treeURI.toString())
                        aInfo5ViewModel.insertAInfo5(aInfo5)
                        aInfo5ViewModel.setTheParentFolderURIString(treeURI.toString())
                    } else if (fileFlag == TEMPLATE_DOCUMENT_LOAD){
                        //Store Template URI into the Template Database Table
                        val aInfo5Template = AInfo5Templates(TEMPLATE_DOCUMENT_URI_ID, treeURI.toString())
                        aInfo5ViewModel.insertAInfo5Templates(aInfo5Template)

                        //Store the Template file contents into the database
                        val aInfo3TemplateFile = AInfo5Templates(TEMPLATE_DOCUMENT_ID,
                            readTextFromUri(treeURI))
                        aInfo5ViewModel.insertAInfo5Templates(aInfo3TemplateFile)

                        //Save the template file contents to the View Model when read
                        aInfo5ViewModel.setTheTemplateString(readTextFromUri(treeURI))
                        //Load the separate templates into the database
                        aInfo5ViewModel.loadDefaultTemplatesIntoTemplateDatabase(aInfo5ViewModel.getTheTemplateString()!!)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aInfo5ViewModel = ViewModelProvider(this, aInfo5ViewModelFactory)[AInfo5ViewModel::class.java]
        lifecycle.addObserver(aInfo5ViewModel)

        aInfo5ViewModel.message.observe(this) {
            it.getContentIfNotHandled().let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        //Getting the templateLoadedIntoDatabase flag from db only when the app is started
        //This flag is necessary so that we know that the templates have been loaded into the db
        val templateLoadedIntoDBFlagID = TEMPLATES_LOADED_INTO_DB_ID
        val templateLoadedIntoDBFlagIDML = mutableListOf<String>(templateLoadedIntoDBFlagID)
        aInfo5ViewModel.getAInfo5TemplatesByIds(templateLoadedIntoDBFlagIDML).observe(this){ list ->
            if (!aInfo5ViewModel.getTheObserveAndActOnceForTemplatesLoadedFlag()){
                if (list.isEmpty()){
                    aInfo5ViewModel.setTheTemplatesHaveBeenLoadedIntoDBFlag(false)
                } else {
                    var templateLoadedIntoDBFlagString = ""
                    for (item in list){
                        templateLoadedIntoDBFlagString += item.template_string
                    }
                    aInfo5ViewModel.setTheTemplatesHaveBeenLoadedIntoDBFlag(templateLoadedIntoDBFlagString.toBoolean())
                    aInfo5ViewModel.setTheObserveAndActOnceForTemplatesLoadedFlag(true)
                }
            }
        }

        initialiseValues()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    //Functions Below

    // Image and Photo related functions


    //Takes URI of the image and returns bitmap
    fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun rotateBitmap(original: Bitmap, degrees: Double): Bitmap? {
        val x = original.width
        val y = original.height
        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }

    fun fileNameFromURI(uri: Uri?): String {

        val returnCursor = uri?.let { contentResolver.query(it, null, null, null, null) }
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (returnCursor != null) {
            returnCursor.moveToFirst()
        }
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        if (returnCursor != null) {
            returnCursor.close()
        }
        //Log.d("photoName", "writeToImageFile: loom  $fileName ")
        return fileName!!
    }

    private fun alterDocument(uri: Uri, data: ByteArray) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use {
                    it.write(
                        data
                    )
                    Toast.makeText(this, "File Write OK!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object{
        // For Screen Related Operations
        const val OPENING_SCREEN_FRAGMENT = "Opening Screen Fragment"
        const val ENTER_NAME_FRAGMENT = "Enter Name Fragment"
        const val SIMPLE_LIST_RV_FRAGMENT = "Simple List RV Fragment"

        const val GOTO_RECYCLERVIEW_FRAGMENT = "Goto Recycler View Fragment"
        const val NOT_RELEVANT = "Not Relevant"

        const val SECTION_FRAGMENT = "Section Fragment"
        const val SECTION_FRAGMENT_COMPANY_INTRO = "Section Fragment Company Intro"
        const val SECTION_FRAGMENT_SECTION_INTRO = "Section Fragment Section Intro"
        const val SECTION_FRAGMENT_SECTION_CHOICE = "Section Fragment Section Choice"
        const val SECTION_FRAGMENT_NEW_SECTION = "Section Fragment New Section"
        const val SECTION_FRAGMENT_SECTION_OBS = "Section Fragment Section Observations"
        const val SECTION_FRAGMENT_REPORTS = "Section Fragment Report Types"
        const val SECTION_FRAGMENT_EDIT_1 = "Section Fragment Edit 1"
        const val SECTION_FRAGMENT_EDIT_2 = "Section Fragment Edit 2"
        const val SECTION_FRAGMENT_DELETE_1 = "Section Fragment Delete 1"
        const val SECTION_FRAGMENT_DELETE_2 = "Section Fragment Delete 2"

        const val INTRODUCTIONS_PAGE = "Introductions"
        const val OBSERVATIONS_PAGE = "Observations"
        const val COMPANY = "Company"
        const val INTROS_FRAGMENT = "Intros Fragment"

        const val CAMERA_FRAGMENT = "Camera Fragment"
        const val PHOTO_MODIFICATION_FRAGMENT = "Photo Modification Fragment"
        const val TAG = "CameraXFragment"
        const val PHOTO_MODIFICATION_FRAGMENT_CAPTIONS = "Photo Modification Fragment Captions"
        const val PHOTO_MODIFICATION_FRAGMENT_IMAGE_TEXT = "Photo Modification Fragment Image Text"

        const val OBSERVATIONS_FRAGMENT = "Observations Fragment"
        const val OBSERVATIONS_FRAGMENT_OBSERVATIONS = "Observations Fragment Observations"
        const val OBSERVATIONS_FRAGMENT_RECOMMENDATIONS = "Observations Fragment Recommendations"
        const val OBSERVATIONS_FRAGMENT_STANDARDS = "Observations Fragment Standards"
        const val OBSERVATIONS_FRAGMENT_GOTO_PAGE = "Observations Fragment Goto Page"



        const val PHOTO_DISPLAY_RV_FRAGMENT = "Photo Display Recycler View Fragment"
        const val RV_PARENT_CHILD_FRAGMENT = "Parent Child Recycler View Fragment"

        // List of FLAGS For File Handling

        const val FILENAME_DEFAULT_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        const val PARENT_DIR_CREATE = "Create a Parent Directory"
        const val PARENT_DIR_GET = "Get a Parent Directory"
        const val CHILD_DIR_CREATE = "Create a Child Directory"
        const val DIR_RENAME = "Rename Directory"
        const val DIRECTORY = "Directory"

        const val TEXT_FILE_CREATE = "Create Text File"
        const val TEXT_FILE_RENAME = "Rename Text File"

        const val IMAGE_FILE_CREATE = "Create Image File"
        const val IMAGE_FILE_RENAME = "Rename Image File"

        const val OPEN_DOCUMENT_TREE = "Open Document Tree"

        const val TEMPLATE_DOCUMENT = "Template Document"

        const val CHOOSE_COMPANY_MUTABLELIST_FOR_RV = "Company_CD_List"
        const val CHOOSE_SECTION_MUTABLELIST_FOR_RV = "Section_CD_List"
        const val CHOOSE_EDIT_MUTABLE_LIST_FOR_RV = "Edit_List"
        const val CHOOSE_DELETE_MUTABLE_LIST_FOR_RV = "Delete_List"
        const val CHOOSE_GOTO_MUTABLE_LIST_FOR_RV = "Goto_List"

        const val FLAG_VALUE_COMPANY = "Company"
        const val FLAG_VALUE_SECTION = "Section"

        //List of Constants for Variables that are not Boolean
        const val COMPANY_INTRODUCTION = "Company Introduction"
        const val SECTION_INTRODUCTION = "Section Introduction"
        const val SECTION_OBSERVATIONS = "Section Observations"

        // List of IDs to use
        const val PARENT_FOLDER_URI_ID = "Parent_Folder_URI"
        const val COMPANY_NAMES_ID = "Company_Names"
        const val COMPANY_CODES_NAMES_ID = "Company_Codes_Names"

        const val COMPANY_SECTION_LIST_ID = "_Company_Section_List_ID"

        const val COMPANY_REPORT_ID = "_Company_Report_ID"

        const val COMPANY_DIRECTORY_URI_ID = "_Directory_URI_ID"
        const val TXT_FILE_3COLUMN_URI_ID = "_ThreeColumn_URI_ID"
        const val TXT_FILE_3COLUMN_EXTENSION = "_ThreeColumn.txt"
        const val TXT_FILE_6COLUMN_URI_ID = "_SixColumn_URI_ID"
        const val TXT_FILE_6COLUMN_EXTENSION = "_SixColumn.txt"

        const val TEMPLATE_DOCUMENT_URI_ID = "Template_Document_URI_ID"
        const val TEMPLATE_DOCUMENT_ID = "Template_Document_ID"
        const val TEMPLATE_IDs_LIST_ID = "Template_IDs_List_ID"
        const val TEMPLATES_LOADED_INTO_DB_ID = "Templates_Loaded_Flag"

        const val COMPANY_INTRO_ID = "_Company_Intros_ID"
        const val SECTION_INTRO_ID = "_Intros_ID"
        const val SECTION_PAGES_ID = "_Pages_ID"

        const val TEMPLATE_DOCUMENT_LOAD = "Template Document Load"
        const val AUDIT_DATABASE_DELETE = "Audit Database Delete"
        const val TEMPLATE_DATABASE_DELETE = "Template Database Delete"
        const val TEMPLATE_SECTION_LIST = "Default_Section_List"

        const val FILE_CONTENT_LIST_ID = "File Content List ID"
        const val FILE_REPORT_ID = "_Report"


        const val IMAGE_FILE_URI_ID = "_Image_URI_ID"
        const val IMAGE_FILE_JPG_EXTENSION = ".jpg"
        const val IMAGE_FILE_PNG_EXTENSION = ".png"
        const val PHOTO_COUNT_ID = "_Photo_Count_ID"
        const val PHOTOS_LIST_ID = "_Photos_List_ID"

        const val COMPANY_AUDIT_DATE_ID = "_Audit_Date_ID"
        const val PRESENT_COMPANY_ID = "_Present_Company_ID"
        const val PRESENT_SECTION_ID = "_Present_Section_ID"
        const val SECTION_PAGES_FRAMEWORK_AND_DATA_ID = "_Section_Pages_Framework_And_Data_ID"
        const val PRESENT_COMPANY_ALL_IDs_ID = "_Present_Company_All_IDs_ID"
        //Serial Numbers for Questions
        const val PRIMARY_QUESTION_SET = "Primary Question Set"
        const val OTHER_QUESTION_SET = "Other Question Set"
        // Required Permissions

        //For Testing
        const val TESTING_TAG = "For Testing"

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()



    }

    //Functions Below

    private fun initialiseValues() {
        //aInfo5ViewModel.clearPresentCompanyAllIds()

    }

    fun openDocumentTree(intent: Intent){
        storageAccessLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    line += "\n"
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    fun areUriPermissionsGranted(uriString: String): Boolean {
        // list of all persisted permissions for our app
        val list = contentResolver.persistedUriPermissions
        for (i in list.indices) {
            val persistedUriString = list[i].uri.toString()
            if (persistedUriString == uriString && list[i].isWritePermission && list[i].isReadPermission) {
                return true
            }
        }
        return false
    }
    fun takePersistableURIPermissions(treeURI: Uri){
        if (treeURI != null) {
            // here we ask the content resolver to persist the permission for us
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            val contentResolver = this.contentResolver
            contentResolver.takePersistableUriPermission(
                treeURI,
                takeFlags
            )
        }
    }


}
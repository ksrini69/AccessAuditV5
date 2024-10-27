package com.example.auditapplication5.presentation.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapplication5.MainActivity
import com.example.auditapplication5.R
import com.example.auditapplication5.data.model.PhotoDetailsDC
import com.example.auditapplication5.databinding.RvPhotoDisplayItemBinding
import java.io.FileDescriptor

class PhotoDisplayRVAdapter(
    private val photosList: MutableList<PhotoDetailsDC>,
    private val context: Context,
    val companyDirectoryURI: Uri,
    private val clickListener: (photoItem: PhotoDetailsDC, flag: Boolean) -> Unit
) : RecyclerView.Adapter<PhotoDisplayRVAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvPhotoDisplayItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photoItem = photosList[position]
        holder.bind(photoItem,position,clickListener)
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

    fun getBitmapFromUri(uri: Uri): Bitmap {
        val contentResolver = context.contentResolver
        val parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver?.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }
    fun rotateBitmap(original: Bitmap, degrees: Double): Bitmap? {
        val x = original.width
        val y = original.height
        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }

    fun scaleBitmap(original: Bitmap): Bitmap? {
        val x = original.width * 0.5
        val y = original.height * 0.5
        val matrix = Matrix()
        //matrix.preRotate(degrees.toFloat())
        return Bitmap.createScaledBitmap(original, 400, 400, true)
    }

    fun uploadImageFile(uri: Uri, imageView: ImageView) {
        val bitmap = rotateBitmap(getBitmapFromUri(uri), 0.00)?.let { scaleBitmap(it) }
        imageView.setImageBitmap(bitmap)
    }

    fun uploadFileNotFound(imageView: ImageView){
        val drawableImage = ContextCompat.getDrawable(context, R.drawable.ic_file_not_found)
        val bitmapFD = drawableImage?.toBitmap()
        imageView.setImageBitmap(bitmapFD)
        imageView.setImageResource(R.drawable.ic_file_not_found)
    }

    inner class ViewHolder(val binding: RvPhotoDisplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            photoItem: PhotoDetailsDC, position: Int,
            clickListener: (photoItem: PhotoDetailsDC, flag: Boolean) -> Unit
        ) {
            var flagImage = true
            binding.tvNameInPhotoDisplayInRvItem.text = photoItem.fullPhotoName
            if (photoItem.photoCaption != ""){
                binding.tvCaptionInPhotoDisplayInRvItem.visibility = View.VISIBLE
                binding.tvCaptionInPhotoDisplayInRvItem.text = photoItem.photoCaption
            } else {
                binding.tvCaptionInPhotoDisplayInRvItem.visibility = View.GONE
            }
            val uri = photoItem.photoUriString.toUri()
            try {
                uploadImageFile(uri, binding.ivRvPhotoDisplayItem)
                flagImage = true
                //Log.d(MainActivity.TESTING_TAG, "bind: True BIND Come Here?")
            }catch (e:Exception){
                uploadFileNotFound(binding.ivRvPhotoDisplayItem)
                flagImage = false
                //Log.d(MainActivity.TESTING_TAG, "bind: False BIND Come Here?")
            }

            binding.root.setOnClickListener {
                //Log.d(MainActivity.TESTING_TAG, "bind: CLICK LISTENER ")
                clickListener(photoItem, flagImage)
            }
        }
    }
}
package com.koenig.chatapp.firebase

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Transformation

object FirebaseImageManager {

    var storage = FirebaseStorage.getInstance().reference
    var imageUri = MutableLiveData<Uri>()

    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child("${userid}.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            imageUri.value = Uri.EMPTY
        }
    }

    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean) {
        // Get the data from an ImageView as bytes
        val imageRef = storage.child("photos").child("${userid}.jpg")
        //val bitmap = (imageView as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if(updating) // Update existing Image
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUri.value = task.result!!
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUri.value = task.result!!
                }
            }
        }
    }

    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {

                    uploadImageToFirebase(userid, bitmap!!,updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    uploadImageToFirebase(userid, bitmap!!,false)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            })
    }

    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()

}
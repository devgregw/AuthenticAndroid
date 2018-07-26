package church.authenticcity.android.classes

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.getAs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/18/2018 at 7:18 PM.
 * Licensed under the MIT License.
 */
class ImageResource(val imageName: String, val width: Int, val height: Int) {
    constructor(map: HashMap<String, Any>) : this(map.getAs("name"), map.getAs("width"), map.getAs("height"))
    constructor() : this("unknown.png", 720, 1080)

    fun saveToGallery(context: Context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            AlertDialog.Builder(context).setTitle("Permission Denied").setMessage("The image could not be saved because you didn't give the Authentic app permission.").setNeutralButton("Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
        else {
            val dialog = ProgressDialog(context, R.style.ProgressDialog).apply {
                setMessage(Utils.makeTypefaceSpan("Downloading image...", Utils.getTitleTypeface(context)))
                isIndeterminate = true
            }
            val handler = Handler(context.mainLooper)
            dialog.show()
            Log.i("Save to gallery", "Load url")
            FirebaseStorage.getInstance().reference.child(imageName).downloadUrl.addOnCompleteListener {
                Log.i("Save to gallery", "Load image")
                Glide.with(context).asBitmap().load(it.result.toString()).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        Log.i("Save to gallery", "Loaded image")
                        if (resource == null) {
                            handler.post {
                                dialog.dismiss()
                                Utils.makeToast(context, "Unable to save image.", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }
                        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageName.removeRange(imageName.lastIndexOf('.'), imageName.length - 1) + ".png")
                        val stream = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()
                        MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null) { s, uri ->
                            handler.post {
                                dialog.dismiss()
                                Utils.makeToast(context, "Image saved", Toast.LENGTH_SHORT).show()
                            }
                            Log.i("Save to gallery", s)
                            Log.i("Save to gallery", uri.toString())
                        }
                    }
                })
            }
        }
    }
}
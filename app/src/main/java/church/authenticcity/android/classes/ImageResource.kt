package church.authenticcity.android.classes

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.getAs
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/18/2018 at 7:18 PM.
 * Licensed under the MIT License.
 */
class ImageResource(val imageName: String, val width: Int, val height: Int) {
    constructor(map: HashMap<String, Any>) : this(map.getAs("name"), map.getAs("width"), map.getAs("height"))
    constructor() : this("unknown.png", 720, 1080)

    private fun isExternal() = imageName.startsWith("http://", true) or imageName.startsWith("https://", true)

    companion object{
        @IntDef(ANCHOR_TOP_LEFT, ANCHOR_CENTER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Anchor
        const val ANCHOR_TOP_LEFT = 0
        const val ANCHOR_CENTER = 1
    }

    fun load(context: Context, into: ImageView) {
        var ref = FirebaseStorage.getInstance().reference
        if (AuthenticApplication.useDevelopmentDatabase)
            ref = ref.child("dev")
        val request = if (isExternal()) Glide.with(context).load(Uri.parse(imageName)) else Glide.with(context).load(ref.child(if (String.isNullOrWhiteSpace(imageName)) "unknown.png" else imageName))
        request.transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.unknown)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Authentic", "Load failed for $imageName", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(into)
    }

    fun calculateHeight(context: Context, fullWidth: Boolean) = calculateHeight(context.resources.displayMetrics.widthPixels / (if (fullWidth) 1 else 2))

    fun calculateHeight(widthPx: Int): Int {
        val ratio = width.toFloat() / (if (height == 0) 1 else height).toFloat()
        return (widthPx / ratio).roundToInt()
    }

    private fun save(context: Context, uri: Uri) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(true).setTitle(imageName).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + imageName)
        manager.enqueue(request)
        Utils.makeToast(context, "Downloading image...", Toast.LENGTH_LONG).show()
    }

    fun saveToGallery(context: Context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            AlertDialog.Builder(context).setTitle("Permission Denied").setMessage("The image could not be saved because you didn't give the Authentic app permission.").setNeutralButton("Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
        else {
            /*val dialog = ProgressDialog(context, R.style.ProgressDialog).apply {
                setMessage(Utils.makeTypefaceSpan("Downloading image...", Utils.getTitleTypeface(context)))
                isIndeterminate = true
            }
            val handler = Handler(context.mainLooper)
            dialog.show()
            Log.i("Save to gallery", "Load url")*/
            if (isExternal()) {
                save(context, Uri.parse(imageName))
                return
            }
            var ref = FirebaseStorage.getInstance().reference
            if (AuthenticApplication.useDevelopmentDatabase)
                ref = ref.child("dev")
            ref.child(imageName).downloadUrl.addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    save(context, it.result!!)
                } else {
                    Utils.makeToast(context, "Could not save image: ${it.exception?.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            }
            /*ref.child(imageName).downloadUrl.addOnCompleteListener {
                Log.i("Save to gallery", "Load image")
                Glide.with(context).asBitmap().load(it.result.toString()).into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        handler.post {
                            dialog.dismiss()
                            Utils.makeToast(context, "Unable to save image.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        Log.i("Save to gallery", "Loaded image")
                        /*val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageName.removeRange(imageName.lastIndexOf('.'), imageName.length - 1) + ".png")
                        val stream = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()
                        MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null) { s, uri ->
                            handler.post {
                                dialog.dismiss()
                                Utils.makeToast(context, "Image saved.", Toast.LENGTH_SHORT).show()
                            }
                            Log.i("Save to gallery", s)
                            Log.i("Save to gallery", uri.toString())
                        }*/
                        MediaStore.Images.Media.insertImage(context.contentResolver, resource, "", "")
                    }
                })
            }*/
        }
    }
}
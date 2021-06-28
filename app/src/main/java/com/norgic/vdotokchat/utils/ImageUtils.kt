package com.norgic.vdotokchat.utils

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64OutputStream
import com.norgic.vdotokchat.extensions.showSnackBar
import java.io.*
import kotlin.math.roundToInt

/**
 * Created By: Norgic
 * Date & Time: On 1/20/21 At 3:31 PM in 2021
 *
 * Image Utils class to write image manipulation related functions
 * such as converting to bitmap or bast64 etc
 */
object ImageUtils {

    /**
     * Function to convert File to Base64 String
     * @return Returns a Base64 string
     * */
    @JvmStatic
    fun convertImageFileToBase64(imageFile: File): String {
        return ByteArrayOutputStream().use { outputStream ->
            Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
                imageFile.inputStream().use { inputStream ->
                    inputStream.copyTo(base64FilterStream)
                }
            }
            return@use outputStream.toString()
        }
    }

    /**
     * Function to get bitmap from resources in android
     * @param resources android resources directory
     * @param resId id of the resources we need
     * @param reqHeight required height of the resource
     * @return Returns a Bitmap
     * */
    @JvmStatic
    fun getBitmap(resources: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    /**
     * function to check wether using android version above 11 or not
     * @param uri is get from onActivityresult of Gallery
     */
     fun convertImageToByte(context: Context, uri: Uri?): ByteArray? {
        var data: ByteArray? = null
        try {
            val cr = context.contentResolver
            val inputStream = cr?.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            data = baos.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }

        return data
    }

    /**
     * Function to convert Bitmap to ByteArray
     * @param context context of the working activity or fragment
     * @param bitmap Bitmap that needs to be converted to ByteArray
     * @return ByteArray Returns a byteArray of a bitmap
     * */
    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray? {
        val buffer = ByteArrayOutputStream(bitmap.width * bitmap.height)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer)
        return buffer.toByteArray()
    }

     fun copyFileToInternalStorage(
        context:Context,
        uri: Uri,
        newDirName: String
    ): String? {
        val returnUri = uri
        val returnCursor: Cursor? = context.contentResolver?.query(
            returnUri, arrayOf<String>(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )

        /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
        val nameIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val name: String? = returnCursor?.getString(nameIndex!!)
        val output: File
        if (newDirName != "") {
            val dir = File(context.getFilesDir().toString() + "/" + newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            output = File(context.getFilesDir().toString() + "/" + newDirName + "/" + name)
        } else {
            output = File(context.getFilesDir().toString() + "/" + name)
        }
        try {
            val inputStream: InputStream? = context.getContentResolver()?.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read: Int = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also { read = it!! } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: java.lang.Exception) {
        }
        return output.path


    }


    /**
     * Function to get a scaled bitmap from a bitmap
     * @param bitmap bitmap type object that need to be converted
     * @param reqWidth required width of the resource
     * @param reqHeight required height of the resource
     * @return Returns a Bitmap
     * */
    @JvmStatic
    fun getScaledBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap? {
        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
    }

    @JvmStatic
    fun getBitmap(byteArray: ByteArray){

    }

    /**
     * Function to scale size of bitmap
     * @param options bitmap options set for calculation
     * @param reqWidth required width of the resource
     * @param reqHeight required height of the resource
     * @return Returns an Integer value of either height or width
     * */
    @JvmStatic
    fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        val stretchWidth = (width.toFloat() / reqWidth.toFloat()).roundToInt()
        val stretchHeight = (height.toFloat() / reqHeight.toFloat()).roundToInt()
        return if (stretchWidth <= stretchHeight) stretchHeight else stretchWidth
    }

    /**
     * Function to encode a bitmap to Base64 String
     * @param image bitmap type that needs to be converted
     * @return Returns a String in Base64 encoding of a bitmap
     * */
    @JvmStatic
    fun encodeToBase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    /**
     * Function to encode a ByteArray to Base64 String
     * @param byteArray ByteArray type that needs to be converted
     * @return Returns a String in Base64 encoding of a ByteArray
     * */
    @JvmStatic
    fun encodeToBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Function to decode a Base64 String to ByteArray
     * @param base64String base64 encoded string type that needs to be converted
     * @return Returns a ByteArray object
     * */
    @JvmStatic
    fun decodeBase64ToByteArray(base64String: String): ByteArray {
        return Base64.decode(base64String, 0)
    }

    /**
     * Function to decode a Base64 String to Bitmap
     * @param input base64 encoded string type that needs to be converted
     * @return Returns a Bitmap object
     * */
    @JvmStatic
    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }
}
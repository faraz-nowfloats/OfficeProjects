package com.framework.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.framework.BaseApplication
import java.io.*
import java.util.*

//supported below and above android 11
object FileUtils {

  val cRes = BaseApplication.instance.contentResolver


  @Throws(IOException::class)
  fun getInputStream(uri: Uri): InputStream? {
    return if (isVirtualFile(uri)) {
      getInputStreamForVirtualFile(uri, getMimeType(uri))
    } else {
      cRes.openInputStream(uri)
    }
  }

  fun getMimeType(uri: Uri): String? {
    return cRes.getType(uri)
  }

  private fun isVirtualFile(uri: Uri): Boolean {

    if (!DocumentsContract.isDocumentUri(BaseApplication.instance, uri)) {
      return false
    }

    val cursor: Cursor? = cRes.query(
      uri,
      arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
      null,
      null,
      null
    )

    val flags: Int = cursor?.use {
      if (cursor.moveToFirst()) {
        cursor.getInt(0)
      } else {
        0
      }
    } ?: 0

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
    } else {
      return false
    }
  }

  @Throws(IOException::class)
  private fun getInputStreamForVirtualFile(
    uri: Uri, mimeTypeFilter: String?
  ): FileInputStream? {

    if (mimeTypeFilter == null) {
      throw FileNotFoundException()
    }
    val openableMimeTypes: Array<String>? =
      cRes.getStreamTypes(uri, mimeTypeFilter)

    return if (openableMimeTypes?.isNotEmpty() == true) {
      cRes
        .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)?.createInputStream()
    } else {
      throw FileNotFoundException()
    }
  }

  fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
    inputStream.use { input ->
      val outputStream = FileOutputStream(outputFile)
      outputStream.use { output ->
        val buffer = ByteArray(4 * 1024) // buffer size
        while (true) {
          val byteCount = input.read(buffer)
          if (byteCount < 0) break
          output.write(buffer, 0, byteCount)
        }
        output.flush()
      }
    }
  }

  fun saveFile(
    sourceuri: Uri,
    destinationDir: String?,
    destFileName: String,
  ): File? {
    val context = BaseApplication.instance
    val destination = destinationDir + File.separator.toString() + destFileName

    var bis: BufferedInputStream? = null
    var bos: BufferedOutputStream? = null
    var input: InputStream? = null
    try {
      input = if (isVirtualFile(sourceuri)) {
        getInputStreamForVirtualFile(sourceuri, getMimeType(sourceuri))

      } else {
        context.contentResolver.openInputStream(sourceuri)
      }
      val directorySetupResult: Boolean
      val destDir = File(destinationDir)
      directorySetupResult = if (!destDir.exists()) {
        destDir.mkdirs()
      } else if (!destDir.isDirectory) {
        replaceFileWithDir(destinationDir)
      } else {
        true
      }
      if (!directorySetupResult) {
      } else {
        val originalsize: Int = input!!.available()
        bis = BufferedInputStream(input)
        bos = BufferedOutputStream(FileOutputStream(destination))
        val buf = ByteArray(originalsize)
        bis.read(buf)
        do {
          bos.write(buf)
        } while (bis.read(buf) !== -1)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        if (bos != null) {
          bos.flush()
          bos.close()
        }
      } catch (ignored: Exception) {
        ignored.printStackTrace()
      }
    }
    return if (!File(destination).exists()) {
      null
    } else {
      File(destination)
    }
  }

  private fun replaceFileWithDir(path: String?): Boolean {
    val file = File(path)
    if (!file.exists()) {
      if (file.mkdirs()) {
        return true
      }
    } else if (file.delete()) {
      val folder = File(path)
      if (folder.mkdirs()) {
        return true
      }
    }
    return false
  }

  fun Bitmap.saveBitmap(): File {
    val f = File(BaseApplication.instance.cacheDir, "tempimg.jpg");
    f.createNewFile()
    //Convert bitmap to byte array
    val bos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    val bitmapdata = bos.toByteArray();
    //write the bytes in file
    var fos: FileOutputStream? = null;
    try {
      fos = FileOutputStream(f)
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }
    try {
      fos?.write(bitmapdata);
      fos?.flush()
      fos?.close()
    } catch (e: IOException) {
      e.printStackTrace();
    }
    return f
  }

  fun Uri.getFileName(withExtension:Boolean=false): String {
    var result: String=""
    try {
      if (scheme == "content") {
        BaseApplication.instance.contentResolver.query(this, null, null, null, null)
          .use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
              result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
          }
      }
      if (result == "") {
        result = path?:""
        val cut = result!!.lastIndexOf(File.separator)
        if (cut != -1) {
          result = result?.substring(cut + 1)
        }
      }
      return if (result.isNotEmpty()&&!withExtension&&result.lastIndexOf(".")>=0) result.substring(0,result.lastIndexOf(".")) else result
    }catch (e:Exception){
      return "Add Book Title"
    }

  }

  fun getTempFile(extension:String):File{
    return File(BaseApplication.instance.getExternalFilesDir(null)?.path+File.separator+"temp."+extension)
  }

  fun File.getExtension(): String {
    val strLength = absolutePath.lastIndexOf(".")
    return if (strLength > 0) absolutePath.substring(strLength + 1).lowercase(Locale.getDefault()) else ""
  }
}


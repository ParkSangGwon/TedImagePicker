package gun0912.tedimagepicker.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun fetchImage(context: Context): File? = suspendCoroutine {
    TedImagePicker.with(context)
        .mediaType(MediaType.IMAGE)
        .showTitle(false)
        .cancelListener { it.resume(null) }
        .errorListener { _ -> it.resume(null) }
        .start { uri ->
            it.resume(File(getRealPathFromURI(uri, context)!!))
        }
}

private fun getRealPathFromURI(uri: Uri, context: Context): String? {
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val loader = CursorLoader(context, uri, proj, null, null, null)
    val cursor: Cursor = loader.loadInBackground()!!
    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    val result = cursor.getString(columnIndex)
    cursor.close()
    return result
}
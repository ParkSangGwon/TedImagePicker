package gun0912.tedimagepicker.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import gun0912.tedimagepicker.builder.type.MediaType
import io.reactivex.Completable
import java.text.SimpleDateFormat
import java.util.*

internal class MediaUtil {
    companion object {

        internal fun getMediaIntentUri(
            context: Context,
            mediaType: MediaType,
            savedDirectoryName: String?
        ): Pair<Intent, Uri> {
            val cameraIntent = when (mediaType) {
                MediaType.IMAGE -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                MediaType.VIDEO -> Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            }

            if (cameraIntent.resolveActivity(context.packageManager) == null) {
                throw PackageManager.NameNotFoundException("Can not start Camera")
            }

            return getMediaUri(context, cameraIntent, mediaType, savedDirectoryName)
        }

        private fun getMediaUri(
            context: Context,
            cameraIntent: Intent,
            mediaType: MediaType,
            savedDirectoryName: String?
        ): Pair<Intent, Uri> {
            val timeStamp =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "${mediaType}_$timeStamp"

            val directoryName =
                if (savedDirectoryName != null) {
                    "${mediaType.savedDirectoryName}/$savedDirectoryName"
                } else {
                    mediaType.savedDirectoryName
                }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + mediaType.fileSuffix)
                put(MediaStore.MediaColumns.MIME_TYPE, mediaType.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directoryName)
                }
            }
            val mediaUri =
                context.contentResolver.insert(mediaType.externalContentUri, contentValues)!!
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
            return cameraIntent to mediaUri
        }

        fun scanMedia(context: Context, uri: Uri): Completable {

            return Completable.create { emitter ->
                MediaScannerConnection.scanFile(context, arrayOf(uri.path), null)
                { _, _ -> emitter.onComplete() }
            }
        }
    }
}
package gun0912.tedimagepicker.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import gun0912.tedimagepicker.builder.type.MediaType
import io.reactivex.Completable
import java.io.File
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

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val directoryName =
                    if (savedDirectoryName != null) {
                        "${mediaType.savedDirectoryName}/$savedDirectoryName"
                    } else {
                        mediaType.savedDirectoryName
                    }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + mediaType.fileSuffix)
                    put(MediaStore.MediaColumns.MIME_TYPE, mediaType.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directoryName)
                }
                val mediaUri =
                    context.contentResolver.insert(mediaType.externalContentUri, contentValues)!!
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
                cameraIntent to mediaUri
            } else {

                val directoryName = savedDirectoryName ?: mediaType.savedDirectoryName
                val directory = Environment.getExternalStoragePublicDirectory(directoryName)
                if (!directory.exists()) {
                    directory.mkdir()
                }

                val file = File.createTempFile(fileName, mediaType.fileSuffix, directory)

                val mediaUri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    file
                )

                val resolvedIntentActivities = context.packageManager
                    .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        mediaUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
                cameraIntent to Uri.fromFile(file)
            }
        }

        fun scanMedia(context: Context, uri: Uri): Completable {

            return Completable.create { emitter ->
                MediaScannerConnection.scanFile(context, arrayOf(uri.path), null)
                { _, _ -> emitter.onComplete() }
            }
        }
    }
}
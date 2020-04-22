package gun0912.tedimagepicker.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
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

            val mediaUri = getMediaUri(context, mediaType, savedDirectoryName)

            if (cameraIntent.resolveActivity(context.packageManager) == null) {
                throw PackageManager.NameNotFoundException("Can not start Camera")
            }

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

            return Pair(cameraIntent, mediaUri)
        }

        private fun getMediaUri(
            context: Context,
            mediaType: MediaType,
            savedDirectoryName: String?
        ): Uri {
            val timeStamp =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "${mediaType}_$timeStamp"
            val directoryName = savedDirectoryName ?: mediaType.savedDirectoryName
            val directory = Environment.getExternalStoragePublicDirectory(directoryName)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + mediaType.fileSuffix)
                put(MediaStore.MediaColumns.MIME_TYPE, mediaType.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, directoryName)
            }

            return context.contentResolver.insert(mediaType.externalContentUri, contentValues)!!
        }

        fun scanMedia(context: Context, uri: Uri): Completable {

            return Completable.create { emitter ->
                MediaScannerConnection.scanFile(context, arrayOf(uri.path), null)
                { _, _ -> emitter.onComplete() }
            }
        }
    }
}
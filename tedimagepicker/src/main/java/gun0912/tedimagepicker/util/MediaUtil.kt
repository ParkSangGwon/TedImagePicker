package gun0912.tedimagepicker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
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

            val mediaFile = getMediaFile(mediaType, savedDirectoryName)

            if (cameraIntent.resolveActivity(context.packageManager) == null) {
                throw PackageManager.NameNotFoundException("Can not start Camera")
            }

            val photoURI = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                mediaFile
            )

            val resolvedIntentActivities = context.packageManager
                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolvedIntentInfo in resolvedIntentActivities) {
                val packageName = resolvedIntentInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            return Pair(cameraIntent, Uri.fromFile(mediaFile))
        }

        private fun getMediaFile(mediaType: MediaType, savedDirectoryName: String?): File {
            val timeStamp =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "${mediaType}_$timeStamp"
            val directoryName = savedDirectoryName ?: when (mediaType) {
                MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
                MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
            }
            val directory = Environment.getExternalStoragePublicDirectory(directoryName)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val fileSuffix = when (mediaType) {
                MediaType.IMAGE -> ".jpg"
                MediaType.VIDEO -> ".mp4"
            }

            return File.createTempFile(fileName, fileSuffix, directory)
        }

        fun scanMedia(context: Context, uri: Uri): Completable {

            return Completable.create { emitter ->
                MediaScannerConnection.scanFile(context, arrayOf(uri.path), null)
                { _, _ -> emitter.onComplete() }
            }
        }
    }
}
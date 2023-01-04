package gun0912.tedimagepicker.builder.type

import android.net.Uri
import android.os.Environment
import android.provider.MediaStore


enum class CameraMedia(
    val intentAction: String,
    val savedDirectoryName: String,
    val fileSuffix: String,
    val mimeType: String,
    val externalContentUri: Uri,
) {
    IMAGE(
        MediaStore.ACTION_IMAGE_CAPTURE,
        Environment.DIRECTORY_PICTURES,
        ".jpg",
        "image/*",
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    ),
    VIDEO(
        MediaStore.ACTION_VIDEO_CAPTURE,
        Environment.DIRECTORY_MOVIES,
        ".mp4",
        "video/*",
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    );
}

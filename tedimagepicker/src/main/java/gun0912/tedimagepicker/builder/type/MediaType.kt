package gun0912.tedimagepicker.builder.type

import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.parcelize.Parcelize


@Parcelize
enum class MediaType(
    val savedDirectoryName: String,
    val fileSuffix: String,
    val mimeType: String,
    val externalContentUri: Uri
) :
    Parcelable {
    IMAGE(
        Environment.DIRECTORY_PICTURES,
        ".jpg",
        "image/*",
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    ),
    VIDEO(
        Environment.DIRECTORY_MOVIES,
        ".mp4",
        "video/*",
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    );
}
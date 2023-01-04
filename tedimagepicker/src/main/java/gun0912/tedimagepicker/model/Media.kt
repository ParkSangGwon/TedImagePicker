package gun0912.tedimagepicker.model

import android.net.Uri

internal sealed class Media(
    open val albumName: String,
    open val uri: Uri,
    open val dateAddedSecond: Long,
) {
    data class Image(
        override val albumName: String,
        override val uri: Uri,
        override val dateAddedSecond: Long,
    ) : Media(albumName, uri, dateAddedSecond)

    data class Video(
        override val albumName: String,
        override val uri: Uri,
        override val dateAddedSecond: Long,
        val duration: Long,
    ) : Media(albumName, uri, dateAddedSecond)
}

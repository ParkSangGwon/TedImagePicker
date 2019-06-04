package gun0912.tedimagepicker.model

import android.net.Uri

internal data class Album(
    val name: String,
    val thumbnailUri: Uri,
    val mediaUris: List<Media>
) {
    val mediaCount: Int = mediaUris.size
}
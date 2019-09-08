package gun0912.tedimagepicker.model

import android.net.Uri

internal data class Media(
    val albumName: String,
    val uri: Uri,
    val dateAddedSecond: Long
)
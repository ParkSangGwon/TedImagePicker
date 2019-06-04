package gun0912.tedimagepicker.builder.listener

import android.net.Uri

interface OnMultiSelectedListener {
    fun onSelected(uriList: List<Uri>)
}
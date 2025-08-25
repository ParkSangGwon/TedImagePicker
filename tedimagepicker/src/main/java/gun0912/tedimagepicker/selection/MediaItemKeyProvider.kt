package gun0912.tedimagepicker.selection

import android.net.Uri
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import gun0912.tedimagepicker.adapter.MediaAdapter

internal class MediaItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Uri>(SCOPE_CACHED) {

    override fun getKey(position: Int): Uri? = runCatching {
        val adapter = recyclerView.adapter as MediaAdapter
        if (position < adapter.headerCount) {
            null
        } else {
            adapter.getItem(position).uri
        }
    }.getOrNull()

    override fun getPosition(key: Uri): Int = runCatching {
        val adapter = recyclerView.adapter as MediaAdapter
        return adapter.getViewPosition(key)
    }.getOrElse { RecyclerView.NO_POSITION }
}

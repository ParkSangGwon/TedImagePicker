package gun0912.tedimagepicker.selection

import android.net.Uri
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import gun0912.tedimagepicker.adapter.MediaAdapter

internal class MediaItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Uri>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Uri>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y) ?: return null
        val viewHolder =
            recyclerView.getChildViewHolder(view) as? MediaAdapter.ImageViewHolder ?: return null
        val adapter = recyclerView.adapter as? MediaAdapter ?: return null

        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return null

        // Headers cannot be selected
        if (position < adapter.headerCount) return null

        return try {
            val media = adapter.getItem(position)
            MediaItemDetails(position, media.uri)
        } catch (e: Exception) {
            null
        }
    }
}

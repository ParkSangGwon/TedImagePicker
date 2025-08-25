package gun0912.tedimagepicker.selection

import android.net.Uri
import androidx.recyclerview.selection.ItemDetailsLookup

internal class MediaItemDetails(
    private val adapterPosition: Int,
    private val selectionKey: Uri,
) : ItemDetailsLookup.ItemDetails<Uri>() {

    override fun getPosition(): Int = adapterPosition

    override fun getSelectionKey(): Uri = selectionKey

}

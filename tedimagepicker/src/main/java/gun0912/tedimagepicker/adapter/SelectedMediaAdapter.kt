package gun0912.tedimagepicker.adapter

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseRecyclerViewAdapter
import gun0912.tedimagepicker.base.BaseViewHolder
import gun0912.tedimagepicker.databinding.ItemSelectedMediaBinding

internal class SelectedMediaAdapter :
    BaseRecyclerViewAdapter<Uri, SelectedMediaAdapter.MediaViewHolder>() {

    var onClearClickListener: ((Uri) -> Unit)? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun getViewHolder(parent: ViewGroup, viewType: ViewType) = MediaViewHolder(parent)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager = recyclerView.layoutManager
    }


    inner class MediaViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemSelectedMediaBinding, Uri>(parent, R.layout.item_selected_media) {

        init {
            binding.ivClear.setOnClickListener {
                val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                    ?: return@setOnClickListener)
                onClearClickListener?.invoke(item)
            }
        }

        override fun bind(data: Uri) {
            binding.uri = data
        }

        override fun recycled() {
            if ((itemView.context as? Activity)?.isDestroyed == true) {
                return
            }
            Glide.with(itemView).clear(binding.ivImage)
        }
    }

}
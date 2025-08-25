package gun0912.tedimagepicker.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseSimpleHeaderAdapter
import gun0912.tedimagepicker.base.BaseViewHolder
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.databinding.ItemGalleryCameraBinding
import gun0912.tedimagepicker.databinding.ItemGalleryMediaBinding
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.util.ToastUtil
import gun0912.tedimagepicker.zoom.TedImageZoomActivity

internal class MediaAdapter(
    private val activity: Activity,
    private val builder: TedImagePickerBaseBuilder<*>,
) : BaseSimpleHeaderAdapter<Media>(if (builder.showCameraTile) 1 else 0) {

    internal val selectedUriList: MutableList<Uri> = mutableListOf()
    var onMediaAddListener: (() -> Unit)? = null
    var selectionTracker: SelectionTracker<Uri>? = null

    override fun getHeaderViewHolder(parent: ViewGroup) = CameraViewHolder(parent)
    override fun getItemViewHolder(parent: ViewGroup) = ImageViewHolder(parent)

    fun initSelectedUriList(uris: List<Uri>) {
        selectedUriList.clear()
        selectedUriList.addAll(uris)
        onMediaAddListener?.invoke()
        notifyItemRangeInserted(headerCount, items.size)
    }

    fun toggleMediaSelect(uri: Uri) {
        if (selectedUriList.contains(uri)) {
            removeMedia(uri)
        } else {
            addMedia(uri)
        }
    }

    private fun addMedia(uri: Uri) {
        if (selectedUriList.size == builder.maxCount) {
            val message =
                builder.maxCountMessage ?: activity.getString(builder.maxCountMessageResId)
            ToastUtil.showToast(message)
        } else {
            selectedUriList.add(uri)
            onMediaAddListener?.invoke()
            refreshSelectedView()
        }
    }

    fun getViewPosition(uri: Uri): Int {
        val itemIndex = items.indexOfFirst { media -> media.uri == uri }
        return if (itemIndex >= 0) itemIndex + headerCount else NO_POSITION
    }


    private fun removeMedia(uri: Uri) {
        val position = getViewPosition(uri)
        selectedUriList.remove(uri)
        notifyItemChanged(position)
        refreshSelectedView()
    }

    private fun refreshSelectedView() {
        selectedUriList.forEach {
            val position: Int = getViewPosition(it)
            notifyItemChanged(position)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemGalleryMediaBinding, Media>(parent, R.layout.item_gallery_media) {

        init {
            binding.run {
                selectType = builder.selectType
                viewZoomOut.setOnTouchListener { view, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            // Prevent SelectionTracker from handling this touch
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                            false // Let the click listener handle it
                        }
                        else -> false
                    }
                }
                viewZoomOut.setOnClickListener {
                    val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                        ?: return@setOnClickListener)
                    startZoomActivity(item)
                }
                showZoom = false
            }

        }

        override fun bind(data: Media) {
            binding.run {
                media = data
                val isSelectedInTracker = selectionTracker?.isSelected(data.uri) ?: false
                val isSelectedInList = selectedUriList.contains(data.uri)
                isSelected = isSelectedInList || isSelectedInTracker
                selectedNumber = selectedUriList.indexOf(data.uri) + 1

                showZoom = builder.showZoomIndicator && media is Media.Image
                showDuration = builder.showVideoDuration && media is Media.Video
                if (data is Media.Video) {
                    binding.duration = data.durationText
                }

            }
        }

        override fun recycled() {
            if (activity.isDestroyed) {
                return
            }
            Glide.with(activity).clear(binding.ivImage)
        }

        private fun startZoomActivity(media: Media) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                binding.ivImage,
                media.uri.toString()
            ).toBundle()

            activity.startActivity(TedImageZoomActivity.getIntent(activity, media.uri), options)

        }
    }

    inner class CameraViewHolder(parent: ViewGroup) : HeaderViewHolder<ItemGalleryCameraBinding>(
        parent, R.layout.item_gallery_camera
    ) {

        init {
            binding.ivImage.setImageResource(builder.cameraTileImageResId)
            itemView.setBackgroundResource(builder.cameraTileBackgroundResId)
        }

    }

}

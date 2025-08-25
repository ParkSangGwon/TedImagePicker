package gun0912.tedimagepicker

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedonactivityresult.model.ActivityResult
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import gun0912.tedimagepicker.adapter.AlbumAdapter
import gun0912.tedimagepicker.adapter.GridSpacingItemDecoration
import gun0912.tedimagepicker.adapter.MediaAdapter
import gun0912.tedimagepicker.adapter.SelectedMediaAdapter
import gun0912.tedimagepicker.base.BaseRecyclerViewAdapter
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.builder.type.AlbumType
import gun0912.tedimagepicker.builder.type.CameraMedia
import gun0912.tedimagepicker.builder.type.MediaType
import gun0912.tedimagepicker.builder.type.SelectType
import gun0912.tedimagepicker.databinding.ActivityTedImagePickerBinding
import gun0912.tedimagepicker.extenstion.close
import gun0912.tedimagepicker.extenstion.isOpen
import gun0912.tedimagepicker.extenstion.setLock
import gun0912.tedimagepicker.extenstion.toggle
import gun0912.tedimagepicker.model.Album
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.partialaccess.PartialAccessManageBottomSheet
import gun0912.tedimagepicker.selection.MediaItemDetailsLookup
import gun0912.tedimagepicker.selection.MediaItemKeyProvider
import gun0912.tedimagepicker.util.GalleryUtil
import gun0912.tedimagepicker.util.MediaUtil
import gun0912.tedimagepicker.util.ToastUtil
import gun0912.tedimagepicker.util.isPartialAccessGranted
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


internal class TedImagePickerActivity
    : AppCompatActivity(),
    PartialAccessManageBottomSheet.Listener {

    private lateinit var binding: ActivityTedImagePickerBinding
    private val albumAdapter by lazy { AlbumAdapter(builder) }
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var selectedMediaAdapter: SelectedMediaAdapter
    private lateinit var selectionTracker: SelectionTracker<Uri>

    private lateinit var builder: TedImagePickerBaseBuilder<*>

    private lateinit var disposable: Disposable

    private var selectedPosition = 0
    private var isUpdatingSelection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSavedInstanceState(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = builder.screenOrientation
        }
        startAnimation()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ted_image_picker)
        binding.imageCountFormat = builder.imageCountFormat
        setupToolbar()
        setupTitle()
        setupRecyclerView()
        setupListener()
        setupSelectedMediaView()
        setupButton()
        setupAlbumType()
        setupPartialAccessView()
        loadMedia()

    }

    private fun startAnimation() {
        if (builder.startEnterAnim != null && builder.startExitAnim != null) {
            overridePendingTransition(builder.startEnterAnim!!, builder.startExitAnim!!)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(builder.showTitle)
        builder.backButtonResId.let {
            binding.toolbar.setNavigationIcon(it)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupTitle() {
        val title = builder.title ?: getString(builder.titleResId)
        setTitle(title)
    }

    private fun setupButton() {
        with(binding) {
            buttonGravity = builder.buttonGravity
            buttonText = builder.buttonText ?: getString(builder.buttonTextResId)
            buttonTextColor =
                ContextCompat.getColor(this@TedImagePickerActivity, builder.buttonTextColorResId)
            buttonBackground = builder.buttonBackgroundResId
            buttonDrawableOnly = builder.buttonDrawableOnly
        }

        setupButtonVisibility()
    }

    private fun setupButtonVisibility() {
        binding.showButton = when {
            builder.selectType == SelectType.SINGLE -> false
            else -> mediaAdapter.selectedUriList.isNotEmpty()
        }
    }

    private fun loadMedia(isRefresh: Boolean = false) {
        disposable = GalleryUtil.getMedia(this, builder.mediaType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albumList: List<Album> ->
                albumAdapter.replaceAll(albumList)
                setSelectedAlbum(selectedPosition)
                if (!isRefresh) {
                    setSelectedUriList(builder.selectedUriList)
                }
                binding.layoutContent.rvMedia.visibility = View.VISIBLE

            }
    }

    private fun setSelectedUriList(uriList: List<Uri>?) =
        uriList?.forEach { uri: Uri -> onMultiMediaClick(uri) }

    private fun setSavedInstanceState(savedInstanceState: Bundle?) {

        val bundle: Bundle? = when {
            savedInstanceState != null -> savedInstanceState
            else -> intent.extras
        }

        builder = bundle?.getParcelable(EXTRA_BUILDER)
            ?: TedImagePickerBaseBuilder<TedImagePickerBaseBuilder<*>>()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_BUILDER, builder)
        super.onSaveInstanceState(outState)
    }

    private fun setupRecyclerView() {
        setupAlbumRecyclerView()
        setupMediaRecyclerView()
        setupSelectedMediaRecyclerView()
    }


    private fun setupAlbumRecyclerView() {

        val albumAdapter = albumAdapter.apply {
            onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Album> {
                override fun onItemClick(data: Album, itemPosition: Int, layoutPosition: Int) {
                    this@TedImagePickerActivity.setSelectedAlbum(itemPosition)
                    binding.drawerLayout.close()
                    binding.isAlbumOpened = false
                }
            }
        }
        binding.rvAlbum.run {
            adapter = albumAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    binding.drawerLayout.setLock(newState == RecyclerView.SCROLL_STATE_DRAGGING)
                }
            })
        }

        binding.rvAlbumDropDown.adapter = albumAdapter

    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter(this, builder).apply {
            onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Media> {
                override fun onItemClick(data: Media, itemPosition: Int, layoutPosition: Int) {
                    binding.isAlbumOpened = false
                    this@TedImagePickerActivity.onMediaClick(data.uri)
                }

                override fun onHeaderClick() {
                    onCameraTileClick()
                }
            }

            onMediaAddListener = {
                with(binding.layoutContent.rvSelectedMedia) {
                    post { smoothScrollToPosition(selectedMediaAdapter.itemCount) }
                }
            }

        }

        binding.layoutContent.rvMedia.run {
            layoutManager = GridLayoutManager(this@TedImagePickerActivity, IMAGE_SPAN_COUNT)
            addItemDecoration(GridSpacingItemDecoration(IMAGE_SPAN_COUNT, 8))
            itemAnimator = null
            adapter = mediaAdapter

            setupSelectionTracker()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    (layoutManager as? LinearLayoutManager)?.let {
                        val firstVisiblePosition = it.findFirstCompletelyVisibleItemPosition()
                        if (firstVisiblePosition <= 0) {
                            return
                        }
                        val media = mediaAdapter.getItem(firstVisiblePosition)
                        val dateString = SimpleDateFormat(
                            builder.scrollIndicatorDateFormat,
                            Locale.getDefault()
                        ).format(Date(TimeUnit.SECONDS.toMillis(media.dateAddedSecond)))
                        binding.layoutContent.fastScroller.setBubbleText(dateString)
                    }
                }
            })
        }

        binding.layoutContent.fastScroller.recyclerView = binding.layoutContent.rvMedia
    }

    private fun setupSelectionTracker() {
        if (builder.selectType != SelectType.MULTI) {
            return
        }

        val recyclerView = binding.layoutContent.rvMedia
        selectionTracker = SelectionTracker.Builder(
            "media-selection",
            recyclerView,
            MediaItemKeyProvider(recyclerView),
            MediaItemDetailsLookup(recyclerView),
            StorageStrategy.createParcelableStorage(Uri::class.java)
        ).withSelectionPredicate(
            object : SelectionTracker.SelectionPredicate<Uri>() {
                override fun canSelectMultiple(): Boolean = true
                override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean =
                    true

                override fun canSetStateForKey(key: Uri, nextState: Boolean): Boolean {
                    // Always allow deselection
                    if (!nextState) return true

                    // Allow if already selected (prevent duplicate selection)
                    if (selectionTracker.isSelected(key)) return true

                    return (selectionTracker.selection.size() < builder.maxCount)
                        .also { canAddItem ->
                            if (!canAddItem) {
                                showMaxCountMessage()
                            }
                        }
                }
            }
        ).build().also {
            mediaAdapter.selectionTracker = it
        }


        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Uri>() {
            override fun onSelectionChanged() {
                syncSelectedListWithTracker()
            }
        })
    }

    private fun syncSelectedListWithTracker() {
        if (isUpdatingSelection) return
        isUpdatingSelection = true

        val newSelectedItems = selectionTracker.selection.toList()
        val lastSelectedItems = mediaAdapter.selectedUriList.toList()

        val addedItems = newSelectedItems - lastSelectedItems.toSet()
        val removedItems = lastSelectedItems - newSelectedItems.toSet()
        val updatedPositions = mutableSetOf<Int>()

        // add items
        if (addedItems.isNotEmpty()) {
            mediaAdapter.selectedUriList.addAll(addedItems)
            val addedViewPositions = addedItems.map { mediaAdapter.getViewPosition(it) }
            updatedPositions.addAll(addedViewPositions)
            mediaAdapter.onMediaAddListener?.invoke()
        }

        // remove items
        if (removedItems.isNotEmpty()) {
            mediaAdapter.selectedUriList.removeAll(removedItems)
            val removedViewPositions = removedItems.map { mediaAdapter.getViewPosition(it) }
            updatedPositions.addAll(removedViewPositions)
        }

        updateSelectedUI()

        updatedPositions
            .filter { it != RecyclerView.NO_POSITION }
            .forEach { mediaAdapter.notifyItemChanged(it) }
        isUpdatingSelection = false
    }

    private fun showMaxCountMessage() {
        val message = builder.maxCountMessage ?: getString(builder.maxCountMessageResId)
        ToastUtil.showToast(message)
    }

    private fun setupSelectedMediaRecyclerView() {
        binding.layoutContent.selectType = builder.selectType

        selectedMediaAdapter = SelectedMediaAdapter().apply {
            onClearClickListener = { uri ->
                onMultiMediaClick(uri)
            }
        }
        binding.layoutContent.rvSelectedMedia.run {
            layoutManager = LinearLayoutManager(
                this@TedImagePickerActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = selectedMediaAdapter

        }

    }

    @SuppressLint("CheckResult")
    private fun onCameraTileClick() {
        val cameraMedia = when (builder.mediaType) {
            MediaType.IMAGE -> CameraMedia.IMAGE
            MediaType.VIDEO -> CameraMedia.VIDEO
            MediaType.IMAGE_AND_VIDEO -> CameraMedia.IMAGE
        }
        val (cameraIntent, uri) = MediaUtil.getMediaIntentUri(
            this@TedImagePickerActivity,
            cameraMedia,
            builder.savedDirectoryName
        )
        TedRxOnActivityResult.with(this@TedImagePickerActivity)
            .startActivityForResult(cameraIntent)
            .subscribe { activityResult: ActivityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    MediaUtil.scanMedia(this, uri)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            loadMedia(true)
                            onMediaClick(uri)
                        }
                }
            }
    }


    private fun onMediaClick(uri: Uri) {
        when (builder.selectType) {
            SelectType.SINGLE -> onSingleMediaClick(uri)
            SelectType.MULTI -> onMultiMediaClick(uri)
        }
    }

    private fun onMultiMediaClick(uri: Uri) {
        isUpdatingSelection = true

        mediaAdapter.toggleMediaSelect(uri)
        toggleSelectionTracker(uri)
        updateSelectedUI()

        isUpdatingSelection = false
    }

    private fun updateSelectedUI() {
        binding.layoutContent.items = mediaAdapter.selectedUriList
        updateSelectedMediaView()
        setupButtonVisibility()
    }

    private fun toggleSelectionTracker(uri: Uri) {
        if (mediaAdapter.selectedUriList.contains(uri)) {
            if (!selectionTracker.isSelected(uri)) {
                selectionTracker.select(uri)
            }
        } else {
            if (selectionTracker.isSelected(uri)) {
                selectionTracker.deselect(uri)
            }
        }
    }

    private fun setupSelectedMediaView() {
        binding.layoutContent.viewSelectedMedia.run {
            if (mediaAdapter.selectedUriList.size > 0) {
                layoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.ted_image_picker_selected_view_height)
            } else {
                layoutParams.height = 0
            }
            requestLayout()
        }
    }

    private fun updateSelectedMediaView() {
        binding.layoutContent.viewSelectedMedia.post {
            binding.layoutContent.viewSelectedMedia.run {
                if (mediaAdapter.selectedUriList.size > 0) {
                    slideView(
                        this,
                        layoutParams.height,
                        resources.getDimensionPixelSize(R.dimen.ted_image_picker_selected_view_height)
                    )
                } else if (mediaAdapter.selectedUriList.size == 0) {
                    slideView(this, layoutParams.height, 0)
                }
            }
        }
    }

    private fun slideView(view: View, currentHeight: Int, newHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(currentHeight, newHeight).apply {
            addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
        }

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(valueAnimator)
        }.start()
    }

    private fun onSingleMediaClick(uri: Uri) {
        val data = Intent().apply {
            putExtra(EXTRA_SELECTED_URI, uri)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun finish() {
        super.finish()
        finishAnimation()
    }

    private fun finishAnimation() {
        if (builder.finishEnterAnim != null && builder.finishExitAnim != null) {
            overridePendingTransition(builder.finishEnterAnim!!, builder.finishExitAnim!!)
        }
    }

    private fun onMultiMediaDone() {


        val selectedUriList = mediaAdapter.selectedUriList
        if (selectedUriList.size < builder.minCount) {
            val message = builder.minCountMessage ?: getString(builder.minCountMessageResId)
            ToastUtil.showToast(message)
        } else {

            val data = Intent().apply {
                putParcelableArrayListExtra(
                    EXTRA_SELECTED_URI_LIST,
                    ArrayList(selectedUriList)
                )
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }

    }


    private fun setSelectedAlbum(selectedPosition: Int) {
        val album = albumAdapter.getItem(selectedPosition)
        if (this.selectedPosition == selectedPosition && binding.selectedAlbum == album) {
            return
        }

        binding.selectedAlbum = album
        this.selectedPosition = selectedPosition
        albumAdapter.setSelectedAlbum(album)
        mediaAdapter.replaceAll(album.mediaUris)
        binding.layoutContent.rvMedia.layoutManager?.scrollToPosition(0)
    }

    private fun setupListener() {
        binding.viewSelectedAlbum.setOnClickListener {
            binding.drawerLayout.toggle()
        }

        binding.viewDoneTop.root.setOnClickListener {
            onMultiMediaDone()
        }
        binding.viewDoneBottom.root.setOnClickListener {
            onMultiMediaDone()
        }

        binding.viewSelectedAlbumDropDown.setOnClickListener {
            binding.isAlbumOpened = !binding.isAlbumOpened
        }

    }

    private fun setupAlbumType() {
        if (builder.albumType == AlbumType.DRAWER) {
            binding.viewSelectedAlbumDropDown.visibility = View.GONE
        } else {
            binding.viewBottom.visibility = View.GONE
            binding.drawerLayout.setLock(true)
        }
    }

    private fun setupPartialAccessView() =
        with(binding.layoutContent.layoutTedImagePickerPartialAccessManage) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                root.isGone = true
                return@with
            }
            root.isVisible = builder.mediaType.isPartialAccessGranted
            tvPartialAccessManage.setOnClickListener { showPartialAccessManageDialog() }
            val mediaTypeText = getString(builder.mediaType.nameResId)
            tvPartialAccessNotice.text =
                getString(R.string.ted_image_picker_partial_access_notice_fmt, mediaTypeText)
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun showPartialAccessManageDialog() {
        PartialAccessManageBottomSheet.show(this, builder.mediaType)
    }

    override fun onRefreshMedia() {
        loadMedia(true)
        setupPartialAccessView()
    }

    override fun onBackPressed() {
        if (isAlbumOpened()) {
            closeAlbum()
        } else {
            super.onBackPressed()
        }

    }

    private fun isAlbumOpened(): Boolean =
        if (builder.albumType == AlbumType.DRAWER) {
            binding.drawerLayout.isOpen()
        } else {
            binding.isAlbumOpened
        }

    private fun closeAlbum() {

        if (builder.albumType == AlbumType.DRAWER) {
            binding.drawerLayout.close()
        } else {
            binding.isAlbumOpened = false
        }
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.onDestroy()
    }


    companion object {
        private const val IMAGE_SPAN_COUNT = 3
        private const val EXTRA_BUILDER = "EXTRA_BUILDER"
        private const val EXTRA_SELECTED_URI = "EXTRA_SELECTED_URI"
        private const val EXTRA_SELECTED_URI_LIST = "EXTRA_SELECTED_URI_LIST"

        internal fun getIntent(context: Context, builder: TedImagePickerBaseBuilder<*>) =
            Intent(context, TedImagePickerActivity::class.java)
                .apply {
                    putExtra(EXTRA_BUILDER, builder)
                }

        internal fun getSelectedUri(data: Intent): Uri? =
            data.getParcelableExtra(EXTRA_SELECTED_URI)

        internal fun getSelectedUriList(data: Intent): List<Uri>? =
            data.getParcelableArrayListExtra(EXTRA_SELECTED_URI_LIST)
    }

}


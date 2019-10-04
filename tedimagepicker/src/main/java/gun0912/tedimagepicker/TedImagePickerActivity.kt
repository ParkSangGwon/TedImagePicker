package gun0912.tedimagepicker

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
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
import gun0912.tedimagepicker.builder.type.SelectType
import gun0912.tedimagepicker.databinding.ActivityTedImagePickerBinding
import gun0912.tedimagepicker.extenstion.*
import gun0912.tedimagepicker.model.Album
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.util.GalleryUtil
import gun0912.tedimagepicker.util.MediaUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


internal class TedImagePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTedImagePickerBinding
    private val albumAdapter = AlbumAdapter()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var selectedMediaAdapter: SelectedMediaAdapter

    private lateinit var builder: TedImagePickerBaseBuilder<*>

    private lateinit var disposable: Disposable

    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSavedInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ted_image_picker)

        setupToolbar()
        setupTitle()
        setupRecyclerView()
        setupListener()
        setupSelectedMediaView()
        setupButton()
        loadMedia()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val toolbarColor = ContextCompat.getColor(this@TedImagePickerActivity, builder.toolbarColorBackgroundResId)
        binding.toolbarColor = toolbarColor
        binding.titleTextColor = ContextCompat.getColor(this@TedImagePickerActivity, builder.toolbarTitleColorResId)

        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = toolbarColor.manipulate(factor = 0.95f)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        builder.backButtonResId.let {
            val drawable = resources.getDrawable(it)
            drawable.setColorFilter(ContextCompat.getColor(this@TedImagePickerActivity, builder.backButtonColorId), PorterDuff.Mode.SRC_IN)
            binding.toolbar.navigationIcon = drawable
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
            buttonTextColor = ContextCompat.getColor(this@TedImagePickerActivity, builder.buttonTextColorResId)
            buttonBackground =
                ContextCompat.getDrawable(this@TedImagePickerActivity, builder.buttonBackgroundResId)
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

        binding.rvAlbum.run {

            adapter = albumAdapter.apply {
                onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Album> {
                    override fun onItemClick(data: Album, itemPosition: Int, layoutPosition: Int) {
                        this@TedImagePickerActivity.setSelectedAlbum(itemPosition)
                        binding.drawerLayout.close()
                    }
                }
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    binding.drawerLayout.setLock(newState == RecyclerView.SCROLL_STATE_DRAGGING)
                }
            })
        }


    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter(this, builder).apply {
            onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Media> {
                override fun onItemClick(data: Media, itemPosition: Int, layoutPosition: Int) {
                    this@TedImagePickerActivity.onMediaClick(data.uri)
                }

                override fun onHeaderClick() {
                    onCameraTileClick()
                }
            }

            onMediaAddListener = {
                binding.layoutContent.rvSelectedMedia.smoothScrollToPosition(selectedMediaAdapter.itemCount)
            }

        }

        binding.layoutContent.rvMedia.run {
            layoutManager = GridLayoutManager(this@TedImagePickerActivity, IMAGE_SPAN_COUNT)
            addItemDecoration(GridSpacingItemDecoration(IMAGE_SPAN_COUNT, 8))
            itemAnimator = null
            adapter = mediaAdapter
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
        val (cameraIntent, uri) = MediaUtil.getMediaIntentUri(
            this@TedImagePickerActivity,
            builder.mediaType
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
        mediaAdapter.toggleMediaSelect(uri)
        binding.layoutContent.items = mediaAdapter.selectedUriList
        updateSelectedMediaView()
        setupButtonVisibility()
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
        Log.d("ted", "mediaAdapter.selectedUriList.size: ${mediaAdapter.selectedUriList.size}")
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

    private fun onMultiMediaDone() {


        val selectedUriList = mediaAdapter.selectedUriList
        if (selectedUriList.size < builder.minCount) {
            val message = builder.minCountMessage ?: getString(builder.minCountMessageResId)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    }

    override fun onBackPressed() {
        binding.drawerLayout.run {
            if (isOpen()) {
                close()
            } else {
                super.onBackPressed()
            }
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


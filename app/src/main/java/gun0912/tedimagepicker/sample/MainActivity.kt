package gun0912.tedimagepicker.sample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.TedRxImagePicker
import gun0912.tedimagepicker.builder.listener.OnErrorListener
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener
import gun0912.tedimagepicker.builder.listener.OnSelectedListener
import gun0912.tedimagepicker.sample.databinding.ActivityMainBinding
import gun0912.tedimagepicker.sample.databinding.ItemImageBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedUriList: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setNormalSingleButton()
        setNormalMultiButton()
        setRxSingleButton()
        setRxMultiButton()
    }


    private fun setNormalSingleButton() {
        binding.btnNormalSingle.setOnClickListener {
            TedImagePicker.with(this)
                .start(object : OnSelectedListener {
                    override fun onSelected(uri: Uri) {
                        showSingleImage(uri)
                    }
                })
        }
    }

    private fun setNormalMultiButton() {
        binding.btnNormalMulti.setOnClickListener {
            TedImagePicker.with(this)
                //.mediaType(MediaType.IMAGE)
                //.scrollIndicatorDateFormat("YYYYMMDD")
                //.buttonGravity(ButtonGravity.BOTTOM)
                .errorListener(object : OnErrorListener {
                    override fun onError(message: String) {
                        Log.d("ted", "message: $message")
                    }
                })
                .selectedUri(selectedUriList)
                .start(object : OnMultiSelectedListener {
                    override fun onSelected(uriList: List<Uri>) {
                        showMultiImage(uriList)
                    }
                })
        }
    }

    private fun setRxSingleButton() {
        binding.btnRxSingle.setOnClickListener {
            TedRxImagePicker.with(this)
                .start()
                .subscribe(this::showSingleImage, Throwable::printStackTrace)
        }
    }

    private fun setRxMultiButton() {
        binding.btnRxMulti.setOnClickListener {
            TedRxImagePicker.with(this)
                .startMultiImage()
                .subscribe(this::showMultiImage, Throwable::printStackTrace)
        }
    }

    private fun showSingleImage(uri: Uri) {
        binding.ivImage.visibility = View.VISIBLE
        binding.containerSelectedPhotos.visibility = View.GONE
        Glide.with(this).load(uri).into(binding.ivImage)

    }


    private fun showMultiImage(uriList: List<Uri>) {
        this.selectedUriList = uriList
        Log.d("ted", "uriList: $uriList")
        binding.ivImage.visibility = View.GONE
        binding.containerSelectedPhotos.visibility = View.VISIBLE

        binding.containerSelectedPhotos.removeAllViews()

        val viewSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
                .toInt()
        uriList.forEach {
            val itemImageBinding = ItemImageBinding.inflate(LayoutInflater.from(this))
            Glide.with(this)
                .load(it)
                .apply(RequestOptions().fitCenter())
                .into(itemImageBinding.ivMedia)
            itemImageBinding.root.layoutParams = FrameLayout.LayoutParams(viewSize, viewSize)
            binding.containerSelectedPhotos.addView(itemImageBinding.root)
        }

    }
}

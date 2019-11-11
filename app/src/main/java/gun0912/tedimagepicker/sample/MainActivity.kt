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
        setRxMultiDropDown()
    }


    private fun setNormalSingleButton() {
        binding.btnNormalSingle.setOnClickListener {
            TedImagePicker.with(this)
                .start { uri -> showSingleImage(uri) }
        }
    }

    private fun setNormalMultiButton() {
        binding.btnNormalMulti.setOnClickListener {
            TedImagePicker.with(this)
                //.mediaType(MediaType.IMAGE)
                //.scrollIndicatorDateFormat("YYYYMMDD")
                //.buttonGravity(ButtonGravity.BOTTOM)
                //.buttonBackground(R.drawable.btn_sample_done_button)
                //.buttonTextColor(R.color.sample_yellow)
                .errorListener { message -> Log.d("ted", "message: $message") }
                .selectedUri(selectedUriList)
                .startMultiImage { list: List<Uri> -> showMultiImage(list) }
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

    private fun setRxMultiDropDown() {
        binding.btnRxMultiDropDown.setOnClickListener {
            TedRxImagePicker.with(this)
                .dropDownAlbum()
                .imageCountTextFormat("%s장")
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

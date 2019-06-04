package gun0912.tedimagepicker.zoom

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.databinding.ActivityZoomOutBinding

internal class TedImageZoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityZoomOutBinding
    private lateinit var uri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSavedInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_zoom_out)

        ViewCompat.setTransitionName(binding.ivMedia, uri.toString())

        supportPostponeEnterTransition()
        loadImage {
            supportStartPostponedEnterTransition()
        }

    }

    private fun loadImage(onLoadingFinished: () -> Unit) {
        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinished()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinished()
                return false
            }
        }
        Glide.with(this)
            .load(uri)
            .apply(RequestOptions().dontTransform())
            .listener(listener)
            .into(binding.ivMedia)
    }

    private fun setSavedInstanceState(savedInstanceState: Bundle?) {

        val bundle: Bundle? = when {
            savedInstanceState != null -> savedInstanceState
            else -> intent.extras
        }

        uri = bundle?.getParcelable(EXTRA_URI) ?: return finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_URI, uri)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val EXTRA_URI = "EXTRA_URI"
        fun getIntent(context: Context, uri: Uri) =
            Intent(context, TedImageZoomActivity::class.java)
                .apply {
                    putExtra(EXTRA_URI, uri)
                }
    }
}
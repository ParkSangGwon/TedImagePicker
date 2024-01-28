package gun0912.tedimagepicker.builder.type

import android.Manifest
import android.os.Build
import android.os.Parcelable
import androidx.annotation.StringRes
import gun0912.tedimagepicker.R
import kotlinx.parcelize.Parcelize


@Parcelize
enum class MediaType(@StringRes val nameResId: Int) : Parcelable {
    IMAGE(R.string.ted_image_picker_image),
    VIDEO(R.string.ted_image_picker_video),
    IMAGE_AND_VIDEO(R.string.ted_image_picker_image_video),
    ;

    val permissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (this) {
                IMAGE -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                VIDEO -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                IMAGE_AND_VIDEO -> arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
}

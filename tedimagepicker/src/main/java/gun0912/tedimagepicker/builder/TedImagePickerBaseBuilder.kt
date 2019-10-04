package gun0912.tedimagepicker.builder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import com.tedpark.tedpermission.rx2.TedRx2Permission
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.TedImagePickerActivity
import gun0912.tedimagepicker.builder.listener.OnErrorListener
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener
import gun0912.tedimagepicker.builder.listener.OnSelectedListener
import gun0912.tedimagepicker.builder.type.ButtonGravity
import gun0912.tedimagepicker.builder.type.MediaType
import gun0912.tedimagepicker.builder.type.SelectType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Suppress("UNCHECKED_CAST")
@Parcelize
open class TedImagePickerBaseBuilder<out B : TedImagePickerBaseBuilder<B>>(
    internal var selectType: SelectType = SelectType.SINGLE,
    internal var mediaType: MediaType = MediaType.IMAGE,
    @ColorRes
    internal var cameraTileBackgroundResId: Int = R.color.ted_image_picker_camera_background,
    @DrawableRes
    internal var cameraTileImageResId: Int = R.drawable.ic_camera_48dp,
    internal var showCameraTile: Boolean = true,
    internal var scrollIndicatorDateFormat: String = "yyyy.MM",
    internal var title: String? = null,
    @StringRes
    internal var titleResId: Int = R.string.ted_image_picker_title,
    internal var buttonGravity: ButtonGravity = ButtonGravity.TOP,
    internal var buttonText: String? = null,
    @ColorRes
    internal var toolbarColorBackgroundResId: Int = R.color.white,
    @ColorRes
    internal var toolbarTitleColorResId: Int = R.color.black,
    @DrawableRes
    internal var buttonBackgroundResId: Int = R.drawable.btn_done_button,
    @ColorRes
    internal var buttonTextColorResId: Int = R.color.white,
    @StringRes
    internal var buttonTextResId: Int = R.string.ted_image_picker_done,
    internal var selectedUriList: List<Uri>? = null,
    @DrawableRes
    internal var backButtonResId: Int = R.drawable.ic_arrow_back_black_24dp,
    @ColorRes
    internal var backButtonColorId: Int = R.color.black,
    internal var maxCount: Int = Int.MAX_VALUE,
    internal var maxCountMessage: String? = null,
    @StringRes
    internal var maxCountMessageResId: Int = R.string.ted_image_picker_max_count,
    internal var minCount: Int = Int.MIN_VALUE,
    internal var minCountMessage: String? = null,
    @StringRes
    internal var minCountMessageResId: Int = R.string.ted_image_picker_min_count,
    internal var showZoomIndicator: Boolean = true
) : Parcelable {


    @IgnoredOnParcel
    protected var onSelectedListener: OnSelectedListener? = null
    @IgnoredOnParcel
    protected var onMultiSelectedListener: OnMultiSelectedListener? = null
    @IgnoredOnParcel
    protected var onErrorListener: OnErrorListener? = null


    @SuppressLint("CheckResult")
    protected fun startInternal(context: Context) {
        checkPermission(context)
            .subscribe({ permissionResult ->
                if (permissionResult.isGranted) {
                    startActivity(context)
                } else {
                    onErrorListener?.onError("Permission denied")
                }
            }, { throwable -> onErrorListener?.onError(throwable.localizedMessage) })
    }

    private fun checkPermission(context: Context) = TedRx2Permission.with(context)
        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .request()

    private fun startActivity(context: Context) {
        TedImagePickerActivity.getIntent(context, this)
            .run {
                TedRxOnActivityResult.with(context).startActivityForResult(this)
            }.run {
                subscribe({ activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        onComplete(activityResult.data)
                    }
                }, { throwable -> onErrorListener?.onError(throwable.localizedMessage) })
            }

    }

    private fun onComplete(data: Intent) {
        val selectedUri =
            TedImagePickerActivity.getSelectedUri(data)
        val selectedUriList =
            TedImagePickerActivity.getSelectedUriList(data)
        when {
            selectedUri != null -> onSelectedListener?.onSelected(selectedUri)
            selectedUriList != null -> onMultiSelectedListener?.onSelected(selectedUriList)
            else -> IllegalStateException("selectedUri/selectedUriList can not null")
        }
    }

    fun mediaType(mediaType: MediaType): B {
        this.mediaType = mediaType
        return this as B
    }

    fun image(): B = mediaType(MediaType.IMAGE)
    
    fun video(): B = mediaType(MediaType.VIDEO)

    fun cameraTileBackground(@ColorRes cameraTileBackgroundResId: Int): B {
        this.cameraTileBackgroundResId = cameraTileBackgroundResId
        return this as B
    }

    fun cameraTileImage(@DrawableRes cameraTileImage: Int): B {
        this.cameraTileImageResId = cameraTileImage
        return this as B
    }

    fun showCameraTile(show: Boolean): B {
        this.showCameraTile = show
        return this as B
    }

    fun scrollIndicatorDateFormat(formatString: String): B {
        this.scrollIndicatorDateFormat = formatString
        return this as B
    }

    fun title(text: String): B {
        this.title = text
        return this as B
    }

    fun title(@StringRes textResId: Int): B {
        this.titleResId = textResId
        return this as B
    }

    fun toolbarBackgroundColor(@ColorRes colorId: Int) : B {
        this.toolbarColorBackgroundResId = colorId
        return this as B
    }

    fun toolbarTitleColor(@ColorRes colorId: Int): B {
        this.toolbarTitleColorResId = colorId
        return this as B
    }

    fun buttonGravity(buttonGravity: ButtonGravity): B {
        this.buttonGravity = buttonGravity
        return this as B
    }

    fun buttonText(text: String): B {
        this.buttonText = text
        return this as B
    }

    fun buttonText(@StringRes textResId: Int): B {
        this.buttonTextResId = textResId
        return this as B
    }

    fun buttonBackground(@DrawableRes buttonBackgroundResId: Int): B {
        this.buttonBackgroundResId = buttonBackgroundResId
        return this as B
    }

    fun buttonTextColor(@ColorRes buttonTextColorResId: Int): B {
        this.buttonTextColorResId = buttonTextColorResId
        return this as B
    }

    fun selectedUri(uriList: List<Uri>?): B {
        this.selectedUriList = uriList
        return this as B
    }

    fun backButton(@DrawableRes backButtonResId: Int): B {
        this.backButtonResId = backButtonResId
        return this as B
    }

    fun backButtonColor(@ColorRes backButtonColorResId: Int) : B {
        backButtonColorId = backButtonColorResId
        return this as B
    }

    fun max(maxCount: Int, maxCountMessage: String): B {
        this.maxCount = maxCount
        this.maxCountMessage = maxCountMessage
        return this as B
    }

    fun max(maxCount: Int, @StringRes maxCountMessageResId: Int): B {
        this.maxCount = maxCount
        this.maxCountMessageResId = maxCountMessageResId
        return this as B
    }

    fun min(minCount: Int, minCountMessage: String): B {
        this.minCount = minCount
        this.minCountMessage = minCountMessage
        return this as B
    }

    fun min(minCount: Int, @StringRes minCountMessageResId: Int): B {
        this.minCount = minCount
        this.minCountMessageResId = minCountMessageResId
        return this as B
    }

    fun zoomIndicator(show: Boolean): B {
        this.showZoomIndicator = show
        return this as B
    }

}
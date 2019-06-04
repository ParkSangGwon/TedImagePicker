package gun0912.tedimagepicker.builder

import android.content.Context
import gun0912.tedimagepicker.builder.listener.OnErrorListener
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener
import gun0912.tedimagepicker.builder.listener.OnSelectedListener
import gun0912.tedimagepicker.builder.type.SelectType
import java.lang.ref.WeakReference


class TedImagePicker {
    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(WeakReference(context))
    }

    class Builder(private val contextWeakReference: WeakReference<Context>) :
        TedImagePickerBaseBuilder<Builder>() {


        fun errorListener(onErrorListener: OnErrorListener): Builder {
            this.onErrorListener = onErrorListener
            return this
        }


        fun start(onSelectedListener: OnSelectedListener) {
            this.onSelectedListener = onSelectedListener
            selectType = SelectType.SINGLE
            contextWeakReference.get()?.let {
                startInternal(it)
            }

        }

        fun start(onMultiSelectedListener: OnMultiSelectedListener) {
            this.onMultiSelectedListener = onMultiSelectedListener
            selectType = SelectType.MULTI
            contextWeakReference.get()?.let {
                startInternal(it)
            }
        }

    }


}



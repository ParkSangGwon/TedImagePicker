package gun0912.tedimagepicker.base

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding

internal abstract class BaseSimpleHeaderAdapter<D>(protected val headerCount: Int = HEADER_COUNT) :
    BaseRecyclerViewAdapter<D, BaseViewHolder<ViewDataBinding, D>>(headerCount) {

    abstract fun getItemViewHolder(parent: ViewGroup): BaseViewHolder<ViewDataBinding, D>
    abstract fun getHeaderViewHolder(parent: ViewGroup): HeaderViewHolder<ViewDataBinding>


    override fun getViewHolder(
        parent: ViewGroup,
        viewType: ViewType
    ): BaseViewHolder<*, D> {
        return when (viewType) {
            ViewType.HEADER -> getHeaderViewHolder(parent)
            ViewType.ITEM -> getItemViewHolder(parent)
        }
    }

    open inner class HeaderViewHolder<out B : ViewDataBinding>(parent: ViewGroup, @LayoutRes layoutRes: Int) :
        BaseViewHolder<B, D>(parent, layoutRes) {

        override fun bind(data: D) {
            // no-op
        }
    }

    companion object {
        private const val HEADER_COUNT = 1
    }


}
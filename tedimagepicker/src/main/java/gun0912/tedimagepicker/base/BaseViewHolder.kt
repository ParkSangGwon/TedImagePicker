package gun0912.tedimagepicker.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


internal abstract class BaseViewHolder<out B : ViewDataBinding, D>(parent: ViewGroup, @LayoutRes layoutRes: Int) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {
    protected val context: Context = itemView.context
    protected val binding: B = DataBindingUtil.bind(itemView)!!

    abstract fun bind(data: D)

    open fun recycled() {

    }
}
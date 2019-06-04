package gun0912.tedimagepicker.base

import androidx.recyclerview.widget.DiffUtil

internal class BaseDiffUtilCallback<D>(private val oldList: List<D>, private val newList: List<D>) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun getOldListSize() = oldList.size


    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}
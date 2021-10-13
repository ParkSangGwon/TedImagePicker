package gun0912.tedimagepicker.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gun0912.tedimagepicker.databinding.LayoutScrollerBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: LayoutScrollerBinding

    var recyclerView: RecyclerView? = null
        set(value) {
            field = value
            field?.run {
                addOnScrollListener(scrollListener)
            }
        }

    private val scrollListener = ScrollListener()
    private var viewHeight: Int = 0
    private val hideScrollerSubject = PublishSubject.create<Boolean>()

    private var currentAnimator: AnimatorSet? = null
    private var hideDisposable: Disposable? = null

    init {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL
        clipChildren = false
        binding = LayoutScrollerBinding.inflate(LayoutInflater.from(context), this, true)
        setupHideScrollerSubscribe()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchScroller(event)) {
                    binding.viewScroller.isSelected = true
                    showBubble()
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (binding.viewScroller.isSelected) {
                    showScroller(event)
                    hideScrollerSubject.onNext(true)
                    true
                } else {
                    false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                binding.viewScroller.isSelected = false
                hideBubble()
                return false
            }
            else -> super.onTouchEvent(event)

        }
    }

    private fun isTouchScroller(event: MotionEvent): Boolean {
        val scrollerRect = Rect().apply {
            binding.viewScroller.getHitRect(this)
        }
        return scrollerRect.contains(event.x.toInt(), event.y.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewHeight = h
    }


    private fun setupHideScrollerSubscribe() {
        hideDisposable = hideScrollerSubject.debounce(HIDE_DELAY_SECOND, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { !binding.viewScroller.isSelected }
            .subscribe({
                hideAnimateHandle()
            }, { throwable -> throwable.printStackTrace() })
    }


    private fun showScroller(event: MotionEvent) {
        currentAnimator?.cancel()

        setScrollerPosition(event.y)
        setRecyclerViewPosition(event.y)
    }


    private fun setScrollerPosition(positionY: Float) {
        binding.viewScroller.y = getValueInRange(
            positionY - (binding.viewScroller.height / 2),
            viewHeight - binding.viewScroller.height
        )

        binding.viewBubble.y = getValueInRange(
            positionY - (binding.viewBubble.height / 2),
            viewHeight - binding.viewBubble.height
        )
    }

    private fun setRecyclerViewPosition(positionY: Float) {
        recyclerView?.adapter?.run {
            val proportion: Float = when {
                binding.viewScroller.y == 0f -> 0f
                binding.viewScroller.y + binding.viewScroller.height >= viewHeight - SCROLLER_MAX_POSITION_GAP -> 1f
                else -> positionY / viewHeight
            }
            val targetPos: Float = getValueInRange(proportion * itemCount, itemCount - 1)

            (recyclerView?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                targetPos.roundToInt(),
                0
            )
        }

    }

    private fun getValueInRange(value: Float, max: Int): Float = value.coerceIn(0f, max.toFloat())

    private fun showAnimateHandle() {
        if (binding.viewScroller.visibility == View.VISIBLE) {
            return
        }
        binding.viewScroller.visibility = View.VISIBLE

        ObjectAnimator.ofFloat(
            binding.viewScroller,
            TRANSLATION_X,
            binding.viewScroller.width.toFloat(),
            0f
        ).apply {
            duration = ANIMATION_TIME_HANDLE
        }.start()
    }


    private fun hideAnimateHandle() {
        if (binding.viewScroller.visibility == View.INVISIBLE) {
            return
        }

        ObjectAnimator.ofFloat(
            binding.viewScroller,
            TRANSLATION_X,
            0f,
            binding.viewScroller.width.toFloat()
        ).apply {

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.viewScroller.visibility = View.INVISIBLE
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    binding.viewScroller.visibility = View.INVISIBLE
                    currentAnimator = null
                }
            })

            duration = ANIMATION_TIME_HANDLE
        }.start()

    }


    private fun showBubble() {
        if (binding.viewBubble.visibility == View.VISIBLE) {
            return
        }
        binding.viewBubble.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(
            binding.viewBubble,
            TRANSLATION_X,
            binding.viewBubble.width.toFloat(),
            0f
        ).apply {
            duration = ANIMATION_TIME_BUBBLE
        }.start()
    }

    private fun hideBubble() {
        if (binding.viewBubble.visibility == View.INVISIBLE) {
            return
        }
        ObjectAnimator.ofFloat(
            binding.viewBubble,
            TRANSLATION_X,
            0f,
            binding.viewBubble.width.toFloat()
        ).apply {

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.viewBubble.visibility = View.INVISIBLE

                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    binding.viewBubble.visibility = View.INVISIBLE
                }
            })
            duration = ANIMATION_TIME_BUBBLE
        }.start()
    }

    override fun onDetachedFromWindow() {
        recyclerView?.removeOnScrollListener(scrollListener)
        hideDisposable?.dispose()
        super.onDetachedFromWindow()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (dy == 0) {
                return
            }

            (rv.layoutManager as? LinearLayoutManager)?.run {
                if (binding.viewScroller.visibility == View.INVISIBLE) {
                    showAnimateHandle()
                }

                updateBubbleAndHandlePosition()
                hideScrollerSubject.onNext(true)
            }
        }

    }


    private fun updateBubbleAndHandlePosition() {
        if (binding.viewScroller.isSelected) {
            return
        }
        recyclerView?.let {
            val verticalScrollOffset = it.computeVerticalScrollOffset()
            val verticalScrollRange = it.computeVerticalScrollRange()
            val proportion =
                verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - viewHeight)
            setScrollerPosition(viewHeight * proportion)
        }

    }

    fun setBubbleText(text: String) {
        binding.tvBubble.text = text
    }

    companion object {
        private const val HIDE_DELAY_SECOND: Long = 1
        private const val SCROLLER_MAX_POSITION_GAP: Long = 5
        private const val TRANSLATION_X = "translationX"
        private const val ANIMATION_TIME_HANDLE = 400L
        private const val ANIMATION_TIME_BUBBLE = 150L
    }
}
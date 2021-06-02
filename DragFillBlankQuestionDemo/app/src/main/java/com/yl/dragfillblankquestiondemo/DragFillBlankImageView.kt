package com.yl.dragfillblankquestiondemo

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.OnDragListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isEmpty
import java.util.*

/**
 * 拖拽图片题
 * Created by yangle on 2017/10/9.
 */
class DragFillBlankImageView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), OnDragListener,
    View.OnTouchListener {
    private var llOption: LinearLayoutCompat? = null
    private var llInput: LinearLayoutCompat? = null
    private lateinit var dragImageQuestion: DragImageQuestion
    // 需要填写答案View的位置
    private var emptyIndex = -1
    private var answerView: View? = null
    // 选项位置
    private var optionPosition = 0
    // 一次拖拽填空是否完成
    private var isFillBlank = false

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_drag_fill_blank2, this)
        llOption = findViewById<View>(R.id.ll_option) as LinearLayoutCompat
        llInput = findViewById<View>(R.id.inputContent) as LinearLayoutCompat
    }

    fun reset() {
        isFillBlank = false
        emptyIndex = -1
        answerView = null
        setData(dragImageQuestion)
    }

    /**
     * 设置数据
     */
    fun setData(dragImageQuestion: DragImageQuestion) {
        if (dragImageQuestion.inputList == null || dragImageQuestion.inputList.isEmpty()
            || dragImageQuestion.optionList == null || dragImageQuestion.optionList.isEmpty()
        ) {
            return
        }
        this.dragImageQuestion = dragImageQuestion
        // 避免重复创建拖拽选项
        if (llInput!!.isEmpty()) {
            // 图片列表
            val itemList: MutableList<ImageView> = ArrayList()

            for (index in 0 until dragImageQuestion.inputList.size) {
                val option = dragImageQuestion.inputList[index]
                val item = ImageView(context)
                val params = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, dp2px(10f), 0)
                item.layoutParams = params
                if (option.bitmap != null) {
                    item.setImageBitmap(option.bitmap)
                    item.setBackgroundColor(Color.parseColor("#4DB6AC"))
                } else {
                    emptyIndex = index
                    item.setImageBitmap(
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.ic_launcher
                        )
                    )
                    answerView = item
                }
                itemList.add(item)
            }
            // 显示拖拽选项
            for (i in itemList.indices) {
                llInput!!.addView(itemList[i])
            }
        }
        // 避免重复创建拖拽选项
        if (llOption!!.isEmpty()) {
            // 拖拽选项列表
            val itemList: MutableList<ImageView> = ArrayList()
            for (option in dragImageQuestion.optionList!!) {
                val btnAnswer = ImageView(context)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, dp2px(10f), 0)
                btnAnswer.layoutParams = params
                btnAnswer.setBackgroundColor(Color.parseColor("#4DB6AC"))
                btnAnswer.tag = option.optionIndex
                btnAnswer.setImageBitmap(option.bitmap)
                btnAnswer.setOnTouchListener(this)
                itemList.add(btnAnswer)
            }

            // 显示拖拽选项
            for (i in itemList.indices) {
                llOption!!.addView(itemList[i])
            }
            llInput!!.setOnDragListener(this)
        }
    }

    /**
     * 开始拖拽
     *
     * @param v 当前对象
     */
    private fun startDrag(v: View) {
        // 选项内容
        val optionContent = v.tag.toString()
        // 记录当前答案选项的位置
        optionPosition = getOptionPosition(optionContent)
        // 开始拖拽后在列表中隐藏答案选项
        v.visibility = INVISIBLE
        val item = ClipData.Item(optionContent)
        val data = ClipData(null, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v.startDragAndDrop(data, DragShadowBuilder(v), null, 0)
        } else {
            v.startDrag(data, DragShadowBuilder(v), null, 0)
        }
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d(TAG, "onDrag: ACTION_DRAG_STARTED")
                return event.clipDescription.hasMimeType(
                    ClipDescription.MIMETYPE_TEXT_PLAIN
                )
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d(TAG, "onDrag: ACTION_DRAG_ENTERED")
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                Log.d(TAG, "onDrag: ACTION_DRAG_LOCATION")
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d(TAG, "onDrag: ACTION_DRAG_EXITED")
                return true
            }
            DragEvent.ACTION_DROP -> {
                // 当前x、y坐标
                val currentX = event.x
                val currentY = event.y

                var emptyView = llInput!!.getChildAt(emptyIndex) as ImageView

                Log.d(
                    TAG, "onDrag: ${emptyView.visibility}" +
                            " ${emptyView.left}" +
                            "  ${emptyView.right}" +
                            "  ${emptyView.top}" +
                            " ${emptyView.bottom}"
                )
                // 如果拖拽答案没有进行填空则return
                val isContinue =
                    currentX > emptyView.left && currentX < emptyView.right && currentY < emptyView.bottom && currentY > emptyView.top
                if (!isContinue) {
                    isFillBlank = false;
                    return true
                }
                // 释放拖放阴影，并获取移动数据
                val item = event.clipData.getItemAt(0)
                val answer = item.text.toString().toInt()

                Log.d(TAG, "onDrag:answer $answer")
                emptyView.setImageBitmap(dragImageQuestion.optionList!![answer].bitmap)
                emptyView.setBackgroundColor(Color.GREEN)
                emptyView.visibility = View.VISIBLE
                emptyView.tag = answer
                isFillBlank = true
                completeAnswer()
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d(TAG, "onDrag:ACTION_DRAG_ENDED $isFillBlank $optionPosition")
                if (!isFillBlank) {
                    llOption!!.getChildAt(optionPosition).visibility = VISIBLE
                    initEmptyView()
                }
                return true
            }
            else -> {
            }
        }
        return false
    }

    private fun initEmptyView() {
        Log.d(TAG, "initEmptyView: $emptyIndex")
        val item = llInput!!.getChildAt(emptyIndex) as ImageView
        item.setImageBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.mipmap.ic_launcher
            )
        )
        item.tag = null
        item.visibility = VISIBLE
        item.setBackgroundColor(Color.BLACK)
        unCompleteAnswer()
    }

    /**
     * 获取选项位置
     * @param option 选项内容
     * @return 选项位置
     */
    private fun getOptionPosition(option: String): Int {
        for (i in 0 until llOption!!.childCount) {
            val btnOption = llOption!!.getChildAt(i)
            if (btnOption.tag.toString() == option) {
                return i
            }
        }
        return 0
    }

    /**
     * dp转px
     *
     * @param dp dp值
     * @return px值
     */
    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            resources.displayMetrics
        ).toInt()
    }

    init {
        initView()
    }

    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        if (v.tag == null) {
            return false
        }
        startDrag(v)
        return true
    }

    private fun completeAnswer() {
        // 填空完成，选项不允许拖动
        for (index in 0 until llOption!!.childCount) {
            llOption!!.getChildAt(index).setOnTouchListener(null)
        }
        // 答案可以拖动
        answerView?.setOnTouchListener(this)
    }

    private fun unCompleteAnswer() {
        for (index in 0 until llOption!!.childCount) {
            llOption!!.getChildAt(index).setOnTouchListener(this)
        }
        answerView?.setOnTouchListener(null)
    }

    companion object {
        private const val TAG = "DragFillBlankImageView"
    }
}
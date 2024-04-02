package com.yifeplayte.maxfreeform.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import cn.fkj233.ui.R
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.LayoutPair
import cn.fkj233.ui.activity.dp2px
import cn.fkj233.ui.activity.fragment.MIUIFragment
import cn.fkj233.ui.activity.view.BaseView
import cn.fkj233.ui.activity.view.LinearContainerV
import cn.fkj233.ui.activity.view.RoundCornerImageView
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV

/**
 * Modified from {@link cn.fkj233.ui.activity.view.TextSummaryWithSwitchV}
 */
class ImageTextSummaryWithSwitchV(
    private val textSummaryV: TextSummaryV,
    private val switchV: SwitchV,
    private val drawable: Drawable,
    private val round: Float = 30f,
    private val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        textSummaryV.notShowMargins(true)
        return LinearContainerV(
            LinearContainerV.HORIZONTAL,
            arrayOf(
                LayoutPair(
                    RoundCornerImageView(
                        context,
                        dp2px(context, round),
                        dp2px(context, round)
                    ).also {
                        it.setPadding(0, dp2px(context, 10f), 0, dp2px(context, 10f))
                        it.background = drawable
                    },
                    LinearLayout.LayoutParams(
                        dp2px(context, 50f),
                        dp2px(context, 50f)
                    ).apply {
                        marginEnd = dp2px(context, 10f)
                    }
                ),
                LayoutPair(
                    textSummaryV.create(context, callBacks),
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).also { it.gravity = Gravity.CENTER_VERTICAL }
                ),
                LayoutPair(
                    switchV.create(context, callBacks),
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.gravity = Gravity.CENTER_VERTICAL })
            ),
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).also {
                it.setMargins(0, dp2px(context, 10f), 0, dp2px(context, 10f))
            }
        ).create(context, callBacks).also {
            dataBindingRecv?.setView(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDraw(thiz: MIUIFragment, group: LinearLayout, view: View) {
        thiz.apply {
            group.apply {
                addView(view) // 带文本的开关
                setOnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> if (switchV.switch.isEnabled) background =
                            context.getDrawable(R.drawable.ic_main_down_bg)

                        MotionEvent.ACTION_UP -> {
                            val touchX: Float = motionEvent.x
                            val touchY: Float = motionEvent.y
                            val maxX = width.toFloat()
                            val maxY = height.toFloat()
                            if (touchX < 0 || touchX > maxX || touchY < 0 || touchY > maxY) {
                                setPressed(false)
                                return@setOnTouchListener false
                            }
                            if (switchV.switch.isEnabled) {
                                switchV.click()
                                callBacks?.let { it1 -> it1() }
                                background = context.getDrawable(R.drawable.ic_main_bg)
                            }
                        }

                        else -> background = context.getDrawable(R.drawable.ic_main_bg)
                    }
                    true
                }
            }
        }
    }
}
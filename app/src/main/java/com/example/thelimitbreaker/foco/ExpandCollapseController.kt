package com.example.thelimitbreaker.foco

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.opengl.ETC1.getHeight
import android.support.v4.content.ContextCompat
import android.support.v7.view.menu.ActionMenuItemView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import kotlinx.android.synthetic.main.layout_place_prefs.view.*


class ExpandCollapseController(private val listView: ListView, private val context: Context) {
    private var mRemoveObserver = 0
    private var translateCoords = IntArray(2)

    private val touchDisabler = View.OnTouchListener { _, _ -> true }


    private fun getAnimation(view: View, translateTop: Float, translateBottom: Float): Animator {

        val top = view.top
        val bottom = view.bottom
        val endTop = (top + translateTop).toInt()
        val endBottom = (bottom + translateBottom).toInt()

        val translationTop = PropertyValuesHolder.ofInt("top", top, endTop)
        val translationBottom = PropertyValuesHolder.ofInt("bottom", bottom, endBottom)

        val anim = ObjectAnimator.ofPropertyValuesHolder(view, translationTop, translationBottom)
        anim.duration = 150
        return anim
    }

    private fun getExpandedTopBottom(top: Int, bottom: Int, yDelta: Int): IntArray {
        var yTranslateTop = 0
        var yTranslateBottom = yDelta

        val height = bottom - top

        val isOverTop = top < 0
        val isBelowBottom = top + height + yDelta > listView.height
        if (isOverTop) {
            yTranslateTop = top
            yTranslateBottom = yDelta - yTranslateTop
        } else if (isBelowBottom) {
            val deltaBelow = top + height + yDelta - listView.height
            yTranslateTop = if (top - deltaBelow < 0) top else deltaBelow
            yTranslateBottom = yDelta - yTranslateTop
        }

        return intArrayOf(yTranslateTop, yTranslateBottom)
    }

    fun expandView(itemView: View, bind: () -> Unit, collapseOther:View? = null) {
        val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)
        val tvTimeHead = itemView.findViewById<TextView>(R.id.tvTimeHead)
        val ivExpandMore = itemView.findViewById<ImageView>(R.id.ivExpand)
        val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)

        listView.setOnTouchListener(touchDisabler)
        itemView.setOnTouchListener(touchDisabler)
        var oldTop = 0
        var oldBottom = 0
        val oldCoordinates = HashMap<View, IntArray>()
        if(collapseOther!=null){
            with(collapseOther) {
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                val otherTitle = findViewById<TextView>(R.id.tvPlaceTitle)
                val otherExtraContent = findViewById<RelativeLayout>(R.id.extraContent)
                val otherTimeHead = findViewById<TextView>(R.id.tvTimeHead)
                val otherExpand = findViewById<ImageView>(R.id.ivExpand)
                otherTitle.setTextColor(ContextCompat.getColor(context, com.example.thelimitbreaker.foco.R.color.colorPrimaryDark))
                otherExtraContent.visibility = View.GONE
                otherTimeHead.visibility = View.VISIBLE
                otherExpand.visibility = View.VISIBLE
            }
        }else{
            mRemoveObserver=1
            oldTop=itemView.top
            oldBottom=itemView.bottom
            val childCount = listView.childCount
            for (i in 0 until childCount) {
                val v = listView.getChildAt(i)

                oldCoordinates[v] = intArrayOf(v.top, v.bottom)
            }

            itemView.background=ContextCompat.getDrawable(context,R.drawable.reg_background)
            tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            extraContent.visibility = View.VISIBLE
            tvTimeHead.visibility = View.GONE
            ivExpandMore.visibility = View.GONE

        }





        itemView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean =
                    when (mRemoveObserver) {
                        0 -> {
                            mRemoveObserver=1
                             oldTop = itemView.top
                             oldBottom = itemView.bottom

                            val childCount = listView.childCount
                            for (i in 0 until childCount) {
                                val v = listView.getChildAt(i)

                                oldCoordinates[v] = intArrayOf(v.top, v.bottom)
                            }

                            itemView.background=ContextCompat.getDrawable(context,R.drawable.reg_background)
                            tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                            extraContent.visibility = View.VISIBLE
                            tvTimeHead.visibility = View.GONE
                            ivExpandMore.visibility = View.GONE
                            listView.requestLayout()
                            false
                        }
                        1 -> {
                            mRemoveObserver = 2
                            val newTop = itemView.top
                            val newBottom = itemView.bottom
                            val oldHeight = oldBottom - oldTop
                            val newHeight = newBottom - newTop
                            val delta = newHeight - oldHeight
                            translateCoords = getExpandedTopBottom(oldTop, oldBottom, delta)
                            bind()
                            listView.requestLayout()
                            false
                        }
                        else -> {
                            mRemoveObserver = 0
                            itemView.viewTreeObserver.removeOnPreDrawListener(this)

                            val animations = ArrayList<Animator>()

                            val yTranslateTop = translateCoords[0]
                            val yTranslateBottom = translateCoords[1]
                            val index = listView.indexOfChild(itemView)
                            for (v in oldCoordinates.keys) {

                                v.parent?.run {
                                    val i = listView.indexOfChild(v)
                                    val oldChildTopBottom = oldCoordinates[v]
                                    v.top = oldChildTopBottom!![0]
                                    v.bottom = oldChildTopBottom[1]
                                    if (v != itemView) {
                                        val delta =
                                                if (i > index) yTranslateBottom
                                                else -yTranslateTop
                                        animations.add(getAnimation(v, delta * 1f, delta * 1f))
                                    }
                                }
                            }

                            val selViewTop = itemView.top
                            val selViewBottom = itemView.bottom
                            val selViewEndTop = selViewTop + (-yTranslateTop)
                            val selViewEndBottom = selViewBottom + yTranslateBottom

                            val expandTopAnim = ObjectAnimator.ofInt(
                                    itemView,
                                    "top",
                                    selViewTop, selViewEndTop
                            )


                            expandTopAnim.duration = 150
                            val expandBottomAnim = ObjectAnimator.ofInt(
                                    itemView,
                                    "bottom",
                                    selViewBottom, selViewEndBottom
                            )
                            expandBottomAnim.duration = 150
                            animations.add(expandTopAnim)
                            animations.add(expandBottomAnim)
                            animations.add(
                                    ObjectAnimator.ofFloat(
                                            extraContent,
                                            "alpha",
                                            0f, 1f
                                    )
                            )


                            val animatorSet = AnimatorSet()
                            animatorSet.playTogether(animations)
                            animatorSet.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {// TODO Not Implemented
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    listView.setOnTouchListener(null)
                                    itemView.setOnTouchListener(null)
                                }

                                override fun onAnimationCancel(animation: Animator?) {// TODO Not Implemented
                                }

                                override fun onAnimationStart(animation: Animator?) {// TODO Not Implemented
                                }

                            })
                            animatorSet.start()
                            true
                        }
                    }
        })
    }

    fun collapseView(itemView: View) {
        val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)
        val tvTimeHead = itemView.findViewById<TextView>(R.id.tvTimeHead)
        val ivExpandMore = itemView.findViewById<ImageView>(R.id.ivExpand)
        val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)

        val oldTop = itemView.top
        val oldBottom = itemView.bottom
        listView.setOnTouchListener(touchDisabler)
        itemView.setOnTouchListener(touchDisabler)
        val oldCoordinates = HashMap<View, IntArray>()
        for (i in 0 until listView.childCount) {
            val child = listView.getChildAt(i)
            val oldTopBottom = IntArray(2)
            oldTopBottom[0] = child!!.top
            oldTopBottom[1] = child.bottom
            oldCoordinates[child] = oldTopBottom
        }

        itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        extraContent.visibility = View.GONE
        tvTimeHead.visibility = View.VISIBLE
        ivExpandMore.visibility = View.VISIBLE

        itemView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (mRemoveObserver == 0) {
                    mRemoveObserver = 1

                    val newTop = itemView.top
                    val newBottom = itemView.bottom

                    val oldHeight = oldBottom - oldTop
                    val newHeight = newBottom - newTop
                    val delta = newHeight - oldHeight
                    translateCoords[0] = newTop - oldTop
                    translateCoords[1] = newBottom - oldBottom
                    listView.requestLayout()
                    return false
                }
                mRemoveObserver = 0
                itemView.viewTreeObserver.removeOnPreDrawListener(this)

                val animations = ArrayList<Animator>()

                val yTranslateTop = translateCoords[0]
                val yTranslateBottom = translateCoords[1]

                val index = listView.indexOfChild(itemView)

                for (i in 0 until listView.childCount) {
                    val v = listView.getChildAt(i)

                    val oldTopBottom = oldCoordinates[v]
                    if (oldTopBottom != null) {
                        v.top = oldTopBottom[0]
                        v.bottom = oldTopBottom[1]
                    } else {
                        val delta = if (i > index) -yTranslateBottom else yTranslateTop
                        v.top = v.top + delta
                        v.bottom = v.bottom + delta
                    }
                    if (v != itemView) {

                        val diff = if (i > index) yTranslateBottom else yTranslateTop
                        animations.add(getAnimation(v, diff * 1f, diff * 1f))
                    }
                }
                val selViewTop = itemView.top
                val selViewBottom = itemView.bottom
                val selViewEndTop = selViewTop + yTranslateTop
                val selViewEndBottom = selViewBottom + yTranslateBottom

                val collapseTopAnim = ObjectAnimator.ofInt(
                        itemView,
                        "top",
                        selViewTop, selViewEndTop
                )


                collapseTopAnim.duration = 150
                val collapseBottomAnim = ObjectAnimator.ofInt(
                        itemView,
                        "bottom",
                        selViewBottom, selViewEndBottom
                )
                collapseBottomAnim.duration = 150
                animations.add(collapseTopAnim)
                animations.add(collapseBottomAnim)
                animations.add(
                        ObjectAnimator.ofFloat(
                                extraContent,
                                "alpha",
                                1f, 0f
                        )
                )
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(animations)
                animatorSet.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {// TODO Not Implemented
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        listView.setOnTouchListener(null)
                        itemView.setOnTouchListener(null)
                    }

                    override fun onAnimationCancel(animation: Animator?) {// TODO Not Implemented
                    }

                    override fun onAnimationStart(animation: Animator?) {// TODO Not Implemented
                    }

                })
                animatorSet.start()
                return true
            }

        })

    }


}
package co.vandierendonck.trionfinfc.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import co.vandierendonck.trionfinfc.R
import co.vandierendonck.trionfinfc.db.Card
import co.vandierendonck.trionfinfc.db.Rank
import co.vandierendonck.trionfinfc.db.Rank.*
import co.vandierendonck.trionfinfc.db.Suit
import org.jetbrains.anko.dimen

class HandView : ScrollView {
    private var cards: List<Card> = ArrayList()
    private var onClickListener: ((view: View) -> Unit)? = null
    val count: Int
        get() = cards.count()
    val last: Card?
        get() {
            return try {
                cards.last()
            } catch (e: Exception) {
                null
            }
        }

    constructor(context: Context) : super(context) 
    constructor(context: Context,
                attributeSet: AttributeSet) : super(context, attributeSet) 
    constructor(context: Context,
                attributeSet: AttributeSet,
                defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) 
    constructor(context: Context,
                attributeSet: AttributeSet,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes) 

    fun addItems(cards: List<Card>) {
        this.cards = cards
        notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        redraw()
    }

    fun setOnClickListener(listener: ((view: View) -> Unit)) {
        onClickListener = listener
    }

    private fun redraw() {
        removeAllViews()

        val child = LinearLayout(context)
        val childLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        childLp.topMargin = dimen(R.dimen.scrollview_margin)
        childLp.bottomMargin = dimen(R.dimen.scrollview_margin)
        childLp.gravity = Gravity.CENTER

        child.setPadding(0, 0, 0, 2*dimen(R.dimen.scrollview_margin))
        child.orientation = LinearLayout.VERTICAL
        child.gravity = Gravity.CENTER

        addView(child, childLp)

        var row = LinearLayout(context)
        cards.forEachIndexed { index, card ->
            val imageView = ImageView(context)
            val imageViewLp = LinearLayout.LayoutParams(dimen(R.dimen.card_width), dimen(R.dimen.card_height))

            if (onClickListener != null) {
                imageView.tag = card
                imageView.setOnClickListener(onClickListener)
            }

            if (index % 4 != 0) {
                imageViewLp.marginStart = dimen(R.dimen.card_offset)
            } else {
                row = LinearLayout(context)

                val rowLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (index != 0)
                    rowLp.topMargin = dimen(R.dimen.card_top_offset)

                row.gravity = Gravity.CENTER_HORIZONTAL
                row.orientation = LinearLayout.HORIZONTAL

                val tv = TextView(context)
                val tvLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)

                if (index + 4 < cards.count())
                    tvLp.topMargin = dimen(R.dimen.label_margin_top)
                else
                    tv.gravity = Gravity.CENTER_VERTICAL
                tvLp.marginEnd = dimen(R.dimen.label_margin_end)

                tv.setTextColor(ContextCompat.getColor(context, R.color.subtitle))
                tv.text = card.playerId.toString()

                row.addView(tv, tvLp)
                child.addView(row, rowLp)
            }
            imageView.setBackgroundResource(R.drawable.shadow_rect)

            val resId: Int = when (card.suit) {
                Suit.SPADES -> {
                    when (card.rank) {

                        NONE -> TODO()
                        SEVEN -> R.drawable.ic_7s
                        EIGHT -> R.drawable.ic_8s
                        NINE -> R.drawable.ic_9s
                        JACK -> R.drawable.ic_js
                        QUEEN -> R.drawable.ic_qs
                        KING -> R.drawable.ic_ks
                        ACE -> R.drawable.ic_1s
                        MANILLE -> R.drawable.ic_10s
                    }
                }
                Suit.HEARTS -> {
                    when (card.rank) {

                        NONE -> TODO()
                        SEVEN -> R.drawable.ic_7h
                        EIGHT -> R.drawable.ic_8h
                        NINE -> R.drawable.ic_9h
                        JACK -> R.drawable.ic_jh
                        QUEEN -> R.drawable.ic_qh
                        KING -> R.drawable.ic_kh
                        ACE -> R.drawable.ic_1h
                        MANILLE -> R.drawable.ic_10h
                    }
                }
                Suit.CLUBS -> {
                    when (card.rank) {

                        NONE -> TODO()
                        SEVEN -> R.drawable.ic_7c
                        EIGHT -> R.drawable.ic_8c
                        NINE -> R.drawable.ic_9c
                        JACK -> R.drawable.ic_jc
                        QUEEN -> R.drawable.ic_qc
                        KING -> R.drawable.ic_kc
                        ACE -> R.drawable.ic_1c
                        MANILLE -> R.drawable.ic_10c
                    }
                }
                Suit.DIAMONDS -> {
                    when (card.rank) {

                        NONE -> TODO()
                        SEVEN -> R.drawable.ic_7d
                        EIGHT -> R.drawable.ic_8d
                        NINE -> R.drawable.ic_9d
                        JACK -> R.drawable.ic_jd
                        QUEEN -> R.drawable.ic_qd
                        KING -> R.drawable.ic_kd
                        ACE -> R.drawable.ic_1d
                        MANILLE -> R.drawable.ic_10d
                    }
                }
                Suit.NONE -> TODO()
                Suit.UNDEFINED -> TODO()
            }
            imageView.setImageResource(resId)

            row.addView(imageView, imageViewLp)
        }
    }
}
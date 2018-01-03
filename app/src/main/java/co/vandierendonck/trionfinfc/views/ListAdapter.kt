package co.vandierendonck.trionfinfc.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.vandierendonck.trionfinfc.R
import co.vandierendonck.trionfinfc.db.Identifiable
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import kotlinx.android.synthetic.main.list_row.view.*

class ListAdapter<T: Identifiable>(
        private var context: Context,
        private var dataList: List<T>,
        private var fillIn: (view: View, data: T, position: Int) -> Unit,
        private var onClickListener: View.OnClickListener?,
        private var deleteClickListener: View.OnClickListener?,
        private var editClickListener: View.OnClickListener?
) : BaseSwipeAdapter() {
    val last: T?
        get() {
            return try {
                dataList.last()
            } catch (e: Exception) {
                null
            }
        }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe_layout
    }

    override fun generateView(position: Int, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.list_row, parent, false)
    }

    override fun fillValues(position: Int, vi: View) {
        // hide multiplier field
        vi.multiplier.visibility = View.GONE

        // fill in view
        val data = dataList[position]
        fillIn(vi, data, position)

        // delete game after confirmation
        if (deleteClickListener != null) {
            vi.delete.setOnClickListener(deleteClickListener)
            vi.delete.tag = data
        } else
            vi.delete.visibility = View.GONE

        // edit game name
        if (editClickListener != null) {
            vi.edit.setOnClickListener(editClickListener)
            vi.edit.tag = data
        } else
            vi.edit.visibility = View.GONE

        // set onClick behaviour
        if (onClickListener != null) {
            vi.top_wrapper.setOnClickListener(onClickListener)
            vi.top_wrapper.tag = data
        }
    }

    override fun getItem(position: Int): T {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return dataList[position].id
    }

    override fun getCount(): Int {
        return dataList.count()
    }

    fun addItems(dataList: List<T>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }
}
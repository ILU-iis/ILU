package com.iluiis.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iluiis.app.database.GratitudeEntity

class AllHistoryAdapter(
    private val gratitudeList: List<GratitudeEntity>
) : RecyclerView.Adapter<AllHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvBadgeLatest: TextView = itemView.findViewById(R.id.tvBadgeLatest)
        val tvItem1: TextView = itemView.findViewById(R.id.tvItem1)
        val tvItem2: TextView = itemView.findViewById(R.id.tvItem2)
        val tvItem3: TextView = itemView.findViewById(R.id.tvItem3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gratitude, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = gratitudeList[position]
        holder.tvDate.text = item.date
        holder.tvBadgeLatest.visibility = if (position == 0) View.VISIBLE else View.GONE
        holder.tvItem1.text = item.good1
        holder.tvItem2.text = item.good2
        holder.tvItem3.text = item.good3
    }

    override fun getItemCount(): Int {
        return gratitudeList.size
    }
}

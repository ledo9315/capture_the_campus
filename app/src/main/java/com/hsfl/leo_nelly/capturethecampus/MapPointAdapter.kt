package com.hsfl.leo_nelly.capturethecampus

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MapPointAdapter(
    private var points: MutableList<MapPoint>,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<MapPointAdapter.PointViewHolder>() {

    class PointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mapPointTextView: TextView = view.findViewById(R.id.textView_map_point)
        val deletePointButton: Button = view.findViewById(R.id.deleteFlagButton)

        fun bind(index: Int) {
            "Flag ${index + 1}".also { mapPointTextView.text = it }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flag_item_map, parent, false)
        return PointViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        holder.bind(position)
        holder.deletePointButton.setOnClickListener { onItemDeleted(position) }
    }

    override fun getItemCount(): Int = points.size


    @SuppressLint("NotifyDataSetChanged")
    fun updatePoints(newPoints: List<MapPoint>) {
        points.clear()
        points.addAll(newPoints)
        notifyDataSetChanged()
    }
}


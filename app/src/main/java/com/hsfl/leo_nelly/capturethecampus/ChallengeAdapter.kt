package com.hsfl.leo_nelly.capturethecampus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChallengeAdapter(var challenges: List<Challenge>) : RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null
    private var onItemLongClickListener: ((Int) -> Unit)? = null


    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (Int) -> Unit) {
        onItemLongClickListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.challengeName)
        val descriptionTextView: TextView = view.findViewById(R.id.challengeDescription)
        val flagsToCaptureTextView: TextView = view.findViewById(R.id.flagsToCapture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.challenge_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.nameTextView.text = challenge.name
        holder.descriptionTextView.text = challenge.description
        "Flags to Capture: ${challenge.flagsToCapture}".also { holder.flagsToCaptureTextView.text = it }

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(position)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(position)
            true
        }
    }

    fun updateChallenges(newChallenges: List<Challenge>) {
        challenges = newChallenges.toMutableList()
        notifyDataSetChanged()
    }


    override fun getItemCount() = challenges.size
}

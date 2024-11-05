package com.example.luisgantar_uts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private var absences: List<Absen>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absence, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val absence = absences[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        holder.timestampTextView.text = dateFormat.format(absence.timestamp)
        holder.typeTextView.text = "Absen ${absence.type}"

        Glide.with(holder.itemView.context)
            .load(absence.photoUri)
            .override(1600, 1200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(holder.photoImageView)
    }



    override fun getItemCount() = absences.size

    fun updateData(newAbsences: List<Absen>) {
        absences = newAbsences
        notifyDataSetChanged()
    }
}
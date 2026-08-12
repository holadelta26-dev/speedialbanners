package com.example.speeddialbanners

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SpeedDialAdapter(
    private val slots: MutableList<SpeedDialSlot>,
    private val onShortPress: (SpeedDialSlot) -> Unit, // asignar / editar
    private val onLongPress: (SpeedDialSlot) -> Unit    // llamar
) : RecyclerView.Adapter<SpeedDialAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBanner: ImageView = view.findViewById(R.id.ivBanner)
        val tvSlotNumber: TextView = view.findViewById(R.id.tvSlotNumber)
        val tvContactName: TextView = view.findViewById(R.id.tvContactName)
        val tvEmptyHint: TextView = view.findViewById(R.id.tvEmptyHint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speed_dial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        // Mostramos "mantén presionado" = tecla 1 al 8; el 1 aparece como "1", etc.
        holder.tvSlotNumber.text = (position + 1).toString()

        if (slot.isEmpty) {
            holder.ivBanner.setImageDrawable(null)
            holder.ivBanner.setBackgroundColor(0xFF2A2A2A.toInt())
            holder.tvContactName.text = ""
            holder.tvEmptyHint.visibility = View.VISIBLE
        } else {
            holder.tvEmptyHint.visibility = View.GONE
            holder.tvContactName.text = slot.contactName ?: slot.phoneNumber
            if (!slot.bannerUri.isNullOrEmpty()) {
                holder.ivBanner.setImageURI(Uri.parse(slot.bannerUri))
            } else {
                holder.ivBanner.setImageDrawable(null)
                holder.ivBanner.setBackgroundColor(0xFF37474F.toInt())
            }
        }

        holder.itemView.setOnClickListener { onShortPress(slot) }
        holder.itemView.setOnLongClickListener {
            if (!slot.isEmpty) {
                onLongPress(slot)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = slots.size

    fun refresh() = notifyDataSetChanged()
}

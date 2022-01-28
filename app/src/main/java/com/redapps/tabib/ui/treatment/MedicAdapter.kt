package com.redapps.tabib.ui.treatment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.redapps.tabib.R
import com.redapps.tabib.model.Medicament

class MedicAdapter() : RecyclerView.Adapter<MedicAdapter.MedicViewHolder>(){

    private val medics = mutableListOf<Medicament>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medic_item_layout, parent, false)
        return MedicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicViewHolder, position: Int) {
        val context = holder.itemView.context
        val medic = medics[position]
        holder.name.text = medic.nameMedicament
        Glide.with(context)
            .load(if (position % 2 == 0) R.drawable.medic1 else R.drawable.medic2)
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return medics.size
    }

    fun setMedics(newMedics : List<Medicament>){
        medics.clear()
        medics.addAll(newMedics)
        notifyDataSetChanged()
    }

    class MedicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name : TextView = view.findViewById(R.id.textMedicName)
        val image : ImageView = view.findViewById(R.id.imageMedic)
    }
}
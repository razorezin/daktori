package com.redapps.tabib.ui.booking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.utils.AppConstants
import com.redapps.tabib.utils.MenuUtils

class DoctorAdapter: RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private val doctors = mutableListOf<Doctor>() // All doctors
    private var doctorsFiltered = listOf<Doctor>()// To filter doctors with search, filters...
    private var querySave : String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_item_layout, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorsFiltered[position]
        val context = holder.itemView.context
        holder.name.text = doctor.lastName + " " + doctor.firstName
        holder.speciality.text = doctor.speciality
        holder.phone.text = doctor.phone
        Glide.with(holder.itemView.context)
            .load(AppConstants.BASE_URL + doctor.photo)
            .placeholder(R.drawable.doctor1)
            .into(holder.image)
        holder.itemView.setOnClickListener(View.OnClickListener {
            val context = holder.itemView.context
            val extras = FragmentNavigatorExtras(
                    holder.image to prepareTransition(context, holder.image, R.string.image_transition_name)
                    ,holder.name to prepareTransition(context, holder.name, R.string.name_transition_name)
                    ,holder.speciality to prepareTransition(context, holder.speciality, R.string.speciality_transition_name)
                    ,holder.location to prepareTransition(context, holder.location, R.string.location_transition_name)
                    ,holder.phone to prepareTransition(context, holder.phone, R.string.phone_transition_name))

            val gson = Gson()
            val docJson = gson.toJson(doctor)

            it.findNavController().navigate(BookingFragmentDirections.actionDoctorDetail(docJson), extras)
        })

        holder.phone.setOnClickListener {
            MenuUtils.showPhoneDialog(context, doctor.phone)
        }

        holder.location.setOnClickListener {
            MenuUtils.openMaps(context, doctor.longitude, doctor.latitude)
        }

        holder.location.text = MenuUtils.getAddresse(context, doctor.longitude, doctor.latitude)
    }

    override fun getItemCount(): Int {
        return doctorsFiltered.size
    }

    fun setDoctors(list: List<Doctor>){
        doctors.clear()
        doctors.addAll(list)
        //filterDoctors(querySave)
        doctorsFiltered = list
        notifyDataSetChanged()
    }

    fun filterDoctors(query: String){
        querySave = query
        doctorsFiltered = doctors.filter {
            it.firstName.contains(querySave, true) || it.lastName.contains(query, true) || it.speciality.contains(query, true)}
        notifyDataSetChanged()
    }

    private fun prepareTransition(context: Context, view: View, id: Int): String{
        val name = context.getString(id)
        ViewCompat.setTransitionName(view, name)
        return name
    }

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var name = itemView.findViewById<TextView>(R.id.textNameBooking)
        var speciality = itemView.findViewById<TextView>(R.id.textSpecialityBooking)
        var location = itemView.findViewById<TextView>(R.id.textLocationBooking)
        var phone = itemView.findViewById<TextView>(R.id.textPhoneBooking)
        var image = itemView.findViewById<ImageView>(R.id.imageDoctorBooking)
        var locationImage = itemView.findViewById<ImageView>(R.id.imageLocation)
        var phoneImage = itemView.findViewById<ImageView>(R.id.imagePhone)

    }
}
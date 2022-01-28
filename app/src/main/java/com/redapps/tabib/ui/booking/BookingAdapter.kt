package com.redapps.tabib.ui.booking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clovertech.autolib.utils.PrefUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.databinding.ReserveBottomsheetLayoutBinding
import com.redapps.tabib.model.*
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.utils.AppConstants
import com.redapps.tabib.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class BookingAdapter(val fragment: Fragment, val doctor: Doctor) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private val bookings = mutableListOf<Booking>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item_layout, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val context = holder.itemView.context
        holder.date.text = booking.startDate.dateToString("dd MMMM")
        holder.timeStart.text = booking.startDate.dateToString("hh:mm")
        holder.timeEnd.text = booking.endDate.dateToString("hh:mm")
        if (!booking.booked){
            holder.itemView.alpha = 1F
            holder.itemView.setOnClickListener {
                showReserveDialog(fragment, booking.startDate, doctor)
            }
        } else {
            holder.itemView.alpha = 0.3F
            holder.itemView.setOnClickListener {
                ToastUtils.longToast(context, context.getString(R.string.already_reserved))
            }
        }
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    fun setBookings(list: List<Booking>) {
        bookings.clear()
        bookings.addAll(list)
        notifyDataSetChanged()
    }

    fun clear(){
        bookings.clear()
        notifyDataSetChanged()
    }

    private fun Date.dateToString(format: String): String {
        //simple date formatter
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())

        //return the formatted date string
        return dateFormatter.format(this)
    }

    private fun String.toDate(format: String): Date?{
        return SimpleDateFormat(format).parse(this)
    }

    private fun showReserveDialog(fragment: Fragment, date: Date, doctor: Doctor){
        val dialog = BottomSheetDialog(fragment.requireContext())
        val binding = ReserveBottomsheetLayoutBinding.inflate(fragment.layoutInflater)
        dialog.setContentView(binding.root)

        binding.textDateReserve.text = date.dateToString("MMMM dd, yyyy")
        binding.textNameReserve.text = doctor.lastName + " " + doctor.firstName
        binding.textTimeReserve.text = date.dateToString("hh:mm")
        binding.textSpecialityReserve.text = doctor.speciality
        Glide.with(fragment)
            .load(AppConstants.BASE_URL + doctor.photo)
            .placeholder(R.drawable.doctor1)
            .into(binding.imageView9)

        binding.buttonReserve.setOnClickListener {
            val userJson = PrefUtils.with(fragment.requireContext()).getString(PrefUtils.Keys.USER, "")
            val user = Gson().fromJson(userJson, User::class.java)
            reserve(fragment.requireContext(), doctor.id, user.id, date.dateToString("yyyy-MM-dd HH:mm"), dialog)
            //dialog.dismiss()
        }

        dialog.show()
    }

    private fun reserve(context: Context, idDoc: Int, idPatient: Int, date: String, dialog: BottomSheetDialog){
        DoctorApiClient.instance.reserveAppointment(Reserve(idDoc, idPatient, date)).enqueue(object :
            Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                if (response.isSuccessful){
                    ToastUtils.longToast(context, "Reserved!")
                    dialog.dismiss()
                    val f = fragment as DoctorDetailFragment
                    f.fetchAppointments(idDoc, date)
                } else {
                    ToastUtils.longToast(context, "Error : " + response.message())
                }
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                ToastUtils.longToast(context, "Failed : " + t.message)
            }

        })
    }


    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val date = itemView.findViewById<TextView>(R.id.textDateBooking)
        val location = itemView.findViewById<TextView>(R.id.textLocationBookingItem)
        val timeStart = itemView.findViewById<TextView>(R.id.textStartTimeBooking)
        val timeEnd = itemView.findViewById<TextView>(R.id.textEndTimeBooking)

    }

}
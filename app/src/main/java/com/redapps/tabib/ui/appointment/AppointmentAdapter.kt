package com.redapps.tabib.ui.appointment


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.model.Appointment
import com.redapps.tabib.model.AppointmentDetail
import com.redapps.tabib.utils.UserUtils
import net.glxn.qrgen.android.QRCode
import java.text.SimpleDateFormat
import java.util.*


class AppointmentAdapter(val activity: Activity) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>(){

    private val NORMAL_TYPE_VIEW = 0
    private val SELECTED_TYPE_VIEW = 1
    private val DISABLED_TYPE_VIEW = 2


        private val appointments = mutableListOf<Appointment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return when (viewType){
            SELECTED_TYPE_VIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.appointment_item_current_layout, parent, false)
                AppointmentViewHolder(view)
            }
            DISABLED_TYPE_VIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.appointment_item_disabled_layout, parent, false)
                AppointmentViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.appointment_item_layout, parent, false)
                AppointmentViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val context = holder.itemView.context
        val appointment = appointments[position]
        Glide.with(context)
            .load(if (position % 2 == 0) R.drawable.doctor1 else R.drawable.doctor2)
            .into(holder.image)
        holder.itemView.setOnClickListener {
            showQrDialog(activity, appointment)
        }
        holder.date.text = appointment.date.dateToString("dd MMMM, yyyy")
        holder.timeStart.text = appointment.date.dateToString("hh:mm")
        val cal = Calendar.getInstance()
        cal.time = appointment.date
        cal.set(Calendar.MINUTE, cal[Calendar.MINUTE] + 30)
        holder.timeEnd.text = cal.time.dateToString("hh:mm")
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    override fun getItemViewType(position: Int): Int {
        val startDate = appointments[position].date
        val cal = Calendar.getInstance()
        val currentDate = cal.time
        cal.time = startDate
        cal.set(Calendar.MINUTE, cal[Calendar.MINUTE] + 30)
        val endDate = cal.time
        return if (currentDate.after(endDate))
            DISABLED_TYPE_VIEW
        else if (currentDate.before(startDate))
            NORMAL_TYPE_VIEW
        else
            SELECTED_TYPE_VIEW
    }

    fun setAppointments(newAppointments: List<Appointment>){
        appointments.clear()
        appointments.addAll(newAppointments)
        appointments.sortByDescending { it.date }
        notifyDataSetChanged()
    }

    private fun showQrDialog(activity: Activity, appointment: Appointment){
        val dialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.qr_dialog_layout, null)
        dialog.setContentView(view)

        val user = UserUtils.getCurrentUser(activity)
        val appointment = AppointmentDetail(
            user.name + " " + user.surname,
            user.phone,
            appointment.date
        )
        val appointJson = Gson().toJson(appointment)

        val qrImage = view.findViewById<ImageView>(R.id.imageQrCode)
            Glide.with(activity)
                .load(QRCode.from(appointJson).bitmap())
                .into(qrImage)

        dialog.show()
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

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date : TextView = view.findViewById(R.id.textDateAppointment)
        val name : TextView = view.findViewById(R.id.textNameAppointment)
        val speciality : TextView = view.findViewById(R.id.textSpecialityAppointment)
        val location : TextView = view.findViewById(R.id.textLocationAppointment)
        val phone : TextView = view.findViewById(R.id.textPhoneAppointment)
        val timeStart : TextView = view.findViewById(R.id.textTimeStartAppointment)
        val timeEnd : TextView = view.findViewById(R.id.textTimeEndAppointment)
        val image : ImageView = view.findViewById(R.id.imageDoctorAppointment)

    }
}
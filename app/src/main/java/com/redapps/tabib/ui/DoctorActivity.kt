package com.redapps.tabib.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.clovertech.autolib.utils.PrefUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.redapps.tabib.R
import com.redapps.tabib.databinding.ActivityDoctorBinding
import com.redapps.tabib.databinding.AppointmentDetailLayoutBinding
import com.redapps.tabib.model.*
import com.redapps.tabib.network.AuthApiClient
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.ui.appointment.AppointmentAdapter
import com.redapps.tabib.utils.AppConstants
import com.redapps.tabib.utils.MenuUtils
import com.redapps.tabib.utils.ToastUtils
import com.redapps.tabib.utils.UserUtils
import com.redapps.tabib.viewmodel.DoctorViewModel
import com.redapps.tabib.viewmodel.PatientViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class DoctorActivity : AppCompatActivity() {

    val TAG = "LOG TAG"

    private lateinit var binding: ActivityDoctorBinding
    private lateinit var user : User
    private lateinit var vmDoctor : DoctorViewModel
    private val appointmentAdapter = AppointmentAdapter(this)
    private var currentMonth = 0
    private val calendar = Calendar.getInstance()
    private lateinit var currentDate : Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vmDoctor = ViewModelProvider(this).get(DoctorViewModel::class.java)

        // Get current User
        user = UserUtils.getCurrentUser(this)

        // Account image click
        binding.imageAccountMain.setOnClickListener {
            MenuUtils.showAccountDialog(this)
        }

        // QR scan button click
        var scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //ToastUtils.longToast(this, "Appointment ID : " + result.data!!.getStringExtra("SCAN_RESULT"))
                val resultStr = result.data!!.getStringExtra("SCAN_RESULT")
                val appointment = Gson().fromJson(resultStr, AppointmentDetail::class.java)
                showAppointmentDetailDialog(appointment)
            }
        }
        binding.buttonScanQr.setOnClickListener {
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.setPackage("com.google.zxing.client.android")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
                scanLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No QR scanner app found on your phone", Toast.LENGTH_LONG).show()
            }
        }

        // On Refresh
        binding.swipeDoctor.setOnRefreshListener {
            fetchAppointments()
        }

        setupCalendar()
        setupAppointmentRecycler()
        setupObservers()

        // temp
        Glide.with(this)
            .load(R.drawable.doctor1)
            .into(binding.imageAccountMain)

    }

    override fun onResume() {
        super.onResume()
        sendFCMToken()
    }

    private fun showAppointmentDetailDialog(appointment: AppointmentDetail) {
        val dialog = BottomSheetDialog(this)
        //val view = this.layoutInflater.inflate(R.layout.appointment_detail_layout, null)
        val bind = AppointmentDetailLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)

        bind.textAppointmentPatientName.text = appointment.name
        bind.textAppointmentPhone.text = appointment.phone
        bind.textAppointmentDate.text = appointment.date.dateToString("dd MMMM, yyyy")
        bind.textAppointmentTime.text = appointment.date.dateToString("hh:mm")

        dialog.show()
    }

    private fun setupObservers(){
        vmDoctor.appointments.observeForever {
            updateAppointments(it)
        }
        vmDoctor.dataLoading.observeForever {
            binding.swipeDoctor.isRefreshing = it
            when (it) {
                true -> {
                    binding.recyclerAppointmentDoctor.visibility = View.GONE
                }
                false -> {
                    binding.recyclerAppointmentDoctor.visibility = View.VISIBLE
                }
            }
        }
        vmDoctor.empty.observeForever {
            when (it) {
                true -> binding.emptyLayoutDoc.visibility = View.VISIBLE
                false -> binding.emptyLayoutDoc.visibility = View.GONE
            }
        }
        vmDoctor.failed.observeForever {
            when (it) {
                true -> binding.failedLayoutDoc.visibility = View.VISIBLE
                false -> binding.failedLayoutDoc.visibility = View.GONE
            }
        }
        vmDoctor.toastMessage.observeForever {
            try {
                ToastUtils.longToast(this, it)
            } catch (e: Exception) {
            }
        }
    }

    private fun updateAppointments(it: List<Appointment>) {
        appointmentAdapter.setAppointments(it)
    }

    private fun setupAppointmentRecycler() {
        binding.recyclerAppointmentDoctor.adapter = appointmentAdapter
        binding.recyclerAppointmentDoctor.layoutManager = LinearLayoutManager(this)

        fetchAppointments()
    }

    private fun fetchAppointments(){
        vmDoctor.fetchAppointments(user.id, currentDate.dateToString("yyyy-MM-dd"))
    }

    private fun setupCalendar() {
        calendar.time = Date()
        currentMonth = calendar[Calendar.MONTH]

        // Init view manager
        val myCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                // return item layout files, which you have created
                val cal = Calendar.getInstance()
                cal.time = date
                return if (cal[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY || cal[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY) {
                    R.layout.calendar_item_disbled
                } else if (!isSelected) {
                    R.layout.calendar_item_layout
                } else {
                    R.layout.calendar_item_selected_layout
                }
            }

            override fun bindDataToCalendarView(
                holder: SingleRowCalendarAdapter.CalendarViewHolder,
                date: Date,
                position: Int,
                isSelected: Boolean
            ) {
                // bind data to calendar item views
                val dayNumber = holder.itemView.findViewById<TextView>(R.id.textCalendarDayNumber)
                val dayName3 = holder.itemView.findViewById<TextView>(R.id.textCalendarDayName3)
                dayNumber.text = DateUtils.getDayNumber(date)
                dayName3.text = DateUtils.getDay3LettersName(date)
            }
        }

        // Init selection manager
        val mySelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                // return true if item can be selected
                val cal = Calendar.getInstance()
                cal.time = date
                return !(cal[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY || cal[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY)
            }
        }

        // Setup observers
        val myCalendarChangesObserver = object : CalendarChangesObserver {

            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                vmDoctor.fetchAppointments(user.id, date.dateToString("yyyy-MM-dd"))
                //fetchAppointments(doctor.id, date.dateToString("yyyy-MM-dd"))
                //bookingAdapter.setBookings(getBookingsFromInterval(date, doctor.startHour, doctor.endHour))
                super.whenSelectionChanged(isSelected, position, date)
                currentDate = date
            }
        }

        val calendarView: SingleRowCalendar = binding.calendarDoctorActivity
        val day = calendar[Calendar.DAY_OF_MONTH] - 1

        val cal = calendarView.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            initialPositionIndex = day
            includeCurrentDate = true
            setDates(getDatesOfCurrentMonth())
            init()
        }

        cal.select(day)

        binding.buttonCalendarLeftDoctor.setOnClickListener {
            cal.setDates(getDatesOfPreviousMonth())
            binding.textDateDoctor.text = "${DateUtils.getMonthName(calendar.time)}, " +
                    "${DateUtils.getYear(calendar.time)}"
            cal.bringToFront()
        }

        binding.buttonCalendarRightDoctor.setOnClickListener {
            cal.setDates(getDatesOfNextMonth())
            binding.textDateDoctor.text = "${DateUtils.getMonthName(calendar.time)}, " +
                    "${DateUtils.getYear(calendar.time)}"
            cal.bringToFront()

        }

        binding.textDateDoctor.text = "${DateUtils.getMonthName(calendar.time)}, " +
                "${DateUtils.getYear(calendar.time)}"
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

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfCurrentMonth(): List<Date> {
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentMonth = calendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }

    private fun getDatesOfNextMonth(): List<Date> {
        currentMonth++ // + because we want next month
        if (currentMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] + 1)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        list.add(calendar.time)
        while (currentMonth == calendar[Calendar.MONTH]) {
            calendar.add(Calendar.DATE, +1)
            if (calendar[Calendar.MONTH] == currentMonth)
                list.add(calendar.time)
        }
        calendar.add(Calendar.DATE, -1)
        return list
    }

    private fun sendFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            if (token != null){
                PrefUtils.with(this).save(PrefUtils.Keys.FCM_TOKEN, token)
                AuthApiClient.instance.sendToken(NotificationToken(user.id, token)).enqueue(object :
                    Callback<NotificationToken> {
                    override fun onResponse(call: Call<NotificationToken>, response: Response<NotificationToken>) {
                        if (response.isSuccessful){
                            ToastUtils.longToast(applicationContext, "Token sent!")
                        } else {

                        }
                    }

                    override fun onFailure(call: Call<NotificationToken>, t: Throwable) {

                    }
                })
            }

            // Log and toast
            Log.d(TAG, "FCM token : " + token)
        })
    }
}
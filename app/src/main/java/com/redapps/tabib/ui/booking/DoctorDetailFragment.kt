package com.redapps.tabib.ui.booking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.redapps.tabib.R
import com.redapps.tabib.databinding.FragmentBookingBinding
import com.redapps.tabib.databinding.FragmentDoctorDetailBinding
import com.redapps.tabib.model.*
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.utils.AppConstants
import com.redapps.tabib.utils.MenuUtils
import com.redapps.tabib.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class DoctorDetailFragment : Fragment() {

    private lateinit var bookingViewModel: BookingViewModel
    private var _binding: FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!
    private var currentMonth = 0
    private val calendar = Calendar.getInstance()
    private lateinit var bookingAdapter: BookingAdapter
    private val args: DoctorDetailFragmentArgs by navArgs()
    private lateinit var doctor: Doctor
    private lateinit var currentDate : Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bookingViewModel =
            ViewModelProvider(this).get(BookingViewModel::class.java)

        _binding = FragmentDoctorDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gson = Gson()
        doctor = gson.fromJson(args.doc, Doctor::class.java)

        setupViews(doctor)

        binding.buttonBackDoctorDetail.setOnClickListener {
            findNavController().navigate(R.id.action_nav_doctor_detail_to_navigation_booking)
        }

        bookingAdapter = BookingAdapter(this, doctor)

        initCalendar()
        initBookingRecycler()
    }

    private fun setupViews(doctor: Doctor) {
        binding.textNameDoctorDetail.text = doctor.lastName + " " + doctor.firstName
        binding.textSpecialityDoctorDetail.text = doctor.speciality
        binding.textPhoneDoctorDetail.text = doctor.phone
        Glide.with(requireActivity())
            .load(AppConstants.BASE_URL + doctor.photo)
            .placeholder(R.drawable.doctor1)
            .into(binding.imageDoctorDetail)
        binding.textPhoneDoctorDetail.setOnClickListener {
            MenuUtils.showPhoneDialog(requireContext(), doctor.phone)
        }
        binding.textLocationDoctorDetail.setOnClickListener {
            MenuUtils.openMaps(requireContext(), doctor.longitude, doctor.latitude)
        }
        binding.textLocationDoctorDetail.text = MenuUtils.getAddresse(requireContext(), doctor.longitude, doctor.latitude)
    }

    private fun initCalendar(){
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
                currentDate = date
                fetchAppointments(doctor.id, date.dateToString("yyyy-MM-dd"))
                //bookingAdapter.setBookings(getBookingsFromInterval(date, doctor.startHour, doctor.endHour))
                super.whenSelectionChanged(isSelected, position, date)
            }
        }

        val calendarView: SingleRowCalendar = binding.calendarBooking
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

        binding.buttonCalendarLeftBooking.setOnClickListener {
            cal.setDates(getDatesOfPreviousMonth())
            binding.textCalendarMonthBooking.text = "${DateUtils.getMonthName(calendar.time)}, " +
                    "${DateUtils.getYear(calendar.time)}"
            cal.bringToFront()
        }

        binding.buttonCalendarRightBooking.setOnClickListener {
            cal.setDates(getDatesOfNextMonth())
            binding.textCalendarMonthBooking.text = "${DateUtils.getMonthName(calendar.time)}, " +
                    "${DateUtils.getYear(calendar.time)}"
            cal.bringToFront()

        }

        binding.textCalendarMonthBooking.text = "${DateUtils.getMonthName(calendar.time)}, " +
                "${DateUtils.getYear(calendar.time)}"
    }

    private fun initBookingRecycler() {
        val recycler = binding.recyclerTimeBooking
        recycler.adapter = bookingAdapter
        recycler.layoutManager = LinearLayoutManager(context)

        fetchAppointments(doctor.id, currentDate.dateToString("yyyy-MM-dd"))

        binding.swipeBookings.setOnRefreshListener {
            fetchAppointments(doctor.id, currentDate.dateToString("yyyy-MM-dd"))
        }
    }

    private fun getBookingsFromInterval(date: Date, start: String, end: String): MutableList<Booking>{
        val result = mutableListOf<Booking>()
        val cal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        endCal.time = end.toDate("hh:mm")
        endCal.set(date.dateToString("yyyy-MM-dd").split("-")[0].toInt(),
            date.dateToString("yyyy-MM-dd").split("-")[1].toInt() - 1,
            date.dateToString("yyyy-MM-dd").split("-")[2].toInt())
        cal.time = start.toDate("hh:mm")
        cal.set(date.dateToString("yyyy-MM-dd").split("-")[0].toInt(),
            date.dateToString("yyyy-MM-dd").split("-")[1].toInt() - 1,
            date.dateToString("yyyy-MM-dd").split("-")[2].toInt())
        var stop = false
        while (!stop){
            val startCal = Calendar.getInstance()
            startCal.time = cal.time
            cal.set(Calendar.MINUTE, cal[Calendar.MINUTE] + 30)
            if (!cal.after(endCal)) {
                result.add(Booking(false, startCal.time, cal.time))
            } else stop = true
        }
        return result
    }

    fun fetchAppointments(idDoc: Int, date: String){
        bookingAdapter.clear()
        binding.swipeBookings.isRefreshing = true
        DoctorApiClient.instance.getAppointmentsByDocAndDate(BookingFetch(idDoc, date)).enqueue(object : Callback<List<Booking>>{
            override fun onResponse(call: Call<List<Booking>>, response: Response<List<Booking>>) {
                if (response.isSuccessful){
                    val reservedDated = response.body()!!.map { it.startDate.dateToString("hh:mm") }
                    val bookings = getBookingsFromInterval(date.toDate("yyyy-MM-dd")!!, doctor.startHour, doctor.endHour)
                    for (booking in bookings){
                        booking.booked = booking.startDate.dateToString("hh:mm") in reservedDated
                    }
                    bookingAdapter.setBookings(bookings)
                } else {
                    ToastUtils.longToast(requireContext(), "Error : " + response.message())
                }
                binding.swipeBookings.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Booking>>, t: Throwable) {
                ToastUtils.longToast(requireContext(), "Failed : " + t.message)
                binding.swipeBookings.isRefreshing = false
            }

        })
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

    private fun getRandomBookings(count: Int): List<Booking>{
        val list = mutableListOf<Booking>()
        val booking = Booking(false, Calendar.getInstance().time, Calendar.getInstance().time)
        for (i in 1..count){
            list.add(booking)
        }
        return list
    }

}
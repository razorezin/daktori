package com.redapps.tabib.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.redapps.tabib.databinding.FragmentBookingBinding
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.utils.ToastUtils
import com.redapps.tabib.viewmodel.DoctorViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingFragment : Fragment() {

    private val doctorAdapter = DoctorAdapter()

    private lateinit var vm: DoctorViewModel
    private lateinit var binding : FragmentBookingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vm =
            ViewModelProvider(this).get(DoctorViewModel::class.java)

        binding = FragmentBookingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setups
        initDoctorRecycler()
        setupSearch()
        setupObservers()

        // On refresh
        binding.root.setOnRefreshListener {
            //fetchDoctors()
            vm.fetchDoctors()
        }

        // Initial fetch
        vm.fetchDoctors()
    }

    private fun setupObservers(){
        vm.doctors.observeForever {
            updateDoctors(it)
        }
        vm.dataLoading.observeForever {
            binding.root.isRefreshing = it
            when (it) {
                true -> binding.searchDoctorBooking.visibility = View.GONE
                false -> binding.searchDoctorBooking.visibility = View.VISIBLE
            }
        }
        vm.empty.observeForever {
            when (it) {
                true -> binding.emptyLayout.visibility = View.VISIBLE
                false -> binding.emptyLayout.visibility = View.GONE
            }
        }
        vm.failed.observeForever {
            when (it) {
                true -> binding.failedLayout.visibility = View.VISIBLE
                false -> binding.failedLayout.visibility = View.GONE
            }
        }
        vm.toastMessage.observeForever {
            try {
                ToastUtils.longToast(requireContext(), it)
            } catch (e: Exception) {
            }
        }
    }

    private fun setupSearch() {
        binding.searchDoctorBooking.doOnTextChanged { text, start, before, count ->
            doctorAdapter.filterDoctors(text.toString())
        }
    }

    private fun updateDoctors(newDoctors: List<Doctor>){
        doctorAdapter.setDoctors(newDoctors)
    }

    private fun initDoctorRecycler(){
        val recycler = binding.recyclerDoctorBooking
        recycler.adapter = doctorAdapter
        recycler.layoutManager = LinearLayoutManager(context)
    }

    private fun getRandomDoctors(count: Int): List<Doctor>{
        val list = mutableListOf<Doctor>()
        val doctor = Doctor(1, "Ahmed", "Doctor", "", "05 35 54 23 88",
        "Psychology", 0.0, 0.0, "9", "16")
        val doctor1 = Doctor(1, "Hadjer", "Doctor", "", "05 55 64 44 35",
            "Cardiology", 0.0, 0.0, "9", "16")
        for (i in 1..count){
            if (i % 2 == 1) list.add(doctor) else list.add(doctor1)
        }
        return list
    }
}
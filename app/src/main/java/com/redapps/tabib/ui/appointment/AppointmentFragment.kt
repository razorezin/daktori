package com.redapps.tabib.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.clovertech.autolib.utils.PrefUtils
import com.google.gson.Gson
import com.redapps.tabib.databinding.FragmentAppointmentBinding
import com.redapps.tabib.model.Appointment
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.model.User
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.network.PatientApiClient
import com.redapps.tabib.utils.ToastUtils
import com.redapps.tabib.viewmodel.PatientViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentFragment : Fragment() {

    private lateinit var adapter : AppointmentAdapter

    private lateinit var vmPatient: PatientViewModel
    private lateinit var binding : FragmentAppointmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vmPatient =
            ViewModelProvider(this).get(PatientViewModel::class.java)

        binding = FragmentAppointmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userJson = PrefUtils.with(requireContext()).getString(PrefUtils.Keys.USER, "")
        val user = Gson().fromJson(userJson, User::class.java)

        binding.root.setOnRefreshListener {
            vmPatient.fetchAppointments(user.id)
        }

        // Setups
        initRecycler()
        setupObservers()
        vmPatient.fetchAppointments(user.id)

    }

    private fun setupObservers(){
        vmPatient.appointments.observeForever {
            updateAppointments(it)
        }
        vmPatient.dataLoading.observeForever {
            binding.root.isRefreshing = it
            when (it) {
                true -> binding.textTitleAppointments.visibility = View.GONE
                false -> binding.textTitleAppointments.visibility = View.VISIBLE
            }
        }
        vmPatient.empty.observeForever {
            when (it) {
                true -> binding.emptyLayout.visibility = View.VISIBLE
                false -> binding.emptyLayout.visibility = View.GONE
            }
        }
        vmPatient.failed.observeForever {
            when (it) {
                true -> binding.layoutFailed.visibility = View.VISIBLE
                false -> binding.layoutFailed.visibility = View.GONE
            }
        }
        vmPatient.toastMessage.observeForever {
            try {
                ToastUtils.longToast(requireContext(), it)
            } catch (e: Exception) {
            }
        }
    }

    private fun fetchAppointments(idPatient: Int){
        binding.root.isRefreshing = true
        PatientApiClient.instance.getAppointmentsByPatient(idPatient).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                if (response.isSuccessful){
                    updateAppointments(response.body()!!)
                } else {
                    ToastUtils.longToast(requireContext(), "Error  : " + response.message())
                }
                binding.root.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                ToastUtils.longToast(requireContext(), "Failed : " + t.message)
                binding.root.isRefreshing = false
            }
        })
    }

    private fun updateAppointments(newAppointments: List<Appointment>){
        if (newAppointments.isNotEmpty()){
            binding.emptyLayout.visibility = View.GONE
        } else {
            binding.emptyLayout.visibility = View.VISIBLE
        }
        adapter.setAppointments(newAppointments)
    }

    private fun initRecycler() {
        adapter = AppointmentAdapter(requireActivity())
        binding.recyclerAppointment.adapter = adapter
        binding.recyclerAppointment.layoutManager = LinearLayoutManager(requireContext())
    }
}
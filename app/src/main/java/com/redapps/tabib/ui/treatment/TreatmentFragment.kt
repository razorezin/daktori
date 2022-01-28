package com.redapps.tabib.ui.treatment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.clovertech.autolib.utils.PrefUtils
import com.google.gson.Gson
import com.redapps.tabib.databinding.FragmentTreatmentBinding
import com.redapps.tabib.model.Medicament
import com.redapps.tabib.model.Treatment
import com.redapps.tabib.model.User
import com.redapps.tabib.utils.ToastUtils
import com.redapps.tabib.viewmodel.DoctorViewModel
import com.redapps.tabib.viewmodel.PatientViewModel
import kotlin.math.abs

class TreatmentFragment : Fragment() {

    private lateinit var treatmentAdapter : TreatmentAdapter
    private lateinit var vmPatient: PatientViewModel
    private lateinit var vmDoctor: DoctorViewModel
    private lateinit var binding : FragmentTreatmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vmPatient =
            ViewModelProvider(this).get(PatientViewModel::class.java)

        vmDoctor =
            ViewModelProvider(this).get(DoctorViewModel::class.java)

        binding = FragmentTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userJson = PrefUtils.with(requireContext()).getString(PrefUtils.Keys.USER, "")
        val user = Gson().fromJson(userJson, User::class.java)

        initTreatmentsPager()
        setupObservers()
        binding.root.setOnRefreshListener {
            vmPatient.fetchTreatments(requireContext(), user.id)
            vmDoctor.fetchDoctors()
        }
        vmPatient.fetchTreatments(requireContext(), user.id)
        vmDoctor.fetchDoctors()

    }

    private fun setupObservers(){
        vmPatient.getTreatments(requireContext()).observeForever {
            updateTreatments(it)
        }
        vmPatient.dataLoading.observeForever {
            binding.root.isRefreshing = it
            when (it) {
                true -> {
                    binding.textTreatmentNumber.visibility = View.GONE
                    binding.textTitle1Treatment.visibility = View.GONE
                    binding.textTitle2Treatment.visibility = View.GONE
                    binding.pagerTreatment.visibility = View.GONE
                }
                false -> {
                    binding.textTreatmentNumber.visibility = View.VISIBLE
                    binding.textTitle1Treatment.visibility = View.VISIBLE
                    binding.textTitle2Treatment.visibility = View.VISIBLE
                    binding.pagerTreatment.visibility = View.VISIBLE
                }
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
                true -> binding.failedLayout.visibility = View.VISIBLE
                false -> binding.failedLayout.visibility = View.GONE
            }
        }
        vmPatient.toastMessage.observeForever {
            try {
                ToastUtils.longToast(requireContext(), it)
            } catch (e: Exception) {
            }
        }
        vmDoctor.doctors.observeForever {
            treatmentAdapter.setDoctors(it)
        }
    }

    private fun initTreatmentsPager() {
        treatmentAdapter = TreatmentAdapter()
        val pager = binding.pagerTreatment
        pager.adapter = treatmentAdapter
        pager.clipToPadding = false
        pager.clipChildren = false
        pager.offscreenPageLimit = 2
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(4))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        pager.setPageTransformer(transformer)
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // update medics
            }
        })
    }

    private fun updateTreatments(newTreatments: List<Treatment>){
        treatmentAdapter.setTreatments(newTreatments)
        binding.textTreatmentNumber.text = newTreatments.size.toString()
    }

    private fun getRandomMedics(count: Int): List<Medicament> {
        val list = mutableListOf<Medicament>()
        val medic = Medicament(1, "Doliprane", 2)
        val medic1 = Medicament(2, "Efferalgan", 2)
        for (i in 1..count){
            if (i % 2 == 0) list.add(medic) else list.add(medic1)
        }
        return list
    }
}
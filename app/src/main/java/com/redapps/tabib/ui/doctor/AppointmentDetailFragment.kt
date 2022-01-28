package com.redapps.tabib.ui.doctor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redapps.tabib.R
import com.redapps.tabib.databinding.FragmentAppointmentDetailBinding

class AppointmentDetailFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentAppointmentDetailBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


}
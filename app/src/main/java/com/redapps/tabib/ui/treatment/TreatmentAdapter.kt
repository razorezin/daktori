package com.redapps.tabib.ui.treatment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.clovertech.autolib.utils.PrefUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.databinding.AdviceDialogLayoutBinding
import com.redapps.tabib.model.*
import com.redapps.tabib.network.PatientApiClient
import com.redapps.tabib.utils.AppConstants
import com.redapps.tabib.utils.ToastUtils
import com.redapps.tabib.utils.UserUtils
import com.redapps.tabib.viewmodel.DoctorViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TreatmentAdapter : RecyclerView.Adapter<TreatmentAdapter.TreatmentViewHolder>() {

    private val treatments = mutableListOf<Treatment>()
    private var doctors = listOf<Doctor>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.treatment_item_layout, parent, false)
        return TreatmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreatmentViewHolder, position: Int) {
        val treatment = treatments[position]
        val context = holder.itemView.context
        var doctor = doctors.find { it.id == treatment.idDoc }
        holder.textTitle.text = context.getString(R.string.treatment) + " " + treatment.idTreatment
        Glide.with(context)
            .load(AppConstants.BASE_URL + (doctor?.photo ?: ""))
            .placeholder(R.drawable.doctor1)
            .into(holder.imageDoc)
        holder.medicRecycler.layoutManager = LinearLayoutManager(context)
        val adapter = MedicAdapter()
        adapter.setMedics(treatment.medicamentList)
        holder.medicRecycler.adapter = adapter
        holder.buttonAdvice.setOnClickListener {
            showAdviceDialog(context, treatment)
        }
        holder.textDocName.text = doctor?.lastName + " " + doctor?.firstName
        holder.textDocSpe.text = doctor?.speciality
        holder.textDocPhone.text = doctor?.phone
    }

    private fun showAdviceDialog(context: Context, treatment: Treatment) {
        val dialog = BottomSheetDialog(context)
        val binding = AdviceDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        binding.buttonSendAdvice.setOnClickListener {
            // Schedule advice sending
            val message = binding.editAdvice.text.toString()
            val user = UserUtils.getCurrentUser(context)
            val advice = Advice(user.id, treatment.idDoc, message)
            val adviceJson = Gson().toJson(advice)
            PrefUtils.with(context).save(PrefUtils.Keys.ADVICE_TO_SEND, adviceJson)
            scheduleAdviceSend(context)
            dialog.dismiss()
            ToastUtils.longToast(context, "Sending advice...")
        }

        dialog.show()
    }

    private fun scheduleAdviceSend(context: Context){
        val constraints = Constraints.Builder().
        setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val req= OneTimeWorkRequest.Builder (AdviceWorker::class.java).
        setConstraints(constraints).addTag("advice_tag")
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork("advice_send_worker", ExistingWorkPolicy.REPLACE,req)
    }

    override fun getItemCount(): Int {
        return treatments.size
    }

    fun setTreatments(newTreatments: List<Treatment>){
        treatments.clear()
        treatments.addAll(newTreatments)
        notifyDataSetChanged()
    }

    fun setDoctors(list: List<Doctor>){
        doctors = list
        notifyDataSetChanged()
    }

    class TreatmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle : TextView = view.findViewById(R.id.textTreatmentTitle)
        val textTime : TextView = view.findViewById(R.id.textTimeLeftTreatment)
        val textDocName : TextView = view.findViewById(R.id.textNameTreatment)
        val textDocSpe : TextView = view.findViewById(R.id.textSpecialityTreatment)
        val textDocPhone : TextView = view.findViewById(R.id.textPhoneTreatment)
        val imageDoc : ImageView = view.findViewById(R.id.imageDoctorTreatment)
        val medicRecycler : RecyclerView = view.findViewById(R.id.recyclerMedics)
        val buttonAdvice : Button = view.findViewById(R.id.buttonAdvice)
    }
}
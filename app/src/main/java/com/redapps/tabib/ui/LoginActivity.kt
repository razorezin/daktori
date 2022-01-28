package com.redapps.tabib.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.clovertech.autolib.utils.PrefUtils
import com.google.gson.Gson
import com.redapps.tabib.databinding.ActivityLoginBinding
import com.redapps.tabib.model.User
import com.redapps.tabib.model.UserLogin
import com.redapps.tabib.network.AuthApiClient
import com.redapps.tabib.utils.ToastUtils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user from prefs
        val userJson = PrefUtils.with(this).getString(PrefUtils.Keys.USER, "")
        if (userJson != ""){
            // already logged in
            val gson = Gson()
            val user : User = gson.fromJson(userJson, User::class.java)
            if (user.docSpeciality != null){
                startDoctorActivity(user)
            } else  {
                startPatientActivity(user)
            }
        }

        // On login
        binding.buttonLogin.setOnClickListener {
            binding.buttonLogin.visibility = View.INVISIBLE
            binding.buttonLogin.isEnabled = false
            binding.progressLogin.visibility = View.VISIBLE

            val phone = binding.editPhoneLogin.text.toString()
            val password = binding.editPasswordLogin.text.toString()

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("phone", phone)
                .addFormDataPart("password", password)
                .build()
            AuthApiClient.instance.login(requestBody).enqueue(object : Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful){
                        val user = response.body()
                        ToastUtils.longToast(applicationContext, "Logged in")
                        if (user!!.docSpeciality != null){
                            startDoctorActivity(user)
                        } else  {
                            startPatientActivity(user)
                        }
                    } else {
                        binding.buttonLogin.visibility = View.VISIBLE
                        binding.buttonLogin.isEnabled = true
                        binding.progressLogin.visibility = View.GONE
                        ToastUtils.longToast(applicationContext, "Wrong phone or password")
                        binding.editPhoneLogin.setError("Check your phone")
                        binding.editPasswordLogin.setError("Check your password")
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    ToastUtils.longToast(applicationContext, "Failed : " + t.message)
                    binding.buttonLogin.visibility = View.VISIBLE
                    binding.buttonLogin.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                }

            })
        }

        // temp
        binding.textForgotPass.setOnClickListener {
            startDoctorActivity(User(1, "User", "Test", "", "", "", "", "", ""))
        }
    }

    private fun startPatientActivity(user: User){
        PrefUtils.with(this).save(PrefUtils.Keys.USER, Gson().toJson(user))
        startActivity(Intent(this, PatientActivity::class.java))
        finish()
    }

    private fun startDoctorActivity(user: User){
        PrefUtils.with(this).save(PrefUtils.Keys.USER, Gson().toJson(user))
        startActivity(Intent(this, DoctorActivity::class.java))
        finish()
    }


}
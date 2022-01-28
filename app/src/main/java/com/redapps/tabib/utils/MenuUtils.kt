package com.redapps.tabib.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.clovertech.autolib.utils.PrefUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.databinding.PhoneDialogLayoutBinding
import com.redapps.tabib.model.User
import com.redapps.tabib.ui.LoginActivity
import com.redapps.tabib.ui.PatientActivity
import java.util.*
import java.util.Locale


object MenuUtils {

    fun showAccountDialog(activity: AppCompatActivity){
        val dialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.account_bottomsheet_layout, null)
        dialog.setContentView(view)

        // Get user
        val user = Gson().fromJson(PrefUtils.with(activity).getString(PrefUtils.Keys.USER, ""), User::class.java)

        val logoutButton = view.findViewById<TextView>(R.id.textLogout)
        val settingButton = view.findViewById<TextView>(R.id.textSettings)

        // Setup onClickListeners
        logoutButton.setOnClickListener {
            // Delete user from prefs
            PrefUtils.with(activity).save(PrefUtils.Keys.USER, "")
            // Goto login activity
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
        }
        settingButton.setOnClickListener {

        }

        // Setup name
        val nameView = view.findViewById<TextView>(R.id.textNameMenu)
        nameView.text = user.name + " " + user.surname

        // temp
        Glide.with(activity)
            .load(if (activity is PatientActivity) R.drawable.patient else R.drawable.doctor1)
            .into(view.findViewById(R.id.imageAccountMenu))

        dialog.show()
    }

    fun showPhoneDialog(context: Context, phone: String){
        val dialog = BottomSheetDialog(context)
        val binding = PhoneDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        binding.textCallPhone.setOnClickListener {
            callIntent(context, phone)
        }
        binding.textSendSMS.setOnClickListener {
            sendSMSIntent(context, phone)
        }
        binding.textCopyPhone.setOnClickListener {
            copyToClipboard(context, phone)
        }

        dialog.show()
    }

    fun callIntent(context: Context, phone: String){
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    fun sendSMSIntent(context: Context, phone: String){
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null)))
    }

    fun copyToClipboard(context: Context, text: String){
        val clipboard: ClipboardManager =
            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Phone number",
            text
        )
        clipboard.setPrimaryClip(clip)
        ToastUtils.longToast(context, context.getString(R.string.copied))
    }

    fun openMaps(context: Context, longitude: Double, latitude: Double){
        val uri = java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
        /*val uri =
            "http://maps.google.com/maps?f=d&hl=en&saddr=" + latitude1.toString() + "," + longitude1.toString() + "&daddr=" + latitude2.toString() + "," + longitude2
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(Intent.createChooser(intent, "Select an application"))*/
    }

    fun getAddresse(context: Context, longitude: Double, latitude: Double) : String{
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(context, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        val address: String =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        /*
        val city: String = addresses[0].getLocality()
        val state: String = addresses[0].getAdminArea()
        val country: String = addresses[0].getCountryName()
        val postalCode: String? = addresses[0].getPostalCode()
        val knownName: String = addresses[0].getFeatureName()*/

        return address
    }

}
package com.redapps.tabib.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clovertech.autolib.utils.PrefUtils
import com.google.gson.Gson
import com.redapps.tabib.R
import com.redapps.tabib.databinding.FragmentChatBinding
import com.redapps.tabib.model.Message
import com.redapps.tabib.model.User

class ChatAdapter(val context: Context) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){

    private val RIGHT_MSG_TYPE = 0
    private val LEFT_MSG_TYPE = 1


    private val messages = mutableListOf<Message>()
    private val user = Gson().fromJson(PrefUtils.with(context).getString(PrefUtils.Keys.USER, ""), User::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.message_right_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return if (user.id == messages[position].idSender)
            RIGHT_MSG_TYPE
        else
            LEFT_MSG_TYPE
    }

    override fun getItemCount() = messages.size

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    }
}
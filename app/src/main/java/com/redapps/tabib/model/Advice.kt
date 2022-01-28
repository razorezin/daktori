package com.redapps.tabib.model

import com.google.gson.annotations.SerializedName

data class Advice(
    @SerializedName("idSender") var idSender: Int,
    @SerializedName("idReceiver") var idReceiver: Int,
    @SerializedName("AdviceMessage") var message: String
)

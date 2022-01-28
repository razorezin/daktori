package com.redapps.tabib.model

import java.util.*

data class Message(
    var id: Int,
    var idSender : Int,
    var idReceiver : Int,
    var message : String,
    var date: Date
)

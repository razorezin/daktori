package com.redapps.tabib.model

import retrofit2.http.Body

data class NotificationToken(
    //var idToken: Int,
    var idDoc: Int,
    var token: String
)

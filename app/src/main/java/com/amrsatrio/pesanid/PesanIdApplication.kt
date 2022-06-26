package com.amrsatrio.pesanid

import android.app.Application

class PesanIdApplication : Application() {
	var userToken: Token? = null
}

data class Token(val access_token: String, val token_type: String)
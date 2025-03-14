package com.example.qcounter.network


import android.content.Context
import com.example.slaughterhousescreen.util.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitBuilder(context: Context) {

    //lazy: define heavy variable as lazy to execute it when call it only

    private val apiService: ApiService by lazy {
       val baseUrl = PreferenceManager.getBaseUrl(context)
     //  val baseUrl = "http://192.168.0.195/Api/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl?:"")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }



    suspend fun getContoursDetails(ip:String) =apiService.getContorsDetails(ip)

    suspend fun getTicketDetails(counterNo: String, branchCode:String) = apiService.getTicketDetails(counterNo,branchCode)


}



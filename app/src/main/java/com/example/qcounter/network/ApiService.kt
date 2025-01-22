package com.example.qcounter.network

import com.example.qcounter.dataclass.CountersResponse
import com.example.qcounter.dataclass.TicketResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {


    @GET("api/Counters")
    suspend fun getContorsDetails(
        @Query("ip") ip: String
    ): CountersResponse

    @GET("api/KHCC_Get_Ticket")
    suspend fun getTicketDetails(
        @Query("CounterNo") counterNo: String ,
        @Query("BranchCode") branchCode: String ,
    ): TicketResponse

}
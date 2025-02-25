package com.example.qcounter.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountersResponse (

    @SerialName("counterno"     ) var counterno     : String? = null,
    @SerialName("BranchCode"    ) var BranchCode    : String? = null,
    @SerialName("CounterTypeID" ) var CounterTypeID : String? = null

)
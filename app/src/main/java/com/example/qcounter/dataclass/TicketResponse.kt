package com.example.qcounter.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketResponse (

    @SerialName("ticketno"        ) var ticketno        : String? = null,
    @SerialName("ResourceDisplay" ) var ResourceDisplay : String? = null

)
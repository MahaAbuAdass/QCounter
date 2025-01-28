package com.example.qcounter.dataclass

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

data class FileURL(
    @SerialName("fileName") var fileName: String? = null,
    @SerialName("Duration") var Duration: String? = null,
    @SerialName("OrderNo") var OrderNo: Int? = null

)

package com.example.qcounter.dataclass

import kotlinx.serialization.SerialName

data class DeviceConfiguration(
    @SerialName("ButtonColor"     ) var ButtonColor     : String? = null,
    @SerialName("FontType"        ) var FontType        : String? = null,
    @SerialName("FontColor"       ) var FontColor       : String? = null,
    @SerialName("FontSize"        ) var FontSize        : String? = null,
    @SerialName("ysnHeader"       ) var ysnHeader       : String? = null,
    @SerialName("ysnFooter"       ) var ysnFooter       : String? = null,
    @SerialName("BGImage"         ) var BGImage         : String? = null,
    @SerialName("ScrollMessageAr" ) var ScrollMessageAr : String? = null,
    @SerialName("ScrollMessageEn" ) var ScrollMessageEn : String? = null,
    @SerialName("LogoImage"       ) var LogoImage       : String? = null
)

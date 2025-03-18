package com.example.qcounter.dataclass

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

data class DeviceConfiguration(
    @SerializedName("ButtonColor"     ) var ButtonColor     : String? = null,
    @SerializedName("FontType"        ) var FontType        : String? = null,
    @SerializedName("FontColor"       ) var FontColor       : String? = null,
    @SerializedName("FontSize"        ) var FontSize        : String? = null,
    @SerializedName("ysnHeader"       ) var ysnHeader       : String? = null,
    @SerializedName("ysnFooter"       ) var ysnFooter       : String? = null,
    @SerializedName("BGImage"         ) var BGImage         : String? = null,
    @SerializedName("ysnBGColor"      ) var ysnBGColor      : String? = null,
    @SerializedName("BGColor"         ) var BGColor         : String? = null,
    @SerializedName("ScrollMessageAr" ) var ScrollMessageAr : String? = null,
    @SerializedName("ScrollMessageEn" ) var ScrollMessageEn : String? = null,
    @SerializedName("LogoImage"       ) var LogoImage       : String? = null ,
    @SerializedName("EnableAds"       ) var EnableAds       : String? = null

)

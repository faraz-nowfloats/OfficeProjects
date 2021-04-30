package com.boost.presignin.model.other

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TANDetails(
        @SerializedName("DocumentFile")
        var documentFile: Any? = null,
        @SerializedName("DocumentName")
        var documentName: String? = null,
        @SerializedName("Number")
        var number: String? = null,
        @SerializedName("VerificationStatus")
        var verificationStatus: String? = null
): Serializable
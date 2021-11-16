package com.festive.poster.models

import com.google.gson.annotations.SerializedName

data class GetTemplateViewConfigResult(
    @SerializedName("templatePacks")
    val templatePacks: FestivePosterSectionModel
)
package com.boost.cart.data.api_model.GetAllFeatures.response

import com.google.gson.annotations.SerializedName

data class GetAllFeaturesResponse(
  val Data: List<Data>,
  val Extra: Extra
)
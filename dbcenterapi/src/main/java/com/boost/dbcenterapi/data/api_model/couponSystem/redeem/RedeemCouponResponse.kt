package com.boost.dbcenterapi.data.api_model.couponSystem.redeem

data class RedeemCouponResponse(
  val discountAmount: Double,
  val success: Boolean,
  val message: String
)
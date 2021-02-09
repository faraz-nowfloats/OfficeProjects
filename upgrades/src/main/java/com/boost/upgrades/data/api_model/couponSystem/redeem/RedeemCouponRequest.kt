package com.boost.upgrades.data.api_model.couponSystem.redeem

data class RedeemCouponRequest(
    val cartValue: Double,
    val couponCode: String,
    val fpId: String?
)
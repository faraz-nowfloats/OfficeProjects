package com.boost.upgrades.data.api_model.PaymentThroughEmail

data class PaymentPriorityEmailRequestBody(
  val ClientId: String,
  val Message: String,
  val Subject: String,
  val EmailIds: List<String>
)
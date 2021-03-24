package com.inventoryorder.rest

enum class TaskCode {
  GET_SELLER_SUMMARY,
  GET_LIST_ORDER,
  GET_LIST_ORDER_FILTER,
  GET_ASSURE_PURCHASE_ORDER,
  GET_LIST_CANCELLED_ORDER,
  GET_ORDER_DETAILS,
  GET_LIST_IN_COMPLETE_ORDER,
  CONFIRM_ORDER_TASK,
  SEND_LINK_ORDER_TASK,
  SEND_RE_BOOKING_ORDER_TASK,
  SEND_FEEDBACK_REQUEST,
  CANCEL_ORDER_TASK,
  DELIVERED_ORDER_TASK,
  MARK_PAYMENT_DONE_TASK,
  MARK_PAYMENT_MERCHANT_TASK,
  SHIPPED_ORDER_TASK,
  PRODUCT_ORDER_DETAILS_TASK,
  GET_ALL_SERVICES,
  GET_BIZ_FLOAT_MESSAGE,
  GET_ALL_PRODUCT_LIST,
  GET_USER_SUMMARY,
  GET_SUBSCRIBER_COUNT,
  GET_USER_MESSAGE_COUNT,
  GET_MAP_SUMMARY,
  GET_USER_CALL_SUMMARY,
  GET_DOCTOR_DATA,
  POST_ORDER_INITIATE,
  POST_ORDER_UPDATE,
  POST_ORDER_EXTRA_FILED_UPDATE,
  GET_DOCTOR_WEEKLY_SCHEDULE,
  GET_DOCTOR_API_DATA,
  ADD_API_CONSULT_DATA,
  UPDATE_API_CONSULT_DATA,
  SEND_SMS,
  SEND_MAIL,
  GET_SEARCH_LISTING
}
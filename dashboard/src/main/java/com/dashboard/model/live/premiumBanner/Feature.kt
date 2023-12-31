package com.dashboard.model.live.premiumBanner


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Feature(
  @SerializedName("boost_widget_key")
  var boostWidgetKey: String? = null,
  @SerializedName("createdon")
  var createdon: String? = null,
  @SerializedName("description")
  var description: String? = null,
  @SerializedName("description_title")
  var descriptionTitle: String? = null,
  @SerializedName("discount_percent")
  var discountPercent: Double? = null,
  @SerializedName("exclusive_to_categories")
  var exclusiveToCategories: ArrayList<String>? = null,
  @SerializedName("extended_properties")
  var extendedProperties: ArrayList<ExtendedProperty>? = null,
  @SerializedName("feature_banner")
  var featureBanner: FeatureBanner? = null,
  @SerializedName("feature_code")
  var featureCode: String? = null,
  @SerializedName("feature_importance")
  var featureImportance: Double? = null,
  @SerializedName("is_premium")
  var isPremium: Boolean? = null,
  @SerializedName("isarchived")
  var isarchived: Boolean? = null,
  @SerializedName("_kid")
  var kid: String? = null,
  @SerializedName("learn_more_link")
  var learnMoreLink: LearnMoreLink? = null,
  @SerializedName("name")
  var name: String? = null,
  @SerializedName("_parentClassId")
  var parentClassId: String? = null,
  @SerializedName("_parentClassName")
  var parentClassName: String? = null,
  @SerializedName("price")
  var price: Double? = null,
  @SerializedName("primary_image")
  var primaryImage: PrimaryImage? = null,
  @SerializedName("_propertyName")
  var propertyName: String? = null,
  @SerializedName("secondary_images")
  var secondaryImages: ArrayList<SecondaryImage>? = null,
  @SerializedName("target_business_usecase")
  var targetBusinessUsecase: String? = null,
  @SerializedName("time_to_activation")
  var timeToActivation: Double? = null,
  @SerializedName("total_installs")
  var totalInstalls: String? = null,
  @SerializedName("updatedon")
  var updatedon: String? = null,
  @SerializedName("usecase_importance")
  var usecaseImportance: String? = null,
  @SerializedName("websiteid")
  var websiteid: String? = null
) : Serializable
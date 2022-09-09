package com.boost.dbcenterapi.data.api_model.PurchaseResponse

data class PurchasedPackageDetails(
    val CreatedOn: String,
    val CurrencyCode: String,
    val Desc: Any,
    val ExpiryInMths: Double,
    val ExternalApplicationDetails: Any,
    val Identifier: String,
    val IsArchived: Boolean,
    val Name: String,
    val Price: Double,
    val PrimaryImageUri: Any,
    val Priority: Int,
    val Screenshots: Any,
    val SupportedPaymentMethods: Any,
    val Taxes: List<Taxe>,
    val Type: Int,
    val ValidCity: Any,
    val ValidCountry: List<String>,
    val ValidityInMths: Double,
    val WidgetPacks: List<WidgetPack>,
    val _id: Any,
    val maxDiscount: Any,
    val packageCategory: Any,
    val packageVisibilityType: Int,
    val productClassification: ProductClassification,
    val renewalChannelId: Any,
    val revenueShare: Any,
    val supportChannelId: Any,
    val unitVariants: List<UnitVariant>,
    val upSellChannelId: Any
)
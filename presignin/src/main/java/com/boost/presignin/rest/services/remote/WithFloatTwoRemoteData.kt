package com.boost.presignin.rest.services.remote

import com.boost.presignin.model.accessToken.AccessTokenRequest
import com.boost.presignin.model.authToken.AccessTokenResponse
import com.boost.presignin.model.fpList.FPListResponse
import com.boost.presignin.model.fpdetail.UserFpDetailsResponse
import com.boost.presignin.model.location.LocationResponse
import com.boost.presignin.model.login.*
import com.boost.presignin.model.onboardingRequest.CreateProfileRequest
import com.boost.presignin.model.userprofile.BusinessProfileResponse
import com.boost.presignin.model.userprofile.ConnectUserProfileResponse
import com.boost.presignin.model.userprofile.ResponseMobileIsRegistered
import com.boost.presignin.model.vertical_categories.Categories
import com.boost.presignin.rest.EndPoints
import com.framework.BuildConfig
import com.onboarding.nowfloats.model.googleAuth.FirebaseTokenResponse
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface WithFloatTwoRemoteData {


  @Headers("Content-Type: application/json")
  @POST(EndPoints.CREATE_MERCHANT_PROFILE)
  fun createUserProfile(@Body userProfileRequest: CreateProfileRequest?): Observable<Response<BusinessProfileResponse>>

  @GET(EndPoints.CHECK_MOBILE_IS_REGISTERED)
  fun checkIfMobileNumberIsRegistered(
    @Query(value = "mobileNumber") mobileNumber: Long?,
    @Query(value = "clientId") clientId: String?
  ): Observable<Response<ResponseMobileIsRegistered>>

  @Headers("Content-Type: application/json")
  @POST(EndPoints.CONNECT_MERCHANT_AUTH_PROVIDER)
  fun connectUserProfile(@Body userProfileRequest: CreateProfileRequest): Observable<Response<ConnectUserProfileResponse>>

  @Headers("Content-Type: application/json")
  @POST(EndPoints.VERIFY_LOGIN)
  fun verifyUserProfile(@Body userProfileVerificationRequest: UserProfileVerificationRequest): Observable<Response<VerificationRequestResult>>

  @Headers("Content-Type: application/json")
  @POST(EndPoints.VERTICAL_VERIFY_LOGIN)
  fun verifyUserProfileVertical(@Body userProfileVerificationRequest: UserProfileVerificationRequest): Observable<Response<VerificationRequestResultV3>>

  @Headers("Content-Type: application/json")
  @POST(EndPoints.FORGET_PASSWORD)
  fun forgotPassword(@Body request: ForgotPassRequest): Observable<Response<ResponseBody>>

  @GET(EndPoints.GET_FP_DETAILS_BY_PHONE)
  fun getFpDetailsByPhone(
    @Query("number") number: Long?,
    @Query(value = "clientId") clientId: String?
  ): Observable<Response<Any>>

  @GET(EndPoints.SEND_OTP_INDIA)
  fun sendOtpIndia(
    @Query("mobileNumber") number: Long?,
    @Query("messageTemplate") messageTemplate: String? = "Your one time Boost 360 verification code is [OTP]. The code is valid for 10 minutes, Please DO NOT share this code with anyone.#${BuildConfig.APP_SIGNATURE_HASH}",
    @Query(value = "clientId") clientId: String?
  ): Observable<Response<ResponseBody>>

  @GET(EndPoints.VERIFY_OTP)
  fun verifyOtp(
    @Query("mobileNumber") number: String?, @Query("otp") otp: String?,
    @Query(value = "clientId") clientId: String?,
  ): Observable<Response<ResponseBody>>

  @GET(EndPoints.VERIFY_LOGIN_OTP)
  fun verifyLoginOtp(
    @Query("mobileNumber") number: String?, @Query("otp") otp: String?,
    @Query(value = "clientId") clientId: String?,
  ): Observable<Response<VerifyOtpResponse>>

  @POST(EndPoints.CREATE_ACCESS_TOKEN)
  fun createAccessToken(
    @Body request: AccessTokenRequest?
  ): Observable<Response<AccessTokenResponse>>

  @GET(EndPoints.FP_LIST_REGISTERED_MOBILE)
  fun getFpListForRegisteredMobile(
    @Query("mobileNumber") number: String?,
    @Query(value = "clientId") clientId: String?
  ): Observable<Response<FPListResponse>>

  @GET(EndPoints.GET_FP_DETAILS)
  fun getFpDetails(
    @Path("fpid") fpid: String,
    @QueryMap map: Map<String, String>,
  ): Observable<Response<UserFpDetailsResponse>>

  @Headers("Content-Type: application/json", "Accept: application/json")
  @POST(EndPoints.REGISTER_CHANNEL)
  fun post_RegisterRia(@Body map: HashMap<String?, String?>?): Observable<Response<Unit>>

  @GET(EndPoints.GET_FIREBASE_TOKEN)
  fun getFirebaseToken(
    @Query("clientId") client_id: String?
  ):Observable<Response<FirebaseTokenResponse>>

  @Headers(
    "authority: api.whatismyip.com",
    "accept: */*",
    "accept-language: en-GB,en-US;q=0.9,en;q=0.8",
    "cache-control: no-cache",
    "origin: https://www.whatismyip.com",
    "pragma: no-cache",
    "referer: https://www.whatismyip.com/"
  )
  @GET
  fun getIPInfo(@Url url:String): Observable<Response<LocationResponse>>

  @GET(EndPoints.VERTICAL_VERIFY_LOGIN_OTP)
  fun verifyLoginOtpVertical(
    @Query("mobileNumber") number: String?, @Query("otp") otp: String?,
    @Query(value = "clientId") clientId: String?,
  ): Observable<Response<VerifyOtpResponse>>

  @GET(EndPoints.GET_VERTICAL_CATEGORIES)
  fun getVerticalCategories(
    @Path("appExperienceCode") appECode: String?
  ): Observable<Response<Categories>>
}

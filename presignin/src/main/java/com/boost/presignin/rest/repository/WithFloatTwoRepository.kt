package com.boost.presignin.rest.repository

import com.boost.presignin.base.rest.AppBaseLocalService
import com.boost.presignin.base.rest.AppBaseRepository
import com.boost.presignin.rest.TaskCode
import com.boost.presignin.rest.apiClients.WithFloatsApiTwoClient
import com.boost.presignin.rest.services.remote.WithFloatTwoRemoteData
import com.boost.presignin.rest.userprofile.UserProfileRequest
import com.boost.presignin.rest.userprofile.UserProfileVerificationRequest
import com.framework.base.BaseResponse
import io.reactivex.Observable
import retrofit2.Retrofit

object WithFloatTwoRepository : AppBaseRepository<WithFloatTwoRemoteData, AppBaseLocalService>() {

  fun isMobileIsRegistered(number: Long?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.checkIfMobileNumberIsRegistered(number), TaskCode.CHECK_MOBILE_IS_REGISTERED)
  }

    fun connectUserProfile(userProfileRequest: UserProfileRequest): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.connectUserProfile(userProfileRequest), TaskCode.CONNECT_USER_PROFILE)
    }

    fun createUserProfile(userProfileRequest: UserProfileRequest): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.createUserProfile(userProfileRequest), TaskCode.CREATE_USER_PROFILE)
    }

    fun getFpDetailsByPhone(number: Long?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.getFpDetailsByPhone(number), TaskCode.GET_FP_DETAILS_BY_PHONE)
    }

    fun sendOtpIndia(number: Long?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.sendOtpIndia(number), TaskCode.SEND_OTP_INDIA)
    }

    fun verifyOtp(number: String?, otp: String?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.verifyOtp(number, otp), TaskCode.VERIFY_OTP)
    }

    fun getFpListForRegisteredNumber(number: String?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.getFpListForRegisteredMobile(number), TaskCode.GET_FP_LIST_FOR_REGISTERED_NUMBER)
    }

    fun verifyUserProfile(
            userProfileVerificationRequest: UserProfileVerificationRequest,
    ): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.verifyUserProfile(userProfileVerificationRequest), TaskCode.VERIFY_USER_PROFILE)
    }


    override fun getRemoteDataSourceClass(): Class<WithFloatTwoRemoteData> {
        return WithFloatTwoRemoteData::class.java
  }

  override fun getLocalDataSourceInstance(): AppBaseLocalService {
    return AppBaseLocalService()
  }

  override fun getApiClient(): Retrofit {
    return WithFloatsApiTwoClient.shared.retrofit
  }


}

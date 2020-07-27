package com.appservice.base.rest

import com.appservice.rest.TaskCode
import com.appservice.rest.apiClients.WithFloatsApiTwoClient
import com.framework.base.BaseRepository
import com.framework.base.BaseResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.Retrofit

abstract class AppBaseRepository<RemoteDataSource, LocalDataSource : AppBaseLocalService> :
    BaseRepository<RemoteDataSource, LocalDataSource>() {

  protected fun <T> makeRemoteRequest(observable: Observable<Response<T>>, taskCode: TaskCode): Observable<BaseResponse> {
    return makeRemoteRequest(observable, taskCode.ordinal)
  }

  override fun getApiClient(): Retrofit {
    return WithFloatsApiTwoClient.shared.retrofit
  }

  fun makeLocalRequest(observable: Observable<BaseResponse>, taskCode: TaskCode): Observable<BaseResponse> {
    return makeLocalResponse(observable, taskCode.ordinal)
  }

  protected fun onFailure(response: BaseResponse, taskCode: TaskCode) {
    super.onFailure(response, taskCode.ordinal)
  }

  protected fun onSuccess(response: BaseResponse, taskCode: TaskCode) {
    super.onSuccess(response, taskCode.ordinal)
    localDataSource.saveToLocal(response, taskCode)
  }
}
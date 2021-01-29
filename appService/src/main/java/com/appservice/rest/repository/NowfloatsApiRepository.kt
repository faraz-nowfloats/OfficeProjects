package com.appservice.rest.repository

import com.appservice.base.rest.AppBaseLocalService
import com.appservice.base.rest.AppBaseRepository
import com.appservice.model.serviceProduct.Product
import com.appservice.model.serviceProduct.delete.DeleteProductRequest
import com.appservice.model.serviceProduct.update.ProductUpdate
import com.appservice.model.servicev1.DeleteSecondaryImageRequest
import com.appservice.model.servicev1.DeleteServiceRequest
import com.appservice.model.servicev1.ServiceModelV1
import com.appservice.model.servicev1.UploadImageRequest
import com.appservice.rest.EndPoints.DELETE_SERVICE
import com.appservice.rest.EndPoints.GET_SERVICE_DETAILS
import com.appservice.rest.TaskCode
import com.appservice.rest.apiClients.NowfloatsApiClient
import com.appservice.rest.apiClients.WithFloatsApiTwoClient
import com.appservice.rest.services.NowfloatsRemoteData
import com.appservice.rest.services.WithFloatTwoRemoteData
import com.appservice.ui.catalog.RequestWeeklyAppointment
import com.framework.base.BaseResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Retrofit

object NowfloatsApiRepository : AppBaseRepository<NowfloatsRemoteData, AppBaseLocalService>() {

    fun createService(request: ServiceModelV1?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.createService(request), TaskCode.POST_CREATE_SERVICE)
    }

    fun updateService(request: ServiceModelV1?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.updateService(request), TaskCode.POST_UPDATE_SERVICE)
    }

    fun addPrimaryImage(request: UploadImageRequest?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.addImage(request), TaskCode.ADD_SERVICE_PRIMARY_IMAGE_V1)
    }

    fun addSecondaryImage(request: UploadImageRequest?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.addImage(request), TaskCode.ADD_SERVICE_SECONDARY_IMAGE_V1)
    }

    fun getServiceDetail(serviceId: String?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.getServiceDetails(serviceId), TaskCode.GET_SERVICE_DETAILS)
    }

    fun deleteService(request: DeleteServiceRequest?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.deleteService(request), TaskCode.DELETE_SERVICE)
    }

    fun addUpdateImageProductService(
            clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
            productId: String?, requestBody: RequestBody?,
    ): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.addUpdateImageProductService(clientId, requestType, requestId, totalChunks,
                currentChunkNumber, productId, requestBody), TaskCode.ADD_UPDATE_IMAGE_PRODUCT_SERVICE)
    }

    fun getNotificationCount(clientId: String?, fpId: String?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.getNotificationCount(clientId, fpId), TaskCode.GET_NOTIFICATION)
    }

    override fun getRemoteDataSourceClass(): Class<NowfloatsRemoteData> {
        return NowfloatsRemoteData::class.java
    }

    override fun getLocalDataSourceInstance(): AppBaseLocalService {
        return AppBaseLocalService()
    }

    override fun getApiClient(): Retrofit {
        return NowfloatsApiClient.shared.retrofit
    }

    fun createProduct(request: Product?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.createProduct(request = request), TaskCode.POST_CREATE_PRODUCT)
    }
    fun updateProduct(request: ProductUpdate?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.updateProduct(request), TaskCode.POST_UPDATE_PRODUCT)
    }

    fun deleteProduct(request: DeleteProductRequest?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.deleteProduct(request), TaskCode.POST_UPDATE_PRODUCT)
    }

    fun addUpdateImageProduct(
            clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
            productId: String?, requestBody: RequestBody?,
    ): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.addUpdateImageProduct(clientId, requestType, requestId, totalChunks,
                currentChunkNumber, productId, requestBody), TaskCode.ADD_UPDATE_IMAGE_PRODUCT_SERVICE)
    }

    fun addServiceTiming(request: RequestWeeklyAppointment?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.addServiceTiming(request),TaskCode.GET_SERVICE_TIMING)

    }

    fun updateServiceTiming(request: RequestWeeklyAppointment?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.updateServiceTiming(request),TaskCode.GET_SERVICE_TIMING)

    }

    fun getServiceTiming(serviceId: String?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.getServiceTimings(serviceId),TaskCode.GET_SERVICE_TIMING)

    }

    fun deleteSecondaryImage(request: DeleteSecondaryImageRequest?): Observable<BaseResponse> {
        return makeRemoteRequest(remoteDataSource.deleteSecondaryImage(request), TaskCode.DELETE_SECONDARY_IMAGE)
    }
}

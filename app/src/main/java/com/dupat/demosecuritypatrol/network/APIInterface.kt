package com.dupat.demosecuritypatrol.network

import com.dupat.demosecuritypatrol.interceptor.TokenInterceptor
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LocationData
import com.dupat.demosecuritypatrol.network.response.data.LoginData
import com.dupat.demosecuritypatrol.network.response.data.ReportData
import com.dupat.demosecuritypatrol.session.SharedPrefManager
import com.dupat.demosecuritypatrol.utils.MyApplication
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface APIInterface {

    @FormUrlEncoded
    @POST("api/login-user")
    @Headers("No-Auth: true")
    suspend fun userLogin(
        @Field("username") id:String,
        @Field("password") password: String
    ) : Response<WebResponse<LoginData>>

    @GET("api/zone/{id}")
    @Headers("No-Auth: true")
    suspend fun locationDetail(
        @Path("id") id: String
    ) : Response<WebResponse<LocationData>>

    @Multipart
    @POST("api/report")
    @Headers("No-Auth: true")
    suspend fun addReport(
            @Part photo: MultipartBody.Part,
            @Part("user_id") user_id: RequestBody,
            @Part("zone_id") zone_id: RequestBody,
            @Part("status") status: RequestBody,
            @Part("latitude") latitude: RequestBody,
            @Part("longitude") longitude: RequestBody,
            @Part("note") note: RequestBody
    ) : Response<WebResponse<ReportData>>
//
//    @FormUrlEncoded
//    @PUT("user/{user_id}")
//    suspend fun completeProfile(
//        @Path("user_id") user_id:String,
//        @Field("nama") nama: String,
//        @Field("email") email: String,
//        @Field("angkatan") angkatan: String,
//        @Field("jurusan_id") jurusan_id: Int
//    ) : Response<WebResponse<UserModel>>
//
//    @FormUrlEncoded
//    @PUT("user/{user_id}")
//    suspend fun changePassword(
//        @Path("user_id") user_id:String,
//        @Field("old_password") nama: String,
//        @Field("password") email: String
//    ) : Response<WebResponse<UserModel>>
//
//    @GET("cluster/jurusan/{jurusan_id}")
//    suspend fun getClusterByMajor(
//        @Path("jurusan_id") jurusan_id: Int
//    ) : Response<WebResponse<List<ClusterData>>>
//
//    @GET("jurusan")
//    suspend fun getAllMajor() : Response<WebResponse<List<MajorModel>>>

    companion object{
        operator fun invoke() : APIInterface{
            var client = OkHttpClient.Builder().apply {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                addInterceptor(TokenInterceptor(SharedPrefManager.getString(MyApplication.ctx!!,"token")))
            }.build()

            return Retrofit
                .Builder()
                .client(client)
                .baseUrl("http://dinopriyano.my.id/security_patrol_backend/public/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(APIInterface::class.java)
        }

    }
}
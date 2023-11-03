package com.chenliee.library.http

import com.chenliee.library.utils.OkHttpUtil
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 *@Author：chenliee
 *@Date：2023/10/30 17:07
 *Describe:
 */
class RetrofitManager private constructor() {
    private val retrofit: Retrofit

    init {
        val builder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addNetworkInterceptor(StethoInterceptor())

        // 创建Retrofit
        retrofit = Retrofit.Builder()
            .client(builder.build())
            .addCallAdapterFactory(
                RxJava3CallAdapterFactory.create()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(OkHttpUtil.getBaseUrl())
            .build()
    }

    companion object {
        private const val DEFAULT_TIME_OUT = 10
        private const val DEFAULT_READ_TIME_OUT = 10
        private const val DEFAULT_WRITE_TIME_OUT = 10
        private var instance: RetrofitManager? = null

        fun getInstance(): RetrofitManager {
            if (instance == null) {
                synchronized(RetrofitManager::class.java) {
                    if (instance == null) {
                        instance = RetrofitManager()
                    }
                }
            }
            return instance!!
        }
    }

    /**
     * 获取对应的Service
     * @param service Service 的 class
     * @param <T>
     * @return
    </T> */
    fun <T> create(service: Class<T>?): T? {
        return service?.let { retrofit.create(it) }
    }
}


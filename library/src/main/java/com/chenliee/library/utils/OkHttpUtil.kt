package com.chenliee.library.utils

/**
 *@Author：chenliee
 *@Date：2023/11/2 16:43
 *Describe:
 */
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.chenliee.library.BuildConfig
import com.chenliee.library.Global
import com.chenliee.library.event.Event
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Deprecated("弃用")
class OkHttpUtil private constructor() {

    private var client: OkHttpClient

    init {
        val unauthorizedInterceptor =
                Interceptor { chain ->
                    val request = chain.request()
                    val response =
                            chain.proceed(request)
                    if (response.message == "Unauthorized") {
                        Handler(Looper.getMainLooper()).post {
                            Event.eventBus.post(
                                Event.ReLoginEvent()
                            )
                        }
                    }
                    response
                }
        val builder = OkHttpClient.Builder()
        builder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer ${Global.token}"
                ) // 添加token到header中
                .build()
            chain.proceed(request)
        }
        builder.addInterceptor(unauthorizedInterceptor)
        builder.addNetworkInterceptor(StethoInterceptor())
        client = builder
            .retryOnConnectionFailure(true)
            .connectTimeout(
                6000,
                TimeUnit.SECONDS
            ) //连接超时
            .readTimeout(6000, TimeUnit.SECONDS) //读取超时
            .writeTimeout(6000, TimeUnit.SECONDS) //写超时
            .build()
    }

    companion object {
        private var BASE_URL: String = BuildConfig.APP_DOMAIN
        private var instance: OkHttpUtil? = null

        private fun setBaseUrl(env: String): String{
            return when(env){
                "dev" -> Global.devUrl
                "uat" -> Global.uatUrl
                "pro" -> Global.proUrl
                else -> ""
            }
        }

        fun getBaseUrl(): String {
            return BASE_URL
        }

        fun getInstance(): OkHttpUtil {
            if (instance == null) {
                synchronized(OkHttpUtil::class.java) {
                    if (instance == null) {
                        instance = OkHttpUtil()
                    }
                }
            }
            return instance!!
        }
    }

    private fun getAbsoluteUrl(relativeUrl: String): String {
        return BASE_URL + relativeUrl
    }

    private fun showErrorToast(
        message: String,
        context: Context,
        url: String,
        deferred: CompletableDeferred<String?>
    ) {
        deferred.cancel()
        Handler(Looper.getMainLooper()).post {
            ToastUtil.getInstance().showToast(
                context,
                "url：$url message: $message"
            )
        }
    }

    suspend fun get(
        url: String,
        params: HashMap<String, String>? = null,
        context: Context
    ): String? {
        val deferred = CompletableDeferred<String?>()
        var urlRequest = ""
        var newUrl: String
        if (params != null && params.isNotEmpty()) {
            val list = ArrayList(params.keys)
            newUrl = "$url?"
            for (key in params.keys) {
                urlRequest += if (list[params.size - 1].equals(
                        key
                    )
                ) {
                    key + "=" + params[key]
                } else {
                    key + "=" + params[key] + "&"
                }
            }
            newUrl += urlRequest
        } else newUrl = url
        val request = Request.Builder()
            .url(getAbsoluteUrl(newUrl))
            .build()
        Log.d("get请求", request.url.toString())
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    Log.d("getFail $url", e.toString())
                    showErrorToast(
                        message = e.toString(),
                        url = newUrl,
                        context = context,
                        deferred = deferred,
                    )
                }
                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    val responseData =
                            response.body?.string()
                    Log.d(
                        "get $url",
                        responseData.toString()
                    )

                    val jsonObject = Gson().fromJson(
                        responseData,
                        JsonObject::class.java
                    )
                    if (response.code == 200) {
                        val value =
                                jsonObject.get("success").asBoolean
                        if (value) {
                            deferred.complete(
                                responseData
                            )
                        } else {
                            showErrorToast(
                                message = jsonObject.get(
                                    "message"
                                ).asString,
                                url = newUrl,
                                context = context,
                                deferred = deferred,
                            )
                        }
                    } else {
                        showErrorToast(
                            message = jsonObject.get("message").asString,
                            url = newUrl,
                            context = context,
                            deferred = deferred,
                        )
                    }
                }
            })
        return try {
            deferred.await()
        } catch (e: CancellationException) {
            // 捕获自定义异常，返回null
            null
        }
    }

    suspend fun post(
        url: String,
        json: JSONObject? = null,
        context: Context
    ): String? {
        val deferred = CompletableDeferred<String?>()
        val mediaType =
                "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString()
            .toRequestBody(mediaType)
        val request = if (json == null) {
            Request.Builder()
                .url(getAbsoluteUrl(url))
                .post(
                    ByteArray(0).toRequestBody(
                        null
                    )
                )
                .build()
        } else {
            Request.Builder()
                .url(getAbsoluteUrl(url))
                .post(requestBody)
                .build()
        }
        Log.d("post請求", request.url.toString())
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    showErrorToast(
                        message = e.toString(),
                        url = url,
                        context = context,
                        deferred = deferred,
                    )
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    val responseData =
                            response.body?.string()
                    Log.d(
                        "post $url",
                        responseData.toString()
                    )
                    val jsonObject = Gson().fromJson(
                        responseData,
                        JsonObject::class.java
                    )
                    if (response.code == 200) {
                        val value =
                                jsonObject.get("success").asBoolean
                        if (value) {
                            deferred.complete(
                                responseData
                            )
                        } else {
                            showErrorToast(
                                message = jsonObject.get(
                                    "message"
                                ).asString,
                                url = url,
                                context = context,
                                deferred = deferred,
                            )
                        }
                    } else {
                        showErrorToast(
                            message = jsonObject.get("message").asString,
                            url = url,
                            context = context,
                            deferred = deferred,
                        )
                    }
                }
            })
        return try {
            deferred.await()
        } catch (e: CancellationException) {
            // 捕获自定义异常，返回null
            null
        }
    }

    suspend fun put(
        url: String,
        json: JSONObject,
        params: HashMap<String, String>? = null,
        context: Context
    ): String? {
        val deferred = CompletableDeferred<String?>()
        val mediaType =
                "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString()
            .toRequestBody(mediaType)
        var urlRequest = ""
        var newUrl = ""
        if (params != null) {
            if (params.size > 0) {
                val list = ArrayList(params.keys)
                newUrl = "$url?"
                for (key in params.keys) {
                    urlRequest += if (list[params.size - 1].equals(
                            key
                        )
                    ) {
                        key + "=" + params[key]
                    } else {
                        key + "=" + params[key] + "&"
                    }
                }
                newUrl += urlRequest
            }
        } else newUrl = url
        val request = Request.Builder()
            .url(getAbsoluteUrl(newUrl))
            .put(requestBody)
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    showErrorToast(
                        message = e.toString(),
                        url = newUrl,
                        context = context,
                        deferred = deferred,
                    )
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    val responseData =
                            response.body?.string()
                    val jsonObject = Gson().fromJson(
                        responseData,
                        JsonObject::class.java
                    )
                    if (response.code == 200) {
                        val value =
                                jsonObject.get("success").asBoolean
                        if (value) {
                            deferred.complete(
                                responseData
                            )
                        } else {
                            showErrorToast(
                                message = jsonObject.get(
                                    "message"
                                ).asString,
                                url = newUrl,
                                context = context,
                                deferred = deferred,
                            )
                        }
                    } else {
                        showErrorToast(
                            message = jsonObject.get("message").asString,
                            url = newUrl,
                            context = context,
                            deferred = deferred,
                        )
                    }

                }
            })
        return try {
            deferred.await()
        } catch (e: CancellationException) {
            // 捕获自定义异常，返回null
            null
        }
    }

    fun getJson(url: String): String {
        val request = Request.Builder()
            .url(getAbsoluteUrl(url))
            .build()

        client.newCall(request).execute()
            .use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                return response.body?.string() ?: ""
            }
    }
}
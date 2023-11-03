package com.chenliee.library.http

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.HttpException


/**
 *@Author：chenliee
 *@Date：2023/10/30 17:14
 *Describe:
 */

open class CommonObserve {
    /**
     *
     * @param observable
     * @param <T>
     * @return
    </T> */
    protected fun <T : Any> observe(
        observable: Observable<BaseResponse<T>>,
        hello: (arg: T) -> Unit
    ): Disposable {
        val commonLoad = CommonLoad<T>()
        return observable
            .onErrorResumeNext { throwable->
                if (throwable is HttpException) {
                    val response = throwable.response()
                    if (response != null && !response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = Gson().fromJson(
                            errorBody,
                            JsonObject::class.java
                        )
                        Observable.error(
                            ErrorException(
                            errorMessage.get("success").asBoolean,
                            errorMessage.get("message").asString)
                        )
                    } else {
                        Observable.error(throwable)
                    }
                } else {
                    Observable.error(throwable)
                }
            }
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                commonLoad.apply(it)
            }
            .subscribe(
                { onNext ->
                    hello(onNext)
                },
                { error ->
                    if (error is ErrorException) {
                        Log.e(
                            "ErrorException",
                            error.getErrorMessage()
                        )
                    } else {
                        Log.e("${error.cause}", error.message!!)
                    }
                }
            )
    }
}
package com.chenliee.library.http

import io.reactivex.rxjava3.functions.Function
/**
 *@Author：chenliee
 *@Date：2023/10/31 10:04
 *Describe:
 */

class CommonLoad<T : Any> :
    Function<BaseResponse<T>, T> {
    override fun apply(t: BaseResponse<T>): T {
        if (t.success != true) {
            throw ErrorException(
                t.success!!,
                t.message
            )
        }
        return t.data!!
    }

}

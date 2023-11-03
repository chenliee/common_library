package com.chenliee.library.http

/**
 *@Author：chenliee
 *@Date：2023/10/31 10:04
 *Describe:
 */

class BaseResponse<T> {
    var success : Boolean? = null
    var message: String? = null
    var data: T? = null
}
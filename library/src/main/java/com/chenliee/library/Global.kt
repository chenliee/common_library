package com.chenliee.library

/**
 *@Author：chenliee
 *@Date：2023/11/2 16:51
 *Describe:
 */
object Global {
    private var instance: Global? = null

    var mid = ""
    var pid = ""
    var uid = ""
    var token = ""
    var brand = ""
    var shopId = ""
    var pageSize = "10"
    var devUrl = ""
    var uatUrl = ""
    var proUrl = ""

    private fun getInstance(): Global {
        instance ?: synchronized(this) {
            instance ?: Global.also { instance = it }
        }
        return instance!!
    }

    fun initBaseUrl(
        devUrl: String,
        uatUrl: String,
        proUrl: String
    ): Global {
        Global.devUrl = devUrl
        Global.uatUrl = uatUrl
        Global.proUrl = proUrl
        getInstance()
        return this
    }

    fun initDistributor(
        mid: String,
        pid: String,
        brand: String?,
        shopId: String? = null
    ): Global {
        Global.pid = pid
        Global.mid = mid
        Global.brand = brand ?: ""
        Global.shopId = shopId ?: ""
        getInstance()
        return this
    }

    fun initToken(
        uid: String?,
        token: String?
    ): Global {
        Global.token = token ?: ""
        Global.uid = uid ?: ""
        getInstance()
        return this
    }

}
package com.chenliee.library.utils

/**
 *@Author：chenliee
 *@Date：2023/11/2 16:48
 *Describe:
 */
import android.content.Context
import android.widget.Toast

/**
 *@Author：chenliee
 *@Date：2023/9/14 13:50
 *Describe:
 */
class ToastUtil private constructor() {
    private var toast: Toast? = null

    companion object {
        private var instance: ToastUtil? = null

        fun getInstance(): ToastUtil {
            if (instance == null) {
                instance = ToastUtil()
            }
            return instance!!
        }
    }

    /**
     * 显示字符串
     * @param context
     * @param content
     */
    fun showToast(
        context: Context?,
        content: String?
    ) {
        if (toast == null) {
            toast = Toast.makeText(
                context,
                content,
                Toast.LENGTH_SHORT
            )
        } else {
            toast!!.setText(content)
        }
        toast!!.show()
    }

    /**
     * 显示非字符串
     * @param context
     * @param content
     */
    fun showToast(context: Context?, content: Int) {
        if (toast == null) {
            toast = Toast.makeText(
                context,
                content.toString() + "",
                Toast.LENGTH_SHORT
            )
        } else {
            toast!!.setText(content.toString() + "")
        }
        toast!!.show()
    }
}

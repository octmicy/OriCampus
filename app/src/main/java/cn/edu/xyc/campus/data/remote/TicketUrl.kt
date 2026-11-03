package cn.edu.xyc.campus.data.remote

import cn.edu.xyc.campus.data.model.ThirdApp

/** 按门户 openThirdPage 语义构造跳转地址：hrefType != 5 的应用拼接 ticket 免密登录 */
fun ThirdApp.ticketUrl(): String {
    val sep = if (href.contains("?")) "&" else "?"
    return if (hrefType == 5) href
    else href + sep + "ticket=" + SessionStore.token.orEmpty()
}
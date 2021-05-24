package data.remote.utils

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder().header("User-Agent", "SRC-Client/1.0.0").build()
        return chain.proceed(request)
    }
}

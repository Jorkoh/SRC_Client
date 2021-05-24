package data.remote.utils

import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.Response

class LoggerInterceptor : Interceptor {
    private val logger = KotlinLogging.logger {}

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder().url(originalRequest.url()).build()
        logger.debug { "REQUEST: ${request.url()}" }
        return chain.proceed(request)
    }
}

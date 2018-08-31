package com.breuninger.mobl.datadogEvents

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.fluent.Executor
import org.apache.http.client.fluent.Request.Post
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class DatadogEventRepository constructor(apiKey: String,
                                                 private val connectTimeout: Int     // in milliseconds
                                                 ,
                                                 private val socketTimeout: Int      // in milliseconds
                                                 ,
                                                 private val proxy: HttpHost?,
                                                 executor: Executor?,
                                                 private val useCompression: Boolean, private val enabled: Boolean, private val tags: List<String>) {
  private val seriesUrl: String
  private val executor: Executor

  init {
    this.seriesUrl = String.format("%s/events?api_key=%s", BASE_URL, apiKey)
    if (executor != null) {
      this.executor = executor
    } else {
      this.executor = Executor.newInstance()
    }
  }

  fun sendEvent(event: DatadogEvent): Optional<DatadogResponseDto> {
    if (enabled) {
      try {
        event.setTags(this.tags)
        val request = HttpRequest(this)
        return request.send(event)
      } catch (e: IOException) {
        if (log.isDebugEnabled()) {
          log.debug("Failure in sending event to datadog", e)
        } else {
          val msg = e.message
          log.warn(msg)
        }
      }

    }
    return Optional.empty()
  }


  class Builder {
    internal var apiKey: String = ""
    internal var connectTimeout = 5000
    internal var socketTimeout = 5000
    internal var proxy: HttpHost? = null
    internal var executor: Executor? = null
    internal var useCompression = false
    internal var isEnabled = false
    internal var tags: List<String> = listOf("")

    fun withApiKey(key: String): Builder {
      this.apiKey = key
      return this
    }

    fun withConnectTimeout(milliseconds: Int): Builder {
      this.connectTimeout = milliseconds
      return this
    }

    fun withSocketTimeout(milliseconds: Int): Builder {
      this.socketTimeout = milliseconds
      return this
    }

    fun withProxy(proxyHost: String, proxyPort: Int): Builder {
      this.proxy = HttpHost(proxyHost, proxyPort)
      return this
    }

    fun withExecutor(executor: Executor): Builder {
      this.executor = executor
      return this
    }

    fun withCompression(compression: Boolean): Builder {
      this.useCompression = compression
      return this
    }

    fun isEnabled(isEnabled: Boolean): Builder {
      this.isEnabled = isEnabled
      return this
    }

    fun withTags(tags: List<String>): Builder {
      this.tags = tags
      return this
    }

    fun build(): DatadogEventRepository {
      return DatadogEventRepository(apiKey,
        connectTimeout, socketTimeout, proxy, executor, useCompression, isEnabled, tags)
    }

    companion object {
      internal val gson = Gson()
    }
  }

  class HttpRequest constructor(private val eventRepository: DatadogEventRepository) {

    @Throws(IOException::class)
    fun send(event: DatadogEvent): Optional<DatadogResponseDto> {
      val postBody = event.deflate()
      log.debug("Sending HTTP POST request to {}, uncompressed POST body length is: {}",
        this.eventRepository.seriesUrl, postBody.length)

      log.debug("Uncompressed POST body is: \n{}", postBody)

      val request = Post(eventRepository.seriesUrl)
        .useExpectContinue()
        .connectTimeout(this.eventRepository.connectTimeout)
        .socketTimeout(this.eventRepository.socketTimeout)

      if (this.eventRepository.useCompression) {
        request
          .addHeader("Content-Encoding", "deflate")
          .addHeader("Content-MD5", DigestUtils.md5Hex(postBody))
          .bodyString(postBody, ContentType.APPLICATION_JSON)
      } else {
        request.bodyString(postBody, ContentType.APPLICATION_JSON)
      }

      if (this.eventRepository.proxy != null) {
        request.viaProxy(this.eventRepository.proxy)
      }

      val response = this.eventRepository.executor.execute(request)

      if (log.isWarnEnabled()) {
        return response.handleResponse(DatadogResponseHandler())
      } else {
        response.discardContent()
      }
      return Optional.empty()
    }
  }

  private class DatadogResponseHandler : ResponseHandler<Optional<DatadogResponseDto>> {

    @Throws(IOException::class)
    override fun handleResponse(response: HttpResponse): Optional<DatadogResponseDto> {
      val statusCode = response.statusLine.statusCode
      if (statusCode >= 400) {
        log.warn(getLogMessage("Failure sending event to Datadog: ", response))
      } else {
        if (log.isDebugEnabled()) {
          log.debug(getLogMessage("Sent event to Datadog: ", response))
        }
        return Optional.of(
          Builder.gson.fromJson(JsonReader(
            InputStreamReader(
              response.entity.content, UTF_8)), DatadogResponseDto::class.java))
      }
      return Optional.empty()
    }

    @Throws(IOException::class)
    private fun getLogMessage(headline: String, response: HttpResponse): String {
      val sb = StringBuilder()

      sb.append(headline)
      sb.append('\n')
      sb.append("  Status: ").append(response.statusLine.statusCode).append('\n')

      val content = EntityUtils.toString(response.entity, "UTF-8")
      sb.append("  Content: ").append(content)
      return sb.toString()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DatadogEventRepository::class.java)
    private val BASE_URL = "https://app.datadoghq.com/api/v1"
  }
}

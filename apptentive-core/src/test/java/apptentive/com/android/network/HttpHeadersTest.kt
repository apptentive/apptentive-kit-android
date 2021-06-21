package apptentive.com.android.network

import apptentive.com.android.network.HttpHeaders.Companion.ACCEPT_ENCODING
import apptentive.com.android.network.HttpHeaders.Companion.CONTENT_LENGTH
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HttpHeadersTest {
    @Test
    fun setSetValues() {
        val headers = MutableHttpHeaders()
        headers[ACCEPT_ENCODING] = "application/json"
        headers[CONTENT_LENGTH] = "1000"
        for (entry in headers) {
            when (entry.name) {
                ACCEPT_ENCODING -> assertThat(entry.value).isEqualTo("application/json")
                CONTENT_LENGTH -> assertThat(entry.value).isEqualTo("1000")
            }
        }
    }

    @Test
    fun equality() {
        val headers1 = MutableHttpHeaders().apply {
            this["key"] = "value"
        }
        val headers2 = MutableHttpHeaders().apply {
            this["key"] = "value"
        }
        assertThat(headers1).isEqualTo(headers2)
    }
}

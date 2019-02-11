package apptentive.com.android.network

import apptentive.com.android.network.HttpHeaders.Companion.ACCEPT_ENCODING
import apptentive.com.android.network.HttpHeaders.Companion.CONTENT_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpHeadersTest {
    @Test
    fun setSetValues() {
        val headers = MutableHttpHeaders()
        headers[ACCEPT_ENCODING] = "application/json"
        headers[CONTENT_LENGTH] = "1000"
        for (entry in headers) {
            when (entry.name) {
                ACCEPT_ENCODING -> assertEquals(entry.value, "application/json")
                CONTENT_LENGTH -> assertEquals(entry.value, "1000")
            }
        }

    }
}
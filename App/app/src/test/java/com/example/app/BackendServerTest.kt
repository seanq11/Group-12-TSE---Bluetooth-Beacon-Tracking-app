package com.example.app

import android.content.Context
import okhttp3.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Test for backend ////////////////////////////////////////////////////////////////////////////////
class BackendServerTest {

    // variables used in testing
    private lateinit var backendServer   : BackendServer
    @Mock private lateinit var call      : Call
    @Mock private lateinit var context   : Context
    @Mock private lateinit var httpClient: OkHttpClient
    @Mock private lateinit var response  : Response

    // setup of test before test methods are called
    @Before fun setUp() {
        MockitoAnnotations.openMocks(this)
        backendServer = BackendServer()
        backendServer.constructor(context)
        backendServer.client = httpClient
    }

    // jsonstring test
    @Test fun testMakeJSONStr() {
        val expected = JSONObject().apply {
            put("time", System.currentTimeMillis())
            put("x", 10.0)
            put("y", 20.0)
        }.toString()
        val result = backendServer.makeJSONStr(10.0, 20.0)
        assertEquals(expected, result)
    }

    // test to see if the report location command is successful
    @Test fun testReportLocationSuccessful() {
        val xTestPos = 10.0
        val yTestPos = 20.0

        val requestBody = RequestBody.create(
            MediaType.parse("application/json"),
            backendServer.makeJSONStr(xTestPos, yTestPos)
        )

        val request = Request.Builder().apply{
            url("")
            post(requestBody)
        }.build()

        `when`(httpClient.newCall(request)).thenReturn(call)
        `when`(response.isSuccessful).thenReturn(true)

        backendServer.reportLocation(xTestPos, yTestPos)
    }

    // test command to see if the command is not successful
    @Test fun testReportLocationFailure() {
        val x = 10.0
        val y = 20.0

        val requestBody = RequestBody.create(
            MediaType.parse("application/json"),
            backendServer.makeJSONStr(x, y)
        )

        val request = Request.Builder().url("").post(requestBody).build()

        `when`(httpClient.newCall(request)).thenReturn(call)
        `when`(response.isSuccessful).thenReturn(false)
        `when`(response.toString()).thenReturn("Error")
        backendServer.reportLocation(x, y)
    }
}
package com.example

import com.example.data.api.SmartOsmApiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartOsmApiConfigTest {
    @Test
    fun referenceApiContract_isConfigured() {
        assertTrue(SmartOsmApiConfig.DATA_BASE_URL.startsWith("https://"))
        assertTrue(SmartOsmApiConfig.AUTH_BASE_URL.startsWith("https://"))
        assertEquals("api/exchange/osm/auth/login", SmartOsmApiConfig.LOGIN_PATH)
        assertEquals("api/exchange/auth/token/refreshAccessToken", SmartOsmApiConfig.REFRESH_TOKEN_PATH)
        assertEquals("households", SmartOsmApiConfig.HOUSEHOLDS_PATH)
        assertEquals("mosquito-larvae", SmartOsmApiConfig.MOSQUITO_LARVAE_PATH)
        assertEquals("reports", SmartOsmApiConfig.REPORTS_PATH)
        assertEquals("ncd-screenings", SmartOsmApiConfig.NCD_SCREENINGS_PATH)
    }
}

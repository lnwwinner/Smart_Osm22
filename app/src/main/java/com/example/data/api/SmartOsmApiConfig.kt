package com.example.data.api

/**
 * API contract extracted from the reference Smart OSM APK (v1.0.8).
 * No credentials are embedded here.
 */
object SmartOsmApiConfig {
    const val DATA_BASE_URL = "https://api-smartosm.hss.moph.go.th/api/v1/"
    const val AUTH_BASE_URL = "https://gw1.hss.moph.go.th/"

    const val LOGIN_PATH = "api/exchange/osm/auth/login"
    const val REFRESH_TOKEN_PATH = "api/exchange/auth/token/refreshAccessToken"

    const val HOUSEHOLDS_PATH = "households"
    const val MOSQUITO_LARVAE_PATH = "mosquito-larvae"
    const val REPORTS_PATH = "reports"
    const val NCD_SCREENINGS_PATH = "ncd-screenings"
    const val LOOKUPS_PATH = "lookups"
    const val NOTIFICATIONS_PATH = "notifications"

    const val ACCESS_TOKEN_FIELD = "access_token"
    const val REFRESH_TOKEN_FIELD = "refresh_token"
    const val EXPIRES_IN_FIELD = "expires_in"
}

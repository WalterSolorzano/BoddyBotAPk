package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "place_id") val placeId: Long? = null,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "lat") val lat: String,
    @Json(name = "lon") val lon: String
)

@JsonClass(generateAdapter = true)
data class GoogleGeocodeResponse(
    val results: List<GoogleGeocodeResult>,
    val status: String
)

@JsonClass(generateAdapter = true)
data class GoogleGeocodeResult(
    @Json(name = "formatted_address") val formattedAddress: String,
    val geometry: GoogleGeometry
)

@JsonClass(generateAdapter = true)
data class GoogleGeometry(
    val location: GoogleLocation
)

@JsonClass(generateAdapter = true)
data class GoogleLocation(
    val lat: Double,
    val lng: Double
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeather? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val is_day: Int,
    val time: String
)

interface GeocodingApi {
    @GET("search")
    suspend fun searchNominatim(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Header("User-Agent") userAgent: String = "UniBuddyStudentApp/1.0"
    ): List<GeocodingResult>

    @GET
    suspend fun searchGoogle(
        @Url url: String,
        @Query("address") address: String,
        @Query("key") key: String
    ): GoogleGeocodeResponse

    @GET
    suspend fun fetchWeatherUrl(
        @Url url: String,
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true
    ): OpenMeteoResponse
}

object GeocodingServiceClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: GeocodingApi = retrofit.create(GeocodingApi::class.java)

    /**
     * Searches for a location by text query.
     * Integrates Google Geocoding API if key is present; falls back to live free-lookup if not.
     */
    suspend fun searchLocation(query: String, googleMapsApiKey: String? = null): List<GeocodingResult> {
        if (query.isBlank()) return emptyList()
        return try {
            if (!googleMapsApiKey.isNullOrBlank()) {
                val response = api.searchGoogle(
                    url = "https://maps.googleapis.com/maps/api/geocode/json",
                    address = query,
                    key = googleMapsApiKey
                )
                if (response.status == "OK") {
                    response.results.map {
                        GeocodingResult(
                            placeId = null,
                            displayName = it.formattedAddress,
                            lat = it.geometry.location.lat.toString(),
                            lon = it.geometry.location.lng.toString()
                        )
                    }
                } else {
                    // Fallback to Nominatim on non-OK Google response
                    api.searchNominatim(query = query)
                }
            } else {
                api.searchNominatim(query = query)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Fetches current weather from free Open-Meteo API
     */
    suspend fun fetchCurrentWeather(lat: Double, lon: Double): OpenMeteoResponse? {
        return try {
            api.fetchWeatherUrl("https://api.open-meteo.com/v1/forecast", lat, lon, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

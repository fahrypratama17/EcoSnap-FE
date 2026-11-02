package com.example.ecosnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.navigation.EcoSnapNavHost
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil
import java.io.File

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    MapLibre.getInstance(applicationContext)

    val httpClient = OkHttpClient.Builder()
      .cache(
        Cache(
          File(cacheDir, "ecosnap_map_http"),
          50L * 1024L * 1024L
        )
      )
      .addInterceptor { chain ->
        val request = chain.request()

        val updateRequest =
          if (request.url.host == "tile.openstreetmap.org") {
            request.newBuilder()
              .header(
                "User-Agent",
                "EcoSnap/0.1 (Android; com.example.ecosnap)"
              )
              .build()
          } else {
            request
          }

        chain.proceed(updateRequest)
      }
      .build()

    HttpRequestUtil.setOkHttpClient(httpClient)

    setContent {
      EcosnapTheme(
        darkTheme = false,
        dynamicColor = false
      ) {
        EcoSnapNavHost()
      }
    }
  }
}

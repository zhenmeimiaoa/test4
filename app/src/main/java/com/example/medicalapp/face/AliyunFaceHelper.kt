package com.example.medicalapp.face

import android.graphics.Bitmap
import com.aliyun.facebody20191230.Client
import com.aliyun.facebody20191230.models.CompareFaceAdvanceRequest
import com.aliyun.teaopenapi.models.Config
import com.aliyun.teautil.models.RuntimeOptions
import com.example.medicalapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AliyunFaceHelper {
    
    private val client: Client by lazy {
        val accessKeyId = BuildConfig.ALIYUN_ACCESS_KEY_ID
        val accessKeySecret = BuildConfig.ALIYUN_ACCESS_KEY_SECRET
        
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            throw IllegalStateException("Aliyun credentials not configured in local.properties")
        }
        
        val config = Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
        config.endpoint = "facebody.cn-shanghai.aliyuncs.com"
        Client(config)
    }
    
    suspend fun compareFaces(idCardBitmap: Bitmap, cameraBitmap: Bitmap): Pair<Double, String> {
        return withContext(Dispatchers.IO) {
            try {
                val streamA = bitmapToInputStream(idCardBitmap)
                val streamB = bitmapToInputStream(cameraBitmap)
                
                val request = CompareFaceAdvanceRequest()
                    .setImageURLAObject(streamA)
                    .setImageURLBObject(streamB)
                
                val runtime = RuntimeOptions()
                val response = client.compareFaceAdvance(request, runtime)
                
                val body = response.body
                if (body != null && body.data != null) {
                    val confidence = body.data.confidence ?: 0.0
                    Pair(confidence, "Success")
                } else {
                    Pair(0.0, "API Error: Empty response")
                }
                
            } catch (e: Exception) {
                Pair(0.0, "Exception: ${e.message}")
            }
        }
    }
    
    private fun bitmapToInputStream(bitmap: Bitmap): ByteArrayInputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
    
    fun close() {}
}

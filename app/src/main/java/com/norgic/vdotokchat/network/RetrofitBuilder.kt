package com.norgic.vdotokchat.network

import android.content.Context
import com.norgic.vdotokchat.utils.ApplicationConstants.API_BASE_URL
import com.norgic.vdotokchat.utils.ApplicationConstants.SDK_AUTH_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


/**
 * Created By: Norgic
 * Date & Time: On 5/5/21 At 1:00 PM in 2021
 */
object RetrofitBuilder {

    fun makeRetrofitService(context: Context): ApiService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        clientBuilder.addInterceptor(interceptor)
        setSSLCert(context, clientBuilder)

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }



    private fun setSSLCert(context: Context, httpClient: OkHttpClient.Builder) {
        // Load CAs from an InputStream
        val certificateFactory: CertificateFactory?
        try {
            certificateFactory = CertificateFactory.getInstance("X.509")
            val am = context.assets
            val inputStream = am.open(getCertificateFileName())
            val certificate = certificateFactory.generateCertificate(inputStream)
            inputStream.close()
            // Create a KeyStore containing our trusted CAs
            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", certificate)

            // Create a TrustManager that trusts the CAs in our KeyStore.
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            val x509TrustManager = trustManagers[0] as X509TrustManager


            // Create an SSLSocketFactory that uses our TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(x509TrustManager), null)
            httpClient.sslSocketFactory(sslContext.socketFactory, x509TrustManager)
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
    }

    private fun getCertificateFileName(): String {
        return "vdotok_com.crt"
    }

}
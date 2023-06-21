package com.vdotok.chat.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class GlideModule : AppGlideModule() {

//    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        // Glide default Bitmap Format is set to RGB_565 since it
//        // consumed just 50% memory footprint compared to ARGB_8888.
//        // Increase memory usage for quality with:
//        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
//    }
//
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        super.registerComponents(context, glide, registry)
//        val okHttpClient = OkHttpClient()
//        registry.replace(GlideUrl::class.java, InputStream::class.java,
//            OkHttpUrlLoader.Factory(okHttpClient)
//        )
//    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val okHttpClient: OkHttpClient? = UnsafeOkHttpClient.getUnsafeOkHttpClient()
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient!!)
        )
        glide.registry.imageHeaderParsers.removeAll(object :
            MutableCollection<ExifInterfaceImageHeaderParser?> {
            override val size: Int
                get() = 0

            override fun contains(element: ExifInterfaceImageHeaderParser?): Boolean {
                return false
            }

            override fun containsAll(elements: Collection<ExifInterfaceImageHeaderParser?>): Boolean {
                return false
            }

            override fun isEmpty(): Boolean {
                return false
            }

            override fun add(element: ExifInterfaceImageHeaderParser?): Boolean {
                return false
            }

            override fun addAll(elements: Collection<ExifInterfaceImageHeaderParser?>): Boolean {
                return false
            }

            override fun clear() {

            }

            override fun iterator(): MutableIterator<ExifInterfaceImageHeaderParser?> {
                TODO("Implement something here!")
            }

            override fun remove(element: ExifInterfaceImageHeaderParser?): Boolean {
                return false
            }

            override fun removeAll(elements: Collection<ExifInterfaceImageHeaderParser?>): Boolean {
                return false
            }

            override fun retainAll(elements: Collection<ExifInterfaceImageHeaderParser?>): Boolean {
                return false
            }

        })
    }
}
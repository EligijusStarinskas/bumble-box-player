package com.eliga.bbox

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*

/**
 * Created by eliga on 21/02/2017.
 */

class ExoPlayerHelper {

    companion object {

        internal val BANDWIDTH_METER = DefaultBandwidthMeter()
        internal val DEFAULT_COOKIE_MANAGER: CookieManager = CookieManager()

        init {
            DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        }

        fun buildMediaSource(
                context: Context, uri: Uri, mediaDataSourceFactory: DataSource.Factory, mainHandler: Handler,
                overrideExtension: String? = null): MediaSource {
            val type = Util.inferContentType(if (!TextUtils.isEmpty(overrideExtension))
                "." + overrideExtension
            else
                uri.lastPathSegment)
            when (type) {
                C.TYPE_SS -> return SsMediaSource(uri, buildDataSourceFactory(context, false),
                        DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null /* eventLogger */)
                C.TYPE_DASH -> return DashMediaSource(uri, buildDataSourceFactory(context, false),
                        DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null /* eventLogger */)
                C.TYPE_HLS -> return HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null /* eventLogger */)
                C.TYPE_OTHER -> return ExtractorMediaSource(uri, mediaDataSourceFactory, DefaultExtractorsFactory(),
                        mainHandler, null /* eventLogger */)
                else -> {
                    throw IllegalStateException("Unsupported type: " + type)
                }
            }
        }

        @Throws(UnsupportedDrmException::class)
        fun buildDrmSessionManager(
                context: Context, uuid: UUID, licenseUrl: String, keyRequestProperties: Map<String, String>?,
                mainHandler: Handler): DrmSessionManager<FrameworkMediaCrypto>? {
            if (Util.SDK_INT < 18) {
                return null
            }

            val drmCallback = HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(context, false), keyRequestProperties)
            return DefaultDrmSessionManager(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler,
                    null /* eventLogger */)
        }

        internal fun buildHttpDataSourceFactory(
                context: Context, bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
            return DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Toro"), bandwidthMeter)
        }

        internal fun buildDataSourceFactory(
                context: Context, bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
            return DefaultDataSourceFactory(context, bandwidthMeter,
                    buildHttpDataSourceFactory(context, bandwidthMeter))
        }

        internal fun buildHttpDataSourceFactory(
                context: Context, useBandwidthMeter: Boolean): HttpDataSource.Factory {
            return buildHttpDataSourceFactory(context, if (useBandwidthMeter)
                BANDWIDTH_METER
            else
                null)
        }

        internal fun buildDataSourceFactory(context: Context, useBandwidthMeter: Boolean): DataSource.Factory {
            return buildDataSourceFactory(context, if (useBandwidthMeter)
                BANDWIDTH_METER
            else
                null)
        }
    }
}

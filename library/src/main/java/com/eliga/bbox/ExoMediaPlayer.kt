package com.eliga.bbox

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.eliga.bboxplayer.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.drm.UnsupportedDrmException
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataRenderer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import java.util.*


class ExoMediaPlayer(private val context: Context) : ExoPlayer.EventListener, MetadataRenderer.Output {
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private val mainHandler = Handler()
    private var mediaSource: MediaSource? = null

    private lateinit var player: SimpleExoPlayer

    private fun initializePlayer() {
        val drmSchemeUuid = if (mediaSource is DrmMedia) getDrmUuid((mediaSource as DrmMedia).type) else null
        var drmSessionManager: DrmSessionManager<FrameworkMediaCrypto>? = null
        if (drmSchemeUuid != null) {
            val drmLicenseUrl = (mediaSource as DrmMedia).licenseUrl
            val keyRequestPropertiesArray = (mediaSource as DrmMedia).keyRequestPropertiesArray
            val keyRequestProperties: Map<String, String>?
            if (keyRequestPropertiesArray.size < 2) {
                keyRequestProperties = null
            } else {
                keyRequestProperties = HashMap()
                var i = 0
                while (i < keyRequestPropertiesArray.size - 1) {
                    keyRequestProperties.put(keyRequestPropertiesArray[i],
                            keyRequestPropertiesArray[i + 1])
                    i += 2
                }
            }

            try {
                drmSessionManager = ExoPlayerHelper.buildDrmSessionManager(context, drmSchemeUuid, drmLicenseUrl,
                        keyRequestProperties, mainHandler)
            } catch (e: UnsupportedDrmException) {
                val errorStringId = if (Util.SDK_INT < 18)
                    R.string.error_drm_not_supported
                else
                    if (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME)
                        R.string.error_drm_unsupported_scheme
                    else
                        R.string.error_drm_unknown
                Toast.makeText(context, errorStringId, Toast.LENGTH_SHORT).show()
            }

        }

        val videoTrackSelectionFactory = AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, DefaultLoadControl(), drmSessionManager, SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF)
        player.addListener(this)
        player.setMetadataOutput(this)
        player.playWhenReady = true
        player.prepare(mediaSource, false, false)
    }

    fun releasePlayer() {
        player.removeListener(this)
        player.release()
        mediaSource = null
    }

    @Throws(ParserException::class)
    fun setMedia(media: Media) {
        setMedia(media, ExoPlayerHelper.buildDataSourceFactory(context, true))
    }

    @Throws(ParserException::class)
    fun setMedia(media: Media, mediaDataSourceFactory: DataSource.Factory) {
        val mediaSource = ExoPlayerHelper.buildMediaSource(context, media.mediaUri, mediaDataSourceFactory, mainHandler)
        setMediaSource(mediaSource)
    }

    @Throws(ParserException::class)
    fun setMediaSource(source: MediaSource?) {
        if (source == null || source == this.mediaSource) { // including null
            return
        }

        this.mediaSource = source
        initializePlayer()
    }

    @Throws(ParserException::class)
    private fun getDrmUuid(typeString: String): UUID {
        when (typeString.toLowerCase()) {
            "widevine" -> return C.WIDEVINE_UUID
            "playready" -> return C.PLAYREADY_UUID
            else -> try {
                return UUID.fromString(typeString)
            } catch (e: RuntimeException) {
                throw ParserException("Unsupported drm type: " + typeString)
            }

        }
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity() {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    }

    override fun onMetadata(metadata: Metadata) {
        Log.v("metadata", metadata.toString())
    }
}

package com.example.m3u8research

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.m3u8research.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val url = intent.getStringExtra(URL_EXTRA).orEmpty()
        val type = intent.getStringExtra(TYPE_EXTRA).orEmpty()
        initPlayer(url, type)
    }

    @SuppressLint("StaticFieldLeak")
    private fun initPlayer(url: String, type: String) {
        player = ExoPlayer.Builder(this).build()
        if (type == getString(R.string.youtube)) {
            object : YouTubeExtractor(this@PlayerActivity) {
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    videoMeta: VideoMeta?
                ) {
                    val iTag = 137 // tag of video 1080
                    val audioTag = 140 // tag m4a audio
                    // 720, 1080, 480
                    var videoUrl = ""
                    val iTags = listOf(22, 37, 18)
                    for (i in iTags) {
                        val ytFile = ytFiles?.get(i)
                        if (ytFile != null) {
                            val downloadUrl = ytFile.url
                            if (downloadUrl != null && downloadUrl.isNotEmpty()) {
                                videoUrl = downloadUrl
                            }
                        }
                    }

                    Log.i(TAG, "onExtractionComplete: $videoUrl")

                    if (videoUrl.isBlank()) {
                        videoUrl = ytFiles?.get(iTag)?.url.orEmpty()
                    }

                    val audioUrl = ytFiles?.get(audioTag)?.url.orEmpty()
                    val audioSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(audioUrl))

                    val videoSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(videoUrl))

                    player.setMediaSource(
                        MergingMediaSource(
                            true,
                            videoSource,
                            audioSource
                        ),
                        true
                    )
                }
            }.extract(url)
        } else {
            val mediaSource = createMediaSource(url, type)
            player.setMediaSource(mediaSource)
        }
        binding.mainPlayer.player = player
        player.prepare()
        player.playWhenReady = true
    }

    private fun createMediaSource(url: String, type: String): MediaSource {
        val factory = DefaultHttpDataSource.Factory().apply {
            setReadTimeoutMs(15 * 60 * 1000)
        }

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .build()

        Log.i(TAG, "createMediaSource: URL is $url")
        Log.i(TAG, "createMediaSource: Type is $type must be ${getString(R.string.file_mp4)}")

        when (type) {
            getString(R.string.file_mp4) -> {
                return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
            }

            getString(R.string.file_m3u8) -> {
                return HlsMediaSource.Factory(factory).createMediaSource(mediaItem)
            }
        }

        return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::player.isInitialized) {
            player.stop()
            player.release()
        }
    }

    companion object {
        const val URL_EXTRA = "url"
        const val TYPE_EXTRA = "type"
        private val TAG = PlayerActivity::class.simpleName
    }
}
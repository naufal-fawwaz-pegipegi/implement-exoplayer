package com.example.m3u8research

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.m3u8research.databinding.ActivityFullscreenBinding
import com.example.m3u8research.fragments.VideoFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class FullscreenActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var binding: ActivityFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(this.window, false)
        WindowInsetsControllerCompat(this.window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val position = intent.getLongExtra("extra_position", 0)
        val url = intent.getStringExtra("extra_url").toString()
        val type = intent.getStringExtra("extra_type").toString()
        val isPlaying = intent.getBooleanExtra("extra_playing", false)

        binding.mainPlayer.findViewById<ImageFilterView>(R.id.exo_fullscreen).isVisible = false
        binding.mainPlayer.findViewById<ImageFilterView>(R.id.exo_minimal_fullscreen).isVisible = true
        binding.mainPlayer.findViewById<ImageFilterView>(R.id.exo_minimal_fullscreen).setOnClickListener {
            val intent = Intent()
            intent.putExtra("extra_position", player?.currentPosition ?: 0L)
            intent.putExtra("extra_playing", player?.isPlaying)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        initPlayer(url, type, position, isPlaying)
    }

    @SuppressLint("StaticFieldLeak")
    private fun initPlayer(url: String, type: String, position: Long, isPlaying: Boolean) {
        player = ExoPlayer.Builder(this).build()
        if (type == getString(R.string.youtube)) {
            object : YouTubeExtractor(this) {
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

                    player?.setMediaSource(
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
            player?.setMediaSource(mediaSource)
        }

        binding.mainPlayer.player = player
        player?.prepare()
        player?.seekTo(position)
        player?.playWhenReady = isPlaying
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
            VideoFragment.TYPE_MP4 -> {
                return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
            }

            VideoFragment.TYPE_M3U8 -> {
                return HlsMediaSource.Factory(factory).createMediaSource(mediaItem)
            }
        }

        return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
    }

    override fun onDestroy() {
        super.onDestroy()

        player?.stop()
        player?.release()
    }

    companion object {
        private val TAG = FullscreenActivity::class.simpleName
    }
}
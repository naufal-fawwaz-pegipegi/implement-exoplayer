package com.example.m3u8research

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.m3u8research.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class PlayerActivity : AppCompatActivity() {

    private var binding: ActivityPlayerBinding? = null

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val url = intent.getStringExtra(URL_EXTRA).orEmpty()

        Log.i(PlayerActivity::class.java.simpleName, "onCreate: $url")

        val mediaSource = createMediaSource(url)
        player = ExoPlayer.Builder(this).build()
        player.setMediaSource(mediaSource)

        binding?.mainPlayer?.player = player

        player.prepare()
        player.seekTo(0)
    }

    private fun createMediaSource(url: String): MediaSource {
        val factory = DefaultHttpDataSource.Factory()
        factory.setUserAgent("default-user-agent")

        return HlsMediaSource.Factory(factory).createMediaSource(
            MediaItem.fromUri(
                Uri.parse(url)
            )
        )
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
    }
}
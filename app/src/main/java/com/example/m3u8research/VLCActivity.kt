package com.example.m3u8research

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.m3u8research.databinding.ActivityVlcBinding
import com.example.m3u8research.fragments.OnConfigurationChanged
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.lang.Exception

class VLCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVlcBinding

    private var mediaPlayer: MediaPlayer? = null
    private var libVlc: LibVLC? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVlcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val url = intent.getStringExtra(EXTRA_URL)

        initPlayer(url)
    }

    private fun initPlayer(url: String?) {
        libVlc = LibVLC(this, arrayListOf<String?>().apply {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("--rtsp-tcp")
            add("-vvv")
        })

        mediaPlayer = MediaPlayer(libVlc)
        mediaPlayer?.attachViews(binding.layoutVideo, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)

        try {
            Media(libVlc, Uri.parse(url)).apply {
                setHWDecoderEnabled(true, false)
                addOption(":network-caching=150")
                addOption(":clock-jitter=0")
                addOption(":clock-synchro=0")
                mediaPlayer?.media = this
            }.release()

            mediaPlayer?.play()
        } catch (e: Exception) {
            Log.e(TAG, "initPlayer: ${e.localizedMessage}")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume called")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart called")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop called")
        mediaPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called")
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVlc?.release()
    }

    companion object {
        private val TAG = VLCActivity::class.simpleName
        private const val ENABLE_SUBTITLES = false
        private const val USE_TEXTURE_VIEW = false
        const val EXTRA_URL = "url_extra"
    }
}
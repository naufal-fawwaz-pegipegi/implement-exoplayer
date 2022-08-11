package com.example.m3u8research

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.example.m3u8research.databinding.ActivityMainBinding
import com.example.m3u8research.databinding.ActivityPlayerPlainBinding
import java.lang.Exception

class PlayerPlainActivity : AppCompatActivity(), SurfaceHolder.Callback, MediaPlayer.OnVideoSizeChangedListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {

    private lateinit var binding: ActivityPlayerPlainBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var holder: SurfaceHolder
    private var videoWidth = 0
    private var videoHeight = 0

    private var isSizeKnown = false
    private var isVideoReadyToPlayed = false

    private val path = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerPlainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        holder = binding.playerView.holder
        holder.addCallback(this)
    }

    private fun playVideo() {
        doCleanUp()
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.setDisplay(holder)
            mediaPlayer.setOnBufferingUpdateListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "playVideo: ${e.localizedMessage}")
        }
    }

    private fun doCleanUp() {
        videoWidth = 0
        videoHeight = 0
        isSizeKnown = false
        isVideoReadyToPlayed = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated called")
        playVideo()
    }

    override fun surfaceChanged(holder: SurfaceHolder, i: Int, j: Int, k: Int) {
        Log.d(TAG, "surfaceChanged called")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed called")
    }

    override fun onVideoSizeChanged(player: MediaPlayer?, width: Int, height: Int) {
        Log.d(TAG, "onVideoSizeChanged called")
        if (width == 0 || height == 0) {
            Log.e(TAG, "onVideoSizeChanged: width($width) or height($height)")
            return
        }

        isSizeKnown = true
        videoHeight = height
        videoWidth = width

        if (isVideoReadyToPlayed) {
            startVideoPlayback()
        }
    }

    private fun startVideoPlayback() {
        Log.i(TAG, "startVideoPlayback called")
        holder.setFixedSize(videoWidth, videoHeight)
        mediaPlayer.start()
    }

    override fun onBufferingUpdate(player: MediaPlayer?, percentage: Int) {
        Log.d(TAG, "onBufferingUpdate: $percentage")
    }

    override fun onPrepared(player: MediaPlayer?) {
        Log.d(TAG, "onPrepared called")
        isVideoReadyToPlayed = true
        if (isVideoReadyToPlayed) {
            startVideoPlayback()
        }
    }

    override fun onCompletion(player: MediaPlayer?) {
        Log.d(TAG, "onCompletion called")
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
        doCleanUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        doCleanUp()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer.release()
    }

    companion object {
        private val TAG = PlayerPlainActivity::class.simpleName
    }
}
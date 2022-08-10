package com.example.m3u8research

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.m3u8research.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val defaultUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
//    private val defaultAnotherUrl = "http://media.developer.dolby.com/DolbyVision_Atmos/profile5_HLS/master.m3u8"
//    private val defaultAnotherUrl = "http://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8"
//    private val defaultAnotherUrl = "https://dacastmmd.mmdlive.lldns.net/dacastmmd/e448bec06c5c49bbbe82bdeb170fc32f/manifest.m3u8?p=79&h=3e2e034290e07bc2798b02ba004d7566"
    private val defaultAnotherUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.defaultButton?.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.URL_EXTRA, defaultUrl)
            startActivity(intent)
        }

        binding?.defaultAnotherButton?.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.URL_EXTRA, defaultAnotherUrl)
            startActivity(intent)
        }

        binding?.submitButton?.setOnClickListener {
            val url = binding?.videoUrlTextEdit?.text.toString()

            if (url.isNotBlank()) {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.URL_EXTRA, url)
                startActivity(intent)
            } else {
                binding?.let {
                    Snackbar.make(it.root, "Please enter the URL", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
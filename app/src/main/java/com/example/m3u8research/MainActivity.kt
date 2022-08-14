package com.example.m3u8research

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.m3u8research.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val defaultM3u8 = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
    private val defaultMp4 = "http://techslides.com/demos/sample-videos/small.mp4"
    private val defaultYoutube = "https://www.youtube.com/watch?v=2x1eaCUtcMM"

    private lateinit var types: List<String>
    private var urlDefault = defaultMp4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupData()
        setContentView(binding.root)
        setupDropdown()
        setOnClickListener()
    }

    private fun setupData() {
        types = listOf(
            getString(R.string.file_m3u8),
            getString(R.string.file_mp4),
            getString(R.string.youtube)
        )
    }

    private fun setOnClickListener() {
        binding.defaultPlayerButton.setOnClickListener {
            val url = binding.videoUrlTextEdit.text.toString()

            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TYPE_EXTRA, binding.typeTextEdit.text.toString())
            intent.putExtra(PlayerActivity.URL_EXTRA, url.ifBlank { urlDefault })
            startActivity(intent)
        }

        binding.viewPagerButton.setOnClickListener {
            val url = binding.videoUrlTextEdit.text.toString()

            val intent = Intent(this, ViewPagerPlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TYPE_EXTRA, binding.typeTextEdit.text.toString())
            intent.putExtra(PlayerActivity.URL_EXTRA, url.ifBlank { urlDefault })
            startActivity(intent)
        }

        binding.nativePlayerButton.setOnClickListener {
            val url = binding.videoUrlTextEdit.text.toString()

            val intent = Intent(this, PlayerPlainActivity::class.java)
            intent.putExtra(PlayerActivity.TYPE_EXTRA, binding.typeTextEdit.text.toString())
            intent.putExtra(PlayerActivity.URL_EXTRA, url.ifBlank { urlDefault })
            startActivity(intent)
        }

        binding.vlcPlayerButton.setOnClickListener {
            val url = binding.videoUrlTextEdit.text.toString()

            val intent = Intent(this, VLCActivity::class.java)
            intent.putExtra(VLCActivity.EXTRA_URL, url.ifBlank { urlDefault })
            startActivity(intent)
        }
    }



    private fun setupDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        binding.typeTextEdit.setAdapter(adapter)

        binding.typeTextEdit.addTextChangedListener {
            if (binding.videoUrlTextEdit.text.toString().isBlank()) {
                when (binding.typeTextEdit.text.toString()) {
                    getString(R.string.file_mp4) -> {
                        urlDefault = defaultMp4
                    }

                    getString(R.string.file_m3u8) -> {
                        urlDefault = defaultM3u8
                    }

                    getString(R.string.youtube) -> {
                        urlDefault = defaultYoutube
                    }
                }
            }
        }
    }
}
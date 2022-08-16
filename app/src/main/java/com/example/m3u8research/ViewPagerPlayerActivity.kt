package com.example.m3u8research

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.m3u8research.databinding.ActivityViewPagerPlayerBinding
import com.example.m3u8research.fragments.OnConfigurationChanged
import com.example.m3u8research.fragments.VideoFragment
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerPlayerActivity : AppCompatActivity(), OnConfigurationChanged {

    private lateinit var binding: ActivityViewPagerPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPagerPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = VideoPagerAdapter(this)
        binding.pager.adapter = pagerAdapter

        TabLayoutMediator(binding.indicatorTabLayout, binding.pager) { _, _ -> }.attach()
    }

    override fun changeToLandscape() {
        binding.indicatorTabLayout.isVisible = false
        supportActionBar?.hide()
        binding.pager.isUserInputEnabled = false
    }

    override fun changeToPortrait() {
        binding.indicatorTabLayout.isVisible = true
        supportActionBar?.show()
        binding.pager.isUserInputEnabled = true
    }
}

class VideoPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    private val defaultM3u8 = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
    private val defaultMp4 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    private val defaultYoutube = "https://www.youtube.com/watch?v=2x1eaCUtcMM"

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                VideoFragment.newInstance(defaultMp4, VideoFragment.TYPE_MP4)
            }

            1 -> {
                VideoFragment.newInstance(defaultM3u8, VideoFragment.TYPE_M3U8)
            }

            else -> {
                VideoFragment.newInstance(defaultMp4, VideoFragment.TYPE_MP4)
            }
        }
    }
}
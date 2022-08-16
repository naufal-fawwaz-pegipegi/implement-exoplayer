package com.example.m3u8research.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.m3u8research.FullscreenActivity
import com.example.m3u8research.R
import com.example.m3u8research.databinding.FragmentVideo1Binding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.button.MaterialButton
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class VideoFragment : Fragment() {

    private lateinit var binding: FragmentVideo1Binding
    private lateinit var player: ExoPlayer
    private var isInFullscreen = false
    private var onConfigurationChanged: OnConfigurationChanged? = null

    private val defaultArtwork = "https://via.placeholder.com/300/09f/fff.png"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideo1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            onConfigurationChanged = context as OnConfigurationChanged
        } catch (e: Exception) {
            Toast.makeText(context, "Error ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private val fullscreenActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val position = it.data?.getLongExtra("extra_position", 0L) ?: 0L
                val isPlaying = it.data?.getBooleanExtra("extra_playing", false) ?: false
                player.seekTo(position)
                if (isPlaying) {
                    player.play()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(URL_EXTRA).orEmpty()
        val type = arguments?.getString(TYPE_EXTRA).orEmpty()
        setArtwork()
        initPlayer(url, type)

        val fullscreenButton = binding.mainPlayer.findViewById<ImageFilterView>(R.id.exo_fullscreen)
        val hideFullscreenButton =
            binding.mainPlayer.findViewById<ImageFilterView>(R.id.exo_minimal_fullscreen)

        fullscreenButton.setOnClickListener {
            val intent = Intent(requireContext(), FullscreenActivity::class.java)
            intent.putExtra("extra_url", url)
            intent.putExtra("extra_type", type)
            intent.putExtra("extra_position", player.currentPosition)
            intent.putExtra("extra_playing", player.isPlaying)
            fullscreenActivityResult.launch(intent)

//            it.isVisible = false
//            hideFullscreenButton.isVisible = true
//            isInFullscreen = true
//            setFullscreen()
        }

        hideFullscreenButton.setOnClickListener {
//            it.isVisible = false
//            fullscreenButton.isVisible = true
//            isInFullscreen = false
//            setFullscreen()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isInFullscreen) {
                        hideFullscreenButton.isVisible = false
                        fullscreenButton.isVisible = true
                        isInFullscreen = false
                        setFullscreen()
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            })
    }

    private fun setArtwork() {
//        Glide.with(requireContext())
//            .asBitmap()
//            .load(defaultArtwork)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    val drawable = BitmapDrawable(requireContext().resources, resource)
//                    binding.mainPlayer.defaultArtwork = drawable
//                    binding.mainPlayer.useArtwork = true
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // No-ops
//                }
//            })

//        Glide.with(requireContext())
//            .asBitmap()
//            .load(defaultArtwork)
//            .transition(BitmapTransitionOptions.withCrossFade())
//            .into(binding.thumbnailVideo)
    }

    private fun getDrawableFromUrl(): Drawable {
        try {
            val urlObj = URL(defaultArtwork)
            val connection = urlObj.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            return BitmapDrawable(requireContext().resources, bitmap)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun setFullscreen() {
        if (isInFullscreen) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
            WindowInsetsControllerCompat(requireActivity().window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            val params = binding.mainPlayer.layoutParams as ConstraintLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.mainPlayer.layoutParams = params
            onConfigurationChanged?.changeToLandscape()
        } else {
            WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
            WindowInsetsControllerCompat(requireActivity().window, binding.root).show(
                WindowInsetsCompat.Type.systemBars()
            )
            val params = binding.mainPlayer.layoutParams as ConstraintLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = (250 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
            binding.mainPlayer.layoutParams = params
            onConfigurationChanged?.changeToPortrait()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun initPlayer(url: String, type: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        if (type == getString(R.string.youtube)) {
            object : YouTubeExtractor(requireContext()) {
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
        player.seekTo(0)
        player.playWhenReady = false
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
            TYPE_MP4 -> {
                return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
            }

            TYPE_M3U8 -> {
                return HlsMediaSource.Factory(factory).createMediaSource(mediaItem)
            }
        }

        return ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
    }

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) {
            player.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView")

        if (::player.isInitialized) {
            player.stop()
            player.release()
        }
    }

    companion object {
        fun newInstance(url: String, type: String): VideoFragment {
            val bundle = Bundle()
            bundle.putString(URL_EXTRA, url)
            bundle.putString(TYPE_EXTRA, type)
            val fragment = VideoFragment()
            fragment.arguments = bundle
            return fragment
        }

        private const val URL_EXTRA = "url"
        private const val TYPE_EXTRA = "type"

        const val TYPE_MP4 = "mp4"
        const val TYPE_M3U8 = "m3u8"
        private val TAG = VideoFragment::class.simpleName
    }
}
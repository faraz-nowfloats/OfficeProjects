package com.boost.presignin.ui.intro

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.core.view.isVisible
import com.boost.presignin.R
import com.boost.presignin.base.AppBaseFragment
import com.boost.presignin.databinding.FragmentPreSigninIntroBinding
import com.boost.presignin.model.IntroItem
import com.framework.models.BaseViewModel

class PreSigninIntroFragment : AppBaseFragment<FragmentPreSigninIntroBinding, BaseViewModel>() {
    var videoDuration: Int = 0
    var timer: CountDownTimer? = null;
    private var mediaPlayer: MediaPlayer? = null;
    private var mute = false;


    var onSkip: (() -> Unit)? = null

    var playPause: ((b: Boolean) -> Unit)? = null

    private val TAG = "IntroFragment"

    private var buffering = true;

    companion object {
        private var INTRO_ITEM = "INTRO_ITEM"
        private var POSITION = "POSITION"

        @JvmStatic
        fun newInstance(introItem: IntroItem, position: Int) =
                PreSigninIntroFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(INTRO_ITEM, introItem)
                        putInt(POSITION, position)
                    }
                }
    }

    private val introItem by lazy { requireArguments().getSerializable(INTRO_ITEM) as IntroItem }
    private val position by lazy { requireArguments().getInt(POSITION) }
    override fun getLayout(): Int {
        return R.layout.fragment_pre_signin_intro
    }

    override fun onCreateView() {
        super.onCreateView()
        binding?.introItem = introItem;
        binding?.presiginIntroImg?.setImageResource(introItem.imageResource)

        if (position == 0) {
            binding?.firstScreenLogoContainer?.isVisible = true;
            binding?.playPauseLottie?.setAnimation(R.raw.play_pause_lottie);
            binding?.presiginIntroImg?.setOnClickListener {
                playPause?.let { it5 -> it5(true) }
                binding?.introImgContainer?.isVisible = false;
                binding?.videoViewContainer?.isVisible = true;
                binding?.progressBar?.isVisible = true
                binding?.videoView?.setOnPreparedListener {
                    mediaPlayer = it;
                    videoDuration = mediaPlayer?.duration ?: 0

                }
                binding?.videoView?.setVideoPath("https://cdn.withfloats.com/boost/videos/en/intro.mp4")
                binding?.videoView?.start();
                binding?.videoView?.setOnInfoListener { p0, p1, p2 ->
                    when (p1) {
                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                            Log.d(TAG, "onCreateView: MEDIA_INFO_BUFFERING_START")
                            binding?.progressBar?.isVisible = true
                            buffering = true

                        }

                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                            Log.d(TAG, "onCreateView: MEDIA_INFO_BUFFERING_END")
                            binding?.progressBar?.isVisible = false
                            binding?.videoViewContainer?.isVisible = true
                            buffering = false

                        }

                        MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                            Log.d(TAG, "onCreateView: MEDIA_INFO_VIDEO_RENDERING_START")
                            binding?.progressBar?.isVisible = false
                            binding?.videoViewContainer?.isVisible = true
                            //binding?.videoView?.isVisible = true
                            setVideoTimerCountDown();
                        }
                    }
                    true
                }
                binding?.videoViewContainer?.setOnClickListener {

                    if (binding!!.videoView.isPlaying) {
                        binding!!.videoView.pause();
                        binding?.playPauseLottie?.isVisible = true;
                        timer?.cancel()
                    }
                }
            }
            binding?.playPauseLottie?.setOnClickListener {
                binding?.videoView?.start();
                it.isVisible = false;
            }

        } else {
            binding?.introTextContainer?.isVisible = true;
        }

        binding?.muteVideo?.setOnClickListener {
            muteUnmute()
        }

        binding?.skipVideo?.setOnClickListener {
            onSkip?.let { it1 -> it1() };
        }
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onPause() {
        super.onPause()
        if (position == 0) {
            if (binding?.videoView?.isPlaying == true) {
                binding?.videoView?.pause()
                binding?.playPauseLottie?.isVisible = true;
                timer?.cancel()
            }
        }
    }


    private fun setVideoTimerCountDown() {
        val duration = mediaPlayer?.duration ?: 0
        val currentTime = mediaPlayer?.currentPosition ?: 0;
        timer = object : CountDownTimer((duration - currentTime).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val videoDuration = (millisUntilFinished / 1000).toInt()
                if (videoDuration == 0) {
                    timer?.cancel();
                    binding?.videoTime?.text = String.format(getString(R.string.intro_video_time), 0)
                } else {
                    binding?.videoTime?.text = String.format(getString(R.string.intro_video_time), videoDuration)
                }
            }

            override fun onFinish() {
                Log.e("videoCompleted", "&&&&&&&&&&&&&")
            }
        }
        timer?.start()
    }


    override fun onStop() {
        super.onStop()
        binding?.videoView?.suspend()
    }


    private fun muteUnmute() {
        mute = !mute;
        val volume = if (mute) 0.0f else 1.0f
        mediaPlayer?.setVolume(volume, volume)
        binding?.muteIcon?.setImageResource(if (mute) R.drawable.ic_mute else R.drawable.ic_unmute)
    }

}
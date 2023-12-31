package com.boost.marketplace.ui.videos

import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.boost.dbcenterapi.upgradeDB.model.YoutubeVideoModel
import com.boost.marketplace.R
import com.boost.marketplace.adapter.VideosListAdapter
import com.boost.marketplace.databinding.BottomSheetVideosBinding
import com.boost.marketplace.interfaces.VideosListener
import com.boost.marketplace.ui.home.MarketPlaceHomeViewModel
import com.framework.base.BaseBottomSheetDialog
import com.framework.exoFullScreen.MediaPlayer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import kotlinx.android.synthetic.main.bottom_sheet_videos.*

class VideosPopUpBottomSheet :
    BaseBottomSheetDialog<BottomSheetVideosBinding, MarketPlaceHomeViewModel>(), VideosListener {

    lateinit var link: String
    private var videoItem: String? = null
    lateinit var videosListAdapter: VideosListAdapter
    lateinit var player: ExoPlayer

    override fun getLayout(): Int {
        return R.layout.bottom_sheet_videos
    }

    override fun getViewModelClass(): Class<MarketPlaceHomeViewModel> {
        return MarketPlaceHomeViewModel::class.java
    }

    override fun onCreateView() {
        dialog!!.window!!.setBackgroundDrawableResource(com.boost.cart.R.color.fullscreen_color)
        dialog.behavior.isDraggable = true
        viewModel = ViewModelProviders.of(baseActivity).get(MarketPlaceHomeViewModel::class.java)
        getBundle()
        initView()
        initMvvm()
    }

    private fun initView() {
    //    videosListAdapter = VideosListAdapter(ArrayList(), this)
        viewModel?.GetHelp()
        initializeVideosRecycler()
    }

    private fun initializeVideosRecycler() {
        val gridLayoutManager = LinearLayoutManager(context)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        videos_pop_up_recycler_view.apply {
            layoutManager = gridLayoutManager
            videos_pop_up_recycler_view.adapter = videosListAdapter
        }

    }

    private fun initMvvm() {
        viewModel?.getYoutubeVideoDetails()?.observe(this, androidx.lifecycle.Observer {
            Log.e("getYoutubeVideoDetails", it.toString())
            updateVideosViewPager(it)
        })
    }

    fun updateVideosViewPager(list: List<YoutubeVideoModel>) {
        val link: List<String> = list.get(0).youtube_link!!.split('/')
//        videoPlayerWebView.getSettings().setJavaScriptEnabled(true)
////    videoPlayerWebView.getSettings().setPluginState(WebSettings.PluginState.ON)
//        videoPlayerWebView.setWebViewClient(WebViewClient())
//        videoPlayerWebView.loadUrl("http://www.youtube.com/embed/" + link.get(link.size - 1) + "?autoplay=1&vq=small")
        videosListAdapter.addUpdates(list)
        videosListAdapter.notifyDataSetChanged()
//        video_sub_title.text = list.get(0).title
//        video_sub_desc.text = list.get(0).desc
    }

    private fun getBundle() {
        this.videoItem = requireArguments().getString("title") ?: ""
        link = requireArguments().getString("link") ?: ""
        binding?.ctvVideoTitle?.text = videoItem
        binding?.mainTxt?.text = videoItem
    }

    private fun initializePlayer() {
        MediaPlayer.initialize(baseActivity)
        player = MediaPlayer.exoPlayer!!
        binding?.playerView?.player = player
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(link)))
        player?.prepare()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
//        MediaPlayer.pausePlayer()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        if (::player.isInitialized && !player.isPlaying)
            player.play()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
//        MediaPlayer.stopPlayer()
        player.stop()
        player.release()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        MediaPlayer.stopPlayer()
        player.stop()
        player.release()
    }

    override fun onDetach() {
        super.onDetach()
//        MediaPlayer.stopPlayer()
        player.stop()
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
//        MediaPlayer.stopPlayer()
        player.stop()
        player.release()
    }

    override fun onPlayYouTubeVideo(videoItem: YoutubeVideoModel,position: Int) {
        Toast.makeText(context, "Loading", Toast.LENGTH_LONG).show()
        link = videoItem.youtube_link.toString()
        MediaPlayer.releasePlayer()
        initializePlayer()
        binding?.ctvVideoTitle?.text = videoItem.title
        binding?.mainTxt?.text = videoItem.desc

    }

    override fun dismiss() {
        super.dismiss()
//        MediaPlayer.stopPlayer()
        player.stop()
        player.release()

    }
}
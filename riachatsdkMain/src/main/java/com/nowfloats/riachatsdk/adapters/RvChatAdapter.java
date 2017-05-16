package com.nowfloats.riachatsdk.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.nowfloats.riachatsdk.CustomWidget.playpause.PlayPauseView;
import com.nowfloats.riachatsdk.R;
import com.nowfloats.riachatsdk.models.Section;
import com.nowfloats.riachatsdk.utils.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by NowFloats on 16-03-2017.
 */

public class RvChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum SectionTypeEnum
    {
        Header(-1), Image(0), Text(1), Graph(2), Gif(3), Audio(4), Video(5), Link(6), EmbeddedHtml(7), Carousel(8), Typing(9), Card(10);
        private final int val;
        private SectionTypeEnum(int val){
            this.val = val;
        }

        public int getValue(){
            return val;
        }
    }

    private List<Section> mChatSections;
    private Context mContext;

    public RvChatAdapter(List<Section> mChatSections, Context context) {
        this.mChatSections = mChatSections;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        Section section = mChatSections.get(position);
        return SectionTypeEnum.valueOf(section.getSectionType()).getValue();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case -1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chat_head, parent, false);
                return new HeaderViewHolder(v);
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_image_row_layout, parent, false);
                return new ImageViewHolder(v);
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_text_row_layout, parent, false);
                return new TextViewHolder(v);
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_graph_row_layout, parent, false);
                return new GraphViewholder(v);
            case 3:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_gif_row_layout, parent, false);
                return new GifViewHolder(v);
            case 4:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_audio_row_layout, parent, false);
                return new AudioViewHolder(v);
            case 5:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_video_row_layout, parent, false);
                return new VideoViewHolder(v);
            case 7:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_embedded_html_row_layout, parent, false);
                return new EmbeddedHtmlViewHolder(v);
            case 9:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_typing_row_layout, parent, false);
                return new TypingViewHolder(v);
            case 10:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card_row_layout, parent, false);
                return new CardViewHolder(v);
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_typing_row_layout, parent, false);
                return new TypingViewHolder(v);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Section section = mChatSections.get(position);
        if(holder instanceof  HeaderViewHolder){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM", Locale.US);
            headerViewHolder.tvDateTime.setText(format.format(new Date()));
        } else if(holder instanceof CardViewHolder){
            CardViewHolder cardViewHolder = (CardViewHolder) holder;
            cardViewHolder.tvConfirmationText.setText(Html.fromHtml(section.getText()));
            if(section.isShowDate()) {
                cardViewHolder.tvDateTime.setVisibility(View.VISIBLE);
                cardViewHolder.tvDateTime.setText(section.getDateTime());
            }else {
                cardViewHolder.tvDateTime.setVisibility(View.GONE);
            }
            ((LinearLayout) cardViewHolder.itemView).setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cardViewHolder.llBubbleContainer.getLayoutParams();
            if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                cardViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.reply_main_bubble);
                lp.setMargins(Utils.dpToPx(mContext, 60), 0, Utils.dpToPx(mContext, 5), 0);
            }else {
                cardViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.reply_followup_bubble);
                lp.setMargins(Utils.dpToPx(mContext, 60), 0, Utils.dpToPx(mContext, 15), 0);
            }
            cardViewHolder.llBubbleContainer.setLayoutParams(lp);
            cardViewHolder.tvConfirmationText.setTextColor(Color.parseColor("#ffffff"));

        } else if(holder instanceof TextViewHolder){
            TextViewHolder textViewHolder = (TextViewHolder) holder;
            textViewHolder.tvMessageText.setText(Html.fromHtml(section.getText()));
            if(section.isShowDate()) {
                textViewHolder.tvDateTime.setVisibility(View.VISIBLE);
                textViewHolder.tvDateTime.setText(section.getDateTime());
            }else {
                textViewHolder.tvDateTime.setVisibility(View.GONE);
            }
            if(!section.isFromRia()){
                ((LinearLayout) textViewHolder.itemView).setGravity(Gravity.RIGHT);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) textViewHolder.llBubbleContainer.getLayoutParams();
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                    textViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.reply_main_bubble);
                    lp.setMargins(Utils.dpToPx(mContext, 60), 0, Utils.dpToPx(mContext, 5), 0);
                }else {
                    textViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.reply_followup_bubble);
                    lp.setMargins(Utils.dpToPx(mContext, 60), 0, Utils.dpToPx(mContext, 15), 0);
                }

                textViewHolder.llBubbleContainer.setLayoutParams(lp);
                textViewHolder.tvMessageText.setTextColor(Color.parseColor("#ffffff"));
            }else {
                ((LinearLayout) textViewHolder.itemView).setGravity(Gravity.LEFT);
                textViewHolder.tvMessageText.setTextColor(Color.parseColor("#808080"));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) textViewHolder.llBubbleContainer.getLayoutParams();
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                    textViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.ria_followup_bubble);
                    lp.setMargins(Utils.dpToPx(mContext, 15), 0, Utils.dpToPx(mContext, 60), 0);
                }else {
                    textViewHolder.llBubbleContainer.setBackgroundResource(R.drawable.ria_main_bubble);
                    lp.setMargins(Utils.dpToPx(mContext, 5), 0, Utils.dpToPx(mContext, 60), 0);
                }


                textViewHolder.llBubbleContainer.setLayoutParams(lp);
            }
        }else if(holder instanceof ImageViewHolder){
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            if(section.isShowDate()) {
                imageViewHolder.tvDateTime.setVisibility(View.VISIBLE);
                imageViewHolder.tvDateTime.setText(section.getDateTime());
            }else {
                imageViewHolder.tvDateTime.setVisibility(View.GONE);
            }
            if(section.isLoading()){
                imageViewHolder.pbLoading.setVisibility(View.VISIBLE);
            }else {
                imageViewHolder.pbLoading.setVisibility(View.INVISIBLE);
            }
            if(section.getTitle()!=null && !section.getTitle().trim().equals("")) {
                imageViewHolder.tvImageTitle.setText(section.getTitle());
            }else {
                imageViewHolder.tvImageTitle.setVisibility(View.GONE);
            }
            Glide.with(mContext)
                    .load(section.getUrl())
                    .centerCrop()
                    .placeholder(R.drawable.default_product_image)
                    .into(imageViewHolder.ivMainImage);
            if(section.getCaption()!=null && !section.getCaption().trim().equals("")) {
                imageViewHolder.tvImageCaption.setText(section.getCaption());
            }else {
                imageViewHolder.tvImageCaption.setVisibility(View.GONE);
            }
            if(!section.isFromRia()){
                ((LinearLayout) imageViewHolder.itemView).setGravity(Gravity.RIGHT);
            }else {
                ((LinearLayout) imageViewHolder.itemView).setGravity(Gravity.LEFT);
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                }else {
                }
            }
        }else if(holder instanceof GifViewHolder){
            GifViewHolder imageViewHolder = (GifViewHolder) holder;
            if(section.isShowDate()) {
                imageViewHolder.tvDateTime.setVisibility(View.VISIBLE);
                imageViewHolder.tvDateTime.setText(section.getDateTime());
            }else {
                imageViewHolder.tvDateTime.setVisibility(View.GONE);
            }
            if(section.getTitle()!=null && !section.getTitle().trim().equals("")) {
                imageViewHolder.tvImageTitle.setText(section.getTitle());
            }else {
                imageViewHolder.tvImageTitle.setVisibility(View.GONE);
            }
            Glide.with(mContext)
                    .load(section.getUrl())
                    .asGif()
                    .centerCrop()
                    .placeholder(R.drawable.default_product_image)
                    .into(imageViewHolder.ivMainImage);
            if(section.getCaption()!=null && !section.getCaption().trim().equals("")) {
                imageViewHolder.tvImageCaption.setText(section.getCaption());
            }else {
                imageViewHolder.tvImageCaption.setVisibility(View.GONE);
            }
            if(!section.isFromRia()){
                ((LinearLayout) imageViewHolder.itemView).setGravity(Gravity.RIGHT);
            }else {
                ((LinearLayout) imageViewHolder.itemView).setGravity(Gravity.LEFT);
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                }else {
                }
            }
        }else if(holder instanceof EmbeddedHtmlViewHolder){
            EmbeddedHtmlViewHolder embeddedHtmlViewHolder = (EmbeddedHtmlViewHolder) holder;
            embeddedHtmlViewHolder.tvTitle.setText(section.getTitle());
            embeddedHtmlViewHolder.wvEmbeddedHtml.getSettings().setJavaScriptEnabled(true);
            embeddedHtmlViewHolder.wvEmbeddedHtml.loadUrl(section.getUrl());
            if(section.isDisplayOpenInBrowserButton()){
                embeddedHtmlViewHolder.tvOpenInBrowser.setVisibility(View.VISIBLE);
            }
            embeddedHtmlViewHolder.tvOpenInBrowser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Open in browser
                }
            });
            embeddedHtmlViewHolder.tvCaption.setText(section.getCaption());
            if(!section.isFromRia()){
                ((LinearLayout) embeddedHtmlViewHolder.itemView).setGravity(Gravity.RIGHT);
            }else {
                ((LinearLayout) embeddedHtmlViewHolder.itemView).setGravity(Gravity.LEFT);
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                }else {
                }
            }
        }else  if(holder instanceof VideoViewHolder){
            //TODO: set loading icon
            MediaController mediaControls = new MediaController(mContext);
            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
            videoViewHolder.tvTitle.setText(section.getTitle());

            if(section.isLoading()){
                videoViewHolder.pbVideoLoading.setVisibility(View.VISIBLE);
            }else {
                videoViewHolder.pbVideoLoading.setVisibility(View.INVISIBLE);
            }

            try{
                videoViewHolder.vvMainVideo.setMediaController(mediaControls);
                videoViewHolder.vvMainVideo.setVideoURI(Uri.parse(section.getUrl()));
            }catch (Exception e){
                e.printStackTrace();
            }
            if(!section.isFromRia()){
                ((LinearLayout) videoViewHolder.itemView).setGravity(Gravity.RIGHT);
            }else {
                ((LinearLayout) videoViewHolder.itemView).setGravity(Gravity.LEFT);
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                }else {
                }
            }
        }else if(holder instanceof AudioViewHolder){

            final AudioViewHolder audioViewHolder = (AudioViewHolder) holder;

            if(!section.isFromRia()){
                ((LinearLayout) audioViewHolder.itemView).setGravity(Gravity.RIGHT);
            }else {
                ((LinearLayout) audioViewHolder.itemView).setGravity(Gravity.LEFT);
                if(mChatSections!= null && mChatSections.size()>0 && position>0 && mChatSections.get(position-1).isFromRia()){
                }else {
                }
            }

            if(section.isLoading()){
                audioViewHolder.pbAudioLoading.setVisibility(View.VISIBLE);
                audioViewHolder.ppvAudio.setEnabled(false);
            }else {
                audioViewHolder.pbAudioLoading.setVisibility(View.GONE);
                audioViewHolder.ppvAudio.setEnabled(true);
            }

            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(section.getUrl());
                mediaPlayer.prepareAsync();
            }catch (IOException e){
                e.printStackTrace();
            }
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int mCurrentPosition = (mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100;
                    audioViewHolder.sbAudio.setProgress(mCurrentPosition);
                    handler.postDelayed(this, 1000);
                }
            };
            audioViewHolder.ppvAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        handler.removeCallbacks(null);
                    }else {
                        mediaPlayer.start();
                        handler.post(runnable);
                    }
                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return mChatSections.size();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder{

        TextView tvDateTime;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_date_time);
        }
    }


    private class ImageViewHolder extends RecyclerView.ViewHolder{

        TextView tvImageTitle, tvDateTime;
        ImageView ivMainImage;
        TextView tvImageCaption;
        ProgressBar pbLoading;


        public ImageViewHolder(View itemView) {
            super(itemView);

            tvImageTitle = (TextView) itemView.findViewById(R.id.tv_title);
            ivMainImage = (ImageView) itemView.findViewById(R.id.iv_main_row_image);
            tvImageCaption = (TextView) itemView.findViewById(R.id.tv_caption);
            pbLoading = (ProgressBar) itemView.findViewById(R.id.pb_loading);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_date_time);

        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder{

        TextView tvMessageText, tvDateTime;
        View itemView;
        LinearLayout llBubbleContainer;

        public TextViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            tvMessageText = (TextView) itemView.findViewById(R.id.tv_message_text);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_date_time);
            llBubbleContainer = (LinearLayout) itemView.findViewById(R.id.ll_bubble_container);
        }
    }

    private class CardViewHolder extends RecyclerView.ViewHolder{

        TextView tvConfirmationText, tvDateTime;
        View itemView;
        LinearLayout llBubbleContainer;

        public CardViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            this.tvConfirmationText = (TextView) itemView.findViewById(R.id.tv_confirmation_text);
            this.tvDateTime = (TextView) itemView.findViewById(R.id.tv_date_time);
            this.llBubbleContainer = (LinearLayout) itemView.findViewById(R.id.ll_bubble_container);
        }
    }

    private class GraphViewholder extends RecyclerView.ViewHolder{

        public GraphViewholder(View itemView) {
            super(itemView);
        }
    }

    private class GifViewHolder extends RecyclerView.ViewHolder{

        TextView tvImageTitle, tvDateTime;
        ImageView ivMainImage;
        TextView tvImageCaption;

        public GifViewHolder(View itemView) {
            super(itemView);

            tvImageTitle = (TextView) itemView.findViewById(R.id.tv_title);
            ivMainImage = (ImageView) itemView.findViewById(R.id.iv_main_row_image);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_date_time);
            tvImageCaption = (TextView) itemView.findViewById(R.id.tv_caption);
        }
    }

    private class AudioViewHolder extends RecyclerView.ViewHolder{
        PlayPauseView ppvAudio;
        SeekBar sbAudio;
        ProgressBar pbAudioLoading;

        public AudioViewHolder(View itemView) {
            super(itemView);

            ppvAudio = (PlayPauseView) itemView.findViewById(R.id.ppv_audio);
            sbAudio = (SeekBar) itemView.findViewById(R.id.sb_audio);
            pbAudioLoading = (ProgressBar) itemView.findViewById(R.id.pb_audio_loading);
            sbAudio.setEnabled(false);
        }
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        VideoView vvMainVideo;
        ProgressBar pbVideoLoading;

        public VideoViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            vvMainVideo = (VideoView) itemView.findViewById(R.id.vv_main_video);
            pbVideoLoading = (ProgressBar) itemView.findViewById(R.id.pb_video_loading);
        }
    }

    private class EmbeddedHtmlViewHolder extends RecyclerView.ViewHolder{

        WebView wvEmbeddedHtml;
        TextView tvTitle, tvOpenInBrowser, tvCaption;

        public EmbeddedHtmlViewHolder(View itemView) {
            super(itemView);

            wvEmbeddedHtml = (WebView) itemView.findViewById(R.id.wv_embedded_html);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvOpenInBrowser = (TextView) itemView.findViewById(R.id.tv_open_in_browser);
            tvCaption = (TextView) itemView.findViewById(R.id.tv_caption);

        }
    }

    private class TypingViewHolder extends RecyclerView.ViewHolder{

        ImageView ivTyping;

        public TypingViewHolder(View itemView) {
            super(itemView);
            ivTyping = (ImageView) itemView.findViewById(R.id.iv_typing_gif);
            Glide.with(mContext)
                    .load(R.drawable.typing)
                    .asGif()
                    .centerCrop()
                    .into(ivTyping);
            Section section = mChatSections.get(mChatSections.size()-1);
            if(!section.isFromRia()){
                ((LinearLayout) itemView).setGravity(Gravity.RIGHT);
            }else {
                if(mChatSections!= null && mChatSections.size()>0 && mChatSections.get(mChatSections.size()-1).isFromRia()){
                }
            }
        }
    }

}

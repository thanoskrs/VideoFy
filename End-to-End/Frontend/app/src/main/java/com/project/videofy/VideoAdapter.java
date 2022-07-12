package com.project.videofy;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import Video.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.videofy.databinding.*;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoFile> videos;
    private FragmentActivity activity;


    public VideoAdapter(List<VideoFile> videos, FragmentActivity activity) {
        this.videos = videos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_container, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        //holder.setVideoData(videos.get(position));

        holder.videoPublishedFrom.setText(videos.get(position).videoPublished);
        holder.videoHashtagsPublished.setText(videos.get(position).videoHashtags);
        holder.videoView.setVideoPath(videos.get(position).videoURL);


        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.videoProgressBar.setVisibility(View.GONE);
                holder.playVideoImageView.setVisibility(View.INVISIBLE);
                mp.start();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mp.isPlaying()) {
                            mp.pause();
                            holder.playVideoImageView.setVisibility(View.VISIBLE);
                        }
                        else {
                            mp.start();
                            holder.playVideoImageView.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                holder.playVideoImageView.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mp.isPlaying()) {
                            mp.pause();
                            holder.playVideoImageView.setVisibility(View.VISIBLE);
                        }
                        else {
                            mp.start();
                            holder.playVideoImageView.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        VideoView videoView;
        TextView videoPublishedFrom, videoHashtagsPublished;
        ProgressBar videoProgressBar;
        ImageView playVideoImageView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = (VideoView) itemView.findViewById(R.id.videoView);
            videoPublishedFrom = (TextView) itemView.findViewById(R.id.videoPublishedFromEditText);
            videoHashtagsPublished = (TextView) itemView.findViewById(R.id.videoHashtagsPublishedEditText);
            videoProgressBar = (ProgressBar) itemView.findViewById(R.id.videoProgressBar);
            playVideoImageView = (ImageView) itemView.findViewById(R.id.playVideoImageView);

        }

        void setVideoData(VideoFile videoItem) {

        }
    }
}

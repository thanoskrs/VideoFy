package com.project.videofy;
import Video.*;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.project.videofy.databinding.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.SystemClock.sleep;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreen extends Fragment {

    ProgressBar downloadVideosProgressBar;
    ViewPager2 videosViewPager;

    public HomeScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeScreen newInstance() {
        HomeScreen fragment = new HomeScreen();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videosViewPager = getActivity().findViewById(R.id.videosViewPager);
        downloadVideosProgressBar = getActivity().findViewById(R.id.downloadVideosProgressBar);
        videosViewPager.setVisibility(View.INVISIBLE);
        downloadVideosProgressBar.setVisibility(View.VISIBLE);

        GetVideos getVideos = new GetVideos();
        getVideos.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String [])null);

    }

    public void ScreenTouch() {

    }

    private class GetVideos extends AsyncTask<String, String, String> {

        private Socket consumerSocket;
        private ObjectOutputStream consumerOutputStream;
        private ObjectInputStream consumerInputStream;

        @Override
        protected String doInBackground(String... strings) {

            try {
                init(LogInActivity.MainServerIp, LogInActivity.MainServerPort);

                consumerOutputStream.writeUTF("Consumer");
                consumerOutputStream.flush();

                consumerOutputStream.writeUTF("ChannelName");
                consumerOutputStream.flush();

                LogInActivity.brokersPublishers = new HashMap<>((HashMap<String, ArrayList<String>>) consumerInputStream.readObject());

                disconnect();
                // if the only user connected is the consumer we don't give his information
                if (LogInActivity.brokersPublishers.size() == 0 || (LogInActivity.brokersPublishers.size() == 1 && LogInActivity.brokersPublishers.containsKey(LogInActivity.channelName.ChannelName))) {
                    Log.e("T", "No user connected.");

                } else {

                    for (String channelName : LogInActivity.brokersPublishers.keySet()) {

                        // check if we follow the publisher and if the publisher is not us
                        if (!channelName.equals(LogInActivity.channelName.ChannelName) && LogInActivity.registeredChannels.contains(channelName)){

                            init(LogInActivity.brokersPublishers.get(channelName).get(0), Integer.parseInt(LogInActivity.brokersPublishers.get(channelName).get(1)));

                            consumerOutputStream.writeObject("Consumer");
                            consumerOutputStream.flush();

                            consumerOutputStream.writeObject("ChannelName");
                            consumerOutputStream.flush();

                            register(channelName);

                            boolean connect = consumerInputStream.readBoolean();

                            if (connect)
                            {
                                Log.e("i =", "connected");
                                if (!LogInActivity.consumerNames.containsKey(channelName))
                                {
                                    HashSet<String> videos = new HashSet<>();
                                    LogInActivity.consumerNames.put(channelName, videos);
                                }

                                String videoname = "";
                                int size = this.consumerInputStream.readInt();
                                for (int i = 0; i < size; i++) {

                                    int sizeOfVideo = this.consumerInputStream.readInt();
                                    videoname = this.consumerInputStream.readUTF();

                                    if (LogInActivity.consumerVideos.containsKey(videoname)) {
                                        try{
                                            consumerOutputStream.writeBoolean(true);
                                            consumerOutputStream.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        continue;
                                    }
                                    else {
                                        consumerOutputStream.writeBoolean(false);
                                        consumerOutputStream.flush();
                                    }

                                    ArrayList<VideoFile> video = new ArrayList<>();

                                    VideoFile videoFile = null;

                                    for (int a = 0; a < sizeOfVideo; a++) {
                                        videoFile = (VideoFile) this.consumerInputStream.readObject();
                                        video.add(videoFile);
                                    }
                                    try {
                                        consumerOutputStream.writeUTF("ok");
                                        consumerOutputStream.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                    LogInActivity.consumerNames.get(channelName).add(videoname);
                                    LogInActivity.consumerVideos.put(videoname, videoFile);
                                    StoreVideos(video);
                                }

                                disconnect();
                            }
                            else {
                                Log.e("Failed to connect to channelname ", channelName);
                                disconnect();
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            int i = 0;
            List<VideoFile> videos = new ArrayList<>();

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(root.getAbsolutePath() + "/VideoFy");

            for (VideoFile Video:LogInActivity.consumerVideos.values())
            {
                VideoFile v = new VideoFile();
                v.videoURL = dir + "/" + Video.getVideoName() + ".mp4";
                v.videoPublished = "@" + Video.getChannelName();
                v.videoHashtags = Video.getHashtags();
                videos.add(v);
                i++;
            }

            if (videos.size() == 0)
            {
                Toast toast;
                toast = Toast.makeText(getContext(), "No content available..", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 450);
                toast.show();
            }

            downloadVideosProgressBar.setVisibility(View.INVISIBLE);
            videosViewPager.setVisibility(View.VISIBLE);
            videosViewPager.setAdapter(new VideoAdapter(videos, getActivity()));
            Log.e("i =", String.valueOf(i));

        }

        private void StoreVideos(ArrayList<VideoFile> videoFile) throws IOException
        {
            int size = 0;
            for (VideoFile element : videoFile) {
                size += element.getVideoFileChunk().length;
            }
            byte[] arr = new byte[size];

            int a = 0;
            for (VideoFile item : videoFile) {
                try{
                    byte[] temp = item.getVideoFileChunk();
                    for (byte b : temp) {
                        arr[a] = b;
                        a++;
                    }

                } catch (Exception e) {
                    System.out.println("ERROR");
                }
            }
            writeVideoData(videoFile.get(0).getVideoName(), arr);
        }

        private void writeVideoData(String name , byte[] data) {


            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(root.getAbsolutePath() + "/VideoFy");
            if (!dir.exists()) {
                dir.mkdirs();
            }



            String path = dir.getAbsolutePath() + "/"+ name + ".mp4";

            boolean exists = new File(path).exists();

            if (!exists) {

                Log.e("NAME", name);
                File file = new File(dir, name + ".mp4");

                FileOutputStream fileOutputStream = null;


                try {
                    fileOutputStream = new FileOutputStream(file);

                    fileOutputStream.write(data);

                    Log.e("PATH", String.valueOf(file));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        private void register(String topic) {
            try {
                consumerOutputStream.writeObject("Register");
                consumerOutputStream.flush();

                consumerOutputStream.writeObject(LogInActivity.channelName.ChannelName);
                consumerOutputStream.flush();

                consumerOutputStream.writeObject(topic);
                consumerOutputStream.flush();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        private void init(String ip, int port) {
            try {
                consumerSocket = new Socket(ip, port);
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void connect() throws IOException {
            try {
                consumerOutputStream = new ObjectOutputStream(consumerSocket.getOutputStream());
                consumerInputStream = new ObjectInputStream(consumerSocket.getInputStream());
            } catch (UnknownHostException unknownHost) {
                Log.e("error broker ", "You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        private void disconnect() {
            try {
                if (consumerOutputStream != null){
                    consumerOutputStream.close();}
                if (consumerInputStream != null){
                    consumerInputStream.close();}
                consumerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
package com.project.videofy;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import Video.VideoFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;

import com.project.videofy.databinding.FragmentContentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HashTagsVideos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HashTagsVideos extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    FragmentContentBinding binding;
    ViewPager2 videosViewPager;
    private Socket consumerSocket;
    private ObjectOutputStream consumerOutputStream;
    private ObjectInputStream consumerInputStream;

    private String ip;
    private int port;
    private String hashtag;

    private String mParam1;
    private String mParam2;

    public ArrayList<String> list_of_videos;
    ArrayAdapter<String> adapter;



    public HashTagsVideos() {}

    // TODO: Rename and change types and number of parameters
    public static HashTagsVideos newInstance(String param1, String param2) {
        HashTagsVideos fragment = new HashTagsVideos();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        list_of_videos = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentContentBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            String pos = getArguments().getString("pos");
            if (pos.equals("hashtag")) {
                this.hashtag = getArguments().getString("value");
                this.ip = getArguments().getString("Ip");
                this.port = getArguments().getInt("Port");

                binding.textViewSelect.setText(binding.textViewSelect.getText() + " #" + hashtag);
                binding.zeroVideos.setText(binding.zeroVideos.getText() + " #" + hashtag);
                binding.hashtagName.setText("#" + hashtag);
            }
        }
        PlayVideoTask task = new PlayVideoTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String [])null);

        initBackButton();
    }

    protected void initPlayVideo()
    {
        ArrayList<String> temp = new ArrayList<>();

        for (String videoname : list_of_videos)
        {
            if (!temp.contains(videoname)) {
                temp.add(videoname);
            }
        }

        adapter = new ArrayAdapter<String>(getActivity() , R.layout.list_items, temp);
        binding.videosList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        binding.videosList.getSelectedItem();

        Log.e("Content!", "video list size " + list_of_videos.size());

        binding.videosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                playData(temp.get(position));
            }
        });
    }

    protected void playData(String videoname)
    {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir = new File(root.getAbsolutePath() + "/VideoFy");

        binding.startImage.setVisibility(View.INVISIBLE);
        binding.videoView2.setVisibility(View.VISIBLE);

        String videoURL = dir + "/" + videoname + ".mp4";
        Log.e("DEBUG", "VIDEO "+videoURL);

        MediaController mediaController = new MediaController(this.getContext());

        binding.videoView2.setVideoURI(Uri.parse(videoURL));
        binding.videoView2.setMediaController(mediaController);
        binding.videoView2.start();

    }

    protected void initBackButton()
    {
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                binding.videoView2.stopPlayback();
                fragmentTransaction.replace(R.id.fragment, new SearchScreen()).commit();
            }
        });
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
            Log.e("DEBUG: ","You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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

        if (dir == null) {
            if (!dir.mkdirs()) {

                Log.e("ERROR IN FILE", "Directory not created");
            }
        }

        String path = dir.getAbsolutePath() + "/"+ name + ".mp4";

        if (!new File(path).exists()) {

            Log.e("NAME", name);
            File file = new File(dir.toString(), name + ".mp4");

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

    private class PlayVideoTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            init(ip, port);
            Log.e("D", "connected to broker");

            try {
                consumerOutputStream.writeObject("Consumer");
                consumerOutputStream.flush();

                consumerOutputStream.writeObject("HashTag");
                consumerOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            register(hashtag);

            Log.e("D", "register");

            int pubs_size = 0;
            try {
                pubs_size = consumerInputStream.readInt();
                Log.e("D total pubs", String.valueOf(pubs_size));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!LogInActivity.consumerHashtags.containsKey(hashtag)) {
                HashSet<String> videonames = new HashSet<>();
                LogInActivity.consumerHashtags.put(hashtag, videonames);
            }

            for (int j = 0; j < pubs_size; j++) {

                boolean connected = false;
                try {
                    connected = consumerInputStream.readBoolean();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connected) {
                    Log.e("D", "connect to pubserver");
                    int size = 0;
                    try {
                        size = consumerInputStream.readInt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // System.out.println((String) this.consumerInputStream.readObject());

                    Log.e("total videos", String.valueOf(size));
                    // we get all videos of a channelName with hashtag selected
                    for (int i = 0; i < size; i++) {
                        int sizeOfVideo = 0;
                        String videoname = null;

                        try {
                            sizeOfVideo = consumerInputStream.readInt();
                            videoname = consumerInputStream.readUTF();

                            Log.e("videoname from broker" , videoname);
                            if (LogInActivity.consumerVideos.containsKey(videoname)) {
                                consumerOutputStream.writeBoolean(true);
                                Log.e("Video exists", videoname);
                                consumerOutputStream.flush();
                                LogInActivity.consumerHashtags.get(hashtag).add(videoname);
                                continue;
                            }
                            else {
                                consumerOutputStream.writeBoolean(false);
                                consumerOutputStream.flush();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ArrayList<VideoFile> video = new ArrayList<>();
                        //  Log.e("DEBUG" ,"sizeofvideo "+ sizeOfVideo );
                        VideoFile videoFile = null;
                        Log.e("sizeFor00corrupted", ""+ sizeOfVideo);

                        for (int a = 0; a < sizeOfVideo; a++) {
                            try {
                                videoFile = (VideoFile) consumerInputStream.readObject();
                            } catch (ClassNotFoundException | IOException e) {
                                e.printStackTrace();
                            }
                            video.add(videoFile);
                        }

                       /*try {
                            consumerOutputStream.writeUTF("ok");
                            consumerOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/


                        LogInActivity.consumerHashtags.get(hashtag).add(videoname);

                        Log.e("DEBUG" ,"videoname "+ videoname );
                        try {

                            if (!LogInActivity.consumerNames.containsKey(videoFile.getChannelName()))
                            {
                                HashSet<String> videos = new HashSet<>();
                                LogInActivity.consumerNames.put(videoFile.getChannelName(), videos);
                            }

                            LogInActivity.consumerNames.get(videoFile.getChannelName()).add(videoname);
                            LogInActivity.consumerVideos.put(videoname , videoFile);
                            StoreVideos(video);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            disconnect();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {

                int videosSize = LogInActivity.consumerHashtags.get(hashtag).size();

                if (videosSize == 0) {
                    //System.out.println("\nNo video with hashtag #" + hashtag_choose);
                    if (binding.textViewSelect.getVisibility() == View.VISIBLE)
                    {
                        binding.textViewSelect.setVisibility(View.INVISIBLE);
                        binding.zeroVideos.setVisibility(View.VISIBLE);
                    }
                    Log.e("DEBUG" , "Zero videos found for " + hashtag);

                }
                else {

                    for (VideoFile Video:LogInActivity.consumerVideos.values())
                    {
                        if (LogInActivity.consumerHashtags.get(hashtag).contains(Video.getVideoName()))
                        {
                            list_of_videos.add(Video.getVideoName());
                        }
                    }
                    Log.e("SizeOfVideos" , "Size" + list_of_videos.size());
                    initPlayVideo();
                }

            } else { }
        }
    }

}


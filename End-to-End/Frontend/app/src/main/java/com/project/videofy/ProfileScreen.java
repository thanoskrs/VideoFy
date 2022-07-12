package com.project.videofy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewbinding.BuildConfig;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.project.videofy.databinding.LoginActivityBinding;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import Video.VideoFile;

import static com.project.videofy.LogInActivity.publishersIP;
import static com.project.videofy.LogInActivity.publishersPort;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileScreen#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ProfileScreen extends Fragment {

    private String channelName;
    private Socket consumerSocket;
    public static String PACKAGE_NAME;
    private String ip;
    private int port;
    GridView gridView;
    boolean initAdapter = false;
    TextView channelNameTxtView;
    private Button logOut;
    private ObjectOutputStream consumerOutputStream;
    private ObjectInputStream consumerInputStream;
    MyAdapter adapter;
    ImageView backbutton;

    ArrayList<VideoFile> videos = new ArrayList<>();

    public ProfileScreen() {

        // Required empty public constructor
    }

    public static ProfileScreen newInstance() {
        ProfileScreen fragment = new ProfileScreen();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile_screen, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {

        gridView = (GridView) getView().findViewById(R.id.gridView);
        channelNameTxtView = (TextView) getView().findViewById(R.id.channelNameTxtView_profile);
        logOut = (Button) getActivity().findViewById(R.id.logOutButton);
        backbutton = (ImageView)getView().findViewById(R.id.backButton);
        //subscribe = (Button) getActivity().findViewById(R.id.logOutButton);

        Bundle bundle = getArguments();
        Log.e("Bundle content's size : " , ""+ bundle.size());

        this.channelName = LogInActivity.channelName.ChannelName; // Main user channelName

        if (bundle.size() > 0 ) {

            logOut.setVisibility(View.GONE);

            backbutton.setVisibility(View.VISIBLE);
            initBackButton();
            this.channelName = (String) bundle.getString("ChannelName");
            this.ip = bundle.getString("Ip");
            this.port = bundle.getInt("Port");

            InitVideoTask task = new InitVideoTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String [])null);

            /*subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogInActivity.registeredChannels.add(channelName);
                }
            });*/
        }
        else {

            //subscribe.setVisibility(View.GONE);
            videos = new ArrayList<>();

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(root.getAbsolutePath() + "/VideoFy");

            for (String videoname :  LogInActivity.channelName.userVideoFilesMap.keySet()) {

                VideoFile video = LogInActivity.channelName.userVideoFilesMap.get(videoname).get(0);
                VideoFile v = new VideoFile();
                v.setVideoName(videoname);
                v.videoURL = dir + "/" + videoname + ".mp4";
                v.videoPublished = "@" + video.getChannelName();
                v.videoHashtags = video.getHashtags();
                videos.add(v);
            }

            if (videos.size() == 0) {
                Toast toast;
                toast = Toast.makeText(getContext(), "You haven't uploaded any video", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 450);
                toast.show();
            }

            adapter = new MyAdapter(getContext(), videos);
            gridView.setAdapter(adapter);

            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Disconnect disconnect = new Disconnect();
                    disconnect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String [])null);
                }
            });
        }

        Log.e("User ", this.channelName);

        Log.e("textview" , (String)this.channelNameTxtView.getText());
        this.channelNameTxtView.setText("@" + this.channelName);

    }

    protected void initBackButton()
    {
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment, new SearchScreen()).commit();
            }
        });
    }


    private class Disconnect extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {

                Socket connectToMS = new Socket(LogInActivity.MainServerIp, LogInActivity.MainServerPort);
                ObjectOutputStream outMainServer = new ObjectOutputStream(connectToMS.getOutputStream());
                ObjectInputStream inMainServer = new ObjectInputStream(connectToMS.getInputStream());

                outMainServer.writeUTF("Appnode");
                outMainServer.flush();

                outMainServer.writeUTF("DISCONNECT");
                outMainServer.flush();

                outMainServer.writeUTF(LogInActivity.channelName.ChannelName);
                outMainServer.flush();

                connectToMS.close();
                outMainServer.close();
                inMainServer.close();


                // connect to pubserver and notify for disconnect
                Socket c = new Socket(publishersIP, publishersPort);
                ObjectOutputStream out = new ObjectOutputStream(c.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(c.getInputStream());

                out.writeUTF("disconnect");
                out.flush();


                out.close();
                in.close();
                c.close();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            //this will clear the back stack and displays no animation on the screen
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            for(Activity activity:LogInActivity.activities)
                activity.finishAndRemoveTask();

            Intent intent = new Intent(getActivity().getApplicationContext() , LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }



    private class InitVideoTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            if (!channelName.equals(LogInActivity.channelName.ChannelName) ){

                Log.e("TT", "1");
                init(LogInActivity.brokersPublishers.get(channelName).get(0), Integer.parseInt(LogInActivity.brokersPublishers.get(channelName).get(1)));
                Log.e("TT", "2");
                try {
                    consumerOutputStream.writeObject("Consumer");
                    consumerOutputStream.flush();

                    consumerOutputStream.writeObject("ChannelName");
                    consumerOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                register(channelName);

                boolean connect = false;
                try {
                    connect = consumerInputStream.readBoolean();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connect)
                {

                    // subscribe to channel, so we can see new vides to homeScreen
                    LogInActivity.registeredChannels.add(channelName);

                    Log.e("i =", "connected");
                    if (!LogInActivity.consumerNames.containsKey(channelName))
                    {
                        HashSet<String> videos = new HashSet<>();
                        LogInActivity.consumerNames.put(channelName, videos);
                    }

                    String videoname = "";
                    int size = 0;
                    try {
                        size = consumerInputStream.readInt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < size; i++) {

                        int sizeOfVideo = 0;
                        try {
                            sizeOfVideo = consumerInputStream.readInt();
                            videoname = consumerInputStream.readUTF();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if (LogInActivity.consumerVideos.containsKey(videoname)) {
                            try {
                                consumerOutputStream.writeBoolean(true);
                                consumerOutputStream.flush();
                                Log.e("Profile-Video-Exists", videoname);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                        else {
                            try {
                                consumerOutputStream.writeBoolean(false);
                                consumerOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        ArrayList<VideoFile> video = new ArrayList<>();

                        VideoFile videoFile = null;

                        for (int a = 0; a < sizeOfVideo; a++) {
                            try {
                                videoFile = (VideoFile) consumerInputStream.readObject();
                            } catch (ClassNotFoundException | IOException e) {
                                e.printStackTrace();
                            }
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
                        try {
                            StoreVideos(video);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    disconnect();
                }
                else {
                    Log.e("Failed to connect to channelName ", channelName);
                    disconnect();
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            videos = new ArrayList<>();

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(root.getAbsolutePath() + "/VideoFy");

            for (String videoname :  LogInActivity.consumerNames.get(channelName))
            {
                VideoFile video = LogInActivity.consumerVideos.get(videoname);
                VideoFile v = new VideoFile();
                v.setVideoName(videoname);
                v.videoURL = dir + "/" + videoname + ".mp4";
                v.videoPublished = "@" + video.getChannelName();
                v.videoHashtags = video.getHashtags();
                videos.add(v);
            }

            if (videos.size() == 0) {
                Toast toast;
                toast = Toast.makeText(getContext(), "No content available..", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 450);
                toast.show();
            }
            else {
                gridView.setAdapter(new MyAdapter(getContext(), videos));
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

    private class DeleteVideo extends AsyncTask<String, String, String> {

        private Socket publisherSocket;
        private ObjectInputStream publisherInputStream;
        private ObjectOutputStream publisherOutputStream;

        @Override
        protected String doInBackground(String... strings) {


            String name = strings[0];

            LogInActivity.channelName.userVideoFilesMap.remove(name);
            Log.e("DELETE", String.format("\nSuccessfully deleted video <%s>.", name));

            HashSet<String> Markup = new HashSet<>(LogInActivity.channelName.hashtagsPublished.keySet());

            for (String key : Markup) {
                LogInActivity.channelName.hashtagsPublished.get(key).remove(name);
                if (LogInActivity.channelName.hashtagsPublished.get(key).size() == 0) {
                    LogInActivity.channelName.hashtagsPublished.remove(key);

                    List<String> hashtagHash = LogInActivity.hashTopic(key);

                    init(hashtagHash.get(0), Integer.parseInt(hashtagHash.get(1))); // sindesi me ton broker pou einai ipefthinos gia to hashtag etsi wste na diagrapsoume

                    try {
                        publisherOutputStream.writeObject("Publisher");
                        publisherOutputStream.flush();

                        publisherOutputStream.writeObject("Delete");
                        publisherOutputStream.flush();

                        publisherOutputStream.writeUTF(LogInActivity.channelName.ChannelName);
                        publisherOutputStream.flush();

                        publisherOutputStream.writeUTF(key);
                        publisherOutputStream.flush();

                        disconnect();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            adapter.notifyDataSetChanged();
            gridView.invalidate();
        }

        private void init(String IP, int port) {
            try {
                publisherSocket = new Socket(IP, port);
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void connect() throws IOException {
            try {
                publisherInputStream = new ObjectInputStream(publisherSocket.getInputStream());
                publisherOutputStream = new ObjectOutputStream(publisherSocket.getOutputStream());
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        private void disconnect()  {
            try {
                publisherOutputStream.close();
                publisherInputStream.close();
                publisherSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
    private class MyAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<VideoFile> videos;
        LayoutInflater inflater;

        public MyAdapter(Context context, ArrayList<VideoFile> videos)
        {
            this.context = context;
            this.videos = videos;
            inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return videos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row_data,null);

            VideoView video = (VideoView) view.findViewById(R.id.video_profile);
            TextView delete = (TextView) view.findViewById(R.id.deleteTxtView);

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(root.getAbsolutePath() + "/VideoFy");

            String videoURL = dir + "/" + videos.get(position).getVideoName() + ".mp4";
            Log.e("DEBUG", "VIDEO "+videoURL);

            Uri uri = Uri.parse(videoURL);
            video.setVideoURI(uri);
            video.seekTo( 1 );
            MediaController mediaController1 = new MediaController(this.context);
            video.setMediaController(mediaController1);
            mediaController1.setAnchorView(video);
            video.setMediaController(null);

            video.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", Integer.toString(position));
                    bundle.putSerializable("videos", videos); //pairnei apo dw tin lista me ta video, auti tha xrisimopoihthei sto fullScreen
                    fullScreenVideo fullScreen = new fullScreenVideo();
                    FragmentManager fragmentManager= getChildFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.profileFragment, fullScreen);
                    fragmentTransaction.commit();
                    fullScreen.setArguments(bundle);

                    visibilityGone();

                }
            });

            video.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    delete.setVisibility(View.VISIBLE);
                    return true;
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delete.getVisibility() == View.VISIBLE) {
                        Log.d("!!", "delete");

                        delete.setVisibility(View.GONE);
                        DeleteVideo deleteVideo = new DeleteVideo();

                        String[] params = new String[1];
                        params[0] = videos.get(position).getVideoName();

                        videos.remove(position);

                        deleteVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    }
                }
            });

            return view;
        }
    }

    private void visibilityGone() {
        getView().findViewById(R.id.profilePicture_image).setVisibility(View.GONE);
        getView().findViewById(R.id.channelNameTxtView_profile).setVisibility(View.GONE);
        getView().findViewById(R.id.separatorLine).setVisibility(View.GONE);
        getView().findViewById(R.id.gridView).setVisibility(View.GONE);
    }


}
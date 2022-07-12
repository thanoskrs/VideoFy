package com.project.videofy;

import Video.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;



import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadScreen extends Fragment {

    ImageButton recordVideoButton, uploadVideoButton, accessVideoButton;
    EditText hashtagsPublished;
    TextView recordText, pickText;
    VideoView uploadVideoView;
    TextView uploadTextView ;
    MediaController mc;
    ImageView shareContent;

    private static int CAMERA_PERMISSION_CODE = 100;

    private Uri videoPath = null;
    private String videoString;

    public UploadScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment UploadScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadScreen newInstance() {
        UploadScreen fragment = new UploadScreen();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (cameraDetected()) {
            Log.e("CAMERA DETECTED", "YES");
            getCameraPermission();
        }
        else {
            Log.e("CAMERA DETECTED", "NO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_upload_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recordVideoButton = (ImageButton) getActivity().findViewById(R.id.recordVideoButton);
        uploadVideoButton = (ImageButton) getActivity().findViewById(R.id.uploadVideoButton);
        accessVideoButton = (ImageButton) getActivity().findViewById(R.id.accessVideoButton);
        uploadVideoView = (VideoView) getActivity().findViewById(R.id.uploadVideoView);
        uploadTextView = (TextView) getActivity().findViewById(R.id.uploadB);
        uploadVideoView.setMediaController(mc);
        hashtagsPublished = (EditText) getActivity().findViewById(R.id.hashtagsPublished);
        recordText = (TextView) getActivity().findViewById(R.id.recordText);
        pickText = (TextView) getActivity().findViewById(R.id.pickText);
        uploadVideoView.setVisibility(View.INVISIBLE);
        shareContent = (ImageView) getActivity().findViewById(R.id.share_content);

        recordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(intent, 1);
            }
        });

        accessVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent  = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                galleryIntent.setType("video/*");
                startActivityForResult(galleryIntent, 10);;
            }
        });

        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoPath != null) {

                    PublishVideo publishVideo = new PublishVideo();
                    publishVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String [])null);
                }
            }
        });
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

    private boolean cameraDetected() {
        return (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    protected void playData(Uri videoPath)
    {
        MediaController mediaController = new MediaController(getContext());

        uploadVideoView.setVideoURI(videoPath);
        uploadVideoView.setMediaController(mediaController);
        uploadVideoView.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 || requestCode == 10) {

            if (resultCode == Activity.RESULT_OK)
            {
                assert data != null;
                videoPath = data.getData();
                videoString = data.getDataString();

                recordVideoButton.setVisibility(View.INVISIBLE);
                accessVideoButton.setVisibility(View.INVISIBLE);
                recordText.setVisibility(View.INVISIBLE);
                pickText.setVisibility(View.INVISIBLE);
                shareContent.setVisibility(View.INVISIBLE);
                uploadVideoView.setVisibility(View.VISIBLE);
                uploadTextView.setVisibility(View.VISIBLE);
                uploadVideoButton.setVisibility(View.VISIBLE);
                hashtagsPublished.setVisibility(View.VISIBLE);

                playData(videoPath);
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("VIDEO_UPLOAD_TAG", "Recording or picking video is cancelled");
            }
            else {
                Log.i("VIDEO_UPLOAD_TAG", "Recording or picking video has got some error");
            }
        }
    }

    public class PublishVideo extends AsyncTask<String, String, String> {


        private Socket publisherSocket;
        private ObjectOutputStream publisherOutputStream;
        private ObjectInputStream publisherInputStream;

        private String videoName;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... strings) {
            try {

                String hashtags = hashtagsPublished.getText().toString();
                String[] hashtagsArray = hashtags.split(" ");

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(getContext(), videoPath);

                ArrayList<VideoFile> chunks = new ArrayList<>(generateChunks(mediaMetadataRetriever, videoString, hashtagsArray));
                LogInActivity.channelName.userVideoFilesMap.put(videoName, chunks);



                for (String hashtag:hashtagsArray) {
                    addHashTag(hashtag, videoName); // update hashtagsPublished on channelName

                    // get the correct broker's ip and port and notify for hashtag
                    List<String> connectToBroker = LogInActivity.hashTopic(hashtag);
                    init(connectToBroker.get(0), Integer.parseInt(connectToBroker.get(1)));
                    notifyBrokersForHashTags(hashtag);
                    disconnect();
                }

            } catch(IOException  ioException){
                ioException.printStackTrace();
            }

            // Store the published video into SD card
            InputStream iStream = null;
            try {
                iStream = getActivity().getContentResolver().openInputStream(videoPath);
                byte[] arrayBytes = getBytes(iStream);
                writeVideoData(videoName, arrayBytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast toast;
            toast = Toast.makeText(getContext(), "Successfully uploaded", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 450);
            toast.show();

            recordVideoButton.setVisibility(View.VISIBLE);
            accessVideoButton.setVisibility(View.VISIBLE);
            recordText.setVisibility(View.VISIBLE);
            pickText.setVisibility(View.VISIBLE);
            shareContent.setVisibility(View.VISIBLE);

            uploadVideoView.setVisibility(View.INVISIBLE);
            uploadTextView.setVisibility(View.INVISIBLE);
            uploadVideoButton.setVisibility(View.INVISIBLE);
            hashtagsPublished.setVisibility(View.INVISIBLE);

        }


        public String getVideoName()
        {
            return this.videoName;
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

        private void addHashTag(String hashtag, String videoName) {
            ArrayList<String> videoNames = new ArrayList<>();
            if (!LogInActivity.channelName.hashtagsPublished.containsKey(hashtag)) {
                videoNames.add(videoName);
            }
            else {
                videoNames = LogInActivity.channelName.hashtagsPublished.get(hashtag);
                videoNames.add(videoName);
            }

            LogInActivity.channelName.hashtagsPublished.put(hashtag, videoNames);
        }

        private void notifyBrokersForHashTags(String hashtag) {
            try {
                publisherOutputStream.writeObject("Publisher");
                publisherOutputStream.flush();

                publisherOutputStream.writeObject("Publish");
                publisherOutputStream.flush();

                publisherOutputStream.writeObject(LogInActivity.channelName.ChannelName);
                publisherOutputStream.flush();

                publisherOutputStream.writeObject(hashtag);
                publisherOutputStream.flush();

                publisherOutputStream.writeObject(LogInActivity.publishersIP);
                publisherOutputStream.flush();

                publisherOutputStream.writeObject(String.valueOf(LogInActivity.publishersPort));
                publisherOutputStream.flush();

                System.out.println((String) publisherInputStream.readObject());

            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public ArrayList<VideoFile> generateChunks(MediaMetadataRetriever metadata, String  path, String[] hashtags) throws IOException {

            Log.e("STRING", videoString);
            Log.e("ST", String.valueOf(videoPath));
            String v= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); // we need it, in order to update channelname's hashmaps
            String ChannelName = LogInActivity.channelName.ChannelName;
            String dateCreated = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            String framerate = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            String frameWidth = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String frameHeight = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            ArrayList<String> associatedHashTags = new ArrayList<>();

            if(v!=null)
                Log.e("videoname", v);
            if(ChannelName!=null)
                Log.e("ChannelName", ChannelName);
            if(dateCreated!=null)
                Log.e("dateCreated", dateCreated);
            if(framerate!=null)
                Log.e("framerate", framerate);
            if(frameWidth!=null)
                Log.e("frameWidth",frameWidth);

            byte[] videoFileChunk = null;

            for (String h:hashtags) {
                associatedHashTags.add(h);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            this.videoName = sdf.format(new Date());
            Log.e("videoname", this.videoName);

            InputStream iStream =  getActivity().getContentResolver().openInputStream(videoPath);
            byte[] arrayBytes = getBytes(iStream);

            ArrayList<VideoFile> videoFiles = new ArrayList<VideoFile>();


            int numOfChunks;

            int totalBytes = arrayBytes.length;

            int kiloByte = (int) Math.pow(2, 10);
            int chunkSize = 512 * kiloByte;

            if (totalBytes % chunkSize == 0) {
                numOfChunks = totalBytes / chunkSize;
            } else {
                numOfChunks = totalBytes / chunkSize + 1;
            }


            for (int i = 0; i < numOfChunks; i++) {
                if (i == numOfChunks - 1) {
                    videoFileChunk = new byte[totalBytes - (i * chunkSize)];
                    for (int j = 0; j < videoFileChunk.length; j++) {
                        videoFileChunk[j] = arrayBytes[(i * chunkSize) + j];
                    }
                } else {
                    videoFileChunk = new byte[chunkSize];
                    for (int j = 0; j < chunkSize; j++) {
                        videoFileChunk[j] = arrayBytes[(i * chunkSize) + j];
                    }
                }

                Log.e("VideoNameUploadScreen", "" + this.videoName);
                VideoFile videoFile = new VideoFile(this.videoName, ChannelName, dateCreated, 0, framerate, frameWidth, frameHeight, associatedHashTags, videoFileChunk);
                videoFile.setVideoFileChunk(videoFileChunk);

                videoFiles.add(videoFile);
            }
            return videoFiles;

        }

        public byte[] getBytes(InputStream inputStream) {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            try {
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
            } catch (IOException ioException) {
                return null;
            }
            return byteBuffer.toByteArray();
        }
    }
}
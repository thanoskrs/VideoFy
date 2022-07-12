package com.project.videofy;


import android.os.AsyncTask;
import android.util.Log;
import Video.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class PublisherServer extends Thread {


    ObjectOutputStream objectOutputStream = null;
    ObjectInputStream objectInputStream = null;

    public PublisherServer(Socket connection) {
        try {
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectInputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void run() {
        try {
            String task = objectInputStream.readUTF();

            if (task.equals("disconnect"))
            {
                System.out.println("Disconnected");
            }
            else if (task.equals("connect"))
            {
                String consName = objectInputStream.readUTF();
                String topic = objectInputStream.readUTF();

                Log.e("CONNECTEd", "OK");

                if (topic.equals(LogInActivity.channelName.ChannelName)) {


                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    Log.e("SIZE FROM PUB", String.valueOf(LogInActivity.channelName.userVideoFilesMap.size()));

                    objectOutputStream.writeInt(LogInActivity.channelName.userVideoFilesMap.size());
                    objectOutputStream.flush();


                    for (String videoname : LogInActivity.channelName.userVideoFilesMap.keySet()) {
                        int videoSize = LogInActivity.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = LogInActivity.channelName.userVideoFilesMap.get(videoname);
                        Log.e("videoname", videoname);
                        this.objectOutputStream.writeUTF(videoname);
                        this.objectOutputStream.flush();

                        Log.e("videoSize", String.valueOf(videoSize));
                        this.objectOutputStream.writeInt(videoSize);
                        this.objectOutputStream.flush();

                        boolean videoExists = this.objectInputStream.readBoolean();
                        if (videoExists) {
                            continue;
                        }

                        boolean t = false;
                        int c = 0;
                        // while (!t) {
                        for (int i = 0; i < videoSize; i++) {
                            push(videoFiles.get(i));
                            c++;
                        }
                        Log.e("C = ", String.valueOf(c) );
                        //t = this.objectInputStream.readBoolean();
                        //  }
                    }
                }
                else if (LogInActivity.channelName.hashtagsPublished.containsKey(topic))
                {
                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(LogInActivity.channelName.hashtagsPublished.get(topic).size());
                    objectOutputStream.flush();

                    for (String videoname : LogInActivity.channelName.hashtagsPublished.get(topic)) {

                        int videoSize = LogInActivity.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = LogInActivity.channelName.userVideoFilesMap.get(videoname);

                        this.objectOutputStream.writeUTF(videoname);
                        this.objectOutputStream.flush();

                        this.objectOutputStream.writeInt(videoSize);
                        this.objectOutputStream.flush();

                        boolean videoExists = this.objectInputStream.readBoolean();
                        if (videoExists) {
                            continue;
                        }

                        boolean t = false;
                        int c = 0;
                        // while (!t) {
                        for (int i = 0; i < videoSize; i++) {
                            push(videoFiles.get(i));
                            c++;
                        }
                        Log.e("C = ", String.valueOf(c) );
                        //t = this.objectInputStream.readBoolean();
                        //  }

                    }
                } else {
                    objectOutputStream.writeObject("Invalid");
                    objectOutputStream.flush();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                objectOutputStream.close();
                objectInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void push(VideoFile videoFile) {
        try {
            this.objectOutputStream.writeObject(videoFile);
            this.objectOutputStream.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /*@Override
    protected Object doInBackground(Object[] objects) {
        try {
            String task = objectInputStream.readUTF();

            if (task.equals("disconnect"))
            {
                System.out.println("Disconnected");
            }
            else if (task.equals("connect"))
            {
                String consName = objectInputStream.readUTF();
                String topic = objectInputStream.readUTF();

                if (topic.equals(LogInActivity.channelName.ChannelName)) {

                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(LogInActivity.channelName.userVideoFilesMap.size());
                    objectOutputStream.flush();

                    for (String videoname : LogInActivity.channelName.userVideoFilesMap.keySet()) {

                        int videoSize = LogInActivity.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = LogInActivity.channelName.userVideoFilesMap.get(videoname);

                        this.objectOutputStream.writeUTF(videoname);
                        this.objectOutputStream.flush();

                        this.objectOutputStream.writeInt(videoSize);
                        this.objectOutputStream.flush();

                        for (int i = 0; i < videoSize; i++) {
                            push(videoFiles.get(i));
                        }
                    }
                }
                else if (LogInActivity.channelName.hashtagsPublished.containsKey(topic))
                {
                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(LogInActivity.channelName.hashtagsPublished.get(topic).size());
                    objectOutputStream.flush();

                    for (String videoname : LogInActivity.channelName.hashtagsPublished.get(topic)) {

                        int videoSize = LogInActivity.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = LogInActivity.channelName.userVideoFilesMap.get(videoname);

                        this.objectOutputStream.writeUTF(videoname);
                        this.objectOutputStream.flush();

                        this.objectOutputStream.writeInt(videoSize);
                        this.objectOutputStream.flush();

                        for (int i = 0; i < videoSize; i++) {
                            push(videoFiles.get(i));
                        }
                    }
                } else {
                    objectOutputStream.writeObject("Invalid");
                    objectOutputStream.flush();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                objectOutputStream.close();
                objectInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return null;
    }*/
}
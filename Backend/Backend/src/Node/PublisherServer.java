package Node;

import Video.VideoFile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PublisherServer extends Thread {

    ObjectOutputStream objectOutputStream = null;
    ObjectInputStream objectInputStream = null;

    public PublisherServer (Socket connection) {
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

                if (topic.equals(AppNode.channelName.ChannelName)) {

                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(AppNode.channelName.userVideoFilesMap.size());
                    objectOutputStream.flush();

                    for (String videoname : AppNode.channelName.userVideoFilesMap.keySet()) {

                        int videoSize = AppNode.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = AppNode.channelName.userVideoFilesMap.get(videoname);

                        this.objectOutputStream.writeUTF(videoname);
                        this.objectOutputStream.flush();

                        this.objectOutputStream.writeInt(videoSize);
                        this.objectOutputStream.flush();

                        for (int i = 0; i < videoSize; i++) {
                            push(videoFiles.get(i));
                        }
                    }
                }
                else if (AppNode.channelName.hashtagsPublished.containsKey(topic))
                {
                    objectOutputStream.writeObject(topic + " is ready for " + consName);
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(AppNode.channelName.hashtagsPublished.get(topic).size());
                    objectOutputStream.flush();

                    for (String videoname : AppNode.channelName.hashtagsPublished.get(topic)) {

                        int videoSize = AppNode.channelName.userVideoFilesMap.get(videoname).size();
                        ArrayList<VideoFile> videoFiles = AppNode.channelName.userVideoFilesMap.get(videoname);

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
    }

    private void push(VideoFile videoFile) {
        try {
            this.objectOutputStream.writeObject(videoFile);
            this.objectOutputStream.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
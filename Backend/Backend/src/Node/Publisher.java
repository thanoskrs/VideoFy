package Node;

import Video.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.xml.sax.SAXException;

public class Publisher {

    private Socket publisherSocket;
    private ObjectOutputStream publisherOutputStream;
    private ObjectInputStream publisherInputStream;

    private String videoName;

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
        if (!AppNode.channelName.hashtagsPublished.containsKey(hashtag)) {
            videoNames.add(videoName);
        }
        else {
            videoNames = AppNode.channelName.hashtagsPublished.get(hashtag);
            videoNames.add(videoName);
        }

        AppNode.channelName.hashtagsPublished.put(hashtag, videoNames);
    }

    private void notifyBrokersForHashTags(String hashtag) {
        try {
            publisherOutputStream.writeObject("Publisher");
            publisherOutputStream.flush();

            publisherOutputStream.writeObject("Publish");
            publisherOutputStream.flush();

            publisherOutputStream.writeObject(AppNode.channelName.ChannelName);
            publisherOutputStream.flush();

            publisherOutputStream.writeObject(hashtag);
            publisherOutputStream.flush();

            publisherOutputStream.writeObject(AppNode.publishersIP);
            publisherOutputStream.flush();

            publisherOutputStream.writeObject(String.valueOf(AppNode.publishersPort));
            publisherOutputStream.flush();

            System.out.println((String) publisherInputStream.readObject());

        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    public void publish() {
        try {

            Scanner in = new Scanner(System.in);
            System.out.println("Enter the path of the video. ");
            System.out.print("> ");

            String path = in.nextLine();

            System.out.println("Add hashtags for video separated by space.");
            System.out.print("> ");

            String hashtags = in.nextLine();
            String[] hashtagsArray = hashtags.split(" ");

            //this.videoName = AppNode.channelName.ChannelName + (++AppNode.channelName.videonameCounter);

            ArrayList<VideoFile> chunks = new ArrayList<>(generateChunks(Util.mp4Info(path, AppNode.channelName.ChannelName), path, hashtagsArray));
            AppNode.channelName.userVideoFilesMap.put(videoName, chunks);

            for (String hashtag:hashtagsArray) {
                addHashTag(hashtag, videoName); // update hashtagsPublished on channelName

                // get the correct broker's ip and port and notify for hashtag
                List<String> connectToBroker = AppNode.hashTopic(hashtag);
                init(connectToBroker.get(0), Integer.parseInt(connectToBroker.get(1)));
                notifyBrokersForHashTags(hashtag);
                disconnect();
            }

        } catch(IOException | TikaException | SAXException ioException){
            ioException.printStackTrace();
        }
    }

    public void delete() {


        if (AppNode.channelName.userVideoFilesMap.size() == 0)
        {
            System.out.println("\nThere is no video to delete");
        }
        else {

            System.out.println();

            for (String video : AppNode.channelName.userVideoFilesMap.keySet()) {
                System.out.println("VideoName: " + video);
            }

            Scanner in = new Scanner(System.in);
            System.out.println("\nEnter the name of the video to delete.");
            System.out.print("> ");

            String name = in.nextLine();


            if (!AppNode.channelName.userVideoFilesMap.containsKey(name)) {
                System.out.println(String.format("\nVideo with name <%s> does not exist.", name));
            } else {

                AppNode.channelName.userVideoFilesMap.remove(name);
                System.out.println(String.format("\nSuccessfully deleted video <%s>.", name));

                HashSet<String> Markup = new HashSet<>(AppNode.channelName.hashtagsPublished.keySet());

                for (String key : Markup) {
                    AppNode.channelName.hashtagsPublished.get(key).remove(name);
                    if (AppNode.channelName.hashtagsPublished.get(key).size() == 0) {
                        AppNode.channelName.hashtagsPublished.remove(key);

                        List<String> hashtagHash = AppNode.hashTopic(key);

                        init(hashtagHash.get(0), Integer.parseInt(hashtagHash.get(1))); // sindesi me ton broker pou einai ipefthinos gia to hashtag etsi wste na diagrapsoume

                        try {
                            publisherOutputStream.writeObject("Publisher");
                            publisherOutputStream.flush();

                            this.publisherOutputStream.writeObject("Delete");
                            this.publisherOutputStream.flush();

                            this.publisherOutputStream.writeUTF(AppNode.channelName.ChannelName);
                            this.publisherOutputStream.flush();

                            this.publisherOutputStream.writeUTF(key);
                            this.publisherOutputStream.flush();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    public ArrayList<VideoFile> generateChunks(Metadata metadata, String path, String[] hashtags) throws IOException, SAXException, TikaException {

        metadata.set(TikaCoreProperties.CREATOR, AppNode.channelName.ChannelName); // set ChannelName

        this.videoName = metadata.get(TikaCoreProperties.TITLE); // we need it, in order to update channelname's hashmaps
        String ChannelName = metadata.get(TikaCoreProperties.CREATOR);
        String dateCreated = metadata.get(TikaCoreProperties.CREATED);
        long length = 0;
        String framerate = null;
        String frameWidth = null;
        String frameHeight = null;
        ArrayList<String> associatedHashTags = new ArrayList<>();
        byte[] videoFileChunk = null;

        for (String h:hashtags) {
            associatedHashTags.add(h);
        }

        byte[] arrayBytes = Files.readAllBytes(Paths.get(path));
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

            VideoFile videoFile = new VideoFile(this.videoName, ChannelName, dateCreated, length, framerate, frameWidth, frameHeight, associatedHashTags, videoFileChunk);
            videoFile.setVideoFileChunk(videoFileChunk);

            videoFiles.add(videoFile);
        }
        return videoFiles;

    }
}
package Node;

import Video.*;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

public class Consumer {

    private Socket consumerSocket;
    private ObjectOutputStream consumerOutputStream;
    private ObjectInputStream consumerInputStream;

    // public HashMap<String , File> consumerVideos = new HashMap<>();

    private void register(String topic) {
        try {
            consumerOutputStream.writeObject("Register");
            consumerOutputStream.flush();

            consumerOutputStream.writeObject(AppNode.channelName.ChannelName);
            consumerOutputStream.flush();

            consumerOutputStream.writeObject(topic);
            consumerOutputStream.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void playData(File file) throws IOException, InterruptedException {
        Desktop.getDesktop().open(file);
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
            System.err.println("You are trying to connect to an unknown host!");
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

    private File StoreVideos(ArrayList<VideoFile> videoFile , String newPath) throws IOException
    {
        File file = new File(newPath);
        int size = 0;
        for (VideoFile element : videoFile) {
            size += element.getVideoFileChunk().length;
        }
        byte[] arr = new byte[size];
        // System.out.println(arr.length);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
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
        fileOutputStream.write(arr);
        fileOutputStream.close();
        //  value.getVideoFile().print_information();
        return file;

    }

    public void consume() {

        try {
            while (true) {

                // choose functionality
                System.out.println("\n1. Choose a hashtag.");
                System.out.println("2. Choose a channelName.");
                System.out.println("3. View downloaded videos.");
                System.out.println("4. Back.");

                Scanner in = new Scanner(System.in);
                System.out.print("> ");

                int choice = -1;
                while (choice != 1 && choice != 2 && choice != 3 && choice != 4) {
                    choice = in.nextInt();
                }

                if (choice == 1) {
                    init(AppNode.MainServerIP, AppNode.MainServerPort);

                    consumerOutputStream.writeUTF("Consumer");
                    consumerOutputStream.flush();

                    consumerOutputStream.writeUTF("HashTag");
                    consumerOutputStream.flush();

                    AppNode.brokersHashtags = (HashMap<String, ArrayList<String>>) consumerInputStream.readObject();

                    if (AppNode.brokersHashtags.size() == 0) {
                        System.out.println("No hashtag published.");

                    }
                    else {

                        for (Map.Entry element : AppNode.brokersHashtags.entrySet()) {
                            System.out.println(String.format("# %s", element.getKey()));
                        }

                        System.out.println("\nPlease type a hashtag or press 1 to exit.");
                        System.out.print("> ");

                        String hashtag_choose = in.next();

                        while (!AppNode.brokersHashtags.containsKey(hashtag_choose) && !hashtag_choose.equals("1")) {
                            System.out.println("> ");
                            System.out.print("\n> ");

                            hashtag_choose = in.next();
                        }

                        disconnect();

                        if (hashtag_choose.equals("1")) {
                            break;
                        }
                        init(AppNode.brokersHashtags.get(hashtag_choose).get(0), Integer.parseInt(AppNode.brokersHashtags.get(hashtag_choose).get(1)));

                        consumerOutputStream.writeObject("Consumer");
                        consumerOutputStream.flush();

                        consumerOutputStream.writeObject("HashTag");
                        consumerOutputStream.flush();

                        register(hashtag_choose);

                        int pubs_size = this.consumerInputStream.readInt();

                        // System.out.println(pubs_size);
                        // we get videos from every channelName -exept us- who published a video with hashtag selected

                        if (!AppNode.consumerHashtags.containsKey(hashtag_choose)) {
                            HashSet<String> videos = new HashSet<>();
                            AppNode.consumerHashtags.put(hashtag_choose, videos);
                        }

                        for (int j = 0; j < pubs_size; j++) {

                            boolean connected = consumerInputStream.readBoolean();

                            if (connected) {
                                int size = this.consumerInputStream.readInt();
                                // System.out.println((String) this.consumerInputStream.readObject());

                                // we get all videos of a channelName with hashtag selected
                                for (int i = 0; i < size; i++) {
                                    int sizeOfVideo = this.consumerInputStream.readInt();
                                    ArrayList<VideoFile> video = new ArrayList<>();

                                    for (int a = 0; a < sizeOfVideo; a++) {
                                        video.add((VideoFile) this.consumerInputStream.readObject());
                                    }
                                    String videoname = this.consumerInputStream.readUTF();


                                    AppNode.consumerHashtags.get(hashtag_choose).add(videoname);
                                    AppNode.consumerVideos.put(videoname, StoreVideos(video, "src/out/" + videoname + ".mp4"));
                                }
                            }
                            else {
                                continue;
                            }
                        }

                        disconnect();
                        while (true) {
                            int videos = AppNode.consumerHashtags.get(hashtag_choose).size();

                            if (videos == 0) {
                                System.out.println("\nNo video with hashtag #" + hashtag_choose);
                                break;
                            } else {
                                //System.out.println("Available videos " + videos);


                                System.out.println();

                                for (String videoname : AppNode.consumerHashtags.get(hashtag_choose)) {
                                    System.out.println("VideoName : " + videoname);
                                }
                                System.out.println("\nType the name of the video to play or press 1 to exit.");
                                System.out.print("> ");

                                String video_choose = in.next();

                                while (!AppNode.consumerHashtags.get(hashtag_choose).contains(video_choose) && !video_choose.equals("1")) {
                                    System.out.println("Invalid videoname. Try again. \n");
                                    System.out.print("> ");

                                    video_choose = in.next();
                                }

                                if (video_choose.equals("1")) {
                                    break;
                                }
                                playData(AppNode.consumerVideos.get(video_choose));
                            }
                        }
                    }
                } else if (choice == 2) {

                    init(AppNode.MainServerIP, AppNode.MainServerPort);

                    consumerOutputStream.writeUTF("Consumer");
                    consumerOutputStream.flush();

                    consumerOutputStream.writeUTF("ChannelName");
                    consumerOutputStream.flush();

                    AppNode.brokersPublishers = new HashMap<>((HashMap<String, ArrayList<String>>) consumerInputStream.readObject());


                    // if the only user connected is the consumer we don't give his information
                    if (AppNode.brokersPublishers.size() == 0 || (AppNode.brokersPublishers.size() == 1 && AppNode.brokersPublishers.containsKey(AppNode.channelName.ChannelName))) {
                        System.out.println("No user connected.");
                        disconnect();
                    }
                    else {

                        System.out.println();

                        for (Map.Entry element : AppNode.brokersPublishers.entrySet()) {
                            if (!element.getKey().equals(AppNode.channelName.ChannelName)){
                                boolean status = false;
                                if (AppNode.brokersPublishers.containsKey((element.getKey())))
                                {
                                    status = true;
                                }
                                System.out.println(String.format("> %s", element.getKey()));
                            }
                        }

                        System.out.println("\nEnter a channelName.");
                        System.out.print("> ");

                        String channelName = in.next();

                        while (!AppNode.brokersPublishers.containsKey(channelName)) {
                            System.out.println("Invalid channelName or disconnected user. Try again. \nExample: > User");
                            System.out.print("\n> ");

                            channelName = in.next();
                        }

                        disconnect();

                        synchronized (AppNode.brokersPublishers) {
                            init(AppNode.brokersPublishers.get(channelName).get(0), Integer.parseInt(AppNode.brokersPublishers.get(channelName).get(1)));
                        }

                        consumerOutputStream.writeObject("Consumer");
                        consumerOutputStream.flush();

                        consumerOutputStream.writeObject("ChannelName");
                        consumerOutputStream.flush();

                        register(channelName);

                        boolean connect = consumerInputStream.readBoolean();

                        if (connect)
                        {
                            if (!AppNode.consumerNames.containsKey(channelName))
                            {
                                HashSet<String> videos = new HashSet<>();
                                AppNode.consumerNames.put(channelName, videos);
                            }

                            String videoname = "";
                            int size = this.consumerInputStream.readInt();
                            for (int i = 0; i < size; i++) {
                                int sizeOfVideo = this.consumerInputStream.readInt();
                                ArrayList<VideoFile> video = new ArrayList<>();

                                for (int a = 0; a < sizeOfVideo; a++) {
                                    video.add((VideoFile) this.consumerInputStream.readObject());
                                }
                                videoname = this.consumerInputStream.readUTF();

                                AppNode.consumerNames.get(channelName).add(videoname);
                                AppNode.consumerVideos.put(videoname, StoreVideos(video, "src/out/" + videoname + ".mp4"));
                            }


                            while (true) {

                                System.out.println();

                                if (!(AppNode.consumerNames.get(channelName).size() == 0)) {
                                    for (String videonamee : AppNode.consumerNames.get(channelName)) {
                                        System.out.println("VideoName : " + videonamee);
                                    }
                                    System.out.println("\nType the name of the video to play or press 1 to exit.");
                                    System.out.print("> ");

                                    String video_choose = in.next();

                                    while (!AppNode.consumerNames.get(channelName).contains(video_choose) && !video_choose.equals("1")) {
                                        System.out.println("Invalid videoname. Try again. \nExample: > videoName1");
                                        System.out.print("\n\n> ");

                                        video_choose = in.next();
                                    }
                                    if (video_choose.equals("1")) {
                                        break;
                                    }
                                    playData(AppNode.consumerVideos.get(video_choose));
                                }
                                else
                                {
                                    System.out.println("No videos for channel " + channelName);
                                    break;
                                }
                            }

                        }
                        else{
                            System.out.println("Failed to connect to channelname " + channelName);
                            disconnect();
                        }
                    }
                } else if (choice == 3) {

                    if (AppNode.consumerVideos.size() == 0) {

                        System.out.println("\nNo video downloaded.");

                    } else {
                        for (String video : AppNode.consumerVideos.keySet()) {
                            System.out.println("VideoName: " + video);
                        }

                        System.out.println("\nType the name of the video to play or press 1 to exit.");
                        System.out.print("> ");

                        String videoToPlay = in.next();

                        while (!AppNode.consumerVideos.containsKey(videoToPlay) && !videoToPlay.equals("1")) {
                            System.out.print("\n> ");
                            videoToPlay = in.next();
                        }

                        if (!videoToPlay.equals("1")) {
                            playData(AppNode.consumerVideos.get(videoToPlay));
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException ioException) {
            ioException.printStackTrace();
        }
    }
}
package Node;

import Video.VideoFile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ActionsForConsumer extends Thread {

    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    private String msg;

    public ActionsForConsumer(ObjectOutputStream out, ObjectInputStream in) throws IOException {

        objectOutputStream = out;
        objectInputStream = in;
    }


    public void run () {



        try {
            msg = ((String) objectInputStream.readObject());
            if (msg.equals("HashTag")) {
                msg = ((String) objectInputStream.readObject());

                if (msg.equals("Register")) {

                    System.out.println(msg);

                    String consName = ((String) objectInputStream.readObject());
                    System.out.println(consName);

                    String hashtag = ((String) objectInputStream.readObject());
                    System.out.println(hashtag);

                    ArrayList<String> pubs = new ArrayList<String>(Broker.registeredPublishers.get(hashtag));

                    int numOfPubs = pubs.size();
                    if (pubs.contains(consName)) {
                        numOfPubs--;
                    }

                    //System.out.println(numOfPubs + " ");
                    this.objectOutputStream.writeInt(numOfPubs);
                    this.objectOutputStream.flush();

                    for (String pubname : pubs) {
                        String IP = Broker.publishersInfo.get(pubname).get(0);
                        int port = Integer.parseInt(Broker.publishersInfo.get(pubname).get(1));
                        // filter consumers
                        if (pubname.equals(consName)) {
                            continue;
                        }
                        pull(IP, port, consName, hashtag);
                    }


               /*if (!Broker.registeredUsers.containsKey(hashtag)) {
                    Broker.registeredUsers.put(hashtag, null);
               }
               Broker.registeredUsers.get(hashtag).add((Consumer) objectInputStream.readObject());*/
                }
            }
            else if (msg.equals("ChannelName"))
            {
                msg = ((String) objectInputStream.readObject());

                if (msg.equals("Register")) {

                    System.out.println(msg);

                    String consName = ((String) objectInputStream.readObject());
                    System.out.println(consName);

                    String channelName = ((String) objectInputStream.readObject());
                    //System.out.println(channelName);

                    //test
                    //System.out.println(Broker.publishersInfo.size());
                    for (String s : Broker.publishersInfo.keySet())
                    {
                        System.out.println(s + " channel ready ");
                    }
                    synchronized (Broker.publishersInfo) {
                        if (Broker.publishersInfo.containsKey(channelName)) {
                            String IP = Broker.publishersInfo.get(channelName).get(0);
                            int port = Integer.parseInt(Broker.publishersInfo.get(channelName).get(1));
                            //System.out.println("channel : " + channelName);
                            pull(IP, port, consName, channelName);
                        } else {
                            this.objectOutputStream.writeBoolean(false);
                            this.objectOutputStream.flush();
                        }
                    }

                }
            }
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private void pull(String ip, int port, String name, String topic) {
        try {
            Socket connect = null;
            boolean connected = true;
            try {

                connect = new Socket(ip, port);

            } catch (IOException ioException) {
                try {
                    this.objectOutputStream.writeBoolean(false);
                    this.objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connected = false;
            }

            if (connected) {

                this.objectOutputStream.writeBoolean(true);
                this.objectOutputStream.flush();

                ObjectOutputStream out = new ObjectOutputStream(connect.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(connect.getInputStream());

                out.writeUTF("connect");
                out.flush();

                out.writeUTF(name);
                out.flush();

                out.writeUTF(topic);
                out.flush();

                String info = (String) in.readObject();

                int number_of_videos = in.readInt();

                this.objectOutputStream.writeInt(number_of_videos);
                this.objectOutputStream.flush();

                // this.objectOutputStream.writeObject(info);
                // this.objectOutputStream.flush();

                for (int j = 0; j < number_of_videos; j++) {
                    String videoname = in.readUTF();
                    int size = in.readInt();

                    //      ArrayList<VideoFile> video = new ArrayList<>();

                    this.objectOutputStream.writeInt(size);
                    this.objectOutputStream.flush();

                    this.objectOutputStream.writeUTF(videoname);
                    this.objectOutputStream.flush();

                    boolean videoExists = this.objectInputStream.readBoolean();

                    if (videoExists) {
                        out.writeBoolean(true);
                        out.flush();
                        continue;
                    } else {
                        out.writeBoolean(false);
                        out.flush();
                    }

                    for (int i = 0; i < size; i++) {
                       this.objectOutputStream.writeObject(in.readObject());
                       this.objectOutputStream.flush();
                    }
                    //

                    String s = this.objectInputStream.readUTF();
                    System.out.println(s);


                }

                out.close();
                in.close();
                connect.close();
            }
            else
            {
                this.objectOutputStream.writeBoolean(false);
                this.objectOutputStream.flush();
            }
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }
}
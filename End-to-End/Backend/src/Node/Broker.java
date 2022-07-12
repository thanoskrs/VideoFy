package Node;

import Video.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Broker {

    // contains hashtag key. Also for every key we have a list that contains the channelName that published the hashtag.
    public static HashMap<String, ArrayList<String>> registeredPublishers;
    // contains channelName key. for every key we have a list that contains channelName's ip at .get(0) position and port at  .get(1) position.
    public static HashMap<String, ArrayList<String>> publishersInfo;

    protected static String brokerIP;
    protected static int brokerPort;

    public static String  MainServerIP;
    public static int MainServerPort;

    Socket connectionSocket = null;
    ServerSocket serverSocket = null;
    private static int port;

    public static void main(String[] args) throws IOException {
        port = Integer.parseInt(args[0]);
        Broker broker = new Broker();
        broker.init();
        broker.getMainServerInfo();
        broker.updateMainServer();
        broker.openServer();
    }

    public Broker() {
        registeredPublishers = new HashMap<String, ArrayList<String>>();
        publishersInfo = new HashMap<String, ArrayList<String>>();
    }

    public void openServer() {
        try {

            while(true) {

                connectionSocket = serverSocket.accept();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                String task = (String) objectInputStream.readObject();

                if (task.equals("AppNode"))
                {
                    String channelname = objectInputStream.readUTF();
                    String Ip = objectInputStream.readUTF();
                    int port1 = objectInputStream.readInt();

                    ArrayList<String> pubInfo = new ArrayList<String>();
                    pubInfo.add(0, Ip);
                    pubInfo.add(1, String.valueOf(port1));
                    Broker.publishersInfo.put(channelname, pubInfo);

                }

                else if (task.equals("Consumer")) {

                    System.out.println("Consumer connected.");

                    Thread actionsForConsumer = new ActionsForConsumer(objectOutputStream, objectInputStream);
                    actionsForConsumer.start();
                }
                else if (task.equals("Publisher")) {

                    System.out.println("Publisher connected.");

                    Thread actionsForPublisher = new ActionsForPublisher(objectOutputStream, objectInputStream);
                    actionsForPublisher.start();
                }
            }
        } catch (IOException | ClassNotFoundException  ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }//C:\Users\User\Desktop\video1.mp4
    }

    public void init() throws IOException {
        serverSocket = new ServerSocket(port);
        brokerPort = port;
        brokerIP = Broker.getIP();

        System.out.println("Broker's Ip : " + brokerIP);
    }

    public void getMainServerInfo() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Info/MainServer.txt"))) {
            String line = br.readLine();

            String[] values = line.split(" ");

            MainServerIP = values[0];
            MainServerPort = Integer.parseInt(values[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMainServer() {
        try {
            Socket connectToMS = new Socket(MainServerIP, MainServerPort);
            ObjectOutputStream out = new ObjectOutputStream(connectToMS.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(connectToMS.getInputStream());

            out.writeUTF("Broker");
            out.flush();

            out.writeObject("Init");
            out.flush();

            out.writeObject(brokerIP);
            out.flush();

            out.writeObject(String.valueOf(brokerPort));
            out.flush();

            connectToMS.close();
            out.close();
            in.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static String getIP()
    {
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

}
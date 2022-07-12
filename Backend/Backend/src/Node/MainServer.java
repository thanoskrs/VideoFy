package Node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainServer extends Thread {

    /* key = hashtag | value => list.get(0) = IP, list.get(1) = port
       Get IP and port of the broker that handles specific hashtag*/
    private static HashMap<String, List<String>> brokersInfo = new HashMap<>();

    /* key = channelName | value => list.get(0) = IP, list.get(1) = port
       Get IP and port of the broker that handles specific channelName*/
    private static HashMap<String, ArrayList<String>> brokersPublishers = new HashMap<>();

    // key = username | value = password
    private static HashMap<String, String> appnodeInfo = new HashMap<>();

    private static ArrayList<ArrayList<String>> brokers = new ArrayList<>();

    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    public static void main(String[] args) throws IOException {

        // get registered users
        init();

        ServerSocket serverSocket = null;
        Socket connectionSocket = null;

        serverSocket = new ServerSocket(5000);

        while (true) {
            connectionSocket = serverSocket.accept();
            Thread mainServer = new MainServer(connectionSocket);
            mainServer.start();
        }

    }

    public MainServer(Socket connectionSocket) {
        try {
            objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static String getIP()
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

    private static void init() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Info/credentials.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");

                String userName = values[0];
                String password = values[1];

                appnodeInfo.put(userName, password);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        try {
            FileWriter myWriter = new FileWriter("src/Info/credentials.txt");
            for (String key : appnodeInfo.keySet()) {
                myWriter.write(key + " " + appnodeInfo.get(key) +"\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            String task = objectInputStream.readUTF();

            if (task.equals("Consumer")) {

                System.out.println("Consumer connected");

                String action = objectInputStream.readUTF();

                if (action.equals("HashTag")) {
                    objectOutputStream.writeObject(brokersInfo);
                    objectOutputStream.flush();
                }
                else if (action.equals("ChannelName")) {

                    objectOutputStream.writeObject(brokersPublishers);
                    objectOutputStream.flush();

                }
            } else if (task.equals("Broker")) {

                System.out.println("Broker connected");
                String action = (String) objectInputStream.readObject();

                if (action.equals("Init")) {
                    String brokerIP = (String) objectInputStream.readObject();
                    String brokerPort = (String) objectInputStream.readObject();

                    ArrayList<String> info = new ArrayList<>();
                    info.add(brokerIP);
                    info.add(brokerPort);

                    brokers.add(info);

                    System.out.println(brokerIP);
                    System.out.println(brokerPort);
                    System.out.println("\n");

                }
                else if (action.equals("Add")) {
                    String hashtag = (String) objectInputStream.readObject();
                    String ip = (String) objectInputStream.readObject();
                    String port = (String) objectInputStream.readObject();

                    synchronized (brokersInfo) {
                        if (!brokersInfo.containsKey(hashtag)) {
                            List<String> connectInfo = new ArrayList<>();
                            connectInfo.add(0, ip);
                            connectInfo.add(1, port);
                            brokersInfo.put(hashtag, connectInfo);
                            System.out.println("hashtag added");
                        }
                    }
                }
                else if (action.equals("Remove")) {
                    String hashtag = (String) objectInputStream.readObject();
                    synchronized (brokersInfo) {
                        brokersInfo.remove(hashtag);
                    }
                }
            } else if (task.equals("Appnode")) {

                String action = objectInputStream.readUTF();
                System.out.println("Appnode connected");

                if (action.equals("Init")) {
                    System.out.println("INIT");
                    objectOutputStream.writeInt(brokers.size());
                    objectOutputStream.flush();

                    for (int i = 0; i < brokers.size(); i++) {
                        objectOutputStream.writeUTF(brokers.get(i).get(0));
                        objectOutputStream.flush();

                        objectOutputStream.writeUTF(brokers.get(i).get(1));
                        objectOutputStream.flush();
                    }
                }
                else if (action.equals("LOG IN")) {

                    String userName = objectInputStream.readUTF();
                    String password = objectInputStream.readUTF();

                    synchronized (appnodeInfo) {
                        if (appnodeInfo.containsKey(userName)) {
                            if (appnodeInfo.get(userName).equals(password)) {
                                objectOutputStream.writeObject(true);
                                objectOutputStream.flush();

                                System.out.println("ChannelName "+ userName + " connected");

                                ArrayList<String> responsibleBroker = new ArrayList<>((ArrayList<String>) objectInputStream.readObject());
                                brokersPublishers.put(userName, responsibleBroker);

                            } else {
                                objectOutputStream.writeObject(false);
                                objectOutputStream.flush();
                            }
                        } else {
                            objectOutputStream.writeObject(false);
                            objectOutputStream.flush();
                        }
                    }
                } else if (action.equals("SIGN UP")) {

                    String userName = objectInputStream.readUTF();

                    synchronized (appnodeInfo) {
                        if (!appnodeInfo.containsKey(userName)) {
                            objectOutputStream.writeObject(true);
                            objectOutputStream.flush();

                            String password = objectInputStream.readUTF();

                            appnodeInfo.put(userName, password);
                            update();
                        } else {
                            objectOutputStream.writeObject(false);
                            objectOutputStream.flush();
                        }
                    }
                } else if (action.equals("DISCONNECT")) {

                    String name = objectInputStream.readUTF();
                    brokersPublishers.remove(name);

                    System.out.println("ChannelName " + name + "disconnected");
                }
            }

        } catch (IOException | ClassNotFoundException ioException) {
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
}
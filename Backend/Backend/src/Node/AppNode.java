package Node;

import Video.ChannelName;
import Video.Util;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.*;

public class AppNode extends Thread {

    private  static ServerSocket publisherServer = null;

    // for every hashtag we have a set with videos that published with the specific hashtag
    public static HashMap<String, HashSet<String>> consumerHashtags = new HashMap<>();

    public static HashMap<String, HashSet<String>> consumerNames = new HashMap<>();
    // for every name of video we have the path to play the video
    public static HashMap<String , File> consumerVideos = new HashMap<>();

    // hash of brokers. We need them for the brokersInfo
    public static ArrayList<BigInteger> brokerHashes = new ArrayList<BigInteger>();

    // HashMap brokersInfo contains broker's hash and its IP/Port
    public static HashMap<BigInteger, ArrayList<String>> brokersInfo = new HashMap<BigInteger, ArrayList<String>>();

    // value = hashtag | key => list.get(0) = brokers IP, list.get(1) = brokers Port
    public static HashMap<String, ArrayList<String>> brokersHashtags = new HashMap<String, ArrayList<String>>();

    // value = channelName | key => list.get(0) = brokers IP, list.get(1) = brokers Port
    public static HashMap<String, ArrayList<String>> brokersPublishers = new HashMap<>();

    private static String channelname = "";
    public static ChannelName channelName = new ChannelName("");

    private enum Exit {YES, NO}
    private static Exit exit = Exit.NO;

    public static String publishersIP;
    public static int publishersPort;

    public static String MainServerIP;
    public static int MainServerPort;

    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {

        Random rand = new Random();
        boolean failed = true;

        while (failed) {

            // choose a random port between [5500, 5999] until we find a free port
            int port = rand.nextInt(500) + 5500;
            // init pubserver
            try {
                publisherServer = new ServerSocket(port);
                failed = false;
            } catch (IOException ioException) {
                continue;
            }
        }

        publishersIP = getIP();
        publishersPort = publisherServer.getLocalPort();

        Thread user = new AppNode();
        user.start();

        Socket connection = null;

        while (exit == Exit.NO) {
            try {
                connection = publisherServer.accept();
                Thread pubServer = new PublisherServer(connection);
                pubServer.start();

                if (exit == Exit.YES) {
                    pubServer.join();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // disconnect pubserver
        try {
            connection.close();
            publisherServer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void run() {

        getMainServerInfo();
        init();
        connect();

        if (exit == Exit.NO) {
            channelName = new ChannelName(channelname);
            System.out.println(String.format("Hey %s. Welcome to TikTok", channelName.ChannelName));

            int role;

            while (true) {
                System.out.println("");
                System.out.println("1. Upload a new video");
                System.out.println("2. Delete a video");
                System.out.println("3. View videos");
                System.out.println("4. Exit");

                while (true) {

                    System.out.print("> ");
                    role = in.nextInt();


                    if (role == 1 || role == 2 || role == 3 || role == 4)
                        break;
                }

                if (role == 1) {
                    Publisher pub = new Publisher();
                    pub.publish();
                } else if (role == 2) {
                    Publisher pub = new Publisher();
                    pub.delete();
                } else if (role == 3) {
                    Consumer cons = new Consumer();
                    cons.consume();
                } else if (role == 4) {
                    disconnect();
                    break;
                }
            }
        }
    }

    public void connect() {
        try {
            while(true) {
                System.out.println("");
                System.out.println("1. LOG IN");
                System.out.println("2. SIGN UP");
                System.out.println("3. EXIT");

                int ans;

                while (true) {

                    System.out.print("> ");
                    ans = in.nextInt();


                    if (ans == 1 || ans == 2 || ans == 3)
                        break;
                }

                if (ans == 1) {

                    Socket connectToMS = new Socket(MainServerIP, MainServerPort);
                    ObjectOutputStream outputStream = new ObjectOutputStream(connectToMS.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(connectToMS.getInputStream());

                    outputStream.writeUTF("Appnode");
                    outputStream.flush();

                    outputStream.writeUTF("LOG IN");
                    outputStream.flush();

                    System.out.print("Username: ");
                    String givenName = in.next();

                    System.out.print("Password: ");
                    String givenPassword = in.next();

                    outputStream.writeUTF(givenName);
                    outputStream.flush();

                    outputStream.writeUTF(givenPassword);
                    outputStream.flush();

                    boolean userFound = (boolean) inputStream.readObject();

                    if (userFound) {

                        System.out.println("\nLogging in ..\n");

                        channelname = givenName;

                        List<String> responsibleBroker = new ArrayList<>(hashTopic(channelname));
                        outputStream.writeObject(responsibleBroker);
                        outputStream.flush();

                        Socket connectToResponsibleBroker = new Socket(responsibleBroker.get(0) , Integer.parseInt(responsibleBroker.get(1)));
                        ObjectOutputStream out = new ObjectOutputStream(connectToResponsibleBroker.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(connectToResponsibleBroker.getInputStream());

                        out.writeObject("AppNode");
                        out.flush();

                        out.writeUTF(channelname);
                        out.flush();

                        out.writeUTF(publishersIP);
                        out.flush();

                        out.writeInt(publishersPort);
                        out.flush();

                        out.close();
                        in.close();
                        connectToResponsibleBroker.close();
                        break;
                    }
                    else
                    {
                        System.out.println("\nInvalid Username or Password");
                        continue;
                    }

                } else if (ans == 2) {

                    Socket connectToMS = new Socket(MainServerIP, MainServerPort);
                    ObjectOutputStream outputStream = new ObjectOutputStream(connectToMS.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(connectToMS.getInputStream());

                    outputStream.writeUTF("Appnode");
                    outputStream.flush();

                    outputStream.writeUTF("SIGN UP");
                    outputStream.flush();

                    System.out.print("Enter username: ");
                    String givenName = in.next();

                    outputStream.writeUTF(givenName);
                    outputStream.flush();

                    boolean valid = (boolean) inputStream.readObject();

                    if (valid) {
                        while (true) {
                            System.out.print("Password: ");
                            String givenPassword = in.next();

                            if (givenPassword.length() < 8) {
                                System.out.println("\nPassword should be at least 8 characters\n");
                                continue;
                            }
                            if (givenPassword.length() > 15) {
                                System.out.println("\nPassword should contain maximum 15 characters\n");
                                continue;
                            }

                            System.out.print("Confirm password: ");
                            String confirmedPassword = in.next();

                            if (givenPassword.equals(confirmedPassword)) {
                                outputStream.writeUTF(givenPassword);
                                outputStream.flush();

                                System.out.println("\nSuccessfully registered.");
                                break;
                            } else {
                                System.out.println("\nPasswords don't match. Try again\n");
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Username already exists.");
                    }
                } else if (ans == 3) {
                    disconnect();
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    public void init() {
        try {
            Socket connectToMS = new Socket(MainServerIP, MainServerPort);
            ObjectOutputStream outputStream = new ObjectOutputStream(connectToMS.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(connectToMS.getInputStream());

            outputStream.writeUTF("Appnode");
            outputStream.flush();

            outputStream.writeUTF("Init");
            outputStream.flush();

            int brokers = inputStream.readInt();

            for (int i = 0; i < brokers; i++) {
                String brokerIP = inputStream.readUTF();
                String brokerPort = inputStream.readUTF();

                BigInteger hash = Util.hash(brokerIP + brokerPort);

                brokerHashes.add(hash);

                List<String> connectInfo = new ArrayList<String>();
                connectInfo.add(0, brokerIP);
                connectInfo.add(1, brokerPort);

                brokersInfo.put(hash, (ArrayList<String>) connectInfo);
            }

            Collections.sort(brokerHashes);

            connectToMS.close();
            outputStream.close();
            inputStream.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void disconnect () {
        // notify mainServer for disconnect

        System.out.println("Leaving TikTok ..");
        exit = Exit.YES;

        try {

            Socket connectToMS = new Socket(MainServerIP, MainServerPort);
            ObjectOutputStream outMainServer = new ObjectOutputStream(connectToMS.getOutputStream());
            ObjectInputStream inMainServer = new ObjectInputStream(connectToMS.getInputStream());

            outMainServer.writeUTF("Appnode");
            outMainServer.flush();

            outMainServer.writeUTF("DISCONNECT");
            outMainServer.flush();

            outMainServer.writeUTF(channelName.ChannelName);
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
    }

    private void getMainServerInfo() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Info/MainServer.txt"))) {
            String line = br.readLine();

            String[] values = line.split(" ");

            MainServerIP = values[0];
            MainServerPort = Integer.parseInt(values[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIP()
    {
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), publisherServer.getLocalPort());
            ip = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static List<String> hashTopic(String topic) {
        BigInteger no = Util.hash(topic);

        if (no.compareTo(brokerHashes.get(0)) > 0 && no.compareTo(brokerHashes.get(1)) < 0) {
            return brokersInfo.get(brokerHashes.get(1));
        } else if (no.compareTo(brokerHashes.get(1)) > 0 && no.compareTo(brokerHashes.get(2)) < 0) {
            return brokersInfo.get(brokerHashes.get(2));
        } else {
            return brokersInfo.get(brokerHashes.get(0));
        }
    }
}
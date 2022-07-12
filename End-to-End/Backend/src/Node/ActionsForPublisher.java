package Node;

import Video.VideoFile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ActionsForPublisher extends  Thread {

    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public ActionsForPublisher(ObjectOutputStream out, ObjectInputStream in) throws IOException {

        this.objectOutputStream = out;
        this.objectInputStream = in;
    }


    public void  run() {
        try {

            String task = (String) objectInputStream.readObject();

            if (task.equals("Publish")) {
                String pubName = (String) objectInputStream.readObject();
                System.out.println(pubName + " connected.");

                String hashtag = (String) objectInputStream.readObject();
                String pubIP = (String) objectInputStream.readObject();
                String pubPort = (String) objectInputStream.readObject();

                synchronized (Broker.registeredPublishers) {
                    if (!Broker.registeredPublishers.containsKey(hashtag)) {
                        ArrayList<String> pubNames = new ArrayList<String>();
                        pubNames.add(pubName);
                        Broker.registeredPublishers.put(hashtag, pubNames);
                    } else {
                        if (!Broker.registeredPublishers.get(hashtag).contains(pubName)) {
                            Broker.registeredPublishers.get(hashtag).add(pubName);
                        }
                        //elegxos an exei anevaasei pali o pub video me to idio hashtag
                    }
                }
                synchronized (Broker.publishersInfo) {
                    //  if (!Broker.publishersInfo.containsKey(pubName)) {
                    ArrayList<String> pubInfo = new ArrayList<String>();
                    pubInfo.add(0, pubIP);
                    pubInfo.add(1, pubPort);
                    Broker.publishersInfo.put(pubName, pubInfo);

                    System.out.println(Broker.publishersInfo.containsKey(pubName) + " " + pubName);
                }

                //VideoFile video = (VideoFile) objectInputStream.readObject();
                objectOutputStream.writeObject("Success: " + hashtag + " added");
                objectOutputStream.flush();

                objectOutputStream.close();
                objectInputStream.close();

                Socket connectToMainServer = new Socket(Broker.MainServerIP, Broker.MainServerPort);

                objectOutputStream = new ObjectOutputStream(connectToMainServer.getOutputStream());
                objectInputStream = new ObjectInputStream(connectToMainServer.getInputStream());

                objectOutputStream.writeUTF("Broker");
                objectOutputStream.flush();

                objectOutputStream.writeObject("Add");
                objectOutputStream.flush();

                objectOutputStream.writeObject(hashtag);
                objectOutputStream.flush();

                objectOutputStream.writeObject(Broker.brokerIP);
                objectOutputStream.flush();

                objectOutputStream.writeObject(String.valueOf(Broker.brokerPort));
                objectOutputStream.flush();

                objectInputStream.close();
                objectOutputStream.close();
                connectToMainServer.close();
            }
            else if (task.equals("Delete")) {
                String publisher = objectInputStream.readUTF();
                String key = objectInputStream.readUTF();

                Broker.registeredPublishers.get(key).remove(publisher);

                boolean updateMainServer = false;
                synchronized (Broker.registeredPublishers) {
                    if (Broker.registeredPublishers.get(key).size() == 0) {
                        Broker.registeredPublishers.remove(key);
                        updateMainServer = true;
                    }
                }

                if (updateMainServer) {
                    Socket connectToMainServer = new Socket(Broker.MainServerIP, Broker.MainServerPort);

                    objectOutputStream = new ObjectOutputStream(connectToMainServer.getOutputStream());
                    objectInputStream = new ObjectInputStream(connectToMainServer.getInputStream());

                    objectOutputStream.writeUTF("Broker");
                    objectOutputStream.flush();

                    objectOutputStream.writeObject("Remove");
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(key);
                    objectOutputStream.flush();

                    objectInputStream.close();
                    objectOutputStream.close();
                    connectToMainServer.close();
                }
            }

        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }
}
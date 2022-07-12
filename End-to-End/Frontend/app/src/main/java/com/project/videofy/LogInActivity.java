package com.project.videofy;
import Video.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.AsyncTask;
import android.widget.Toast;

import com.project.videofy.databinding.LoginActivityBinding;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LogInActivity extends AppCompatActivity {


    public static HashMap<String, String> appnodeInfo = new HashMap<>();
    public static ArrayList<Activity> activities=new ArrayList<Activity>();


    public static ServerSocket publisherServer = null;

    private static String channelname;
    public static ChannelName channelName;
    public static final String MainServerIp = "10.0.2.2";
    public static final int MainServerPort = 5000;

    // hash of brokers. We need them for the brokersInfo
    public static ArrayList<BigInteger> brokerHashes = new ArrayList<BigInteger>();

    // HashMap brokersInfo contains broker's hash and its IP/Port
    public static HashMap<BigInteger, ArrayList<String>> brokersInfo = new HashMap<BigInteger, ArrayList<String>>();

    public static String publishersIP = null;
    public static int publishersPort;
    public static HashMap<String, HashSet<String>> consumerHashtags = new HashMap<>();
    public static HashMap<String, HashSet<String>> consumerNames = new HashMap<>();
    // for every name of video we have the path to play the video
    public static HashMap<String, VideoFile> consumerVideos = new HashMap<String, VideoFile>();
    public static HashMap<String, ArrayList<String>> brokersHashtags = new HashMap<>();
    public static List<String> registeredChannels;
    public static List<String> registeredHashtags;

    // value = channelName | key => list.get(0) = brokers IP, list.get(1) = brokers Port
    public static HashMap<String, ArrayList<String>> brokersPublishers = new HashMap<>();

    private LoginActivityBinding binding ;
    Button submitButton;
    EditText inputUsername, inputPassword;

    int port;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activities.add(this);

        binding = LoginActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        checkNeedPermissions();
        registeredChannels = new ArrayList<>();
        //publishersPort = Integer.parseInt(getIntent().getExtras().getString("key"));
        publishersPort = 5050;

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.white));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_image, null);
        getSupportActionBar().setCustomView(v);

    }
    public void onStart()
    {
        super.onStart();

        initLoginButtonListener();
        initSignUpTextListener();

    }

    private void initLoginButtonListener()
    {
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    if (binding.etUsernameLayout.getEditText().length() == 0) {
                        binding.etUsernameLayout.setError("Enter username.");
                    } else {
                        binding.etUsernameLayout.setError(null);
                        if (binding.etPasswordLayout.getEditText().length() == 0) {
                            binding.etPasswordLayout.setError("Enter password.");
                        } else {
                            binding.etPasswordLayout.setError(null);

                            LoginTask login = new LoginTask();
                            String username = binding.InputTextUsername.getText().toString();
                            String password = binding.InputTextPassword.getText().toString();

                            String[] params = new String[2];
                            params[0] = username;
                            params[1] = password;
                            Log.e("Username: ", params[0]);
                            Log.e("Password: ", params[1]);

                            Log.e("a", "1a");
                            //login.execute(params);
                            login.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                            Log.e("a", "1b");
                        }
                    }
                } else {
                    Toast toast;
                    toast = Toast.makeText(LogInActivity.this, "Check your internet connection.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private void initSignUpTextListener()
    {
        binding.signUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , SignUpActivity.class);
                startActivity(intent);

            }
        });
    }

    private void checkNeedPermissions(){
        //Above 6.0 needs to apply for permission dynamically
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Apply for multiple permissions together
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

    private class LoginTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            Log.e("a", "1");
            if(publishersIP == null) {
                initPubServer();
            }
            Log.e("a", "12");
            init();
            Socket connectToMS = null;
            boolean userFound = false;
            try {
                Log.e("a", "13");
                connectToMS = new Socket(MainServerIp, MainServerPort);
                ObjectOutputStream outputStream = new ObjectOutputStream(connectToMS.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(connectToMS.getInputStream());

                outputStream.writeUTF("Appnode");
                outputStream.flush();

                outputStream.writeUTF("LOG IN");
                outputStream.flush();

                String username = strings[0];
                String password = strings[1];

                outputStream.writeUTF(username);
                outputStream.flush();

                outputStream.writeUTF(password);
                outputStream.flush();

                userFound = (boolean) inputStream.readObject();

                if (userFound) {

                    channelname = username;

                    List<String> responsibleBroker = new ArrayList<String>(hashTopic(username));
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
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (userFound)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                channelName = new ChannelName(channelname);
                RunServer runServer = new RunServer();
                runServer.execute();
                Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                startActivity(intent);

            } else {
                binding.wrongInfoEditText.setText("Invalid username or password.");
                //binding.wrongInfoEditText.setTextColor();
                //binding.etUsernameLayout.setError("Username doesn't exist.");
            }
        }

    }

    private class RunServer extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            Socket connection = null;

            while (true) {
                try {
                    connection = LogInActivity.publisherServer.accept();
                    Thread pubServer = new PublisherServer(connection);
                    pubServer.start();
                    //PublisherServer pubServer = new PublisherServer(connection);
                    //pubServer.execute();

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    public void init() {
        try {
            Socket connectToMS = new Socket(MainServerIp, MainServerPort);
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

    public String getIP()
    {
        //String ip = "";
        /*try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), publisherServer.getLocalPort());
            ip = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }*/

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        return ipAddress;
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

    private void initPubServer() {
        Random rand = new Random();
        boolean failed = true;

        while (failed) {

            // choose a random port between [5500, 5999] until we find a free port
            // init pubserver
            try {
                publisherServer = new ServerSocket(publishersPort);
                failed = false;
            } catch (IOException ioException) {
                continue;
            }
        }

        publishersIP = getIP();
        // publishersPort = publisherServer.getLocalPort();

        Log.e("IP", publishersIP);
        Log.e("Port", String.valueOf(publishersPort));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onDestroy()
    {
        super.onDestroy();
        LogInActivity.activities.remove(this);
    }
}
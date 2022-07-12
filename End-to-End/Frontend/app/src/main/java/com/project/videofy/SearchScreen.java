package com.project.videofy;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.videofy.databinding.FragmentSearchScreenBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SearchScreen extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    String[] listChannelname ,listhashtags;

    ArrayAdapter<String> adapterC , adapterH;
    boolean vH = false;
    boolean vC = false;
    String topic = "";
    private FragmentSearchScreenBinding binding;

    public static HashMap<String, ArrayList<String>> brokersPublishers = new HashMap<>();

    private Socket consumerSocket;
    private ObjectOutputStream consumerOutputStream;
    private ObjectInputStream consumerInputStream;

    private String mParam1;
    private String mParam2;

    public SearchScreen() {}


    public static SearchScreen newInstance() {
        SearchScreen fragment = new SearchScreen();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentSearchScreenBinding.inflate(getLayoutInflater());
        return binding.getRoot();
        //  return inflater.inflate(R.layout.fragment_search_screen, container, false);


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void onViewCreated(@NonNull View view , @Nullable Bundle savedInstanceState){
        super.onViewCreated(view , savedInstanceState);

        listChannelname = new String[]{};

        listhashtags = new String[]{};

        binding.openView.setVisibility(View.VISIBLE);

        binding.searchAreaChannelnames.setQueryHint("Search a ChannelName...");
        binding.searchAreaHashtags.setQueryHint("Search a HashTag...");

        initSearchBar();
    }

    protected void initSearchBar()
    {
        binding.searchBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_channelnames: {
                        binding.openView.setVisibility(View.INVISIBLE);
                        channelNameLayout();
                        break;
                    }
                    case R.id.ic_hashtags: {
                        binding.openView.setVisibility(View.INVISIBLE);
                        hashTagLayout();
                        break;
                    }
                }
                return true;
            }
        });
    }

    protected void channelNameLayout()
    {
        SearchTask searchTask = new SearchTask();
        if (binding.theFragHashTag.getVisibility() == View.VISIBLE) {
            binding.theFragHashTag.setVisibility(View.INVISIBLE);
        }
        binding.theFragChannelName.setVisibility(View.VISIBLE);

        binding.textViewForNullSearchResultsChannelnames.setVisibility(View.INVISIBLE);

        ArrayList list = new ArrayList(Arrays.asList(listChannelname));
        initList_channelname(list);
        searchViewChannelName();
        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ("ChannelName"));
        vC = true;
    }

    protected void hashTagLayout()
    {
        SearchTask searchTask = new SearchTask();

        if (binding.theFragChannelName.getVisibility() == View.VISIBLE) {
            binding.theFragChannelName.setVisibility(View.INVISIBLE);
        }

        Log.e("DEBUG", "Hashtag button clicked");
        binding.theFragHashTag.setVisibility(View.VISIBLE);
        binding.textViewForNullSearchResultsHashtags.setVisibility(View.INVISIBLE);

        //earchTask.execute();

        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ("HashTag"));
        vH = true;

    }

    protected void searchViewChannelName() {
        binding.searchAreaChannelnames.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String search = Objects.requireNonNull(query);
                boolean brk = false;
                if (search.length() == 0) {
                    if (binding.textViewForNullSearchResultsChannelnames.getVisibility()==View.VISIBLE){
                        binding.textViewForNullSearchResultsChannelnames.setVisibility(View.INVISIBLE);
                    }
                    ArrayList list = new ArrayList(Arrays.asList(listChannelname));
                    initList_channelname(list);
                } else {
                    for (String s : listChannelname) {
                        if (search.equals(s)) {
                            brk = true;
                        } else {
                            adapterC.remove(s);
                        }
                    }
                    if (brk) {
                        ArrayList list = new ArrayList(Arrays.asList(search));
                        initList_channelname(list);
                    } else {
                        initList_channelname(null);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    protected void searchViewHashTag()
    {
        binding.searchAreaHashtags.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String search = Objects.requireNonNull(query);
                boolean brk = false;
                if (search.length()==0)
                {
                    if (binding.textViewForNullSearchResultsHashtags.getVisibility()==View.VISIBLE){
                        binding.textViewForNullSearchResultsHashtags.setVisibility(View.INVISIBLE);
                    }
                    ArrayList list = new ArrayList(Arrays.asList(listhashtags));
                    initList_hashtag(list);
                }
                else {
                    for (String s : listhashtags) {
                        if (search.equals(s)) {
                            brk = true;
                        }
                        else {
                            adapterH.remove(s);
                        }
                    }
                    if (brk) {
                        ArrayList list = new ArrayList(Arrays.asList(search));
                        initList_hashtag(list);
                    } else {
                        initList_hashtag(null);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    protected void initList_channelname(ArrayList list) {

        if (list == null)
        {
          //  binding.textViewForNullSearchResultsChannelnames.setVisibility(View.VISIBLE);
        }
        else {

            adapterC = new ArrayAdapter<String>(getActivity(), R.layout.list_items, list);

            binding.channelnamesListView.setAdapter(adapterC);
            adapterC.notifyDataSetChanged();
            binding.channelnamesListView.getSelectedItem();

            binding.channelnamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("DEBUG", String.valueOf(position) + (String) list.get(position));

                    ActionTask actionTask = new ActionTask();
                    Log.e("DEBUG", String.valueOf(position) + (String) list.get(position));

                    Log.e("channelname chosen:", ""+list.get(position));

                    String[] array = new String[2];
                    array[0] = "ChannelName";
                    array[1] = (String)list.get(position);

                    actionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, array);
                }
            });
        }
    }

    protected void initList_hashtag(ArrayList list)
    {

        if (list == null)
        {
         //   binding.textViewForNullSearchResultsHashtags.setVisibility(View.VISIBLE);
        }
        else {
            adapterH = new ArrayAdapter<String>(getActivity(), R.layout.list_items, list);

            binding.hashtagsListView.setAdapter(adapterH);
            adapterH.notifyDataSetChanged();
            binding.hashtagsListView.getSelectedItem();

            binding.hashtagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ActionTask actionTask = new ActionTask();
                    Log.e("DEBUG", String.valueOf(position) + (String) list.get(position));

                    Log.e("hashtag chosen:", listhashtags[position]);

                    String[] array = new String[2];
                    array[0] = "HashTag";
                    array[1] = listhashtags[position];

                    actionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, array);

                    //actionTask.execute(listhashtags[position]);
                }
            });
        }
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

    private class SearchTask extends AsyncTask<String, Void, Boolean> {

        // Bundle bundle = new Bundle();

        @Override
        protected Boolean doInBackground(String... strings) {

            Log.e("DEBUG" , "In SearchTask");

            init(LogInActivity.MainServerIp, LogInActivity.MainServerPort);
            topic = strings[0];
            try {
                consumerOutputStream.writeUTF("Consumer");
                consumerOutputStream.flush();
                consumerOutputStream.writeUTF(topic);
                consumerOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (topic.equals("HashTag")) {
                try {
                    HashMap<String, ArrayList<String>> brokersHashtags1 = (HashMap<String, ArrayList<String>>) consumerInputStream.readObject();

                    for (Map.Entry element : brokersHashtags1.entrySet()) {
                        LogInActivity.brokersHashtags.put((String) element.getKey(), (ArrayList<String>) element.getValue());
                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }

                if (LogInActivity.brokersHashtags.size() == 0) {
                    /** TODO: bind null list message*/
                    // binding.textViewForNullSearchResultsHashtags.setVisibility(View.VISIBLE);
                } else {
                    listhashtags = new String[LogInActivity.brokersHashtags.size()];
                    int i = 0;
                    for (Map.Entry element : LogInActivity.brokersHashtags.entrySet()) {
                        listhashtags[i] = "#"+(String) element.getKey();
                        i++;
                    }
                }
            }
            else if (topic.equals("ChannelName"))
            {
                try {
                    HashMap<String, ArrayList<String>> brokersPubs1 = (HashMap<String, ArrayList<String>>) consumerInputStream.readObject();

                    for (Map.Entry element : brokersPubs1.entrySet()) {
                        LogInActivity.brokersPublishers.put((String) element.getKey(), (ArrayList<String>) element.getValue());
                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }

                if (LogInActivity.brokersPublishers.size() == 0) {
                    /** TODO: bind null list message*/
                    // binding.textViewForNullSearchResultsHashtags.setVisibility(View.VISIBLE);
                } else {
                    listChannelname = new String[LogInActivity.brokersPublishers.size()-1];
                    int i = 0;
                    for (Map.Entry element : LogInActivity.brokersPublishers.entrySet()) {
                        if (!element.getKey().equals(LogInActivity.channelName.ChannelName)) {
                            listChannelname[i] = "@"+(String) element.getKey();
                            i++;
                        }

                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {

                switch (topic){
                    case "HashTag":
                        ArrayList list = new ArrayList(Arrays.asList(listhashtags));
                        initList_hashtag(list);
                        searchViewHashTag();
                    case "ChannelName":
                        ArrayList list1 = new ArrayList(Arrays.asList(listChannelname));
                        initList_channelname(list1);
                        searchViewChannelName();
                }

            } else {
                Log.e("DEBUG" , "error on execution of first task");
            }
        }

    }

    private class ActionTask extends AsyncTask<String, Void, Boolean> {

        Bundle bundle = new Bundle();
        String topic;
        @Override
        protected Boolean doInBackground(String... strings) {

            topic = strings[0];

            if (topic.equals("HashTag"))
            {
                String hashtag_choose = strings[1].replace("#","");
                Log.e("DEBUG", hashtag_choose);
                bundle.putString("value",hashtag_choose );

                bundle.putString("Ip", LogInActivity.brokersHashtags.get(hashtag_choose).get(0));

                bundle.putInt("Port", Integer.parseInt(LogInActivity.brokersHashtags.get(hashtag_choose).get(1)));

                disconnect();
            }
            else if (topic.equals("ChannelName"))
            {
                String channelname_chooce = strings[1].replace("@","");

                Log.e("DEBUG", channelname_chooce);
                bundle.putString("ChannelName",channelname_chooce.replace("@", ""));

                bundle.putString("Ip", LogInActivity.brokersPublishers.get(channelname_chooce).get(0));

                bundle.putInt("Port", Integer.parseInt(LogInActivity.brokersPublishers.get(channelname_chooce).get(1)));

                disconnect();
            }


            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result)
            {
                if (topic.equals("HashTag"))
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment selected = new HashTagsVideos();
                    fragmentManager.beginTransaction().replace(R.id.fragment, selected).commit();

                    bundle.putString("pos", "hashtag");

                    selected.setArguments(bundle);
                }
                else if (topic.equals("ChannelName"))
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment selected = new ProfileScreen();
                    fragmentManager.beginTransaction().replace(R.id.fragment, selected).commit();

                    selected.setArguments(bundle);
                }

            }
            else {
                Log.e("DEBUG" , "error on postExecute of ActionTask");
            }
        }

    }

}
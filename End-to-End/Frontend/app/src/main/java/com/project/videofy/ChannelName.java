package com.project.videofy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import Video.*;

public class ChannelName implements Serializable {

    public String ChannelName;

    // hashtagsPublished contains a hashtag as a key. For each key we hava an arraylist eith the videoname that published with the hashtag
    public HashMap<String, ArrayList<String>> hashtagsPublished;
    public HashMap<String,ArrayList<VideoFile>> userVideoFilesMap;

    public static int videonameCounter = 0;

    public ChannelName(String channelName)
    {
        this.ChannelName = channelName;
        hashtagsPublished = new HashMap<>();
        userVideoFilesMap = new HashMap<>();
    }
}